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

import static com.google.common.base.Strings.isNullOrEmpty;

import com.google.inject.assistedinject.Assisted;
import java.net.URI;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalRuntime;
import org.eclipse.che.api.workspace.server.spi.RuntimeContext;
import org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftProject;

/** @author Sergii Leshchenko */
public class OpenShiftRuntimeContext extends RuntimeContext {
  private final OpenShiftClientFactory clientFactory;
  private final OpenShiftEnvironment openShiftEnvironment;
  private final OpenShiftRuntimeFactory runtimeFactory;
  private final String websocketOutputEndpoint;
  private final String projectName;

  @Inject
  public OpenShiftRuntimeContext(
      @Assisted InternalEnvironment environment,
      @Assisted OpenShiftEnvironment openShiftEnvironment,
      @Assisted RuntimeIdentity identity,
      @Assisted RuntimeInfrastructure infrastructure,
      OpenShiftClientFactory clientFactory,
      OpenShiftRuntimeFactory runtimeFactory,
      @Named("che.websocket.endpoint") String cheWebsocketEndpoint,
      @Nullable @Named("che.infra.openshift.project") String projectName)
      throws ValidationException, InfrastructureException {

    super(environment, identity, infrastructure);
    this.clientFactory = clientFactory;
    this.runtimeFactory = runtimeFactory;
    this.openShiftEnvironment = openShiftEnvironment;
    this.websocketOutputEndpoint = cheWebsocketEndpoint;
    this.projectName = projectName;
  }

  /** Returns OpenShift environment which based on normalized context environment configuration. */
  public OpenShiftEnvironment getOpenShiftEnvironment() {
    return openShiftEnvironment;
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
  public InternalRuntime getRuntime() throws InfrastructureException {
    String name = isNullOrEmpty(projectName) ? getIdentity().getWorkspaceId() : projectName;
    OpenShiftProject project = new OpenShiftProject(name, clientFactory);
    return runtimeFactory.create(this, project);
  }
}
