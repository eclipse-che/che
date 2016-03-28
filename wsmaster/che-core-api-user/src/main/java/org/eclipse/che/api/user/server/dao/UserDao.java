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
package org.eclipse.che.api.user.server.dao;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;

/**
 * DAO interface offers means to perform CRUD operations with {@link User} data. The implementation is not
 * required to be responsible for persistent layer data dto integrity. It simply transfers data from one layer to another, so if
 * you're going to call any of implemented methods it is considered that all needed verifications are already done. <p>
 * <strong>Note:</strong> This particularly does not mean that method call will not make any inconsistency, but this
 * mean that such kind of inconsistencies are expected by design and may be treated further. </p>
 */
public interface UserDao {

    /**
     * Authenticate user.
     *
     * @param alias
     *         user name or alias
     * @param password
     *         password
     * @return user id when authentication success
     * @throws UnauthorizedException
     *         when authentication failed or no such user exists
     * @throws ServerException
     *         when any other error occurs
     *
     */
    String authenticate(String alias, String password) throws UnauthorizedException, ServerException;

    /**
     * Adds user to persistent layer.
     *
     * @param user
     *         - POJO representation of user entity
     * @throws ConflictException
     *         when given user cannot be created
     * @throws ServerException
     *         when any other error occurs
     */
    void create(User user) throws ConflictException, ServerException;

    /**
     * Updates already present in persistent layer user.
     *
     * @param user
     *         POJO representation of user entity
     * @throws NotFoundException
     *         when user is not found
     * @throws ConflictException
     *         when given user cannot be updated
     * @throws ServerException
     *         when any other error occurs
     *
     */
    void update(User user) throws NotFoundException, ServerException, ConflictException;

    /**
     * Removes user from persistent layer by his identifier.
     *
     * @param id
     *         user identifier
     * @throws ConflictException
     *         when given user cannot be deleted
     * @throws ServerException
     *         when any other error occurs
     */
    void remove(String id) throws NotFoundException, ServerException, ConflictException;

    /**
     * Gets user from persistent layer by any of his aliases
     *
     * @param alias
     *         user name or alias
     * @return user POJO
     * @throws NotFoundException
     *         when user doesn't exist
     * @throws ServerException
     *         when any other error occurs
     */
    User getByAlias(String alias) throws NotFoundException, ServerException;

    /**
     * Gets user from persistent layer by his identifier
     *
     * @param id
     *         user identifier
     * @return user POJO
     * @throws NotFoundException
     *         when user doesn't exist
     * @throws ServerException
     *         when any other error occurs
     */
    User getById(String id) throws NotFoundException, ServerException;

    /**
     * Gets user from persistent layer by his username
     *
     * @param userName
     *         user name
     * @return user POJO
     * @throws NotFoundException
     *         when user doesn't exist
     * @throws ServerException
     *         when any other error occurs
     */
    User getByName(String userName) throws NotFoundException, ServerException;
}
