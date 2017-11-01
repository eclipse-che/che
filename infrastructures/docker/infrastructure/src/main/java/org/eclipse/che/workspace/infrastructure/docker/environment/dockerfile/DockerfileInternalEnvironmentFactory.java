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
package org.eclipse.che.workspace.infrastructure.docker.environment.dockerfile;

import static java.lang.String.format;

import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.Warning;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.installer.server.InstallerRegistry;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.InternalEnvironmentFactory;
import org.eclipse.che.api.workspace.server.spi.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.spi.InternalRecipe;
import org.eclipse.che.api.workspace.server.spi.RecipeRetriever;
import org.eclipse.che.workspace.infrastructure.docker.container.ContainersStartStrategy;
import org.eclipse.che.workspace.infrastructure.docker.environment.EnvironmentValidator;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerBuildContext;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;

/**
 * @author Alexander Garagatyi
 * @author Alexander Andrienko
 */
public class DockerfileInternalEnvironmentFactory extends InternalEnvironmentFactory {

  public static final String TYPE = "dockerfile";
  public static final String CONTENT_TYPE = "text/x-dockerfile";

  private final EnvironmentValidator validator;
  private final ContainersStartStrategy startStrategy;

  @Inject
  public DockerfileInternalEnvironmentFactory(
      InstallerRegistry installerRegistry,
      RecipeRetriever recipeRetriever,
      EnvironmentValidator validator,
      ContainersStartStrategy startStrategy) {
    super(installerRegistry, recipeRetriever);
    this.startStrategy = startStrategy;
    this.validator = validator;
  }

  @Override
  protected InternalEnvironment create(
      Map<String, InternalMachineConfig> machines, InternalRecipe recipe, List<Warning> warnings)
      throws InfrastructureException, ValidationException {

    DockerEnvironment dockerEnvironment = dockerEnv(machines, recipe);
    validator.validate(machines, dockerEnvironment);
    return new DockerfileInternalEnvironment(machines, recipe, warnings, dockerEnvironment);
  }

  private DockerEnvironment dockerEnv(
      Map<String, InternalMachineConfig> machines, InternalRecipe recipe)
      throws ValidationException {
    if (!TYPE.equals(recipe.getType())) {
      throw new ValidationException(
          format(
              "Dockerfile environment parser doesn't support recipe type '%s'", recipe.getType()));
    }

    if (!CONTENT_TYPE.equals(recipe.getContentType())) {
      throw new ValidationException(
          format(
              "Content type '%s' of recipe of environment is unsupported."
                  + " Supported values are: text/x-dockerfile",
              recipe.getContentType()));
    }

    Map.Entry<String, InternalMachineConfig> entry = machines.entrySet().iterator().next();
    String machineName = entry.getKey();
    InternalMachineConfig machineConfig = entry.getValue();

    DockerEnvironment cheContainerEnv = new DockerEnvironment();
    DockerContainerConfig container = new DockerContainerConfig();
    cheContainerEnv.getContainers().put(entry.getKey(), container);
    container.setBuild(new DockerBuildContext().setDockerfileContent(recipe.getContent()));

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

    return cheContainerEnv;
  }
}
