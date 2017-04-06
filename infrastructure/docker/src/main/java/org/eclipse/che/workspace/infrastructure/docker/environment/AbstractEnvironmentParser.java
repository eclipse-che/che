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
package org.eclipse.che.workspace.infrastructure.docker.environment;

import com.google.common.base.Joiner;

import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.config.MachineConfig;
import org.eclipse.che.api.environment.server.model.CheServicesEnvironmentImpl;
import org.eclipse.che.workspace.infrastructure.docker.TypeSpecificEnvironmentParser;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Parser for creating {@link CheServicesEnvironmentImpl} from {@link Environment}
 * which contains a single {@link MachineConfig}.
 *
 * @author Alexander Garagatyi
 * @author Alexander Andrienko
 */
public abstract class AbstractEnvironmentParser implements TypeSpecificEnvironmentParser {

    /**
     * Returns machine name which is mapped to {@link MachineConfig}
     * in the {@code environment}.
     * Note: Valid {@link Environment} based on docker file or docker
     * image recipe type contains only one {@link MachineConfig}.
     *
     * @param environment
     *         environment to get machine name
     * @throws IllegalArgumentException
     *         in case if {@code environment} is multiple machine environment
     */
    protected String getMachineName(Environment environment) throws IllegalArgumentException {
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
