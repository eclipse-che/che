/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
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
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for single container instance log watch. It tries the best to watch the
 * logs. It sends single messages to provided {@link PodLogHandler}. Be aware that reading the logs
 * is blocking operation. This class implements {@link Runnable} so it can be easily run in
 * dedicated Thread. The watching log session can be closed anytime with `close()`. It implements
 * {@link Closeable}, so all benefits coming from this may be used as well.
 */
class ContainerLogWatch implements Runnable, Closeable {

  private static final Logger LOG = LoggerFactory.getLogger(ContainerLogWatch.class);

  private static final long WAIT_FOR_SECONDS = 30;
  private static final String POD_INITIALIZING_MESSAGE_MATCH_FORMAT =
      "container \\\"%s\\\" in pod \\\"%s\\\" is waiting to start: PodInitializing";

  private final KubernetesClient client;
  private final String namespace;
  private final String podName;
  private final String containerName;
  private final PodLogHandler logHandler;
  private final String errorMessageMatch;

  // current LogWatch instance. We need it so we can close it from outside in close() method.
  private LogWatch currentLogWatch;

  ContainerLogWatch(
      KubernetesClient client,
      String namespace,
      String podName,
      String containerName,
      PodLogHandler logHandler) {
    this.client = client;
    this.namespace = namespace;
    this.podName = podName;
    this.containerName = containerName;
    this.logHandler = logHandler;
    this.errorMessageMatch =
        String.format(POD_INITIALIZING_MESSAGE_MATCH_FORMAT, containerName, podName);
  }

  /**
   * Do the best effort to get the logs from the container. The method is trying for {@link
   * ContainerLogWatch#WAIT_FOR_SECONDS} seconds to get the logs from the container. If response on
   * log request from the k8s is 40x, it possibly means that container is not ready to get the logs
   * and we'll try again after {@link LogWatcher#WAIT_TIMEOUT} milliseconds.
   */
  @Override
  public void run() {
    boolean successfullWatch = false;
    Stopwatch stopwatch = Stopwatch.createStarted();
    // we try to get logs for WAIT_FOR_SECONDS
    while (!successfullWatch
        || stopwatch.elapsed(TimeUnit.SECONDS) < ContainerLogWatch.WAIT_FOR_SECONDS) {
      // request k8s to get the logs from the container
      try (LogWatch logWatch =
          client
              .pods()
              .inNamespace(namespace)
              .withName(podName)
              .inContainer(containerName)
              .watchLog()) {
        currentLogWatch = logWatch;

        successfullWatch = readAndHandle(currentLogWatch.getOutput(), logHandler);
        if (!successfullWatch) {
          // failed to get the logs this time, so removing this watcher
          LOG.trace(
              "failed to get the logs for '{} : {} : {}'. Container probably still starting after [{}]ms.",
              namespace,
              podName,
              containerName,
              stopwatch.elapsed(TimeUnit.MILLISECONDS));
          // wait before next try
          Thread.sleep(LogWatcher.WAIT_TIMEOUT);
        } else {
          LOG.debug(
              "finished watching the logs of '{} : {} : {}'", namespace, podName, containerName);
        }
      } catch (InterruptedException e) {
        LOG.error(
            "Failed watch the logs '{} : {} : {}', nothing better to do here.",
            namespace,
            podName,
            containerName,
            e);
        return;
      }
    }
  }

  /**
   * Reads given inputStream. If we receive error message about pod is initializing from k8s (see:
   * {@link ContainerLogWatch#isErrorMessage(String)} and {@link
   * ContainerLogWatch#POD_INITIALIZING_MESSAGE_MATCH_FORMAT}), returns false immediately so we can
   * try again later. Otherwise keeps reading the messages from the stream and gives them to given
   * handler. Be aware that it is blocking and potentially long operation!
   *
   * @param inputStream to read log messages from
   * @param handler we delegate log messages to this handler.
   * @return false if error message received from k8s, true at the end of the stream or if
   *     interrupted
   */
  private boolean readAndHandle(InputStream inputStream, PodLogHandler handler) {
    if (inputStream == null) {
      LOG.error(
          "Given InputStream for reading the logs for '{} {} {}' is null.",
          namespace,
          podName,
          containerName);
      return false;
    }
    try (BufferedReader in = new BufferedReader(new InputStreamReader(inputStream))) {
      String logMessage;
      while ((logMessage = in.readLine()) != null) {
        if (this.isErrorMessage(logMessage)) {
          LOG.trace(
              "failed to get the logs for [{} : {}], should try again if enough time.",
              podName,
              containerName);
          return false;
        } else {
          handler.handle(logMessage, containerName);
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

  private boolean isErrorMessage(String message) {
    return message.contains(this.errorMessageMatch);
  }

  @Override
  public void close() {
    if (currentLogWatch != null) {
      currentLogWatch.close();
    }
  }
}
