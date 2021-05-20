/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.namespace.log;

import com.google.common.base.Stopwatch;
import com.google.common.io.ByteStreams;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.RuntimeEventsPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for watching logs in a single container instance. Messages are
 * provided, unbuffered, to a provided {@link PodLogHandler}
 *
 * <p>Reading logs is a blocking operation.
 */
class ContainerLogWatch implements Runnable, Closeable {

  private static final Logger LOG = LoggerFactory.getLogger(ContainerLogWatch.class);

  private final KubernetesClient client;
  private final RuntimeEventsPublisher eventPublisher;
  private final PodLogHandler logHandler;
  private final LogWatchTimeouts timeouts;
  private final long inputStreamLimit;

  private final String namespace;
  private final String podName;
  private final String containerName;

  // current LogWatch instance. We need it so we can close it from outside in close() method.
  private LogWatch currentLogWatch;

  // json parser used to parse log messages to check for errorness
  private final JsonParser jsonParser = new JsonParser();

  // flag whether we should still try to get the logs
  private boolean closed = false;

  ContainerLogWatch(
      KubernetesClient client,
      RuntimeEventsPublisher eventPublisher,
      String namespace,
      String podName,
      String containerName,
      PodLogHandler logHandler,
      LogWatchTimeouts timeouts,
      long inputStreamLimit) {
    this.client = client;
    this.eventPublisher = eventPublisher;
    this.namespace = namespace;
    this.podName = podName;
    this.containerName = containerName;
    this.logHandler = logHandler;
    this.timeouts = timeouts;
    this.inputStreamLimit = inputStreamLimit;
  }

  /**
   * Do the best effort to get the logs from the container. The method is trying for {@link
   * LogWatchTimeouts#getWatchTimeoutMs()} to get the logs from the container. If response on log
   * request from the k8s is 40x, it possibly means that container is not ready to get the logs and
   * we'll try again after {@link LogWatchTimeouts#getWaitBeforeNextTry()} milliseconds.
   */
  @Override
  public void run() {
    Stopwatch stopwatch = Stopwatch.createStarted();
    // we try to get logs for `timeouts.getWatchTimeoutMs()`
    while (stopwatch.elapsed(TimeUnit.MILLISECONDS) < timeouts.getWatchTimeoutMs()) {

      // request k8s to get the logs from the container
      try (LogWatch logWatch =
          client
              .pods()
              .inNamespace(namespace)
              .withName(podName)
              .inContainer(containerName)
              .watchLog()) {
        eventPublisher.sendWatchLogStartedEvent(containerKey());

        // we need to synchroinze here to avoid adding new `currentLogWatch` after we close it
        synchronized (this) {
          if (closed) {
            return;
          }
          currentLogWatch = logWatch;
        }

        if (currentLogWatch.getOutput() == null
            || !readAndHandle(
                ByteStreams.limit(currentLogWatch.getOutput(), inputStreamLimit), logHandler)) {
          // failed to get the logs this time, so removing this watcher
          LOG.trace(
              "failed to get the logs for '{} : {} : {}'. Container probably still starting after [{}]ms.",
              namespace,
              podName,
              containerName,
              stopwatch.elapsed(TimeUnit.MILLISECONDS));

          // wait before next try
          Thread.sleep(timeouts.getWaitBeforeNextTry());
        } else {
          LOG.debug(
              "finished watching the logs of '{} : {} : {}'", namespace, podName, containerName);
          return;
        }
      } catch (InterruptedException e) {
        LOG.error(
            "Failed watch the logs '{} : {} : {}', nothing better to do here.",
            namespace,
            podName,
            containerName,
            e);
        return;
      } finally {
        eventPublisher.sendWatchLogStoppedEvent(containerKey());
      }
    }
  }

  /**
   * Reads given inputStream. If we receive error message about pod is initializing from k8s (see:
   * {@link ContainerLogWatch#isErrorMessage(String)}, returns false immediately so we can try again
   * later. Otherwise keeps reading the messages from the stream and gives them to given handler. Be
   * aware that it is blocking and potentially long operation!
   *
   * @param inputStream to read log messages from
   * @param handler we delegate log messages to this handler.
   * @return false if error message received from k8s, true at the end of the stream or if
   *     interrupted
   */
  private boolean readAndHandle(InputStream inputStream, PodLogHandler handler) {
    try (BufferedReader in = new BufferedReader(new InputStreamReader(inputStream))) {
      String logMessage;
      while ((logMessage = in.readLine()) != null) {
        if (!this.isErrorMessage(logMessage)) {
          handler.handle(logMessage, containerName);
        } else {
          LOG.debug("error message [{}]", logMessage);
          LOG.debug(
              "failed to get the logs for [{} : {}], should try again if enough time.",
              podName,
              containerName);
          return false;
        }
      }
    } catch (IOException e) {
      // TODO: can we somehow recognize if it is failure or intended close()?
      LOG.debug(
          "End of watching log of [{} : {} : {}]. It could be either intended or some connection failure.",
          namespace,
          podName,
          containerName);
      LOG.trace("End of watching log of [{} : {} : {}]", namespace, podName, containerName, e);
    }
    return true;
  }

  /**
   * Tells whether given `message` is error message so we should try to watch again.
   *
   * <p>error message should look like this:
   *
   * <pre>
   *   {
   *    "kind":"Status",
   *    "apiVersion":"v1",
   *    "metadata":{},
   *    "status":"Failure",
   *    "message":"container \"che-plugin-metadata-broker-v3-1-1\" in pod
   *      \"workspace1a7u0mmfknhsgzqc.che-plugin-broker\" is waiting to start: ContainerCreating",
   *    "reason":"BadRequest",
   *    "code":400}
   * </pre>
   *
   * <p>Regular message is usually not a json, so we first try to find pod name. That should
   * eliminate close to 100% regular messages to being checked for error, because container app
   * should not know where it runs. If this initial check fails, we try to parse the message as a
   * json and match it for more details.
   *
   * @param message to check
   * @return true if message is an json error message, false otherwise
   */
  private boolean isErrorMessage(String message) {
    if (!message.contains(podName)) {
      return false;
    }
    try {
      JsonObject json = jsonParser.parse(message).getAsJsonObject();
      return !(!"Status".equals(json.get("kind").getAsString())
          || !"Failure".equals(json.get("status").getAsString())
          || !json.has("code")
          || !json.get("code").getAsString().contains("40"));
    } catch (JsonParseException jpe) {
      LOG.debug("Cannot parse the message as JSON.", jpe);
      return false;
    }
  }

  @Override
  public void close() {
    synchronized (this) {
      closed = true;
      if (currentLogWatch != null) {
        currentLogWatch.close();
      }
    }
  }

  private String containerKey() {
    return namespace + ":" + podName + ":" + containerName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ContainerLogWatch that = (ContainerLogWatch) o;
    return Objects.equals(namespace, that.namespace)
        && Objects.equals(podName, that.podName)
        && Objects.equals(containerName, that.containerName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(namespace, podName, containerName);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", ContainerLogWatch.class.getSimpleName() + "[", "]")
        .add("namespace='" + namespace + "'")
        .add("podName='" + podName + "'")
        .add("containerName='" + containerName + "'")
        .toString();
  }
}
