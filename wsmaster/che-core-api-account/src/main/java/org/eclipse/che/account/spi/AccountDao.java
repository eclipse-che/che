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
package org.eclipse.che.account.spi;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;

/**
 * Defines data access object for {@link AccountImpl}
 *
 * @author Sergii Leschenko
 */
public interface AccountDao {
  /**
   * Creates account.
   *
   * @param account account to create
   * @throws NullPointerException when {@code account} is null
   * @throws ConflictException when account with such name or id already exists
   * @throws ServerException when any other error occurs during account creating
   */
  void create(AccountImpl account) throws ConflictException, ServerException;

  /**
   * Updates account by replacing an existing entity with a new one.
   *
   * @param account account to update
   * @throws NullPointerException when {@code account} is null
   * @throws NotFoundException when account with id {@code account.getId()} doesn't exist
   * @throws ConflictException when name updated with a value which is not unique
   * @throws ServerException when any other error occurs
   */
  void update(AccountImpl account) throws NotFoundException, ConflictException, ServerException;

  /**
   * Gets account by identifier.
   *
   * @param id account identifier
   * @return account instance with given id
   * @throws NullPointerException when {@code id} is null
   * @throws NotFoundException when account with given {@code id} was not found
   * @throws ServerException when any other error occurs during account fetching
   */
  AccountImpl getById(String id) throws NotFoundException, ServerException;

  /**
   * Gets account by name.
   *
   * @param name account name
   * @return account instance with given name
   * @throws NullPointerException when {@code name} is null
   * @throws NotFoundException when account with given {@code name} was not found
   * @throws ServerException when any other error occurs during account fetching
   */
  AccountImpl getByName(String name) throws ServerException, NotFoundException;

  /**
   * Removes account by specified {@code id}
   *
   * @param id account identifier
   * @throws NullPointerException when {@code id} is null
   * @throws ServerException when any other error occurs
   */
  void remove(String id) throws ServerException;
}
