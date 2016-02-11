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
package org.eclipse.che.api.workspace.server.spi;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.workspace.server.model.impl.UsersWorkspaceImpl;

import java.util.List;

/**
 * Defines data access object contract for {@link UsersWorkspaceImpl}.
 *
 * @author Eugene Voevodin
 */
public interface WorkspaceDao {

    /**
     * Creates workspace.
     *
     * @param workspace
     *         workspace to create
     * @return created workspace
     * @throws NullPointerException
     *         when {@code workspace} is null
     * @throws ConflictException
     *         when workspace with given {@link UsersWorkspaceImpl#getName() name}
     *         already exists for given {@link UsersWorkspaceImpl#getOwner() owner}
     * @throws ServerException
     *         when any other error occurs during workspace creation
     */
    UsersWorkspaceImpl create(UsersWorkspaceImpl workspace) throws ConflictException, ServerException;

    /**
     * Updates workspace with new entity, actually replaces(not merges) existing workspaces.
     *
     * <p>Workspace will be fully updated(replaced), all data which was present before update will not be
     * accessible with {@link WorkspaceDao this} class anymore. Expected update usage:
     * <pre>
     *     UsersWorkspaceImpl workspace = workspaceDao.get("workspace123");
     *     ...
     *     workspace.setName("new-workspace-name");
     *     ...
     *     workspaceDao.update(workspace);
     * </pre>
     *
     * @param update
     *         workspace update
     * @return updated workspace
     * @throws NullPointerException
     *         when {@code update} is null
     * @throws NotFoundException
     *         when workspace with given {@link UsersWorkspaceImpl#getId() identifier} was not found
     * @throws ConflictException
     *         when workspace with given {@link UsersWorkspaceImpl#getName() name}
     *         already exists for given {@link UsersWorkspaceImpl#getOwner() owner}
     * @throws ServerException
     *         when any other error occurs during workspace updating
     */
    UsersWorkspaceImpl update(UsersWorkspaceImpl update) throws NotFoundException, ConflictException, ServerException;

    /**
     * Removes workspace.
     *
     * <p>It is up to implementation to do cascade removing of related to workspace data or to
     * forbid remove operation at all
     *
     * <p>Doesn't throw an exception when workspace with given {@code id} does not exist
     *
     * @param id
     *         workspace identifier
     * @throws NullPointerException
     *         when {@code id} is null
     * @throws ConflictException
     *         when any conflict occurs during cascade removing of related to workspace data
     * @throws ServerException
     *         when any other error occurs during workspace removing
     */
    void remove(String id) throws ConflictException, ServerException;

    /**
     * Gets workspace by identifier.
     *
     * @param id
     *         workspace identifier
     * @return workspace instance, never null
     * @throws NullPointerException
     *         when {@code id} is null
     * @throws NotFoundException
     *         when workspace with given {@code id} was not found
     * @throws ServerException
     *         when any other error occurs during workspace fetching
     */
    UsersWorkspaceImpl get(String id) throws NotFoundException, ServerException;

    /**
     * Gets workspace by name and owner.
     *
     * @param owner
     *         owner of workspace
     * @param name
     *         workspace identifier
     * @return workspace instance, never null
     * @throws NullPointerException
     *         when {@code name} or {@code owner} is null
     * @throws NotFoundException
     *         when workspace with given name & owner was not found
     * @throws ServerException
     *         when any other error occurs during workspace fetching
     */
    UsersWorkspaceImpl get(String name, String owner) throws NotFoundException, ServerException;

    /**
     * Gets list of workspaces owned by given {@code owner}.
     *
     * @param owner
     *         workspace owner
     * @return list of workspaces owned by given user.
     * Always returns list(even when user is not owner of any workspace), never null
     * @throws NullPointerException
     *         when {@code owner} is null
     * @throws ServerException
     *         when any other error occurs during workspaces fetching
     */
    List<UsersWorkspaceImpl> getByOwner(String owner) throws ServerException;
}
