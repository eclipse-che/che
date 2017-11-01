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
package org.eclipse.che.workspace.infrastructure.docker.environment.dockerimage;

import static java.lang.String.format;

import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.Warning;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.installer.server.InstallerRegistry;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.InternalEnvironmentFactory;
import org.eclipse.che.api.workspace.server.spi.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.spi.InternalRecipe;
import org.eclipse.che.api.workspace.server.spi.RecipeRetriever;
import org.eclipse.che.workspace.infrastructure.docker.environment.EnvironmentValidator;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;

/**
 * @author Alexander Garagatyi
 * @author Alexander Andrienko
 */
public class DockerimageInternalEnvironmentFactory extends InternalEnvironmentFactory {

  public static final String TYPE = "dockerimage";

  private final EnvironmentValidator validator;

  @Inject
  public DockerimageInternalEnvironmentFactory(
      InstallerRegistry installerRegistry,
      RecipeRetriever recipeRetriever,
      EnvironmentValidator validator) {
    super(installerRegistry, recipeRetriever);
    this.validator = validator;
  }

  @Override
  public InternalEnvironment create(final Environment environment)
      throws InfrastructureException, ValidationException {

    EnvironmentImpl envCopy = new EnvironmentImpl(environment);
    if (envCopy.getRecipe().getLocation() != null) {
      // move image from location to content
      envCopy.getRecipe().setContent(environment.getRecipe().getLocation());
      envCopy.getRecipe().setLocation(null);
    }
    return super.create(envCopy);
  }

  @Override
  protected InternalEnvironment create(
      Map<String, InternalMachineConfig> machines, InternalRecipe recipe, List<Warning> warnings)
      throws InfrastructureException, ValidationException {

    DockerEnvironment dockerEnvironment = dockerEnv(machines, recipe);
    validator.validate(machines, dockerEnvironment);
    return new DockerimageInternalEnvironment(machines, recipe, warnings, dockerEnvironment);
  }

  private DockerEnvironment dockerEnv(
      Map<String, InternalMachineConfig> machines, InternalRecipe recipe)
      throws ValidationException {

    // empty list of machines is not expected, no needs to check for next()
    Map.Entry<String, InternalMachineConfig> entry = machines.entrySet().iterator().next();
    String machineName = entry.getKey();
    InternalMachineConfig machineConfig = entry.getValue();

    DockerEnvironment dockerEnv = new DockerEnvironment();
    DockerContainerConfig container = new DockerContainerConfig();
    dockerEnv.getContainers().put(machineName, container);

    container.setImage(recipe.getContent());
    for (ServerConfig server : machineConfig.getServers().values()) {
      container.addExpose(server.getPort());
    }
    if (machineConfig.getAttributes().containsKey("memoryLimitBytes")) {
      try {
        container.setMemLimit(
            Long.parseLong(machineConfig.getAttributes().get("memoryLimitBytes")));
      } catch (NumberFormatException e) {
        throw new ValidationException(
            format("Value of attribute 'memoryLimitBytes' of machine '%s' is illegal", machineName));
      }
    }

    return dockerEnv;
  }
}
