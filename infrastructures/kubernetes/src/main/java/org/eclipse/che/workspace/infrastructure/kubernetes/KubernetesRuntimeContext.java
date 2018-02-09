/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes;

import com.google.inject.assistedinject.Assisted;
import java.net.URI;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.api.workspace.server.spi.RuntimeContext;
import org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespaceFactory;

/** @author Sergii Leshchenko */
public class KubernetesRuntimeContext<T extends KubernetesEnvironment> extends RuntimeContext<T> {

  private final KubernetesRuntimeFactory runtimeFactory;
  private final KubernetesNamespaceFactory namespaceFactory;
  private final String websocketOutputEndpoint;

  @Inject
  public KubernetesRuntimeContext(
      @Named("che.websocket.endpoint") String cheWebsocketEndpoint,
      KubernetesNamespaceFactory namespaceFactory,
      KubernetesRuntimeFactory runtimeFactory,
      @Assisted T kubernetesEnvironment,
      @Assisted RuntimeIdentity identity,
      @Assisted RuntimeInfrastructure infrastructure)
      throws ValidationException, InfrastructureException {
    super(kubernetesEnvironment, identity, infrastructure);
    this.namespaceFactory = namespaceFactory;
    this.runtimeFactory = runtimeFactory;
    this.websocketOutputEndpoint = cheWebsocketEndpoint;
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
    return runtimeFactory.create(
        this,
        namespaceFactory.create(getIdentity().getWorkspaceId()),
        getEnvironment().getWarnings());
  }
}
