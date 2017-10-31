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
package org.eclipse.che.workspace.infrastructure.docker.environment.compose;

import static org.eclipse.che.workspace.infrastructure.docker.ArgumentsValidator.checkNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.util.Map;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.workspace.server.spi.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.InternalRecipe;
import org.eclipse.che.workspace.infrastructure.docker.environment.DockerConfigSourceSpecificEnvironmentParser;
import org.eclipse.che.workspace.infrastructure.docker.environment.compose.model.ComposeEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.environment.compose.model.ComposeService;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerBuildContext;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;

/**
 * Converters compose file to {@link ComposeEnvironment} and vise versa or converters compose {@link
 * Environment} to {@link DockerEnvironment}.
 *
 * @author Alexander Garagatyi
 * @author Alexander Andrienko
 */
public class ComposeEnvironmentParser implements DockerConfigSourceSpecificEnvironmentParser {

  private static final ObjectMapper YAML_PARSER = new ObjectMapper(new YAMLFactory());

  @Override
  public DockerEnvironment parse(InternalEnvironment environment) throws ValidationException {
    checkNotNull(environment, "Environment should not be null");
    InternalRecipe recipe = environment.getRecipe();
    checkNotNull(environment.getRecipe(), "Environment recipe should not be null");
    ComposeEnvironment composeEnvironment = parse(recipe.getContent(), recipe.getContentType());
    return asDockerEnvironment(composeEnvironment);
  }

  /**
   * Parses compose file into Docker Compose model.
   *
   * @param recipeContent compose file to parse
   * @throws ValidationException when environment or environment recipe is invalid
   */
  @VisibleForTesting
  ComposeEnvironment parse(String recipeContent, String contentType) throws ValidationException {
    checkNotNull(recipeContent, "Recipe content should not be null");
    checkNotNull(contentType, "Recipe content type should not be null");

    ComposeEnvironment composeEnvironment;
    switch (contentType) {
      case "application/x-yaml":
      case "text/yaml":
      case "text/x-yaml":
        try {
          composeEnvironment = YAML_PARSER.readValue(recipeContent, ComposeEnvironment.class);
        } catch (IOException e) {
          throw new ValidationException(
              "Parsing of environment configuration failed. " + e.getLocalizedMessage());
        }
        break;
      default:
        throw new ValidationException(
            "Provided environment recipe content type '"
                + contentType
                + "' is unsupported. Supported values are: "
                + "application/x-yaml, text/yaml, text/x-yaml");
    }
    return composeEnvironment;
  }

  /**
   * Converts Docker Compose environment model into YAML file.
   *
   * @param composeEnvironment Docker Compose environment model file
   * @throws ValidationException when argument is null or conversion to YAML fails
   */
  public String toYaml(ComposeEnvironment composeEnvironment) throws ValidationException {
    checkNotNull(composeEnvironment, "Compose environment should not be null");

    try {
      return YAML_PARSER.writeValueAsString(composeEnvironment);
    } catch (JsonProcessingException e) {
      throw new ValidationException(e.getLocalizedMessage(), e);
    }
  }

  private DockerEnvironment asDockerEnvironment(ComposeEnvironment composeEnvironment) {
    Map<String, DockerContainerConfig> containers =
        Maps.newHashMapWithExpectedSize(composeEnvironment.getServices().size());
    for (Map.Entry<String, ComposeService> composeServiceEntry :
        composeEnvironment.getServices().entrySet()) {
      ComposeService service = composeServiceEntry.getValue();

      DockerContainerConfig cheContainer =
          new DockerContainerConfig()
              .setCommand(service.getCommand())
              .setContainerName(service.getContainerName())
              .setDependsOn(service.getDependsOn())
              .setEntrypoint(service.getEntrypoint())
              .setEnvironment(service.getEnvironment())
              .setExpose(service.getExpose())
              .setImage(service.getImage())
              .setLabels(service.getLabels())
              .setLinks(service.getLinks())
              .setMemLimit(service.getMemLimit())
              .setNetworks(service.getNetworks())
              .setPorts(service.getPorts())
              .setVolumes(service.getVolumes())
              .setVolumesFrom(service.getVolumesFrom());

      if (service.getBuild() != null) {
        cheContainer.setBuild(
            new DockerBuildContext()
                .setContext(service.getBuild().getContext())
                .setDockerfilePath(service.getBuild().getDockerfile())
                .setArgs(service.getBuild().getArgs()));
      }

      containers.put(composeServiceEntry.getKey(), cheContainer);
    }
    return new DockerEnvironment().setContainers(containers);
  }
}
