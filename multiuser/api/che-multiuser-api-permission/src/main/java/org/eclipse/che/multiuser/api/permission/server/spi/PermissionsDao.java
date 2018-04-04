/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.api.permission.server.spi;

import java.util.List;
import java.util.Optional;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.multiuser.api.permission.server.AbstractPermissionsDomain;
import org.eclipse.che.multiuser.api.permission.server.model.impl.AbstractPermissions;

/**
 * General contract of storage for permissions. Single Storage may maintain one or more Domains (it
 * is responsibility of system on top to make the choice consistent) It actually defines CRUD
 * methods with some specific such as: - processing list of permissions - checking for existence but
 * not returning fully qualified stored permission
 *
 * @author gazarenkov
 * @author Sergii Leschenko
 */
public interface PermissionsDao<T extends AbstractPermissions> {

  /** @return store of domains this storage is able to maintain */
  AbstractPermissionsDomain<T> getDomain();

  /**
   * Stores (adds or updates) permissions.
   *
   * @param permissions permission to store
   * @return optional with updated permissions, other way empty optional must be returned
   * @throws ServerException when any other error occurs during permissions storing
   */
  Optional<T> store(T permissions) throws ServerException;

  /**
   * @param userId user id
   * @param instanceId instance id
   * @return user's permissions for specified instance
   * @throws NullPointerException when instance id is null and domain requires it
   * @throws NullPointerException when user id is null
   * @throws NotFoundException when permissions with given user and domain and instance was not
   *     found
   * @throws ServerException when any other error occurs during permissions fetching
   */
  T get(String userId, String instanceId) throws ServerException, NotFoundException;

  /**
   * @param instanceId instance id
   * @param maxItems the maximum number of permissions to return
   * @param skipCount the number of permissions to skip
   * @return set of permissions
   * @throws NotFoundException when given instance was not found
   * @throws ServerException when any other error occurs during permissions fetching
   */
  Page<T> getByInstance(String instanceId, int maxItems, long skipCount)
      throws ServerException, NotFoundException;

  /**
   * @param userId user id
   * @return set of permissions
   * @throws NotFoundException when given instance was not found
   * @throws ServerException when any other error occurs during permissions fetching
   */
  List<T> getByUser(String userId) throws ServerException, NotFoundException;

  /**
   * @param userId user id
   * @param instanceId instance id
   * @param action action name
   * @return true if the permission exists
   * @throws ServerException when any other error occurs during permission existence checking
   */
  boolean exists(String userId, String instanceId, String action) throws ServerException;

  /**
   * Removes permissions of user related to the particular instance of specified domain
   *
   * @param userId user id
   * @param instanceId instance id
   * @throws NotFoundException when permissions with given user and domain and instance was not
   *     found
   * @throws ServerException when any other error occurs during permissions removing
   */
  void remove(String userId, String instanceId) throws ServerException, NotFoundException;
}
