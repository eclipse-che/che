/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
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

import static java.lang.String.format;
import static java.util.Collections.emptyMap;

import com.google.common.annotations.VisibleForTesting;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.WorkspaceRuntimes;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalRuntime;
import org.eclipse.che.commons.schedule.ScheduleDelay;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.RuntimeEventsPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Periodically checks runtimes consistency and forcibly stop ones which has inconsistent state.
 *
 * @author Sergii Leshchenko
 */
@Singleton
public class InconsistentRuntimesDetector {

  private static final Logger LOG = LoggerFactory.getLogger(InconsistentRuntimesDetector.class);

  private final RuntimeEventsPublisher eventPublisher;
  private final WorkspaceRuntimes workspaceRuntimes;

  @Inject
  public InconsistentRuntimesDetector(
      RuntimeEventsPublisher eventPublisher, WorkspaceRuntimes workspaceRuntimes) {
    this.eventPublisher = eventPublisher;
    this.workspaceRuntimes = workspaceRuntimes;
  }

  @ScheduleDelay(
      delayParameterName = "che.infra.kubernetes.runtimes_consistency_check_period_min",
      initialDelayParameterName = "che.infra.kubernetes.runtimes_consistency_check_period_min",
      unit = TimeUnit.MINUTES)
  public void check() {
    Set<String> runningWorkspaces = workspaceRuntimes.getRunning();
    LOG.info(
        "Runtimes consistency check is running. Checking {} workspaces", runningWorkspaces.size());
    for (String runningWorkspaceId : runningWorkspaces) {
      try {
        checkOne(runningWorkspaceId);
      } catch (InfrastructureException e) {
        LOG.error(
            "Checking consistency of runtime for workspace `{}` is failed. Cause: ",
            e.getMessage(),
            e);
      }
    }
  }

  @VisibleForTesting
  void checkOne(String workspaceId) throws InfrastructureException {
    LOG.debug("Checking consistency of runtime for workspace `{}`", workspaceId);
    KubernetesInternalRuntime k8sRuntime = getKubernetesInternalRuntime(workspaceId);
    RuntimeIdentity runtimeId = k8sRuntime.getContext().getIdentity();

    try {
      if (k8sRuntime.isConsistent()) {
        return;
      }
    } catch (InfrastructureException e) {
      throw new InfrastructureException(
          format(
              "Error occurred during runtime '%s:%s' consistency checking. Cause: %s",
              runtimeId.getWorkspaceId(), runtimeId.getOwnerId(), e.getMessage()),
          e);
    }

    // check if status is still RUNNING
    // not to initialize abnormal stop for a runtime that is not RUNNING anymore
    if (!isRunning(k8sRuntime)) {
      return;
    }

    LOG.warn(
        "Found runtime `{}:{}` with inconsistent state. It's going to be stopped automatically",
        runtimeId.getWorkspaceId(),
        runtimeId.getOwnerId());

    stopAbnormally(k8sRuntime);
    LOG.debug("Checking consistency of runtime for workspace `{}` is finished", workspaceId);
  }

  private boolean isRunning(KubernetesInternalRuntime k8sRuntime) throws InfrastructureException {
    try {
      return k8sRuntime.getStatus() == WorkspaceStatus.RUNNING;
    } catch (InfrastructureException e) {
      throw new InfrastructureException(
          "Error occurred during runtime status fetching during consistency checking. Cause: "
              + e.getMessage(),
          e);
    }
  }

  private void stopAbnormally(KubernetesInternalRuntime k8sRuntime) throws InfrastructureException {
    RuntimeIdentity runtimeId = k8sRuntime.getContext().getIdentity();
    eventPublisher.sendAbnormalStoppingEvent(runtimeId, "The runtime has inconsistent state.");
    try {
      k8sRuntime.stop(emptyMap());
    } catch (InfrastructureException e) {
      throw new InfrastructureException(
          format(
              "Failed to stop the runtime '%s:%s' which has inconsistent state. Error: %s",
              runtimeId.getWorkspaceId(), runtimeId.getOwnerId(), e.getMessage()),
          e);
    } finally {
      eventPublisher.sendAbnormalStoppedEvent(runtimeId, "The runtime has inconsistent state.");
    }
  }

  private KubernetesInternalRuntime getKubernetesInternalRuntime(String workspaceId)
      throws InfrastructureException {
    InternalRuntime<?> internalRuntime;
    try {
      internalRuntime = workspaceRuntimes.getInternalRuntime(workspaceId);
    } catch (InfrastructureException | ServerException e) {
      throw new InfrastructureException(
          format(
              "Failed to get internal runtime for workspace `%s` to check consistency. Cause: %s",
              workspaceId, e.getMessage()),
          e);
    }

    RuntimeIdentity runtimeId = internalRuntime.getContext().getIdentity();
    if (!(internalRuntime instanceof KubernetesInternalRuntime)) {
      // must not happen
      throw new InfrastructureException(
          format(
              "Fetched internal runtime '%s:%s' is not Kubernetes, it is not possible to check consistency.",
              runtimeId.getWorkspaceId(), runtimeId.getOwnerId()));
    }

    return (KubernetesInternalRuntime) internalRuntime;
  }
}
