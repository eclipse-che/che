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
package org.eclipse.che.workspace.infrastructure.docker.environment;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.eclipse.che.workspace.infrastructure.docker.ArgumentsValidator.checkArgument;
import static org.eclipse.che.workspace.infrastructure.docker.ArgumentsValidator.checkNotNull;

import com.google.common.base.Joiner;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.config.MachineConfig;
import org.eclipse.che.api.core.model.workspace.config.Recipe;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;

/**
 * Parses {@link Environment} into {@link DockerEnvironment}.
 *
 * @author Alexander Garagatyi
 * @author Alexander Andrienko
 */
public class EnvironmentParser {
  private final Map<String, DockerConfigSourceSpecificEnvironmentParser> environmentParsers;

  @Inject
  public EnvironmentParser(
      Map<String, DockerConfigSourceSpecificEnvironmentParser> environmentParsers) {
    this.environmentParsers = environmentParsers;
  }

  /**
   * Parses {@link Environment} into {@link DockerEnvironment}.
   *
   * @param environment environment to parse
   * @return environment representation as compose environment
   * @throws ValidationException if provided environment is illegal
   * @throws InfrastructureException if fetching of environment recipe content fails
   */
  public DockerEnvironment parse(Environment environment)
      throws ValidationException, InfrastructureException {

    checkNotNull(environment, "Environment should not be null");
    Recipe recipe = environment.getRecipe();
    checkNotNull(recipe, "Environment recipe should not be null");
    checkNotNull(recipe.getType(), "Environment recipe type should not be null");
    checkArgument(
        recipe.getContent() != null || recipe.getLocation() != null,
        "Recipe of environment must contain location or content");
    checkArgument(
        recipe.getContent() == null || recipe.getLocation() == null,
        "Recipe of environment contains mutually exclusive fields location and content");

    DockerConfigSourceSpecificEnvironmentParser parser = environmentParsers.get(recipe.getType());
    if (parser == null) {
      throw new ValidationException(
          format(
              "Environment type '%s' is not supported. " + "Supported environment types: %s",
              recipe.getType(), Joiner.on(", ").join(environmentParsers.keySet())));
    }

    DockerEnvironment dockerEnvironment = parser.parse(environment);

    for (Map.Entry<String, DockerContainerConfig> entry :
        dockerEnvironment.getContainers().entrySet()) {
      MachineConfig machineConfig = environment.getMachines().get(entry.getKey());
      if (machineConfig != null) {
        normalizeMachine(entry.getKey(), entry.getValue(), machineConfig);
      }
    }

    return dockerEnvironment;
  }

  private void normalizeMachine(
      String name, DockerContainerConfig container, MachineConfig machineConfig)
      throws ValidationException {
    if (machineConfig.getAttributes().containsKey("memoryLimitBytes")) {
      try {
        container.setMemLimit(
            Long.parseLong(machineConfig.getAttributes().get("memoryLimitBytes")));
      } catch (NumberFormatException e) {
        throw new ValidationException(
            format("Value of attribute 'memoryLimitBytes' of machine '%s' is illegal", name));
      }
    }
    container.setExpose(
        container
            .getExpose()
            .stream()
            .map(expose -> expose.contains("/") ? expose : expose + "/tcp")
            .collect(toList()));
    for (ServerConfig serverConfig : machineConfig.getServers().values()) {
      String normalizedPort =
          serverConfig.getPort().contains("/")
              ? serverConfig.getPort()
              : serverConfig.getPort() + "/tcp";

      container.getExpose().add(normalizedPort);
    }
  }
}
