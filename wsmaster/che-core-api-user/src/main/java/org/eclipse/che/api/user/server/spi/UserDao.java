/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.user.server.spi;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.model.impl.UserImpl;

/**
 * Defines data access object contract for {@link UserImpl}.
 *
 * <p>The implementation is not required to be responsible for persistent layer data dto integrity.
 * It simply transfers data from one layer to another, so if you're going to call any of implemented
 * methods it is considered that all needed verifications are already done.
 *
 * <p><strong>Note:</strong> This particularly does not mean that method call will not make any
 * inconsistency, but this mean that such kind of inconsistencies are expected by design and may be
 * treated further.
 *
 * @author Yevhenii Voevodin
 * @author Anton Korneta
 */
public interface UserDao {

  /**
   * Gets user by email or name and password
   *
   * @param emailOrName one of user attribute such as email/name(but not id)
   * @param password password
   * @return user identifier
   * @throws NullPointerException when either {@code emailOrName} or {@code password} is null
   * @throws NotFoundException when user with such {@code emailOrName} and {@code password} doesn't
   *     exist
   * @throws ServerException when any other error occurs
   */
  UserImpl getByAliasAndPassword(String emailOrName, String password)
      throws NotFoundException, ServerException;

  /**
   * Creates a new user.
   *
   * @param user user to create
   * @throws NullPointerException when {@code user} is null
   * @throws ConflictException when user with such id/alias/email/name already exists
   * @throws ServerException when any other error occurs
   */
  void create(UserImpl user) throws ConflictException, ServerException;

  /**
   * Updates user by replacing an existing entity with a new one.
   *
   * @param user user to update
   * @throws NullPointerException when {@code user} is null
   * @throws NotFoundException when user with id {@code user.getId()} doesn't exist
   * @throws ConflictException when any of the id/alias/email/name updated with a value which is not
   *     unique
   * @throws ServerException when any other error occurs
   */
  void update(UserImpl user) throws NotFoundException, ServerException, ConflictException;

  /**
   * Removes user.
   *
   * <p>It is up to implementation to do cascade removing of dependent data or to forbid removing at
   * all.
   *
   * <p>Note that this method doesn't throw any exception when user doesn't exist.
   *
   * @param id user identifier
   * @throws NullPointerException when {@code id} is null
   * @throws ServerException when any other error occurs
   */
  void remove(String id) throws ServerException;

  /**
   * Finds user by his alias.
   *
   * <p>This method doesn't work for user's email or name. If it is necessary to get user by name
   * use {@link #getByName(String)} method instead.
   *
   * @param alias user name or alias
   * @return user instance, never null
   * @throws NullPointerException when {@code alias} is null
   * @throws NotFoundException when user with given {@code alias} doesn't exist
   * @throws ServerException when any other error occurs
   */
  UserImpl getByAlias(String alias) throws NotFoundException, ServerException;

  /**
   * Finds user by his identifier.
   *
   * @param id user identifier
   * @return user instance, never null
   * @throws NullPointerException when {@code id} is null
   * @throws NotFoundException when user with given {@code id} doesn't exist
   * @throws ServerException when any other error occurs
   */
  UserImpl getById(String id) throws NotFoundException, ServerException;

  /**
   * Finds user by his name.
   *
   * @param name user name
   * @return user instance, never null
   * @throws NullPointerException when {@code name} is null
   * @throws NotFoundException when user with such {@code name} doesn't exist
   * @throws ServerException when any other error occurs
   */
  UserImpl getByName(String name) throws NotFoundException, ServerException;

  /**
   * Finds user by his email.
   *
   * @param email user email
   * @return user instance, never null
   * @throws NullPointerException when {@code email} is null
   * @throws NotFoundException when user with such {@code email} doesn't exist
   * @throws ServerException when any other error occurs
   */
  UserImpl getByEmail(String email) throws NotFoundException, ServerException;

  /**
   * Gets all users from persistent layer.
   *
   * @param maxItems the maximum number of users to return
   * @param skipCount the number of users to skip
   * @return list of users POJO or empty list if no users were found
   * @throws IllegalArgumentException when {@code maxItems} or {@code skipCount} is negative
   * @throws ServerException when any other error occurs
   */
  Page<UserImpl> getAll(int maxItems, long skipCount) throws ServerException;

  /**
   * Returns all users whose name contains(case insensitively) specified {@code namePart}.
   *
   * @param namePart fragment of user's name
   * @param maxItems the maximum number of users to return
   * @param skipCount the number of users to skip
   * @return list of matched users
   * @throws NullPointerException when {@code namePart} is null
   * @throws IllegalArgumentException when {@code maxItems} or {@code skipCount} is negative or when
   *     {@code skipCount} more than {@value Integer#MAX_VALUE}
   * @throws ServerException when any other error occurs
   */
  Page<UserImpl> getByNamePart(String namePart, int maxItems, long skipCount)
      throws ServerException;

  /**
   * Returns all users whose email address contains(case insensitively) specified {@code emailPart}.
   *
   * <p>For example if email fragment would be 'CHE' then result of search will include the
   * following:
   *
   * <pre>
   *  |        emails          |  result  |
   *  | Cherkassy@example.com  |    +     |
   *  | preacher@example.com   |    +     |
   *  | user@ukr.che           |    +     |
   *  | johny@example.com      |    -     |
   *  | CoachEddie@example.com |    +     |
   * </pre>
   *
   * @param emailPart fragment of user's email
   * @param maxItems the maximum number of users to return
   * @param skipCount the number of users to skip
   * @return list of matched users
   * @throws NullPointerException when {@code emailPart} is null
   * @throws IllegalArgumentException when {@code maxItems} or {@code skipCount} is negative or when
   *     {@code skipCount} more than {@value Integer#MAX_VALUE}
   * @throws ServerException when any other error occurs
   */
  Page<UserImpl> getByEmailPart(String emailPart, int maxItems, long skipCount)
      throws ServerException;

  /**
   * Get count of all users from persistent layer.
   *
   * @return user count
   * @throws ServerException when any error occurs
   */
  long getTotalCount() throws ServerException;
}
