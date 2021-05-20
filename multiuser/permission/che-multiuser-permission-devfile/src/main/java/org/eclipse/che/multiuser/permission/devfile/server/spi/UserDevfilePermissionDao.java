/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.permission.devfile.server.spi;

import java.util.List;
import java.util.Optional;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.multiuser.permission.devfile.server.model.impl.UserDevfilePermissionImpl;

/** Defines data access object contract for {@link UserDevfilePermissionImpl}. */
public interface UserDevfilePermissionDao {

  /**
   * Stores (adds or updates) UserDevfilePermissions.
   *
   * @param userDevfilePermissions userDevfilePermissions to store
   * @return optional with updated userDevfilePermissions, other way empty optional must be returned
   * @throws NullPointerException when {@code userDevfilePermissions} is null
   * @throws ServerException when any other error occurs during userDevfilePermissions storing
   */
  Optional<UserDevfilePermissionImpl> store(UserDevfilePermissionImpl userDevfilePermissions)
      throws ServerException;

  /**
   * Gets userDevfilePermissions by user and userDevfileId
   *
   * @param userDevfileId user devfile identifier
   * @param userId user identifier
   * @return userDevfilePermissions instance, never null
   * @throws NullPointerException when {@code userDevfileId} or {@code userId} is null
   * @throws NotFoundException when permission with given {@code userDevfileId} and {@code userId}
   *     was not found
   * @throws ServerException when any other error occurs during permission fetching
   */
  UserDevfilePermissionImpl getUserDevfilePermission(String userDevfileId, String userId)
      throws ServerException, NotFoundException;

  /**
   * Removes userDevfilePermissions
   *
   * <p>Doesn't throw an exception when userDevfilePermissions with given {@code userDevfileId} and
   * {@code userId} does not exist
   *
   * @param userDevfileId workspace identifier
   * @param userId user identifier
   * @throws NullPointerException when {@code userDevfileId} or {@code userId} is null
   * @throws ServerException when any other error occurs during userDevfilePermissions removing
   */
  void removeUserDevfilePermission(String userDevfileId, String userId) throws ServerException;

  /**
   * Gets userDevfilePermissions by user devfile id.
   *
   * @param userDevfileId user devfile identifier
   * @param maxItems the maximum number of userDevfilePermissions to return
   * @param skipCount the number of userDevfilePermissions to skip
   * @return list of userDevfilePermissions instance
   * @throws NullPointerException when {@code userDevfileId} is null
   * @throws ServerException when any other error occurs during userDevfilePermissions fetching
   */
  Page<UserDevfilePermissionImpl> getUserDevfilePermission(
      String userDevfileId, int maxItems, long skipCount) throws ServerException;

  /**
   * Gets UserDevfilePermissions by user
   *
   * @param userId user identifier
   * @return list of UserDevfilePermissions instance
   * @throws NullPointerException when {@code userId} is null
   * @throws ServerException when any other error occurs during UserDevfilePermissions fetching
   */
  List<UserDevfilePermissionImpl> getUserDevfilePermissionByUser(String userId)
      throws ServerException;
}
