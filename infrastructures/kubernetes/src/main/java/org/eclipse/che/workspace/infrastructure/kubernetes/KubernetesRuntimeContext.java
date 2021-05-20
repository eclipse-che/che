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

import com.google.inject.assistedinject.Assisted;
import java.net.URI;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.api.workspace.server.spi.RuntimeContext;
import org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.KubernetesRuntimeStateCache;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesRuntimeState;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespaceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Sergii Leshchenko */
public class KubernetesRuntimeContext<T extends KubernetesEnvironment> extends RuntimeContext<T> {

  private static final Logger LOG = LoggerFactory.getLogger(KubernetesRuntimeContext.class);

  private final KubernetesRuntimeFactory<T> runtimeFactory;
  private final KubernetesNamespaceFactory namespaceFactory;
  private final String websocketOutputEndpoint;
  private final KubernetesRuntimeStateCache runtimeStatuses;

  @Inject
  public KubernetesRuntimeContext(
      @Named("che.websocket.endpoint") String cheWebsocketEndpoint,
      KubernetesNamespaceFactory namespaceFactory,
      KubernetesRuntimeFactory<T> runtimeFactory,
      KubernetesRuntimeStateCache runtimeStatuses,
      @Assisted T kubernetesEnvironment,
      @Assisted RuntimeIdentity identity,
      @Assisted RuntimeInfrastructure infrastructure)
      throws ValidationException, InfrastructureException {
    super(kubernetesEnvironment, identity, infrastructure);
    this.namespaceFactory = namespaceFactory;
    this.runtimeFactory = runtimeFactory;
    this.websocketOutputEndpoint = cheWebsocketEndpoint;
    this.runtimeStatuses = runtimeStatuses;
  }

  @Override
  public URI getOutputChannel() throws InfrastructureException {
    try {
      return URI.create(websocketOutputEndpoint);
    } catch (IllegalArgumentException ex) {
      throw new InternalInfrastructureException(
          "Failed to get the output channel.  " + ex.getMessage());
    }
  }

  @Override
  public KubernetesInternalRuntime getRuntime() throws InfrastructureException {
    Optional<KubernetesRuntimeState> runtimeStateOpt = runtimeStatuses.get(getIdentity());
    String workspaceId = getIdentity().getWorkspaceId();

    if (!runtimeStateOpt.isPresent()) {
      // there is no cached runtime, create a new one
      return runtimeFactory.create(this, namespaceFactory.getOrCreate(getIdentity()));
    }

    // there is cached runtime, restore cached one
    KubernetesRuntimeState runtimeState = runtimeStateOpt.get();
    RuntimeIdentity runtimeId = runtimeState.getRuntimeId();
    LOG.debug(
        "Restoring runtime `{}:{}:{}`",
        runtimeId.getWorkspaceId(),
        runtimeId.getEnvName(),
        runtimeId.getOwnerId());
    KubernetesInternalRuntime runtime =
        runtimeFactory.create(
            this, namespaceFactory.access(workspaceId, runtimeState.getNamespace()));

    runtime.scheduleRuntimeStateChecks();

    return runtime;
  }
}
