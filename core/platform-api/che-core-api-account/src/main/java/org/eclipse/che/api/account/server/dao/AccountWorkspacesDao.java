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
package org.eclipse.che.api.account.server.dao;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;

import java.util.List;
import java.util.Map;

/**
 * Account - workspaces (one to many) association
 *
 * @author gazarenkov
 */
public interface AccountWorkspacesDao {

    /**
     * Associates new workspace to account
     */
    void create(String accountId, String workspaceId) throws ConflictException, ServerException;

    /**
     * Gets all workspaces associated with account
     */
    List <String> getWorkspaces(String accountId) throws NotFoundException, ServerException;

    /**
     * Gets account  associated with workspace
     */
    String getAccount(String workspaceId) throws NotFoundException, ServerException;


    /**
     * Removes workspace-account association
     */
    void removeWorkspaceRef(String workspaceId) throws NotFoundException, ServerException, ConflictException;

    /**
     * Removes account
     */
    void removeAccount(String accountId) throws NotFoundException, ServerException, ConflictException;


}
