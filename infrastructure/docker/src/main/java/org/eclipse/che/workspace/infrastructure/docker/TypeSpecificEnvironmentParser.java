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
package org.eclipse.che.workspace.infrastructure.docker;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.workspace.server.spi.ValidationException;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;

/**
 * Parser for creating {@link DockerEnvironment} with parameters
 * defined in the {@link Environment}.
 *
 * @author Alexander Andrienko
 */
public interface TypeSpecificEnvironmentParser {
    /**
     * Returns {@link DockerEnvironment} with parameters parsed
     * from {@link Environment}.
     *
     * @param environment
     *         environment to parsing
     * @throws ValidationException
     *         in case invalid argument in the {@link Environment}
     * @throws ServerException
     *         when parsing fails due to some internal server error or
     *         inability to parse environment due to other reasons
     */
    DockerEnvironment parse(Environment environment) throws ValidationException,
                                                            ServerException;
}
