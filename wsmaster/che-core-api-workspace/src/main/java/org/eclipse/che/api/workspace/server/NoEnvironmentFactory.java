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
package org.eclipse.che.api.workspace.server;

import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.che.api.core.model.workspace.Warning;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironmentFactory;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.spi.environment.InternalRecipe;
import org.eclipse.che.api.workspace.server.spi.environment.MachineConfigsValidator;
import org.eclipse.che.api.workspace.server.spi.environment.RecipeRetriever;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * Fake environment factory for a case when sidecar-based workspace has no environment.
 *
 * @author Alexander Garagatyi
 */
public class NoEnvironmentFactory extends InternalEnvironmentFactory<InternalEnvironment> {

  @Inject
  public NoEnvironmentFactory(
      RecipeRetriever recipeRetriever, MachineConfigsValidator machinesValidator) {
    super(recipeRetriever, machinesValidator);
  }

  @Override
  protected InternalEnvironment doCreate(
      @Nullable InternalRecipe recipe,
      Map<String, InternalMachineConfig> machines,
      List<Warning> warnings)
      throws InternalInfrastructureException {
    if (recipe != null) {
      throw new InternalInfrastructureException(
          "No environment factory doesn't accept non-null workspace recipes");
    }
    return new NoEnvInternalEnvironment();
  }

  public static class NoEnvInternalEnvironment extends InternalEnvironment {}
}
