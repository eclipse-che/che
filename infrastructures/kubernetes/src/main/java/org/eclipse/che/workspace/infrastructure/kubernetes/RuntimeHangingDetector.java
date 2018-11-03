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
package org.eclipse.che.workspace.infrastructure.kubernetes;

import static java.util.Collections.emptyMap;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.WorkspaceSharedPool;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.RuntimeEventsPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tracks STARTING/STOPPING runtimes and forcibly stop them if they did not change status before a
 * timeout is reached.
 *
 * @author Sergii Leshchenko
 */
@Singleton
public class RuntimeHangingDetector {

  private static final Logger LOG = LoggerFactory.getLogger(RuntimeHangingDetector.class);

  private Timer timeouts;

  private final WorkspaceSharedPool workspaceSharedPool;
  private final RuntimeEventsPublisher eventPublisher;
  private final Map<String, WaitStatusChangedTask> workspaceId2Task;

  @Inject
  public RuntimeHangingDetector(
      RuntimeEventsPublisher eventPublisher, WorkspaceSharedPool workspaceSharedPool) {
    this.workspaceSharedPool = workspaceSharedPool;
    this.eventPublisher = eventPublisher;
    this.workspaceId2Task = new ConcurrentHashMap<>();
  }

  /**
   * Schedules a task to check whether the specified runtime changed its status from {@link
   * WorkspaceStatus#STARTING} after the specified timeout and if it didn't change then runtime
   * start will be interrupted.
   *
   * <p>MUST be invoked after recovering of {@link WorkspaceStatus#STARTING} runtime to track it and
   * avoid hanging.
   *
   * <p>{@link #stopTracking(RuntimeIdentity)} MUST be invoked if start is normally interrupted or
   * became RUNNING before timeout.
   *
   * @param runtime runtime to track
   * @param timeoutMin timeout before which runtime should change its state
   */
  public synchronized void trackStarting(KubernetesInternalRuntime runtime, long timeoutMin) {
    String workspaceId = runtime.getContext().getIdentity().getWorkspaceId();
    WaitStatusChangedTask waitStartingChangedTask =
        new WaitStatusChangedTask(
            runtime, WorkspaceStatus.STARTING, this::handleHangingStartingRuntime);

    LOG.debug(
        "Registered a task to check runtime '{}' to become RUNNING OR STOPPED after {} minutes",
        workspaceId,
        timeoutMin);
    workspaceId2Task.put(workspaceId, waitStartingChangedTask);
    getTimer().schedule(waitStartingChangedTask, TimeUnit.MINUTES.toMillis(timeoutMin));
  }

  private void handleHangingStartingRuntime(KubernetesInternalRuntime runtime) {
    RuntimeIdentity runtimeId = runtime.getContext().getIdentity();
    try {
      eventPublisher.sendAbnormalStoppingEvent(
          runtimeId, "Workspace is not started in time. Trying interrupt runtime start");
      runtime.stop(emptyMap());
      eventPublisher.sendAbnormalStoppedEvent(runtimeId, "Workspace start reached timeout");
      LOG.info(
          "Start of hanging runtime '{}:{}:{}' is interrupted",
          runtimeId.getWorkspaceId(),
          runtimeId.getEnvName(),
          runtimeId.getOwnerId());
    } catch (InfrastructureException e) {
      LOG.error(
          "Error occurred during start interruption of hanging runtime '{}:{}:{}'. Error: {}",
          runtimeId.getWorkspaceId(),
          runtimeId.getEnvName(),
          runtimeId.getOwnerId(),
          e.getMessage(),
          e);
    }
  }

