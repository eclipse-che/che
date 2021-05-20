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
package org.eclipse.che.multiuser.permission.devfile.server.spi.jpa;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.persist.Transactional;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.devfile.server.event.BeforeDevfileRemovedEvent;
import org.eclipse.che.api.user.server.event.BeforeUserRemovedEvent;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.core.db.cascade.CascadeEventSubscriber;
import org.eclipse.che.multiuser.api.permission.server.AbstractPermissionsDomain;
import org.eclipse.che.multiuser.api.permission.server.jpa.AbstractJpaPermissionsDao;
import org.eclipse.che.multiuser.permission.devfile.server.model.impl.UserDevfilePermissionImpl;
import org.eclipse.che.multiuser.permission.devfile.server.spi.UserDevfilePermissionDao;

/** JPA implementation of {@link UserDevfilePermissionDao}. */
public class JpaUserDevfilePermissionDao
    extends AbstractJpaPermissionsDao<UserDevfilePermissionImpl>
    implements UserDevfilePermissionDao {

  @Inject
  public JpaUserDevfilePermissionDao(
      AbstractPermissionsDomain<UserDevfilePermissionImpl> supportedDomain) {
    super(supportedDomain);
  }

  @Override
  public UserDevfilePermissionImpl get(String userId, String instanceId)
      throws ServerException, NotFoundException {

    requireNonNull(instanceId, "User devfile identifier required");
    requireNonNull(userId, "User identifier required");
    try {
      return new UserDevfilePermissionImpl(getEntity(wildcardToNull(userId), instanceId));
    } catch (RuntimeException x) {
      throw new ServerException(x.getLocalizedMessage(), x);
    }
  }

  @Override
  public List<UserDevfilePermissionImpl> getByUser(String userId) throws ServerException {
    requireNonNull(userId, "User identifier required");
    return doGetByUser(wildcardToNull(userId))
        .stream()
        .map(UserDevfilePermissionImpl::new)
        .collect(toList());
  }

  @Override
  @Transactional
  public Page<UserDevfilePermissionImpl> getByInstance(
      String instanceId, int maxItems, long skipCount) throws ServerException {
    requireNonNull(instanceId, "User devfile identifier required");
    checkArgument(
        skipCount <= Integer.MAX_VALUE,
        "The number of items to skip can't be greater than " + Integer.MAX_VALUE);

    try {
      final EntityManager entityManager = managerProvider.get();
      final List<UserDevfilePermissionImpl> permissions =
          entityManager
              .createNamedQuery(
                  "UserDevfilePermission.getByUserDevfileId", UserDevfilePermissionImpl.class)
              .setParameter("userDevfileId", instanceId)
              .setMaxResults(maxItems)
              .setFirstResult((int) skipCount)
              .getResultList()
              .stream()
              .map(UserDevfilePermissionImpl::new)
              .collect(toList());
      final Long permissionsCount =
          entityManager
              .createNamedQuery("UserDevfilePermission.getCountByUserDevfileId", Long.class)
              .setParameter("userDevfileId", instanceId)
              .getSingleResult();
      return new Page<>(permissions, skipCount, maxItems, permissionsCount);
    } catch (RuntimeException e) {
      throw new ServerException(e.getLocalizedMessage(), e);
    }
  }

  @Override
  protected UserDevfilePermissionImpl getEntity(String userId, String instanceId)
      throws NotFoundException, ServerException {
    try {
      return doGet(userId, instanceId);
    } catch (NoResultException e) {
      throw new NotFoundException(
          format("User %s does not have permissions assigned to devfile %s.", instanceId, userId));
    } catch (RuntimeException e) {
      throw new ServerException(e.getMessage(), e);
    }
  }

  @Override
  public UserDevfilePermissionImpl getUserDevfilePermission(String userDevfileId, String userId)
      throws ServerException, NotFoundException {
    return new UserDevfilePermissionImpl(get(userId, userDevfileId));
  }

  @Override
  public void removeUserDevfilePermission(String userDevfileId, String userId)
      throws ServerException {
    try {
      super.remove(userId, userDevfileId);
    } catch (NotFoundException e) {
      throw new ServerException(e);
    }
  }

  @Override
  public Page<UserDevfilePermissionImpl> getUserDevfilePermission(
      String userDevfileId, int maxItems, long skipCount) throws ServerException {
    return getByInstance(userDevfileId, maxItems, skipCount);
  }

  @Override
  public List<UserDevfilePermissionImpl> getUserDevfilePermissionByUser(String userId)
      throws ServerException {
    return getByUser(userId);
  }

  @Transactional
  protected UserDevfilePermissionImpl doGet(String userId, String instanceId) {
    return managerProvider
        .get()
        .createNamedQuery(
            "UserDevfilePermission.getByUserAndUserDevfileId", UserDevfilePermissionImpl.class)
        .setParameter("userDevfileId", instanceId)
        .setParameter("userId", userId)
        .getSingleResult();
  }

  @Transactional
  protected List<UserDevfilePermissionImpl> doGetByUser(@Nullable String userId)
      throws ServerException {
    try {
      return managerProvider
          .get()
          .createNamedQuery("UserDevfilePermission.getByUserId", UserDevfilePermissionImpl.class)
          .setParameter("userId", userId)
          .getResultList();
    } catch (RuntimeException e) {
      throw new ServerException(e.getLocalizedMessage(), e);
    }
  }

  @Singleton
  public static class RemoveUserDevfilePermissionsBeforeUserDevfileRemovedEventSubscriber
      extends CascadeEventSubscriber<BeforeDevfileRemovedEvent> {
    private static final int PAGE_SIZE = 100;

    @Inject private EventService eventService;
    @Inject private UserDevfilePermissionDao userDevfilePermissionDao;

    @PostConstruct
    public void subscribe() {
      eventService.subscribe(this, BeforeDevfileRemovedEvent.class);
    }

    @PreDestroy
    public void unsubscribe() {
      eventService.unsubscribe(this, BeforeDevfileRemovedEvent.class);
    }

    @Override
    public void onCascadeEvent(BeforeDevfileRemovedEvent event) throws Exception {
      removeUserDevfilePermissions(event.getUserDevfile().getId(), PAGE_SIZE);
    }

    @VisibleForTesting
    void removeUserDevfilePermissions(String userDevfileId, int pageSize) throws ServerException {
      Page<UserDevfilePermissionImpl> permissionsPage;
      do {
        // skip count always equals to 0 because elements will be shifted after removing previous
        // items
        permissionsPage =
            userDevfilePermissionDao.getUserDevfilePermission(userDevfileId, pageSize, 0);
        for (UserDevfilePermissionImpl permission : permissionsPage.getItems()) {
          userDevfilePermissionDao.removeUserDevfilePermission(
              permission.getInstanceId(), permission.getUserId());
        }
      } while (permissionsPage.hasNextPage());
    }
  }

  @Singleton
  public static class RemoveUserDevfilePermissionsBeforeUserRemovedEventSubscriber
      extends CascadeEventSubscriber<BeforeUserRemovedEvent> {
    @Inject private EventService eventService;
    @Inject private UserDevfilePermissionDao userDevfilePermissionDao;

    @PostConstruct
    public void subscribe() {
      eventService.subscribe(this, BeforeUserRemovedEvent.class);
    }

    @PreDestroy
    public void unsubscribe() {
      eventService.unsubscribe(this, BeforeUserRemovedEvent.class);
    }

    @Override
    public void onCascadeEvent(BeforeUserRemovedEvent event) throws Exception {
      for (UserDevfilePermissionImpl permission :
          userDevfilePermissionDao.getUserDevfilePermissionByUser(event.getUser().getId())) {
        userDevfilePermissionDao.removeUserDevfilePermission(
            permission.getInstanceId(), permission.getUserId());
      }
    }
  }
}
