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
import java.util.LinkedList;
import java.util.Queue;
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
  private final Set<LogWatch> lw = new HashSet<>();
  private final Set<String> watching = new HashSet<>();

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
      final String reason = event.getReason();
      LOG.info("[{}][{}] reason [{}]", podName, containerName, reason);
      LOG.info("already watching [{}]", watching);
      if (containerName != null && !watching.contains(containerName)) {
        LOG.info("adding [{}] to watching containers now watching [{}]", containerName, watching);
        pool.submit(
            () -> {
              watching.add(containerName);
              int retries = 0;
              while (retries < 10) {
                LOG.info("watchin, try [{}]", retries);
                try (PrefixedPipedInputStream is = new PrefixedPipedInputStream(containerName);
                    LogWatch log =
                        client
                            .pods()
                            .inNamespace(namespace)
                            .withName(podName)
                            .inContainer(containerName)
                            .watchLog(new PipedOutputStream(is))) {
                  lw.add(log);
                  if (!logHandler.handle(is)) {
                    lw.remove(log);
                    LOG.info("failed to get the logs [{}]", retries);
                    retries++;
                  } else {
                    LOG.info("watched and ended");
                    break;
                  }
                  LOG.info("end of one log, should close now");
                } catch (IOException e) {
                  throw new RuntimeException(e);
                }
                try {
                  LOG.info("waiting 1s before next try");
                  Thread.sleep(1000);
                } catch (InterruptedException e) {
                  e.printStackTrace();
                }
              }
            });
      }
    }
  }

  @Override
  public void close() {
    try {
      LOG.info("waiting 5s before exit to get lal the logs");
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    LOG.info("cleaning logwatches");
    lw.forEach(LogWatch::close);
    lw.clear();
    LOG.info("cleared");
  }
}
