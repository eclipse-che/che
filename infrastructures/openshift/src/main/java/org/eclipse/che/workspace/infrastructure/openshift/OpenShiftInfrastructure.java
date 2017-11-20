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

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.provision.InternalEnvironmentProvisioner;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;

/** @author Sergii Leshchenko */
@Singleton
public class OpenShiftInfrastructure extends RuntimeInfrastructure {

  public static final String NAME = "openshift";

  private final OpenShiftRuntimeContextFactory runtimeContextFactory;
  private final OpenShiftEnvironmentProvisioner osEnvProvisioner;

  @Inject
  public OpenShiftInfrastructure(
      EventService eventService,
      OpenShiftRuntimeContextFactory runtimeContextFactory,
      OpenShiftEnvironmentProvisioner osEnvProvisioner,
      Set<InternalEnvironmentProvisioner> internalEnvProvisioners) {
    super(NAME, ImmutableSet.of(OpenShiftEnvironment.TYPE), eventService, internalEnvProvisioners);
    this.runtimeContextFactory = runtimeContextFactory;
    this.osEnvProvisioner = osEnvProvisioner;
  }

  @Override
  protected OpenShiftRuntimeContext internalPrepare(
      RuntimeIdentity id, InternalEnvironment environment)
      throws ValidationException, InfrastructureException {
    if (!(environment instanceof OpenShiftEnvironment)) {
      throw new ValidationException(
          "Environment type '%s' is not supported. Supported environment " + "types: %s",
          environment.getRecipe().getType(), OpenShiftEnvironment.TYPE);
    }
    OpenShiftEnvironment openShiftEnvironment = (OpenShiftEnvironment) environment;

    osEnvProvisioner.provision(openShiftEnvironment, id);

    return runtimeContextFactory.create(openShiftEnvironment, id, this);
  }
}
