/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.spi;

import java.util.Optional;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;

/**
 * Defines data access object contract for {@link WorkspaceImpl}.
 *
 * @author Eugene Voevodin
 */
public interface WorkspaceDao {

  /**
   * Creates workspace.
   *
   * @param workspace workspace to create
   * @return created workspace
   * @throws NullPointerException when {@code workspace} is null
   * @throws ConflictException when workspace with given name already exists for given namespace
   * @throws ServerException when any other error occurs during workspace creation
   */
  WorkspaceImpl create(WorkspaceImpl workspace) throws ConflictException, ServerException;

  /**
   * Updates workspace with new entity, actually replaces(not merges) existing workspaces.
   *
   * <p>Workspace will be fully updated(replaced), all data which was present before update will not
   * be accessible with {@link WorkspaceDao this} class anymore. Expected update usage:
   *
   * <pre>
   *     UsersWorkspaceImpl workspace = workspaceDao.get("workspace123");
   *     ...
   *     workspace.setName("new-workspace-name");
   *     ...
   *     workspaceDao.update(workspace);
   * </pre>
   *
   * @param update workspace update
   * @return updated workspace
   * @throws NullPointerException when {@code update} is null
   * @throws NotFoundException when workspace with given {@link WorkspaceImpl#getId() identifier}
   *     was not found
   * @throws ConflictException when workspace with given name already exists in given namespace
   * @throws ServerException when any other error occurs during workspace updating
   */
  WorkspaceImpl update(WorkspaceImpl update)
      throws NotFoundException, ConflictException, ServerException;

  /**
   * Removes workspace.
   *
   * <p>It is up to implementation to do cascade removing of related to workspace data or to forbid
   * remove operation at all
   *
   * <p>Doesn't throw an exception when workspace with given {@code id} does not exist
   *
   * @param id workspace identifier
   * @return removed workspace
   * @throws NullPointerException when {@code id} is null
   * @throws ServerException when any other error occurs during workspace removing
   */
  Optional<WorkspaceImpl> remove(String id) throws ServerException;

  /**
   * Gets workspace by identifier.
   *
   * @param id workspace identifier
   * @return workspace instance, never null
   * @throws NullPointerException when {@code id} is null
   * @throws NotFoundException when workspace with given {@code id} was not found
   * @throws ServerException when any other error occurs during workspace fetching
   */
  WorkspaceImpl get(String id) throws NotFoundException, ServerException;

  /**
   * Gets workspace by name in namespace.
   *
   * @param namespace namespace of workspace
   * @param name workspace identifier
   * @return workspace instance, never null
   * @throws NullPointerException when {@code name} or {@code owner} is null
   * @throws NotFoundException when workspace with given name & owner was not found
   * @throws ServerException when any other error occurs during workspace fetching
   */
  WorkspaceImpl get(String name, String namespace) throws NotFoundException, ServerException;

  /**
   * Gets list of workspaces in given namespace.
   *
   * @param namespace workspace namespace
   * @return list of workspaces in given namespace. Always returns list(even when there are no
   *     workspace in given namespace), never null
   * @throws NullPointerException when {@code owner} is null
   * @throws ServerException when any other error occurs during workspaces fetching
   */
  Page<WorkspaceImpl> getByNamespace(String namespace, int maxItems, long skipCount)
      throws ServerException;

  /**
   * Gets list of workspaces which user can read
   *
   * @param userId id of user
   * @return list of workspaces which user can read
   * @throws ServerException when any other error occurs during workspaces fetching
   */
  Page<WorkspaceImpl> getWorkspaces(String userId, int maxItems, long skipCount)
      throws ServerException;

  /**
   * Gets workspaces by temporary attribute.
   *
   * @param isTemporary When {@code true}, only temporary workspaces should be retrieved. When
   *     {@code false}, only non-temporary workspaces should be retrieved.
   * @param skipCount the number of workspaces to skip
   * @param maxItems the maximum number of workspaces to return
   * @return list of workspaces or empty list if no workspaces were found
   * @throws ServerException when any other error occurs during workspaces fetching
   * @throws IllegalArgumentException when {@code maxItems} or {@code skipCount} is negative
   */
  Page<WorkspaceImpl> getWorkspaces(boolean isTemporary, int maxItems, long skipCount)
      throws ServerException;

  /**
   * Get the count of all workspaces from the persistent layer.
   *
   * @return workspace count
   * @throws ServerException when any error occurs
   */
  long getWorkspacesTotalCount() throws ServerException;
}
