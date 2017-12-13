/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.openshift;

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
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftProjectFactory;

/** @author Sergii Leshchenko */
public class OpenShiftRuntimeContext extends RuntimeContext<OpenShiftEnvironment> {

  private final OpenShiftRuntimeFactory runtimeFactory;
  private final OpenShiftProjectFactory projectFactory;
  private final String websocketOutputEndpoint;

  @Inject
  public OpenShiftRuntimeContext(
      @Named("che.websocket.endpoint") String cheWebsocketEndpoint,
      OpenShiftProjectFactory projectFactory,
      OpenShiftRuntimeFactory runtimeFactory,
      @Assisted OpenShiftEnvironment openShiftEnvironment,
      @Assisted RuntimeIdentity identity,
      @Assisted RuntimeInfrastructure infrastructure)
      throws ValidationException, InfrastructureException {
    super(openShiftEnvironment, identity, infrastructure);
    this.projectFactory = projectFactory;
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
  public OpenShiftInternalRuntime getRuntime() throws InfrastructureException {
    return runtimeFactory.create(
        this,
        projectFactory.create(getIdentity().getWorkspaceId()),
        getEnvironment().getWarnings());
  }
}
