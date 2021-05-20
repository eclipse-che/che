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

import static org.eclipse.che.api.workspace.shared.Constants.DEBUG_WORKSPACE_START;
import static org.eclipse.che.api.workspace.shared.Constants.DEBUG_WORKSPACE_START_LOG_LIMIT_BYTES;

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
import org.eclipse.che.workspace.infrastructure.kubernetes.util.RuntimeEventsPublisher;
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

  // 10MB
  public static final Long DEFAULT_LOG_LIMIT_BYTES = 10L * 1024 * 1024;

  private static final String STARTED_EVENT_REASON = "Started";

  private final KubernetesClient client;
  private final RuntimeEventsPublisher eventsPublisher;
  private final Set<PodLogHandler> logHandlers = ConcurrentHashMap.newKeySet();
  private final Executor containerWatchersThreadPool;
  private final LogWatchTimeouts timeouts;
  private final long inputStreamLimit;

  private final String namespace;
  private final String workspaceId;
  private final Set<String> podsOfInterest;

  private boolean closed = false;

  /**
   * Map of current watchers where key is name of the container and value is {@link
   * ContainerLogWatch} instance.
   */
  private final Map<String, ContainerLogWatch> currentContainerWatchers = new ConcurrentHashMap<>();

  public LogWatcher(
      KubernetesClientFactory clientFactory,
      RuntimeEventsPublisher eventsPublisher,
      String workspaceId,
      String namespace,
      Set<String> podsOfInterest,
      Executor executor,
      LogWatchTimeouts timeouts,
      long inputStreamLimit)
      throws InfrastructureException {
    this.client = clientFactory.create(workspaceId);
    this.eventsPublisher = eventsPublisher;
    this.workspaceId = workspaceId;
    this.namespace = namespace;
    this.containerWatchersThreadPool = executor;
    this.timeouts = timeouts;
    this.podsOfInterest = podsOfInterest;
    this.inputStreamLimit = inputStreamLimit;
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
          if (closed) {
            return;
          }
          if (podsOfInterest.contains(podName)
              && !currentContainerWatchers.containsKey(podContainerKey(podName, containerName))) {
            ContainerLogWatch logWatch =
                new ContainerLogWatch(
                    client,
                    eventsPublisher,
                    namespace,
                    podName,
                    containerName,
                    logHandler,
                    timeouts,
                    inputStreamLimit);
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
        closed = true;
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

  /**
   * Gets log limit bytes from given `startOptions` if it's set there under {@link
   * org.eclipse.che.api.workspace.shared.Constants#DEBUG_WORKSPACE_START_LOG_LIMIT_BYTES} key.
   * Otherwise returns default {@link LogWatcher#DEFAULT_LOG_LIMIT_BYTES}.
   *
   * @param startOptions options where we'll try to find log limit param
   * @return valid log limit bytes
   */
  public static long getLogLimitBytes(Map<String, String> startOptions) {
    if (startOptions == null
        || startOptions.isEmpty()
        || !startOptions.containsKey(DEBUG_WORKSPACE_START_LOG_LIMIT_BYTES)) {
      return DEFAULT_LOG_LIMIT_BYTES;
    } else {
      try {
        return Long.parseLong(startOptions.get(DEBUG_WORKSPACE_START_LOG_LIMIT_BYTES));
      } catch (NumberFormatException nfe) {
        LOG.debug(
            "failed to parse log limit bytes value from startOptions. Value '{}'",
            startOptions.get(DEBUG_WORKSPACE_START_LOG_LIMIT_BYTES),
            nfe);
        return DEFAULT_LOG_LIMIT_BYTES;
      }
    }
  }

  /**
   * Takes `startOptions` map and tells whether it's set so we should watch the logs. To return
   * true, flag must be stored under {@link
   * org.eclipse.che.api.workspace.shared.Constants#DEBUG_WORKSPACE_START} key.
   *
   * @param startOptions options where we'll try to find log watch flag
   * @return true if we should watch the logs, false otherwise
   */
  public static boolean shouldWatchLogs(Map<String, String> startOptions) {
    if (startOptions == null || startOptions.isEmpty()) {
      return false;
    }
    return Boolean.parseBoolean(
        startOptions.getOrDefault(DEBUG_WORKSPACE_START, Boolean.FALSE.toString()));
  }
}