  /**
   * Schedules a task to check whether the specified runtime changed its status from {@link
   * WorkspaceStatus#STOPPING} after the specified timeout and if it didn't change then runtime will
   * be stopped forcibly.
   *
   * <p>MUST be invoked after recovering of {@link WorkspaceStatus#STOPPING} runtime to track it and
   * avoid hanging.
   *
   * <p>{@link #stopTracking(RuntimeIdentity)} MUST be invoked if stop is normally finished before
   * timeout.
   *
   * @param runtime runtime to track
   * @param timeoutMin timeout before which runtime should change its state
   */
  public synchronized void trackStopping(KubernetesInternalRuntime runtime, long timeoutMin) {
    String workspaceId = runtime.getContext().getIdentity().getWorkspaceId();
    WaitStatusChangedTask waitStoppingChangedTask =
        new WaitStatusChangedTask(
            runtime, WorkspaceStatus.STOPPING, this::handleHangingStoppingRuntime);

    LOG.debug(
        "Registered a task to check workspace {} to become STOPPED after {} minutes",
        workspaceId,
        timeoutMin);
    workspaceId2Task.put(workspaceId, waitStoppingChangedTask);
    getTimer().schedule(waitStoppingChangedTask, TimeUnit.MINUTES.toMillis(timeoutMin));
  }

  private void handleHangingStoppingRuntime(KubernetesInternalRuntime runtime) {
    RuntimeIdentity runtimeId = runtime.getContext().getIdentity();
    try {
      eventPublisher.sendAbnormalStoppingEvent(
          runtimeId, "Workspace is not stopped in time. Trying to stop it forcibly");
      runtime.internalStop(emptyMap());
      runtime.markStopped();
      eventPublisher.sendAbnormalStoppedEvent(runtimeId, "Workspace stop reached timeout");
      LOG.info(
          "Runtime '{}:{}:{}' is not stopped in time. Stopped it forcibly",
          runtimeId.getWorkspaceId(),
          runtimeId.getEnvName(),
          runtimeId.getOwnerId());
    } catch (InfrastructureException e) {
      LOG.error(
          "Error occurred during forcibly stopping of hanging runtime '{}:{}:{}'. Error: {}",
          runtimeId.getWorkspaceId(),
          runtimeId.getEnvName(),
          runtimeId.getOwnerId(),
          e.getMessage(),
          e);
    }
  }

  /**
   * Stop tracking of runtime it is was registered before, otherwise do nothing.
   *
   * @param runtimeId identifier of runtime that should not be tracked anymore
   */
  public synchronized void stopTracking(RuntimeIdentity runtimeId) {
    TimerTask timerTask = workspaceId2Task.remove(runtimeId.getWorkspaceId());
    if (timerTask != null) {
      LOG.debug("Tracking task for workspace {} is canceled", runtimeId.getWorkspaceId());
      timerTask.cancel();

      if (workspaceId2Task.isEmpty()) {
        timeouts.cancel();
        timeouts = null;
      }
    }
  }

  private Timer getTimer() {
    if (timeouts == null) {
      timeouts = new Timer("TrackRuntimesStatuses", true);
    }
    return timeouts;
  }

  private class WaitStatusChangedTask extends TimerTask {

    private final KubernetesInternalRuntime runtime;
    private final WorkspaceStatus trackedStatus;
    private final Consumer<KubernetesInternalRuntime> failureCallback;

    private WaitStatusChangedTask(
        KubernetesInternalRuntime runtime,
        WorkspaceStatus trackedStatus,
        Consumer<KubernetesInternalRuntime> failureCallback) {
      this.runtime = runtime;
      this.trackedStatus = trackedStatus;
      this.failureCallback = failureCallback;
    }

    @Override
    public void run() {
      String workspaceId = runtime.getContext().getIdentity().getWorkspaceId();
      workspaceId2Task.remove(workspaceId);
      if (getRuntimeStatus(runtime) == trackedStatus) {
        LOG.debug(
            "Timeout is reached but workspace with id '{}' is still {}.",
            workspaceId,
            trackedStatus);

        workspaceSharedPool.execute(() -> failureCallback.accept(runtime));
      } else {
        LOG.debug(
            "Timeout is reached and workspace with id '{}' is not anymore",
            workspaceId,
            trackedStatus);
      }
    }
  }

  private WorkspaceStatus getRuntimeStatus(KubernetesInternalRuntime runtime) {
    try {
      return runtime.getStatus();
    } catch (InfrastructureException e) {
      return null;
    }
  }
}
