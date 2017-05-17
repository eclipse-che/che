/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.workspace.infrastructure.docker.environment.dockerfile;

import com.google.common.base.Joiner;

import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.config.Recipe;
import org.eclipse.che.workspace.infrastructure.docker.environment.DockerConfigSourceSpecificEnvironmentParser;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerBuildContext;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;

import static java.lang.String.format;
import static org.eclipse.che.workspace.infrastructure.docker.ArgumentsValidator.checkArgument;

/**
 * Dockerfile specific environment parser.
 *
 * @author Alexander Garagatyi
 * @author Alexander Andrienko
 */
public class DockerfileEnvironmentParser implements DockerConfigSourceSpecificEnvironmentParser {

    @Override
    public DockerEnvironment parse(Environment environment) throws ValidationException {
        Recipe recipe = environment.getRecipe();

        if (!"dockerfile".equals(recipe.getType())) {
            throw new ValidationException(format("Dockerfile environment parser doesn't support recipe type '%s'",
                                                      recipe.getType()));
        }

        if (!"text/x-dockerfile".equals(recipe.getContentType())) {
            throw new ValidationException(format("Content type '%s' of recipe of environment is unsupported." +
                                                      " Supported values are: text/x-dockerfile",
                                                      recipe.getContentType()));
        }

        DockerEnvironment cheServiceEnv = new DockerEnvironment();
        DockerContainerConfig service = new DockerContainerConfig();
        cheServiceEnv.getServices().put(getMachineName(environment), service);

        if (recipe.getLocation() != null) {
            service.setBuild(new DockerBuildContext().withContext(recipe.getLocation()));
        } else {
            service.setBuild(new DockerBuildContext().withDockerfileContent(recipe.getContent()));
        }

        return cheServiceEnv;
    }

    private String getMachineName(Environment environment) throws ValidationException {
        checkArgument(environment.getMachines().size() == 1,
                      "Environment of type '%s' doesn't support multiple machines, but contains machines: %s",
                      environment.getRecipe().getType(),
                      Joiner.on(", ").join(environment.getMachines().keySet()));

        return environment.getMachines()
                          .keySet()
                          .iterator()
                          .next();
    }
}
