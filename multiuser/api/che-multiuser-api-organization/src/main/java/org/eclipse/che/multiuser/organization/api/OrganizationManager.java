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
package org.eclipse.che.multiuser.organization.api;

import static java.util.Objects.requireNonNull;
import static org.eclipse.che.multiuser.organization.api.DtoConverter.asDto;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.account.event.BeforeAccountRemovedEvent;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.Pages;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.multiuser.organization.api.event.BeforeOrganizationRemovedEvent;
import org.eclipse.che.multiuser.organization.api.event.OrganizationPersistedEvent;
import org.eclipse.che.multiuser.organization.api.event.OrganizationRemovedEvent;
import org.eclipse.che.multiuser.organization.api.event.OrganizationRenamedEvent;
import org.eclipse.che.multiuser.organization.api.permissions.OrganizationDomain;
import org.eclipse.che.multiuser.organization.shared.model.Member;
import org.eclipse.che.multiuser.organization.shared.model.Organization;
import org.eclipse.che.multiuser.organization.spi.MemberDao;
import org.eclipse.che.multiuser.organization.spi.OrganizationDao;
import org.eclipse.che.multiuser.organization.spi.impl.MemberImpl;
import org.eclipse.che.multiuser.organization.spi.impl.OrganizationImpl;

/**
 * Facade for Organization related operations.
 *
 * @author gazarenkov
 * @author Sergii Leschenko
 */
@Singleton
public class OrganizationManager {

  private final EventService eventService;
  private final OrganizationDao organizationDao;
  private final MemberDao memberDao;
  private final Set<String> reservedNames;

  @Inject
  public OrganizationManager(
      EventService eventService,
      OrganizationDao organizationDao,
      MemberDao memberDao,
      @Named("che.auth.reserved_user_names") String[] reservedNames) {
    this.eventService = eventService;
    this.organizationDao = organizationDao;
    this.memberDao = memberDao;
    this.reservedNames = Sets.newHashSet(reservedNames);
  }

  /**
   * Creates new organization.
   *
   * @param newOrganization organization to create
   * @return created organization
   * @throws NullPointerException when {@code organization} is null
   * @throws NotFoundException when parent organization was not found
   * @throws ConflictException when organization with such id/name already exists
   * @throws ConflictException when specified organization name is reserved
   * @throws ServerException when any other error occurs during organization creation
   */
  @Transactional(rollbackOn = {RuntimeException.class, ApiException.class})
  public Organization create(Organization newOrganization)
      throws NotFoundException, ConflictException, ServerException {
    requireNonNull(newOrganization, "Required non-null organization");
    requireNonNull(newOrganization.getName(), "Required non-null organization name");

    String qualifiedName;
    if (newOrganization.getParent() != null) {
      final Organization parent = getById(newOrganization.getParent());
      qualifiedName = parent.getQualifiedName() + "/" + newOrganization.getName();
    } else {
      qualifiedName = newOrganization.getName();
    }
    checkNameReservation(qualifiedName);

    final OrganizationImpl organization =
        new OrganizationImpl(
            NameGenerator.generate("organization", 16), qualifiedName, newOrganization.getParent());
    organizationDao.create(organization);
    addFirstMember(organization);
    eventService.publish(new OrganizationPersistedEvent(organization)).propagateException();
    return organization;
  }

  /**
   * Updates organization with new entity.
   *
   * @param organizationId id of organization to update
   * @param update organization update
   * @throws NullPointerException when {@code organizationId} or {@code update} is null
   * @throws NotFoundException when organization with given id doesn't exist
   * @throws ConflictException when name updated with a value which is reserved or is not unique
   * @throws ServerException when any other error occurs organization updating
   */
  @Transactional(rollbackOn = {RuntimeException.class, ApiException.class})
  public Organization update(String organizationId, Organization update)
      throws NotFoundException, ConflictException, ServerException {
    requireNonNull(organizationId, "Required non-null organization id");
    requireNonNull(update, "Required non-null organization");
    requireNonNull(update.getName(), "Required non-null organization name");

    final OrganizationImpl organization = organizationDao.getById(organizationId);
    final String oldQualifiedName = organization.getQualifiedName();
    final String oldName = organization.getName();

    final String newName = update.getName();
    final String newQualifiedName = buildQualifiedName(oldQualifiedName, update.getName());

    checkNameReservation(newQualifiedName);
    organization.setQualifiedName(newQualifiedName);

    organizationDao.update(organization);
    if (!newName.equals(oldName)) {
      updateSuborganizationsQualifiedNames(oldQualifiedName, organization.getQualifiedName());

      final String performerName = EnvironmentContext.getCurrent().getSubject().getUserName();
      // should be DTO as it sent via json rpc
      eventService.publish(
          asDto(new OrganizationRenamedEvent(performerName, oldName, newName, organization)));
    }
    return organization;
  }

  /**
   * Removes organization with given id
   *
   * @param organizationId organization id
   * @throws NullPointerException when {@code organizationId} is null
   * @throws ServerException when any other error occurs during organization removing
   */
  @Transactional(rollbackOn = {RuntimeException.class, ApiException.class})
  public void remove(String organizationId) throws ServerException {
    requireNonNull(organizationId, "Required non-null organization id");
    try {
      OrganizationImpl organization = organizationDao.getById(organizationId);
      eventService
          .publish(new BeforeAccountRemovedEvent(organization.getAccount()))
          .propagateException();
      eventService.publish(new BeforeOrganizationRemovedEvent(organization)).propagateException();
      removeSuborganizations(organizationId);
      final List<String> members = removeMembers(organizationId);
      organizationDao.remove(organizationId);
      final String initiator = EnvironmentContext.getCurrent().getSubject().getUserName();
      eventService.publish(asDto(new OrganizationRemovedEvent(initiator, organization, members)));
    } catch (NotFoundException e) {
      // organization is already removed
    }
  }

