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
package org.eclipse.che.workspace.infrastructure.docker.environment.dockerimage;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.config.Recipe;
import org.eclipse.che.workspace.infrastructure.docker.environment.AbstractEnvironmentParser;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerService;

import static java.lang.String.format;

/**
 * Docker image specific environment parser.
 *
 * @author Alexander Garagatyi
 * @author Alexander Andrienko
 */
public class DockerImageEnvironmentParser extends AbstractEnvironmentParser {

    @Override
    public DockerEnvironment parse(Environment environment) throws IllegalArgumentException, ServerException {
        Recipe recipe = environment.getRecipe();

        if (!"dockerimage".equals(recipe.getType())) {
            throw new IllegalArgumentException(format("Docker image environment parser doesn't support recipe type '%s'",
                                                      recipe.getType()));
        }

        DockerEnvironment dockerEnv = new DockerEnvironment();
        DockerService service = new DockerService();
        dockerEnv.getServices().put(getMachineName(environment), service);

        service.setImage(recipe.getLocation());

        return dockerEnv;
    }
}
