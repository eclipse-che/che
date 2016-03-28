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

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * Generic interface for methods called on particular workspace events if some additional actions needed.
 *
 * <p>The most common use-case - to register/unregister workspace in the account
 *
 * @author gazarenkov
 * @author Eugene Voevodin
 */
public interface WorkspaceHooks {

    /**
     * Called before workspace starting.
     *
     * @param workspace
     *         workspace which is going to be started
     * @param accountId
     *         account identifier indicates the account which should be used for runtime workspace
     * @param envName
     *         the name of environment which is going to be started
     * @throws NotFoundException
     *         when any not found error occurs
     * @throws ForbiddenException
     *         when user doesn't have access to start workspace in certain account
     * @throws ServerException
     *         when any other error occurs
     * @throws NullPointerException
     *         when either {@code workspace} or {@code envName} is null
     */
    void beforeStart(Workspace workspace, String envName, @Nullable String accountId) throws NotFoundException,
                                                                                             ForbiddenException,
                                                                                             ServerException;

    /**
     * Called before creating workspace.
     *
     * @param workspace
     *         workspace instance
     * @param accountId
     *         related to workspace account identifier, it is optional and may be null
     * @throws NotFoundException
     *         when any not found error occurs
     * @throws ServerException
     *         when any other error occurs
     */
    void beforeCreate(Workspace workspace, @Nullable String accountId) throws NotFoundException, ServerException;

    /**
     * Called after workspace is created.
     *
     * @param workspace
     *         workspace which was created
     * @param accountId
     *         related to workspace account identifier, it is optional and may be null
     * @throws ServerException
     *         when any other error occurs
     */
    void afterCreate(Workspace workspace, @Nullable String accountId) throws ServerException;

    /**
     * Called after workspace is removed.
     *
     * @param workspaceId
     *         identifier of workspace which was removed
     * @throws ServerException
     *         when any error occurs
     */
    void afterRemove(String workspaceId) throws ServerException;
}
