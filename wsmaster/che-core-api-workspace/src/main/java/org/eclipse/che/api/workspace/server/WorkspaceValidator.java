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
package org.eclipse.che.api.workspace.server;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;

import java.util.Map;

/**
 * Interface for workspace validations.
 *
 * <p>This interface doesn't declare any validation constrains
 * because the validation itself is implementation specific.
 *
 * @author Yevhenii Voevodin
 */
public interface WorkspaceValidator {

    /**
     * Checks that workspace is valid.
     *
     * @param workspace
     *         workspace configuration for validation
     * @throws BadRequestException
     *         in the case of constrain violation
     * @throws ServerException
     *         when constraint can not be validated because of network or other errors
     */
    void validateWorkspace(Workspace workspace) throws BadRequestException,
                                                       ServerException;

    /**
     * Checks that workspace configuration is valid.
     *
     * @param config
     *         workspace configuration for validation
     * @throws BadRequestException
     *         in the case of constrain violation
     * @throws ServerException
     *         when constraint can not be validated because of network or other errors
     */
    void validateConfig(WorkspaceConfig config) throws BadRequestException,
                                                       ServerException;

    /**
     * Checks that workspace instance attributes are valid.
     *
     * @param attributes
     *         workspace instance attributes
     * @throws BadRequestException
     *         in the case of constrain violation
     */
    void validateAttributes(Map<String, String> attributes) throws BadRequestException;
}
