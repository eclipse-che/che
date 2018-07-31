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
package org.eclipse.che.multiuser.api.permission.server;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.eclipse.che.multiuser.api.permission.server.AbstractPermissionsDomain.SET_PERMISSIONS;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.Pages;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.concurrent.StripedLocks;
import org.eclipse.che.commons.lang.concurrent.Unlocker;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.multiuser.api.permission.server.event.PermissionsCreatedEvent;
import org.eclipse.che.multiuser.api.permission.server.event.PermissionsRemovedEvent;
import org.eclipse.che.multiuser.api.permission.server.model.impl.AbstractPermissions;
import org.eclipse.che.multiuser.api.permission.server.spi.PermissionsDao;
import org.eclipse.che.multiuser.api.permission.shared.model.Permissions;

/**
 * Facade for Permissions related operations.
 *
 * @author gazarenkov
 * @author Sergii Leschenko
 * @author Anton Korneta
 */
@Singleton
public class PermissionsManager {

  private final EventService eventService;

  private final List<AbstractPermissionsDomain<? extends AbstractPermissions>> domains;
  private final Map<String, PermissionsDao<? extends AbstractPermissions>> domainToDao;
  private final StripedLocks updateLocks;

  @Inject
  public PermissionsManager(
      EventService eventService, Set<PermissionsDao<? extends AbstractPermissions>> daos)
      throws ServerException {
    this.eventService = eventService;
    final Map<String, PermissionsDao<? extends AbstractPermissions>> domainToDao = new HashMap<>();
    final List<AbstractPermissionsDomain<? extends AbstractPermissions>> domains =
        new ArrayList<>();
    for (PermissionsDao<? extends AbstractPermissions> dao : daos) {
      final AbstractPermissionsDomain<? extends AbstractPermissions> domain = dao.getDomain();
      final PermissionsDao<? extends AbstractPermissions> oldStorage =
          domainToDao.put(domain.getId(), dao);
      domains.add(domain);
      if (oldStorage != null) {
        throw new ServerException(
            "Permissions Domain '"
                + domain.getId()
                + "' should be stored in only one storage. "
                + "Duplicated in "
                + dao.getClass()
                + " and "
                + oldStorage.getClass());
      }
    }
    this.domains = ImmutableList.copyOf(domains);
    this.domainToDao = ImmutableMap.copyOf(domainToDao);
    this.updateLocks = new StripedLocks(16);
  }

  /**
   * Stores (adds or updates) permissions.
   *
   * @param permissions permission to store
   * @throws NotFoundException when permissions have unsupported domain
   * @throws ConflictException when new permissions remove last 'setPermissions' of given instance
   * @throws ServerException when any other error occurs during permissions storing
   */
  public void storePermission(Permissions permissions)
      throws ServerException, ConflictException, NotFoundException {
    final String domainId = permissions.getDomainId();
    final String instanceId = permissions.getInstanceId();
    final String userId = permissions.getUserId();

    try (@SuppressWarnings("unused")
        Unlocker unlocker = updateLocks.writeLock(firstNonNull(instanceId, domainId))) {
      final PermissionsDao<? extends AbstractPermissions> permissionsDao =
          getPermissionsDao(domainId);
      if (!permissions.getActions().contains(SET_PERMISSIONS)
          && userHasLastSetPermissions(permissionsDao, userId, instanceId)) {
        throw new ConflictException(
            "Can't edit permissions because there is not any another user "
                + "with permission 'setPermissions'");
      }
      store(permissionsDao, userId, instanceId, permissions);
    }
  }

  /**
   * Returns user's permissions for specified instance
   *
   * @param userId user id
   * @param domainId domain id
   * @param instanceId instance id
   * @return userId's permissions for specified instanceId
   * @throws NotFoundException when given domainId is unsupported
   * @throws NotFoundException when permissions with given userId and domainId and instanceId was
   *     not found
   * @throws ServerException when any other error occurs during permissions fetching
   */
  public AbstractPermissions get(String userId, String domainId, String instanceId)
      throws ServerException, NotFoundException, ConflictException {
    return getPermissionsDao(domainId).get(userId, instanceId);
  }

  /**
   * Returns users' permissions for specified instance
   *
   * @param domainId domain id
   * @param instanceId instance id
   * @param maxItems the maximum number of permissions to return
   * @param skipCount the number of permissions to skip
   * @return set of permissions
   * @throws NotFoundException when given domainId is unsupported
   * @throws ServerException when any other error occurs during permissions fetching
   */
  @SuppressWarnings("unchecked")
  public Page<AbstractPermissions> getByInstance(
      String domainId, String instanceId, int maxItems, long skipCount)
      throws ServerException, NotFoundException {
    return (Page<AbstractPermissions>)
        getPermissionsDao(domainId).getByInstance(instanceId, maxItems, skipCount);
  }

