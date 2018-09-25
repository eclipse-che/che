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
package org.eclipse.che.workspace.infrastructure.docker.environment.convert;

import static org.eclipse.che.workspace.infrastructure.docker.ArgumentsValidator.checkArgument;

import com.google.common.base.Joiner;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.environment.dockerfile.DockerfileEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerBuildContext;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;

/**
 * Converts {@link DockerfileEnvironment} to {@link DockerEnvironment}.
 *
 * @author Alexander Garagatyi
 * @author Alexander Andrienko
 */
public class DockerfileEnvironmentConverter implements DockerEnvironmentConverter {
  @Override
  public DockerEnvironment convert(InternalEnvironment environment) throws ValidationException {
    if (!(environment instanceof DockerfileEnvironment)) {
      throw new ValidationException("The specified environment is not dockerfile environment");
    }
    DockerfileEnvironment dockerfileEnv = (DockerfileEnvironment) environment;

    String machineName = getMachineName(dockerfileEnv);
    String dockerfile = dockerfileEnv.getDockerfileContent();

    DockerEnvironment cheContainerEnv =
        new DockerEnvironment(
                environment.getRecipe(), environment.getMachines(), environment.getWarnings())
            .setType(DockerEnvironment.TYPE);
    DockerContainerConfig container =
        new DockerContainerConfig()
            .setBuild(new DockerBuildContext().setDockerfileContent(dockerfile));
    cheContainerEnv.getContainers().put(machineName, container);
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
