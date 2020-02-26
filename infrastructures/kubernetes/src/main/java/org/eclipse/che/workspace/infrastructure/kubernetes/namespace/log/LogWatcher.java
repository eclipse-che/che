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
import java.util.concurrent.atomic.AtomicBoolean;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.event.PodEvent;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.event.PodEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class watches workspace's pod events and tries to read the logs of all its containers.
 *
 * <p>Current implementation uses provided thread-pool and each container log watch session runs in
 * separate thread from this thread-pool.
 *
 * <p>Watching logs of individual containers is delegated to instances of {@link ContainerLogWatch}.
 */
public class LogWatcher implements PodEventHandler, Closeable {

  private static final Logger LOG = LoggerFactory.getLogger(LogWatcher.class);

  private static final String STARTED_EVENT_REASON = "Started";

  private final KubernetesClient client;
  private final Set<PodLogHandler> logHandlers = ConcurrentHashMap.newKeySet();
  private final Executor containerWatchersThreadPool;
  private final LogWatchTimeouts timeouts;

  private final String namespace;
  private final String workspaceId;
  private final Set<String> podsOfInterest;

  private final AtomicBoolean closed = new AtomicBoolean(false);

  /**
   * Map of current watchers where key is name of the container and value is {@link
   * ContainerLogWatch} instance.
   */
  private final Map<String, ContainerLogWatch> currentContainerWatchers = new ConcurrentHashMap<>();

  public LogWatcher(
      KubernetesClientFactory clientFactory,
      String workspaceId,
      String namespace,
      Set<String> podsOfInterest,
      Executor executor,
      LogWatchTimeouts timeouts)
      throws InfrastructureException {
    this.client = clientFactory.create(workspaceId);
    this.workspaceId = workspaceId;
    this.namespace = namespace;
    this.containerWatchersThreadPool = executor;
    this.timeouts = timeouts;
    this.podsOfInterest = podsOfInterest;
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
        // we need to synchronize here so we won't add new watcher while we're cleaning them
        synchronized (this) {
          if (closed.get()) {
            return;
          }
          if (podsOfInterest.contains(podName)
              && !currentContainerWatchers.containsKey(podContainerKey(podName, containerName))) {
            ContainerLogWatch logWatch =
                new ContainerLogWatch(
                    client, namespace, podName, containerName, logHandler, timeouts);
            currentContainerWatchers.put(podContainerKey(podName, containerName), logWatch);
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
  }

  private String podContainerKey(String podName, String containerName) {
    return podName + ":" + containerName;
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
   * @param needWait true if we need to pause before cleanup
   */
  public void close(boolean needWait) {
    try {
      if (needWait) {
        LOG.debug(
            "Waiting '{}ms' before closing all log watchers for workspace '{}'.",
            timeouts.getWaitBeforeCleanupMs(),
            workspaceId);
        Thread.sleep(timeouts.getWaitBeforeCleanupMs());
      }
    } catch (InterruptedException e) {
      LOG.error(
          "Interrupted while waiting before closing the log watch for workspace '{}'.",
          workspaceId,
          e);
    } finally {
      LOG.debug("Closing all log watchers for '{}'", workspaceId);
      synchronized (this) {
        closed.set(true);
        currentContainerWatchers.values().forEach(ContainerLogWatch::close);
        currentContainerWatchers.clear();
      }
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
