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
package org.eclipse.che.workspace.infrastructure.docker.environment.dockerfile;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;

import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.Warning;
import org.eclipse.che.api.installer.server.InstallerRegistry;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.*;

/** @author Sergii Leshchenko */
@Singleton
public class DockerfileEnvironmentFactory
    extends InternalEnvironmentFactory<DockerfileEnvironment> {

  private final MemoryAttributeProvisioner memoryProvisioner;

  @Inject
  public DockerfileEnvironmentFactory(
      InstallerRegistry installerRegistry,
      RecipeRetriever recipeRetriever,
      MachineConfigsValidator machinesValidator,
      MemoryAttributeProvisioner memoryProvisioner) {
    super(installerRegistry, recipeRetriever, machinesValidator);
    this.memoryProvisioner = memoryProvisioner;
  }

  @Override
  protected DockerfileEnvironment doCreate(
      InternalRecipe recipe, Map<String, InternalMachineConfig> machines, List<Warning> warnings)
      throws InfrastructureException, ValidationException {
    if (!DockerfileEnvironment.TYPE.equals(recipe.getType())) {
      throw new ValidationException(
          format(
              "Dockerfile environment parser doesn't support recipe type '%s'", recipe.getType()));
    }

    String dockerfile = recipe.getContent();

    checkArgument(dockerfile != null, "Dockerfile content should not be null.");

    addRamAttributes(machines);

    return new DockerfileEnvironment(dockerfile, recipe, machines, warnings);
  }

  void addRamAttributes(Map<String, InternalMachineConfig> machines) {
    // sets default ram limit and request attributes if not present
    for (InternalMachineConfig machineConfig : machines.values()) {
      memoryProvisioner.provision(machineConfig, 0L, 0L);
    }
  }
}
