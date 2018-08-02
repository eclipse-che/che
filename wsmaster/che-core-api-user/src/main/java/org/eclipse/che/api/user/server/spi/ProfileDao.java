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
package org.eclipse.che.api.user.server.spi;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.Profile;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.user.server.model.impl.ProfileImpl;

/**
 * Data access object contract for {@link Profile}.
 *
 * @author Yevhenii Voevodin
 */
public interface ProfileDao {

  /**
   * Creates user profile.
   *
   * @param profile new profile
   * @throws NullPointerException when {@code profile} is null
   * @throws ServerException when any error occurs
   * @throws ConflictException when profile for user {@code profile.getUserId()} already exists
   */
  void create(ProfileImpl profile) throws ServerException, ConflictException;

  /**
   * Updates profile by replacing an existing entity with a new one.
   *
   * @param profile profile update
   * @throws NullPointerException when {@code profile} is null
   * @throws NotFoundException when profile with such id doesn't exist
   * @throws ServerException when any other error occurs
   */
  void update(ProfileImpl profile) throws NotFoundException, ServerException;

  /**
   * Removes profile.
   *
   * @param id profile identifier
   * @throws NullPointerException when {@code id} is null
   * @throws ServerException when any other error occurs
   */
  void remove(String id) throws ServerException;

  /**
   * Finds profile by its id.
   *
   * <p>Due to {@link Profile#getEmail()} and {@link Profile#getUserId()} definition returned
   * profile must contain profile owner's {@link User#getEmail() email} and {@link User#getId()}
   * identifier.
   *
   * @param id profile identifier
   * @return profile with given {@code id}
   * @throws NullPointerException when {@code id} is null
   * @throws NotFoundException when profile with such {@code id} doesn't exist
   * @throws ServerException when any other error occurs
   */
  ProfileImpl getById(String id) throws NotFoundException, ServerException;
}
