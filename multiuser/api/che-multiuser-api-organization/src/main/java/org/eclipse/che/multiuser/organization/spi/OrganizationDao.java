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

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.multiuser.organization.spi.impl.OrganizationImpl;

/**
 * Defines data access object for {@link OrganizationImpl}
 *
 * @author Sergii Leschenko
 */
public interface OrganizationDao {
  /**
   * Creates organization.
   *
   * @param organization organization to create
   * @throws NullPointerException when {@code organization} is null
   * @throws ConflictException when organization with such id/name already exists
   * @throws ServerException when any other error occurs during organization creation
   */
  void create(OrganizationImpl organization) throws ServerException, ConflictException;

  /**
   * Updates organization with new entity.
   *
   * @param update organization update
   * @throws NullPointerException when {@code update} is null
   * @throws NotFoundException when organization with id {@code organization.getId()} doesn't exist
   * @throws ConflictException when name updated with a value which is not unique
   * @throws ServerException when any other error occurs organization updating
   */
  void update(OrganizationImpl update) throws NotFoundException, ConflictException, ServerException;

  /**
   * Removes organization with given id
   *
   * @param organizationId organization id
   * @throws NullPointerException when {@code organizationId} is null
   * @throws ServerException when any other error occurs during organization removing
   */
  void remove(String organizationId) throws ServerException;

  /**
   * Gets organization by identifier.
   *
   * @param organizationId organization id
   * @return organization instance
   * @throws NullPointerException when {@code organizationId} is null
   * @throws NotFoundException when organization with given id was not found
   * @throws ServerException when any other error occurs during organization fetching
   */
  OrganizationImpl getById(String organizationId) throws NotFoundException, ServerException;

  /**
   * Gets organization by name.
   *
   * @param organizationName organization name
   * @return organization instance
   * @throws NullPointerException when {@code organizationName} is null
   * @throws NotFoundException when organization with given name was not found
   * @throws ServerException when any other error occurs during organization fetching
   */
  OrganizationImpl getByName(String organizationName) throws NotFoundException, ServerException;

  /**
   * Gets child organizations by given parent.
   *
   * @param parent id of parent organization
   * @param maxItems the maximum number of organizations to return
   * @param skipCount the number of organizations to skip
   * @return list of children organizations
   * @throws NullPointerException when {@code parent} is null
   * @throws ServerException when any other error occurs during organizations fetching
   */
  Page<OrganizationImpl> getByParent(String parent, int maxItems, long skipCount)
      throws ServerException;

  /**
   * Gets all child organizations by specified parent qualified name.
   *
   * <p>Note that the result will includes all direct and nested suborganizations.
   *
   * @param parentQualifiedName qualified name of parent organization
   * @param maxItems the maximum number of organizations to return
   * @param skipCount the number of organizations to skip
   * @return list of children organizations
   * @throws NullPointerException when {@code parentQualifiedName} is null
   * @throws ServerException when any other error occurs during organizations fetching
   */
  Page<OrganizationImpl> getSuborganizations(
      String parentQualifiedName, int maxItems, long skipCount) throws ServerException;
}
