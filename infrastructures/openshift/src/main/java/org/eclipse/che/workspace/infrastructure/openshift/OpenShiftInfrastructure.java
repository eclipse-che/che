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
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftInternalEnvironment;

/** @author Sergii Leshchenko */
@Singleton
public class OpenShiftInfrastructure extends RuntimeInfrastructure {
  private final OpenShiftRuntimeContextFactory runtimeContextFactory;
  private final OpenShiftInfrastructureProvisioner infrastructureProvisioner;

  @Inject
  public OpenShiftInfrastructure(
      OpenShiftRuntimeContextFactory runtimeContextFactory,
      OpenShiftInfrastructureProvisioner infrastructureProvisioner,
      EventService eventService) {
    super("openshift", ImmutableSet.of("openshift"), eventService);
    this.runtimeContextFactory = runtimeContextFactory;
    this.infrastructureProvisioner = infrastructureProvisioner;
  }

  @Override
  public OpenShiftRuntimeContext prepare(RuntimeIdentity id, InternalEnvironment environment)
      throws ValidationException, InfrastructureException {

    String recipeType = environment.getRecipe().getType();
    if (recipeType.equals("openshift")) {

      OpenShiftInternalEnvironment openShiftEnvironment =
          (OpenShiftInternalEnvironment) environment;
      infrastructureProvisioner.provision(openShiftEnvironment, id);

      return runtimeContextFactory.create(openShiftEnvironment, id, this);
    } else {
      throw new InfrastructureException("Unknown recipe type " + recipeType);
    }
  }
}
