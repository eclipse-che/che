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
import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;
import org.eclipse.che.commons.annotation.Nullable;

import java.util.List;

/**
 * Defines data access object for {@link StackImpl}
 *
 * @author Alexander Andrienko
 */
public interface StackDao {

    /**
     * Create new Stack.
     *
     * @param stack
     *         stack to create
     * @throws NullPointerException
     *         when {@code stack} is null
     * @throws ConflictException
     *         when stack with id equal to {@code stack.getId()} is already exist
     * @throws ServerException
     *         when any error occurs
     */
    void create(StackImpl stack) throws ConflictException, ServerException;

    /**
     * Return existing stack by specified {@code id} or throws {@link NotFoundException}
     * when stack with such identifier doesn't exist.
     *
     * @param id
     *         the stack id
     * @throws NullPointerException
     *         when {@code id} is null
     * @throws NotFoundException
     *         if stack with {@code id} was not found
     * @throws ServerException
     *         when any error occurs
     */
    StackImpl getById(String id) throws NotFoundException, ServerException;

    /**
     * Remove the stack by specified {@code id}.
     *
     * @param id
     *         stack identifier to remove stack
     * @throws NullPointerException
     *         when {@code id} is null
     * @throws ServerException
     *         when any error occurs
     */
    void remove(String id) throws ServerException;

    /**
     * Update stack with new entity, actually replaces(not merges) existed stack.
     *
     * <p>Existed stack will be fully updated(replaced), all data which wos present before update will not be accessible
     * with {@code update} anymore</p> Expected update usage:
     * <pre>
     *     StackImpl stack = stackDao.getById("stack111");
     *     ...
     *     stack.setDescription("Java stack);
     *     ...
     *     stackDao.update(stack);
     * </pre>
     *
     * @param update
     *         the stack for update
     * @throws NullPointerException
     *         when {@code update} is null
     * @throws NotFoundException
     *         when stack with {@code update.getId()} doesn't exist
     * @throws ServerException
     *         when any error occurs
     */
    StackImpl update(StackImpl update) throws NotFoundException, ServerException;

    /**
     * Searches for stacks which which have read permissions for specified user and contains all of specified {@code tags}.
     * Not specified {@code tags} will not take part of search
     * <b>Note: only stack which contains permission <i>public: search<i/> take part of the search</b>
     *
     * @param user
     *         user id for permission checking
     * @param tags
     *         stack tags to search stacks, may be {@code null}
     * @param skipCount
     *         count of items which should be skipped,
     *         if found items contain fewer than {@code skipCount} items
     *         then return empty list items
     * @param maxItems
     *         max count of items to fetch
     * @return list stacks which contains all of specified {@code tags}
     * @throws ServerException
     *         when any error occurs
     * @throws IllegalArgumentException
     *         when {@code skipCount} or {@code maxItems} is negative
     */
    List<StackImpl> searchStacks(String user, @Nullable List<String> tags, int skipCount, int maxItems) throws ServerException;
}
