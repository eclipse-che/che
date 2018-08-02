/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
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

import java.util.List;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * Defines data access object for {@link StackImpl}
 *
 * @author Alexander Andrienko
 */
public interface StackDao {

  /**
   * Create new Stack.
   *
   * @param stack stack to create
   * @throws NullPointerException when {@code stack} is null
   * @throws ConflictException when stack with id equal to {@code stack.getId()} is already exist
   * @throws ServerException when any error occurs
   */
  void create(StackImpl stack) throws ConflictException, ServerException;

  /**
   * Return existing stack by specified {@code id} or throws {@link NotFoundException} when stack
   * with such identifier doesn't exist.
   *
   * @param id the stack id
   * @throws NullPointerException when {@code id} is null
   * @throws NotFoundException if stack with {@code id} was not found
   * @throws ServerException when any error occurs
   */
  StackImpl getById(String id) throws NotFoundException, ServerException;

  /**
   * Remove the stack by specified {@code id}.
   *
   * @param id stack identifier to remove stack
   * @throws NullPointerException when {@code id} is null
   * @throws ServerException when any error occurs
   */
  void remove(String id) throws ServerException;

  /**
   * Update stack with new entity, actually replaces(not merges) existed stack.
   *
   * <p>Existed stack will be fully updated(replaced), all data which wos present before update will
   * not be accessible with {@code update} anymore Expected update usage:
   *
   * <pre>
   *     StackImpl stack = stackDao.getById("stack111");
   *     ...
   *     stack.setDescription("Java stack);
   *     ...
   *     stackDao.update(stack);
   * </pre>
   *
   * @param update the stack for update
   * @throws NullPointerException when {@code update} is null
   * @throws NotFoundException when stack with {@code update.getId()} doesn't exist
   * @throws ConflictException when stack with such name already exists
   * @throws ServerException when any error occurs
   */
  StackImpl update(StackImpl update) throws NotFoundException, ConflictException, ServerException;

  /**
   * Returns those stacks which match the following statements:
   *
   * <ul>
   *   <li>If neither {@code user} no {@code tags} are specified(null values passed to the method)
   *       then all the stacks which contain 'search' action in {@link StackImpl#getPublicActions()
   *       public actions} are returned
   *   <li>If {@code user} is specified then all the stacks which contain 'search' action in stack
   *       public actions(like defined by previous list item) or those which specify 'search' action
   *       in access control entry for given {@code user} are returned
   *   <li>Finally, if {@code tags} are specified then the stacks which match 2 rules above, will be
   *       filtered by the {@code tags}, stack should contain all of the {@code tags} to be in a
   *       result list.
   * </ul>
   *
   * @param user user id for permission checking
   * @param tags stack tags to search stacks, may be {@code null}
   * @param skipCount count of items which should be skipped, if found items contain fewer than
   *     {@code skipCount} items then return empty list items
   * @param maxItems max count of items to fetch
   * @return list stacks which contains all of specified {@code tags}
   * @throws ServerException when any error occurs
   * @throws IllegalArgumentException when {@code skipCount} or {@code maxItems} is negative
   */
  List<StackImpl> searchStacks(
      @Nullable String user, @Nullable List<String> tags, int skipCount, int maxItems)
      throws ServerException;
}
