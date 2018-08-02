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
package org.eclipse.che.selenium.core.client;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.User;

/**
 * @author Mihail Kuznyetsov
 * @author Anton Korneta
 */
public interface TestUserServiceClient {

  /**
   * Creates user form provided data.
   *
   * @param name user name
   * @param email user email
   * @param password user password
   * @throws BadRequestException when user data validation failed
   * @throws ConflictException when user with given email/name exists
   * @throws ServerException when any other exception occurs
   */
  void create(String name, String email, String password)
      throws BadRequestException, ConflictException, ServerException;

  /**
   * Gets user by id.
   *
   * @param id user identifier
   * @return user with given identifier
   * @throws NotFoundException when user with given id not found
   * @throws ServerException when any other exception occurs
   */
  User getById(String id) throws NotFoundException, ServerException;

  /**
   * Gets user by email.
   *
   * @param email user email
   * @return user with given email
   * @throws BadRequestException when specified email is null or empty
   * @throws NotFoundException User with requested email not found
   * @throws ServerException when any other exception occurs
   */
  User findByEmail(String email) throws NotFoundException, ServerException, BadRequestException;

  /**
   * Gets user by name.
   *
   * @param name user name
   * @return user with given name
   * @throws BadRequestException when specified name is null or empty
   * @throws NotFoundException User with requested name not found
   * @throws ServerException when any other exception occurs
   */
  User findByName(String name) throws NotFoundException, ServerException, BadRequestException;

  /**
   * Deletes user by its id.
   *
   * @param id user identifier
   * @throws ConflictException when conflicts occurs e.g. user has related entities
   * @throws ServerException when any other exception occurs
   */
  void remove(String id) throws ServerException, ConflictException;
}
