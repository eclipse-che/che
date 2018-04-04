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
package org.eclipse.che.multiuser.organization.spi;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.multiuser.organization.spi.impl.OrganizationDistributedResourcesImpl;

/**
 * Defines data access object contract for {@link OrganizationDistributedResourcesImpl}.
 *
 * @author Sergii Leschenko
 */
public interface OrganizationDistributedResourcesDao {
  /**
   * Stores (creates or updated) distributed resources for suborganization.
   *
   * @param distributedResources distributed resources to store
   * @throws NullPointerException when either {@code distributedResources} is null
   * @throws ServerException when any other error occurs
   */
  void store(OrganizationDistributedResourcesImpl distributedResources) throws ServerException;

  /**
   * Returns distributed resources for specified suborganization.
   *
   * @param organizationId organization id
   * @return distributed resources for specified suborganization
   * @throws NullPointerException when either {@code organizationId} is null
   * @throws NotFoundException when organization with specified id doesn't have distributed
   *     resources
   * @throws ServerException when any other error occurs
   */
  OrganizationDistributedResourcesImpl get(String organizationId)
      throws NotFoundException, ServerException;

  /**
   * Returns distributed resources for suborganizations of given parent organization.
   *
   * @param organizationId organization id
   * @return distributed resources for suborganizations of given parent organization
   * @throws NullPointerException when either {@code organizationId} is null
   * @throws ServerException when any other error occurs
   */
  Page<OrganizationDistributedResourcesImpl> getByParent(
      String organizationId, int maxItems, long skipCount) throws ServerException;

  /**
   * Remove distributed organization resources.
   *
   * @param organizationId organization id
   * @throws NullPointerException when either {@code organizationId} is null
   * @throws ServerException when any other error occurs
   */
  void remove(String organizationId) throws ServerException;
}
