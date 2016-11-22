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
import org.eclipse.che.api.environment.server.model.CheServiceImpl;
import org.eclipse.che.api.environment.server.model.CheServicesEnvironmentImpl;

import static java.lang.String.format;

/**
 * Docker image specific environment parser.
 *
 * @author Alexander Garagatyi
 * @author Alexander Andrienko
 */
public class DockerImageEnvironmentParser extends DockerEnvironmentParser {

    @Override
    public CheServicesEnvironmentImpl parse(Environment environment) throws IllegalArgumentException, ServerException {
        EnvironmentRecipe recipe = environment.getRecipe();

        if (!"dockerimage".equals(recipe.getType())) {
            throw new IllegalArgumentException(format("Docker image environment parser doesn't support recipe type '%s'",
                                                      recipe.getType()));
        }

        CheServicesEnvironmentImpl cheServiceEnv = new CheServicesEnvironmentImpl();
        CheServiceImpl service = new CheServiceImpl();
        cheServiceEnv.getServices().put(getMachineName(environment), service);

        service.setImage(recipe.getLocation());

        return cheServiceEnv;
    }
}
