/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.docker.machine.parser;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.core.model.workspace.EnvironmentRecipe;
import org.eclipse.che.api.environment.server.model.CheServiceBuildContextImpl;
import org.eclipse.che.api.environment.server.model.CheServiceImpl;
import org.eclipse.che.api.environment.server.model.CheServicesEnvironmentImpl;

import static java.lang.String.format;

/**
 * Dockerfile specific environment parser.
 *
 * @author Alexander Garagatyi
 * @author Alexander Andrienko
 */
public class DockerfileEnvironmentParser extends DockerEnvironmentParser {

    @Override
    public CheServicesEnvironmentImpl parse(Environment environment) throws IllegalArgumentException, ServerException {
        EnvironmentRecipe recipe = environment.getRecipe();

        if (!"dockerfile".equals(recipe.getType())) {
            throw new IllegalArgumentException(format("Dockerfile environment parser doesn't support recipe type '%s'",
                                                      recipe.getType()));
        }

        if (!"text/x-dockerfile".equals(recipe.getContentType())) {
            throw new IllegalArgumentException(format("Content type '%s' of recipe of environment is unsupported." +
                                                      " Supported values are: text/x-dockerfile",
                                                      recipe.getContentType()));
        }

        CheServicesEnvironmentImpl cheServiceEnv = new CheServicesEnvironmentImpl();
        CheServiceImpl service = new CheServiceImpl();
        cheServiceEnv.getServices().put(getMachineName(environment), service);

        if (recipe.getLocation() != null) {
            service.setBuild(new CheServiceBuildContextImpl().withContext(recipe.getLocation()));
        } else {
            service.setBuild(new CheServiceBuildContextImpl().withDockerfileContent(recipe.getContent()));
        }

        return cheServiceEnv;
    }
}
