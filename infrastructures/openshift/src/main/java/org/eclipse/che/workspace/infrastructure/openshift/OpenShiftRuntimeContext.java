/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.openshift;

import com.google.inject.assistedinject.Assisted;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesRuntimeContext;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.KubernetesRuntimeStateCache;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesRuntimeState;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftProjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Sergii Leshchenko */
public class OpenShiftRuntimeContext extends KubernetesRuntimeContext<OpenShiftEnvironment> {

  private static final Logger LOG = LoggerFactory.getLogger(OpenShiftRuntimeContext.class);

  private final OpenShiftRuntimeFactory runtimeFactory;
  private final OpenShiftProjectFactory projectFactory;
  private final KubernetesRuntimeStateCache runtimeStatuses;

  @Inject
  public OpenShiftRuntimeContext(
      @Named("che.websocket.endpoint") String cheWebsocketEndpoint,
      OpenShiftProjectFactory projectFactory,
      OpenShiftRuntimeFactory runtimeFactory,
      KubernetesRuntimeStateCache runtimeStatuses,
      @Assisted OpenShiftEnvironment openShiftEnvironment,
      @Assisted RuntimeIdentity identity,
      @Assisted RuntimeInfrastructure infrastructure)
      throws ValidationException, InfrastructureException {
    super(
        cheWebsocketEndpoint,
        projectFactory,
        null, // should not be used by super class since getRuntime method is overridden
        runtimeStatuses,
        openShiftEnvironment,
        identity,
        infrastructure);
    this.runtimeStatuses = runtimeStatuses;
    this.runtimeFactory = runtimeFactory;
    this.projectFactory = projectFactory;
  }

  @Override
  public OpenShiftInternalRuntime getRuntime() throws InfrastructureException {
    Optional<KubernetesRuntimeState> runtimeStateOpt = runtimeStatuses.get(getIdentity());
    String workspaceId = getIdentity().getWorkspaceId();

    if (!runtimeStateOpt.isPresent()) {
      // there is no cached runtime, create a new one
      return runtimeFactory.create(this, projectFactory.getOrCreate(getIdentity()));
    }

    // there is cached runtime, restore cached one
    KubernetesRuntimeState runtimeState = runtimeStateOpt.get();
    RuntimeIdentity runtimeId = runtimeState.getRuntimeId();
    LOG.debug(
        "Restoring runtime `{}:{}:{}`",
        runtimeId.getWorkspaceId(),
        runtimeId.getEnvName(),
        runtimeId.getOwnerId());
    OpenShiftInternalRuntime runtime =
        runtimeFactory.create(
            this, projectFactory.access(workspaceId, runtimeState.getNamespace()));

    runtime.scheduleRuntimeStateChecks();

    return runtime;
  }
}