  /**
   * Gets organization by identifier.
   *
   * @param organizationId organization id
   * @return organization instance
   * @throws NullPointerException when {@code organizationId} is null
   * @throws NotFoundException when organization with given id was not found
   * @throws ServerException when any other error occurs during organization fetching
   */
  public Organization getById(String organizationId) throws NotFoundException, ServerException {
    requireNonNull(organizationId, "Required non-null organization id");
    return organizationDao.getById(organizationId);
  }

  /**
   * Gets organization by name.
   *
   * @param organizationName organization name
   * @return organization instance
   * @throws NullPointerException when {@code organizationName} is null
   * @throws NotFoundException when organization with given name was not found
   * @throws ServerException when any other error occurs during organization fetching
   */
  public Organization getByName(String organizationName) throws NotFoundException, ServerException {
    requireNonNull(organizationName, "Required non-null organization name");
    return organizationDao.getByName(organizationName);
  }

  /**
   * Gets child organizations by given parent.
   *
   * @param parent id of parent organizations
   * @param maxItems the maximum number of organizations to return
   * @param skipCount the number of organizations to skip
   * @return list of children organizations
   * @throws NullPointerException when {@code parent} is null
   * @throws ServerException when any other error occurs during organizations fetching
   */
  public Page<? extends Organization> getByParent(String parent, int maxItems, long skipCount)
      throws ServerException {
    requireNonNull(parent, "Required non-null parent");
    return organizationDao.getByParent(parent, maxItems, skipCount);
  }

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
  public Page<OrganizationImpl> getSuborganizations(
      String parentQualifiedName, int maxItems, long skipCount) throws ServerException {
    requireNonNull(parentQualifiedName, "Required non-null parent qualified name");
    return organizationDao.getSuborganizations(parentQualifiedName, maxItems, skipCount);
  }

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
  public Page<? extends Organization> getByMember(String userId, int maxItems, int skipCount)
      throws ServerException {
    requireNonNull(userId, "Required non-null user id");
    return memberDao.getOrganizations(userId, maxItems, skipCount);
  }

  private String buildQualifiedName(String oldQualifiedName, String newName) {
    int lastSlashIndex = oldQualifiedName.lastIndexOf("/");
    if (lastSlashIndex != -1) { // check that it is not root organization
      return oldQualifiedName.substring(0, lastSlashIndex + 1) + newName;
    } else {
      return newName;
    }
  }

  private void updateSuborganizationsQualifiedNames(
      String oldQualifiedName, String newQualifiedName)
      throws NotFoundException, ConflictException, ServerException {
    for (OrganizationImpl suborganization :
        Pages.iterate(
            (maxItems, skipCount) ->
                organizationDao.getSuborganizations(oldQualifiedName, maxItems, skipCount))) {
      suborganization.setQualifiedName(
          suborganization.getQualifiedName().replaceFirst(oldQualifiedName, newQualifiedName));
      organizationDao.update(suborganization);
    }
  }

  /**
   * Gets list of members by specified organization id.
   *
   * @param organizationId organization identifier
   * @param maxItems the maximum number of members to return
   * @param skipCount the number of members to skip
   * @return list of members
   * @throws NullPointerException when {@code organizationId} is null
   * @throws ServerException when any other error occurs during organizations fetching
   */
  public Page<? extends Member> getMembers(String organizationId, int maxItems, long skipCount)
      throws ServerException {
    requireNonNull(organizationId, "Required non-null organization id");
    return memberDao.getMembers(organizationId, maxItems, skipCount);
  }

  protected void addFirstMember(Organization organization) throws ServerException {
    memberDao.store(
        new MemberImpl(
            EnvironmentContext.getCurrent().getSubject().getUserId(),
            organization.getId(),
            OrganizationDomain.getActions()));
  }

  /**
   * Removes suborganizations of given parent organization page by page
   *
   * @param organizationId parent organization id
   */
  @VisibleForTesting
  void removeSuborganizations(String organizationId) throws ServerException {
    Page<? extends Organization> suborganizationsPage;
    do {
      // skip count always equals to 0 because elements will be shifted after removing previous
      // items
      suborganizationsPage = organizationDao.getByParent(organizationId, 100, 0);
      for (Organization suborganization : suborganizationsPage.getItems()) {
        remove(suborganization.getId());
      }
    } while (suborganizationsPage.hasNextPage());
  }

  @VisibleForTesting
  List<String> removeMembers(String organizationId) throws ServerException {
    List<String> removed = new ArrayList<>();
    Page<MemberImpl> membersPage;
    do {
      // skip count always equals to 0 because elements will be shifted after removing previous
      // items
      membersPage = memberDao.getMembers(organizationId, 100, 0);
      for (MemberImpl member : membersPage.getItems()) {
        removed.add(member.getUserId());
        memberDao.remove(member.getUserId(), member.getOrganizationId());
      }
    } while (membersPage.hasNextPage());
    return removed;
  }

  /**
   * Checks reservation of organization name
   *
   * @param organizationName organization name to check
   * @throws ConflictException when organization name is reserved and can be used by user
   */
  private void checkNameReservation(String organizationName) throws ConflictException {
    if (reservedNames.contains(organizationName.toLowerCase())) {
      throw new ConflictException(
          String.format("Organization name '%s' is reserved", organizationName));
    }
  }
}
