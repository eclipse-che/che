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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.event.PodEvent;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.event.PodEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class watches workspace's pod events and tries hard to read the logs of all it's containers.
 *
 * <p>Current implementation uses provided thread-pool and each container log watch session runs in
 * separate thread from this thread-pool.
 */
public class LogWatcher implements PodEventHandler, Closeable {

  private static final Logger LOG = LoggerFactory.getLogger(LogWatcher.class);

  // TODO: extract to properties
  private static final long WAIT_FOR_SECONDS = 30;
  private static final int WAIT_TIMEOUT = 2000;

  private static final String STARTED_EVENT_REASON = "Started";

  private final String namespace;
  private final KubernetesClient client;
  private final Set<PodLogHandler> logHandlers = ConcurrentHashMap.newKeySet();
  private final Executor containerWatchersThreadPool;

  // set of current watchers. This is used so we're able to cut-off the watchers from outside.
  private final Set<LogWatch> currentWatchers = ConcurrentHashMap.newKeySet();

  // set of currently observed containers so we don't try to follow same container multiple-times.
  private final Set<String> watchingContainers = ConcurrentHashMap.newKeySet();

  public LogWatcher(
      KubernetesClientFactory clientFactory,
      String workspaceId,
      String namespace,
      Executor executor)
      throws InfrastructureException {
    this.client = clientFactory.create(workspaceId);
    this.namespace = namespace;
    this.containerWatchersThreadPool = executor;
  }

  public void addLogHandler(PodLogHandler handler) {
    logHandlers.add(handler);
  }

  @Override
  public void handle(PodEvent event) {
    final String podName = event.getPodName();

    final String containerName = event.getContainerName();
    if (containerName != null && event.getReason().equals(STARTED_EVENT_REASON)) {
      for (PodLogHandler logHandler : logHandlers) {
        if (logHandler.matchPod(podName) && !watchingContainers.contains(containerName)) {
          watchingContainers.add(containerName);
          LOG.trace(
              "adding [{}] to watching containers now watching [{}]",
              containerName,
              watchingContainers);
          containerWatchersThreadPool.execute(
              new ContainerLogWatch(podName, containerName, logHandler));
        } else {
          LOG.debug(
              "Not for this handler or already watching '{} : {} : {}'",
              namespace,
              podName,
              containerName);
        }
      }
    }
  }

  public void close() {
    this.close(false);
  }

  /**
   * Closes all opened log watchers. In case of failed workspace, we want to block the pod for some
   * time before removing it so we has better change to get all the logs from it. If that's the
   * case, use {@code needWait=false}. Otherwise watchers will be cleaned immediately, which does
   * not ensure that we get all the logs.
   *
   * @param needWait true if we need to block before cleanup
   */
  public void close(boolean needWait) {
    try {
      if (needWait && !currentWatchers.isEmpty()) {
        LOG.debug("Waiting '{}ms' before closing all log watchers.", WAIT_TIMEOUT * 2);
        Thread.sleep(WAIT_TIMEOUT * 2);
      } else {
        LOG.debug("Just close it now!");
      }
    } catch (InterruptedException e) {
      LOG.error("Interrupted waiting for the logs. This should not happen.", e);
    } finally {
      currentWatchers.forEach(LogWatch::close);
      currentWatchers.clear();
    }
  }

  private class ContainerLogWatch implements Runnable {

    private static final String ERROR_MESSAGE_MATCH_FORMAT =
        "container \\\"%s\\\" in pod \\\"%s\\\" is waiting to start: PodInitializing";

    private final String podName;
    private final String containerName;
    private final PodLogHandler logHandler;
    private final String errorMessageMatch;

    private ContainerLogWatch(String podName, String containerName, PodLogHandler logHandler) {
      this.podName = podName;
      this.containerName = containerName;
      this.logHandler = logHandler;
      this.errorMessageMatch = String.format(ERROR_MESSAGE_MATCH_FORMAT, containerName, podName);
    }

    /**
     * Do the best effort to get the logs from the container. The method is trying for {@link
     * LogWatcher#WAIT_FOR_SECONDS} seconds to get the logs from the container. If response on log
     * request from the k8s is 40x, it possibly means that container is not ready to get the logs
     * and we'll try again after {@link LogWatcher#WAIT_TIMEOUT} milliseconds.
     */
    @Override
    public void run() {
      boolean successfullWatch = false;
      Stopwatch stopwatch = Stopwatch.createStarted();
      // we try to get logs for WAIT_FOR_SECONDS
      while (!successfullWatch || stopwatch.elapsed(TimeUnit.SECONDS) < WAIT_FOR_SECONDS) {
        // request k8s to get the logs from the container
        try (LogWatch log =
            client
                .pods()
                .inNamespace(namespace)
                .withName(podName)
                .inContainer(containerName)
                .watchLog()) {
          currentWatchers.add(log);

          successfullWatch = readAndHandle(log.getOutput(), logHandler);
          if (!successfullWatch) {
            // failed to get the logs this time, so removing this watcher
            currentWatchers.remove(log);
            LOG.trace(
                "failed to get the logs for '{} : {} : {}'. Container probably still starting after [{}]ms.",
                namespace,
                podName,
                containerName,
                stopwatch.elapsed(TimeUnit.MILLISECONDS));
            // wait before next try
            Thread.sleep(WAIT_TIMEOUT);
          } else {
            LOG.debug(
                "finished watching the logs of '{} : {} : {}'", namespace, podName, containerName);
          }
        } catch (InterruptedException e) {
          LOG.error("Failed watch the logs, nothing better to do here.", e);
          return;
        }
      }
    }

    /**
     * Reads given inputStream. If we receive error message about pod is initializing from k8s (see:
     * {@link ContainerLogWatch#isErrorMessage(String)} and {@link
     * ContainerLogWatch#ERROR_MESSAGE_MATCH_FORMAT}), returns false immediately so we can try again
     * later. Otherwise keeps reading the messages from the stream and gives them to given handler.
     * Be aware that it is blocking and potentially long operation!
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
  }
}
