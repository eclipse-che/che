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
package org.eclipse.che.api.devfile.server.spi;

import com.google.common.annotations.Beta;
import java.util.List;
import java.util.Optional;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.devfile.UserDevfile;
import org.eclipse.che.commons.lang.Pair;

/** Defines data access object contract for {@code UserDevfileImpl}. */
@Beta
public interface UserDevfileDao {

  /**
   * Creates Devfile.
   *
   * @param devfile devfile to create
   * @return created devfile
   * @throws NullPointerException when {@code devfile} is null
   * @throws ServerException when any other error occurs
   * @throws ConflictException when required namespace is not found.
   */
  UserDevfile create(UserDevfile devfile) throws ServerException, ConflictException;

  /**
   * Updates devfile to the new entity, using replacement strategy.
   *
   * @param devfile devfile to update
   * @return updated devfile
   * @throws NullPointerException when {@code devfile} is null
   * @throws ConflictException when any conflict situation occurs
   * @throws ServerException when any other error occurs
   */
  Optional<UserDevfile> update(UserDevfile devfile)
      throws ConflictException, ServerException, NotFoundException;

  /**
   * Removes devfile.
   *
   * @param id devfile identifier
   * @throws NullPointerException when {@code id} is null
   * @throws ServerException when any other error occurs
   */
  void remove(String id) throws ServerException;

  /**
   * Gets devfile by identifier.
   *
   * @param id devfile identifier
   * @return devfile instance, never null
   * @throws NullPointerException when {@code id} is null
   * @throws ServerException when any other error occurs
   */
  Optional<UserDevfile> getById(String id) throws ServerException;

  /**
   * Gets list of UserDevfiles in given namespace.
   *
   * @param namespace devfiles namespace
   * @return list of devfiles in given namespace. Always returns list(even when there are no devfile
   *     in given namespace), never null
   * @throws NullPointerException when {@code namespace} is null
   * @throws ServerException when any other error occurs during workspaces fetching
   */
  Page<UserDevfile> getByNamespace(String namespace, int maxItems, long skipCount)
      throws ServerException;

  /**
   * Gets all devfiles which user can read filtered by given parameters in a given order
   *
   * @param maxItems the maximum number of workspaces to return
   * @param skipCount the number of workspaces to skip
   * @param filter additional conditions for the desired devfiles. Conditions represented as pairs
   *     of the filed and the value. All pairs would be joined with <b>AND</b> condition. Value of
   *     the pair can start with 'like:' prefix. In this case would be used <i>LIKE</i> query,
   *     otherwise <b>=</b> condition.
   * @param order - a list of fields and directions of sort. By default items would be sorted by id.
   * @return list of devfiles which user can read, never null
   * @throws ServerException when any other error occurs during devfile fetching
   * @throws IllegalArgumentException when maxItems < 1 or skipCount < 0 or sort order is not 'asc'
   *     or 'desc'.
   */
  Page<UserDevfile> getDevfiles(
      int maxItems,
      int skipCount,
      List<Pair<String, String>> filter,
      List<Pair<String, String>> order)
      throws ServerException;

  /**
   * Get the count of all user devfiles from the persistent layer.
   *
   * @return workspace count
   * @throws ServerException when any error occurs
   */
  long getTotalCount() throws ServerException;
}
