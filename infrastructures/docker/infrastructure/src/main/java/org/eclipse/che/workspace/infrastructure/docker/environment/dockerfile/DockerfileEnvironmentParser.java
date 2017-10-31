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
import static org.eclipse.che.workspace.infrastructure.docker.ArgumentsValidator.checkArgument;

import com.google.common.base.Joiner;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.workspace.server.spi.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.InternalRecipe;
import org.eclipse.che.workspace.infrastructure.docker.environment.DockerConfigSourceSpecificEnvironmentParser;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerBuildContext;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;

/**
 * Dockerfile specific environment parser.
 *
 * @author Alexander Garagatyi
 * @author Alexander Andrienko
 */
public class DockerfileEnvironmentParser implements DockerConfigSourceSpecificEnvironmentParser {
  public static final String TYPE = "dockerfile";
  public static final String CONTENT_TYPE = "text/x-dockerfile";

  @Override
  public DockerEnvironment parse(InternalEnvironment environment) throws ValidationException {
    InternalRecipe recipe = environment.getRecipe();

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

    DockerEnvironment cheContainerEnv = new DockerEnvironment();
    DockerContainerConfig container = new DockerContainerConfig();
    cheContainerEnv.getContainers().put(getMachineName(environment), container);
    container.setBuild(new DockerBuildContext().setDockerfileContent(recipe.getContent()));

    return cheContainerEnv;
  }

  private String getMachineName(InternalEnvironment environment) throws ValidationException {
    checkArgument(
        environment.getMachines().size() == 1,
        "Environment of type '%s' doesn't support multiple machines, but contains machines: %s",
        environment.getRecipe().getType(),
        Joiner.on(", ").join(environment.getMachines().keySet()));

    return environment.getMachines().keySet().iterator().next();
  }
}
