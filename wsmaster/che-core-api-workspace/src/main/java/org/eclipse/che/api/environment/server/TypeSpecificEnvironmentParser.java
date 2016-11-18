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
package org.eclipse.che.api.environment.server;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.environment.server.model.CheServicesEnvironmentImpl;

/**
 * Parser for creating {@link CheServicesEnvironmentImpl} with parameters
 * defined in the {@link Environment}.
 *
 * @author Alexander Andrienko
 */
public interface TypeSpecificEnvironmentParser {
    /**
     * Returns {@link CheServicesEnvironmentImpl} with parameters parsed
     * from {@link Environment}.
     *
     * @param environment
     *         environment to parsing
     * @throws IllegalArgumentException
     *         in case invalid argument in the {@link Environment}
     * @throws ServerException
     *         when parsing fails due to some internal server error or
     *         inability to parse environment due to other reasons
     */
    CheServicesEnvironmentImpl parse(Environment environment) throws IllegalArgumentException,
                                                                     ServerException;
}
