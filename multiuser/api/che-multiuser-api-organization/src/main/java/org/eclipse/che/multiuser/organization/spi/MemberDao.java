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
package org.eclipse.che.multiuser.organization.spi;

import java.util.List;
import java.util.Optional;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.multiuser.organization.spi.impl.MemberImpl;
import org.eclipse.che.multiuser.organization.spi.impl.OrganizationImpl;

/**
 * Defines data access object for {@link MemberImpl}
 *
 * @author Sergii Leschenko
 */
public interface MemberDao {
  /**
   * Stores (adds or updates) member.
   *
   * @param member member to store
   * @return optional with updated member, other way empty optional must be returned
   * @throws NullPointerException when {@code member} is null
   * @throws ServerException when organization or user doesn't exist
   * @throws ServerException when any other error occurs during member storing
   */
  Optional<MemberImpl> store(MemberImpl member) throws ServerException;

  /**
   * Removes member with given organization and user
   *
   * @param userId id of user
   * @param organizationId id of organization
   * @throws NullPointerException when {@code organizationId} or {@code userId} is null
   * @throws ServerException when any other error occurs during member removing
   */
  void remove(String userId, String organizationId) throws ServerException;

  /**
   * Returns member for specified organization and user
   *
   * @param organizationId organization id
   * @param userId user id
   * @return member for specified organization and user
   * @throws NullPointerException when {@code organizationId} or {@code userId} is null
   * @throws NotFoundException when member for given user and organization was not found
   * @throws ServerException when any other error occurs during member fetching
   */
  MemberImpl getMember(String organizationId, String userId)
      throws NotFoundException, ServerException;

  /**
   * Returns all members of given organization
   *
   * @param organizationId organization id
   * @param maxItems the maximum number of members to return
   * @param skipCount the number of members to skip
   * @throws NullPointerException when {@code organizationId} is null
   * @throws ServerException when any other error occurs during members fetching
   */
  Page<MemberImpl> getMembers(String organizationId, int maxItems, long skipCount)
      throws ServerException;

  /**
   * Returns all memberships of given user
   *
   * @param userId user id
   * @throws NullPointerException when {@code userId} is null
   * @throws ServerException when any other error occurs during members fetching
   */
  List<MemberImpl> getMemberships(String userId) throws ServerException;

  /**
   * Gets list organizations where user is member.
   *
   * @param userId user id
   * @param maxItems the maximum number of organizations to return
   * @param skipCount the number of organizations to skip
   * @return list of organizations where user is member
   * @throws NullPointerException when {@code userId} is null
   * @throws ServerException when any other error occurs during organizations fetching
   */
  Page<OrganizationImpl> getOrganizations(String userId, int maxItems, long skipCount)
      throws ServerException;
}
