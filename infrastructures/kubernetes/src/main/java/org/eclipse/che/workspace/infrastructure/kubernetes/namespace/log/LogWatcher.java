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
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
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
 * <p>Current implementation have static thread-pool and each container log watch session runs in
 * separate thread. This keeps connections under control, but does it provide enough robustness and
 * performance?
 */
public class LogWatcher implements PodEventHandler {
  private static final Logger LOG = LoggerFactory.getLogger(LogWatcher.class);

  // TODO: extract to properties
  private static final long WAIT_FOR_SECONDS = 30;
  private static final int WAIT_TIMEOUT = 2000;

  private final String namespace;
  private final KubernetesClient client;
  private final PodLogHandler logHandler;

  private final Executor containerWatchersThreadPool;

  // set of current watchers. This is used so we're able to cut-off the watchers from outside.
  private final Set<LogWatch> currentWatchers = new HashSet<>();

  // set of currently observed containers so we're not try to follow same container multiple-times.
  private final Set<String> watchingContainers = new HashSet<>();

  public LogWatcher(
      KubernetesClientFactory clientFactory,
      String workspaceId,
      String namespace,
      PodLogHandler handler,
      Executor executor)
      throws InfrastructureException {
    this.logHandler = handler;
    this.client = clientFactory.create(workspaceId);
    this.namespace = namespace;
    this.containerWatchersThreadPool = executor;
  }

  @Override
  public void handle(PodEvent event) {
    final String podName = event.getPodName();

    LOG.debug("the reason for [{}]'s event is [{}]", event.getContainerName(), event.getReason());
    final String containerName = event.getContainerName();
    if (containerName != null
        && logHandler.matchPod(podName)
        && event.getReason().equals("Started")) {
      if (!watchingContainers.contains(containerName)) {
        watchingContainers.add(containerName);
        LOG.trace(
            "adding [{}] to watching containers now watching [{}]",
            containerName,
            watchingContainers);
        containerWatchersThreadPool.execute(new ContainerLogWatch(podName, containerName));
      } else {
        LOG.debug("sorry, already watching [{}]", containerName);
      }
    } else {
      LOG.debug("don't want to watch this [{}] [{}]", podName, containerName);
    }
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

    private final String podName;
    private final String containerName;

    private ContainerLogWatch(String podName, String containerName) {
      this.podName = podName;
      this.containerName = containerName;
    }

    /**
     * Do the best effort to get the logs from the container. The method tries N times to get the
     * logs from the container. If response on log request from the k8s is 40x, it possibly means
     * that container is not ready to get the logs and we'll try again.
     */
    @Override
    public void run() {
      Stopwatch stopwatch = Stopwatch.createStarted();
      while (stopwatch.elapsed(TimeUnit.SECONDS) < WAIT_FOR_SECONDS) {
        LOG.debug(
            "try watching the logs [{}] [{}]s", containerName, stopwatch.elapsed(TimeUnit.SECONDS));
        try (LogWatch log =
            client
                .pods()
                .inNamespace(namespace)
                .withName(podName)
                .inContainer(containerName)
                .watchLog()) {
          currentWatchers.add(log);
          if (!logHandler.handle(log.getOutput(), containerName)) {
            // failed to get the logs this time, so removing this watcher
            currentWatchers.remove(log);
            LOG.debug(
                "failed to get the logs for [{}] time [{}]s",
                containerName,
                stopwatch.elapsed(TimeUnit.SECONDS));
          } else {
            LOG.info(
                "finished watching the logs of [{} : {} : {}]", namespace, podName, containerName);
            break;
          }

          // wait before next try
          Thread.sleep(WAIT_TIMEOUT);
        } catch (IOException | InterruptedException e) {
          LOG.error("Failed watching the logs, nothing better to do here.", e);
          return;
        }
      }
    }
  }
}