  /**
   * Removes permissions of userId related to the particular instanceId of specified domainId
   *
   * @param userId user id
   * @param domainId domain id
   * @param instanceId instance id
   * @throws NotFoundException when given domainId is unsupported
   * @throws ConflictException when removes last 'setPermissions' of given instanceId
   * @throws ServerException when any other error occurs during permissions removing
   */
  public void remove(String userId, String domainId, String instanceId)
      throws ConflictException, ServerException, NotFoundException {
    final PermissionsDao<? extends AbstractPermissions> permissionsDao =
        getPermissionsDao(domainId);
    Permissions permissions;
    try (@SuppressWarnings("unused")
        Unlocker unlocker = updateLocks.writeLock(firstNonNull(instanceId, domainId))) {
      if (userHasLastSetPermissions(permissionsDao, userId, instanceId)) {
        throw new ConflictException(
            "Can't remove permissions because there is not any another user "
                + "with permission 'setPermissions'");
      }
      permissions = permissionsDao.get(userId, instanceId);
      permissionsDao.remove(userId, instanceId);
    }
    final String initiator = EnvironmentContext.getCurrent().getSubject().getUserName();
    eventService.publish(new PermissionsRemovedEvent(initiator, permissions));
  }

  /**
   * Checks existence of user's permission for specified instance
   *
   * @param userId user id
   * @param domainId domain id
   * @param instanceId instance id
   * @param action action name
   * @return true if the permission exists
   * @throws NotFoundException when given domain is unsupported
   * @throws ServerException when any other error occurs during permission existence checking
   */
  public boolean exists(String userId, String domainId, String instanceId, String action)
      throws ServerException, NotFoundException, ConflictException {
    return getDomain(domainId).getAllowedActions().contains(action)
        && getPermissionsDao(domainId).exists(userId, instanceId, action);
  }

  /**
   * Checks supporting all specified actions by domain with specified id.
   *
   * @param domainId domain id to check supporting
   * @param actions actions to check
   * @throws NotFoundException when domain with specified id is unsupported
   * @throws ConflictException when actions contain unsupported value
   */
  public void checkActionsSupporting(String domainId, List<String> actions)
      throws NotFoundException, ConflictException {
    checkActionsSupporting(getDomain(domainId), actions);
  }

  /** Returns supported domains */
  public List<AbstractPermissionsDomain> getDomains() {
    return new ArrayList<>(domains);
  }

  /**
   * Returns supported domain
   *
   * @throws NotFoundException when given domain is unsupported
   */
  public AbstractPermissionsDomain<? extends AbstractPermissions> getDomain(String domain)
      throws NotFoundException {
    return getPermissionsDao(domain).getDomain();
  }

  private <T extends AbstractPermissions> void store(
      PermissionsDao<T> dao, String userId, String instanceId, Permissions permissions)
      throws ConflictException, ServerException {
    final AbstractPermissionsDomain<T> permissionsDomain = dao.getDomain();
    final T permission =
        permissionsDomain.newInstance(userId, instanceId, permissions.getActions());
    checkActionsSupporting(permissionsDomain, permission.getActions());
    final Optional<T> existing = dao.store(permission);
    if (!existing.isPresent()) {
      Subject subject = EnvironmentContext.getCurrent().getSubject();
      final String initiator = subject.isAnonymous() ? null : subject.getUserName();
      eventService.publish(new PermissionsCreatedEvent(initiator, permissions));
    }
  }

  private void checkActionsSupporting(AbstractPermissionsDomain<?> domain, List<String> actions)
      throws ConflictException {
    final Set<String> allowedActions = new HashSet<>(domain.getAllowedActions());
    final Set<String> unsupportedActions =
        actions
            .stream()
            .filter(action -> !allowedActions.contains(action))
            .collect(Collectors.toSet());
    if (!unsupportedActions.isEmpty()) {
      throw new ConflictException(
          "Domain with id '"
              + domain.getId()
              + "' doesn't support following action(s): "
              + unsupportedActions.stream().collect(Collectors.joining(", ")));
    }
  }

  private PermissionsDao<? extends AbstractPermissions> getPermissionsDao(String domain)
      throws NotFoundException {
    final PermissionsDao<? extends AbstractPermissions> permissionsStorage =
        domainToDao.get(domain);
    if (permissionsStorage == null) {
      throw new NotFoundException("Requested unsupported domain '" + domain + "'");
    }
    return permissionsStorage;
  }

  private boolean userHasLastSetPermissions(
      PermissionsDao<? extends AbstractPermissions> storage, String userId, String instanceId)
      throws ServerException, ConflictException, NotFoundException {
    if (!storage.exists(userId, instanceId, SET_PERMISSIONS)) {
      return false;
    }

    for (AbstractPermissions permissions :
        Pages.iterateLazily(
            (maxItems, skipCount) -> storage.getByInstance(instanceId, maxItems, skipCount))) {
      if (!permissions.getUserId().equals(userId)
          && permissions.getActions().contains(SET_PERMISSIONS)) {
        return false;
      }
    }
    return true;
  }
}
