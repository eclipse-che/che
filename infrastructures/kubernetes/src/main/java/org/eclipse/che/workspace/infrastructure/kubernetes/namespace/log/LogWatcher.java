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

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import java.io.Closeable;
import java.io.IOException;
import java.io.PipedOutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.event.PodEvent;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.event.PodEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogWatcher implements PodEventHandler, Closeable {

  private static final Logger LOG = LoggerFactory.getLogger(LogWatcher.class);

  private static final ExecutorService pool = Executors.newFixedThreadPool(10);

  private final String namespace;
  private final KubernetesClient client;
  private final PodLogHandler logHandler;

  // set of current watchers. This is used so we're able to cut-off the watchers from outside.
  private final Set<LogWatch> currentWatchers = new HashSet<>();

  // set of currently observed containers so we're not try to follow same container multiple-times.
  private final Set<String> watchingContainers = new HashSet<>();

  public LogWatcher(
      KubernetesClientFactory clientFactory,
      String workspaceId,
      String namespace,
      PodLogHandler handler)
      throws InfrastructureException {
    this.logHandler = handler;
    this.client = clientFactory.create(workspaceId);
    this.namespace = namespace;
  }

  @Override
  public void handle(PodEvent event) {
    final String podName = event.getPodName();

    if (logHandler.matchPod(podName)) {
      final String containerName = event.getContainerName();
      if (containerName != null && !watchingContainers.contains(containerName)) {
        watchingContainers.add(containerName);
        LOG.trace("adding [{}] to watching containers now watching [{}]", containerName,
            watchingContainers);
        pool.submit(new ContainerLogWatch(podName, containerName));
      }
    }
  }

  @Override
  public void close() {
    new Thread(() -> {
      try {
        LOG.debug("Waiting 5s before exit to get all the logs");
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        LOG.error("Interrupted waiting for the logs", e);
      } finally {
        currentWatchers.forEach(LogWatch::close);
        currentWatchers.clear();
      }
    }).start();
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
     *
     *
     */
    @Override
    public void run() {
      int retries = 0;
      while (retries < 10) {
        LOG.debug("try watching the logs [{}]", retries);
        try (PrefixedPipedInputStream is = new PrefixedPipedInputStream(containerName);
            LogWatch log =
                client
                    .pods()
                    .inNamespace(namespace)
                    .withName(podName)
                    .inContainer(containerName)
                    .watchLog(new PipedOutputStream(is))) {

          currentWatchers.add(log);
          if (!logHandler.handle(is)) {
            // failed to get the logs this time, so removing this watcher
            currentWatchers.remove(log);
            LOG.debug("failed to get the logs [{}]", retries);
            retries++;
          } else {
            LOG.info("watched and ended");
            break;
          }

          LOG.info("waiting 1s before next try");
          Thread.sleep(1000);
        } catch (IOException | InterruptedException e) {
          LOG.error("Failed watching the logs, nothing better to do here.", e);
          return;
        }
      }
    }
  }
}
