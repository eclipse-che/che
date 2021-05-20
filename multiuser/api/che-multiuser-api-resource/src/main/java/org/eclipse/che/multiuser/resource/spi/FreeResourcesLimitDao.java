/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.resource.spi;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.multiuser.resource.spi.impl.FreeResourcesLimitImpl;

/**
 * Defines data access object contract for {@link FreeResourcesLimitImpl}.
 *
 * @author Sergii Leschenko
 */
public interface FreeResourcesLimitDao {
  /**
   * Stores (creates new one or updates existed) free resource limit.
   *
   * @param resourcesLimit resources limit to store
   * @throws NullPointerException when {@code resourcesLimit} is null
   * @throws ConflictException when the specified account doesn't exist
   * @throws ServerException when any other error occurs
   */
  void store(FreeResourcesLimitImpl resourcesLimit) throws ConflictException, ServerException;

  /**
   * Returns free resources limit for account with specified id.
   *
   * @param accountId account id to fetch resources limit
   * @return free resources limit for account with specified id
   * @throws NullPointerException when {@code accountId} is null
   * @throws NotFoundException when free resources limit for specifies id was not found
   * @throws ServerException when any other error occurs
   */
  FreeResourcesLimitImpl get(String accountId) throws NotFoundException, ServerException;

  /**
   * Gets all free resources limits.
   *
   * @param maxItems the maximum number of limits to return
   * @param skipCount the number of limits to skip
   * @return list of limits POJO or empty list if no limits were found
   * @throws ServerException when any other error occurs
   */
  Page<FreeResourcesLimitImpl> getAll(int maxItems, int skipCount) throws ServerException;

  /**
   * Removes free resources limit for account with specified id.
   *
   * <p>Doesn't throw an exception when resources limit for specified {@code accountId} does not
   * exist
   *
   * @param accountId account id to remove resources limit
   * @throws NullPointerException when {@code accountId} is null
   * @throws ServerException when any other error occurs
   */
  void remove(String accountId) throws ServerException;
}
