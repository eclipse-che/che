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
package org.eclipse.che.api.workspace.server;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;

/**
 * Interface for workspace config validations.
 *
 * <p>This interface doesn't declare any validation constrains
 * because the validation itself is implementation specific.
 *
 * @author Yevhenii Voevodin
 */
public interface WorkspaceConfigValidator {

    /**
     * Checks that workspace is valid.
     *
     * @param config
     *         workspace configuration for validation
     * @throws BadRequestException
     *         in the case of constrain violation
     */
    void validate(WorkspaceConfig config) throws BadRequestException;
}
