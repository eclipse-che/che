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
package org.eclipse.che.workspace.infrastructure.docker.environment.dockerimage;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;

import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.Warning;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.installer.server.InstallerRegistry;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.*;

/** @author Sergii Leshchenko */
@Singleton
public class DockerImageEnvironmentFactory
    extends InternalEnvironmentFactory<DockerImageEnvironment> {

  private final MemoryAttributeProvisioner memoryProvisioner;

  @Inject
  public DockerImageEnvironmentFactory(
      InstallerRegistry installerRegistry,
      RecipeRetriever recipeRetriever,
      MachineConfigsValidator machinesValidator,
      MemoryAttributeProvisioner memoryProvisioner) {
    super(installerRegistry, recipeRetriever, machinesValidator);
    this.memoryProvisioner = memoryProvisioner;
  }

  @Override
  public DockerImageEnvironment create(Environment sourceEnv)
      throws InfrastructureException, ValidationException {
    if (sourceEnv.getRecipe().getLocation() != null) {
      // move image from location to content
      EnvironmentImpl envCopy = new EnvironmentImpl(sourceEnv);
      envCopy.getRecipe().setContent(sourceEnv.getRecipe().getLocation());
      envCopy.getRecipe().setLocation(null);
      return super.create(envCopy);
    }
    return super.create(sourceEnv);
  }

  @Override
  protected DockerImageEnvironment doCreate(
      InternalRecipe recipe, Map<String, InternalMachineConfig> machines, List<Warning> warnings)
      throws InfrastructureException, ValidationException {
    if (!DockerImageEnvironment.TYPE.equals(recipe.getType())) {
      throw new ValidationException(
          format(
              "Docker image environment parser doesn't support recipe type '%s'",
              recipe.getType()));
    }

    String dockerImage = recipe.getContent();

    checkArgument(dockerImage != null, "Docker image should not be null.");

    addRamAttributes(machines);

    return new DockerImageEnvironment(dockerImage, recipe, machines, warnings);
  }

  private void addRamAttributes(Map<String, InternalMachineConfig> machines) {
    for (InternalMachineConfig machineConfig : machines.values()) {
      memoryProvisioner.provision(machineConfig, 0L, 0L);
    }
  }
}
