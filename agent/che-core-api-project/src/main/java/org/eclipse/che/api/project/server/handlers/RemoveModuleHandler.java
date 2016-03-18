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
package org.eclipse.che.api.project.server.handlers;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.project.ProjectConfig;
import org.eclipse.che.api.project.server.FolderEntry;

/**
 * A handler for handling the removal of a module.
 *
 * @author Roman Nikitenko
 */
public interface RemoveModuleHandler extends ProjectHandler {

    /**
     * Called when a module was removed.
     *
     * @param parentFolder
     *         parent folder
     * @param moduleConfig
     *         module configuration
     * @throws ServerException
     *         if an error occurs
     * @throws ConflictException
     *         if operation causes conflict
     * @throws ForbiddenException
     *         if user which perform operation doesn't have required permissions
     */
    void onRemoveModule(FolderEntry parentFolder, ProjectConfig moduleConfig) throws ForbiddenException,
                                                                                     ConflictException,
                                                                                     ServerException;
}
