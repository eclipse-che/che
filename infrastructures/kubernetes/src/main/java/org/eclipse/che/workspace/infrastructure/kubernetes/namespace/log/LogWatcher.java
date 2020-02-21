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
import java.io.Closeable;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
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
 *
 * <p>Watching logs of individual containers is delegated to instances of {@link ContainerLogWatch}.
 */
public class LogWatcher implements PodEventHandler, Closeable {

  private static final Logger LOG = LoggerFactory.getLogger(LogWatcher.class);

  // TODO: extract to che.properties
  protected static final int WAIT_TIMEOUT = 2000;

  private static final String STARTED_EVENT_REASON = "Started";

  private final KubernetesClient client;
  private final Set<PodLogHandler> logHandlers = ConcurrentHashMap.newKeySet();
  private final Executor containerWatchersThreadPool;

  private final String namespace;
  private final String workspaceId;

  /**
   * Map of current watchers where key is name of the container and value is {@link
   * ContainerLogWatch} instance.
   */
  private final Map<String, ContainerLogWatch> currentContainerWatchers = new ConcurrentHashMap<>();

  public LogWatcher(
      KubernetesClientFactory clientFactory,
      String workspaceId,
      String namespace,
      Executor executor)
      throws InfrastructureException {
    this.client = clientFactory.create(workspaceId);
    this.workspaceId = workspaceId;
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
        if (logHandler.matchPod(podName) && !currentContainerWatchers.containsKey(containerName)) {
          ContainerLogWatch logWatch =
              new ContainerLogWatch(
                  client, namespace, podName, containerName, logHandler, WAIT_TIMEOUT);
          currentContainerWatchers.put(containerName, logWatch);
          LOG.trace(
              "adding [{}] to watching containers now watching [{}]",
              containerName,
              currentContainerWatchers.keySet());
          containerWatchersThreadPool.execute(logWatch);
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
   * time before removing it so we have better chance to get all the logs from it. If that's the
   * case, use {@code needWait=false}. Otherwise watchers will be cleaned immediately, which does
   * not ensure that we get all the logs.
   *
   * @param needWait true if we need to block before cleanup
   */
  public void close(boolean needWait) {
    try {
      if (needWait && !currentContainerWatchers.isEmpty()) {
        int waitBeforeClose = WAIT_TIMEOUT * 2;
        LOG.debug(
            "Waiting '{}ms' before closing all log watchers for workspace '{}'.",
            waitBeforeClose,
            workspaceId);
        Thread.sleep(waitBeforeClose);
      }
    } catch (InterruptedException e) {
      LOG.error(
          "Interrupted while waiting before closing the log watch for workspace '{}'.",
          workspaceId,
          e);
    } finally {
      LOG.debug("Closing all log watchers for '{}'", workspaceId);
      currentContainerWatchers.forEach((k, v) -> v.close());
      currentContainerWatchers.clear();
    }
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", LogWatcher.class.getSimpleName() + "[", "]")
        .add("namespace='" + namespace + "'")
        .add("workspaceId='" + workspaceId + "'")
        .toString();
  }
}
