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
package org.eclipse.che.multiuser.permission.workspace.server.spi.jpa;

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
import org.eclipse.che.api.workspace.server.event.BeforeStackRemovedEvent;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.core.db.cascade.CascadeEventSubscriber;
import org.eclipse.che.multiuser.api.permission.server.AbstractPermissionsDomain;
import org.eclipse.che.multiuser.api.permission.server.jpa.AbstractJpaPermissionsDao;
import org.eclipse.che.multiuser.permission.workspace.server.stack.StackPermissionsImpl;

/**
 * JPA based implementation of stack permissions DAO.
 *
 * @author Max Shaposhnik
 */
@Singleton
public class JpaStackPermissionsDao extends AbstractJpaPermissionsDao<StackPermissionsImpl> {

  @Inject
  public JpaStackPermissionsDao(AbstractPermissionsDomain<StackPermissionsImpl> domain) {
    super(domain);
  }

  @Override
  public StackPermissionsImpl get(String userId, String instanceId)
      throws ServerException, NotFoundException {
    requireNonNull(instanceId, "Stack identifier required");
    requireNonNull(userId, "User identifier required");
    try {
      return new StackPermissionsImpl(getEntity(wildcardToNull(userId), instanceId));
    } catch (RuntimeException x) {
      throw new ServerException(x.getLocalizedMessage(), x);
    }
  }

  @Override
  public List<StackPermissionsImpl> getByUser(String userId) throws ServerException {
    requireNonNull(userId, "User identifier required");
    return doGetByUser(wildcardToNull(userId))
        .stream()
        .map(StackPermissionsImpl::new)
        .collect(toList());
  }

  @Override
  @Transactional
  public Page<StackPermissionsImpl> getByInstance(String instanceId, int maxItems, long skipCount)
      throws ServerException {
    requireNonNull(instanceId, "Stack identifier required");
    checkArgument(
        skipCount <= Integer.MAX_VALUE,
        "The number of items to skip can't be greater than " + Integer.MAX_VALUE);

    try {
      final EntityManager entityManager = managerProvider.get();
      final List<StackPermissionsImpl> stacks =
          entityManager
              .createNamedQuery("StackPermissions.getByStackId", StackPermissionsImpl.class)
              .setFirstResult((int) skipCount)
              .setMaxResults(maxItems)
              .setParameter("stackId", instanceId)
              .getResultList()
              .stream()
              .map(StackPermissionsImpl::new)
              .collect(toList());
      final Long permissionsCount =
          entityManager
              .createNamedQuery("StackPermissions.getCountByStackId", Long.class)
              .setParameter("stackId", instanceId)
              .getSingleResult();

      return new Page<>(stacks, skipCount, maxItems, permissionsCount);
    } catch (RuntimeException e) {
      throw new ServerException(e.getLocalizedMessage(), e);
    }
  }

  @Override
  protected StackPermissionsImpl getEntity(String userId, String instanceId)
      throws NotFoundException, ServerException {
    try {
      return doGet(userId, instanceId);
    } catch (NoResultException e) {
      throw new NotFoundException(
          format("Permissions on stack '%s' of user '%s' was not found.", instanceId, userId));
    } catch (RuntimeException e) {
      throw new ServerException(e.getMessage(), e);
    }
  }

  @Transactional
  protected StackPermissionsImpl doGet(String userId, String instanceId) {
    if (userId == null) {
      return managerProvider
          .get()
          .createNamedQuery("StackPermissions.getByStackIdPublic", StackPermissionsImpl.class)
          .setParameter("stackId", instanceId)
          .getSingleResult();
    } else {
      return managerProvider
          .get()
          .createNamedQuery("StackPermissions.getByUserAndStackId", StackPermissionsImpl.class)
          .setParameter("stackId", instanceId)
          .setParameter("userId", userId)
          .getSingleResult();
    }
  }

  @Transactional
  protected List<StackPermissionsImpl> doGetByUser(@Nullable String userId) throws ServerException {
    try {
      return managerProvider
          .get()
          .createNamedQuery("StackPermissions.getByUserId", StackPermissionsImpl.class)
          .setParameter("userId", userId)
          .getResultList();
    } catch (RuntimeException e) {
      throw new ServerException(e.getLocalizedMessage(), e);
    }
  }

  @Singleton
  public static class RemovePermissionsBeforeStackRemovedEventSubscriber
      extends CascadeEventSubscriber<BeforeStackRemovedEvent> {
    private static final int PAGE_SIZE = 100;
    @Inject private EventService eventService;
    @Inject private JpaStackPermissionsDao dao;

    @PostConstruct
    public void subscribe() {
      eventService.subscribe(this, BeforeStackRemovedEvent.class);
    }

    @PreDestroy
    public void unsubscribe() {
      eventService.unsubscribe(this, BeforeStackRemovedEvent.class);
    }

    @Override
    public void onCascadeEvent(BeforeStackRemovedEvent event) throws Exception {
      removeStackPermissions(event.getStack().getId(), PAGE_SIZE);
    }

    @VisibleForTesting
    void removeStackPermissions(String stackId, int pageSize)
        throws ServerException, NotFoundException {
      Page<StackPermissionsImpl> stacksPage;
      do {
        // skip count always equals to 0 because elements will be shifted after removing previous
        // items
        stacksPage = dao.getByInstance(stackId, pageSize, 0);
        for (StackPermissionsImpl stackPermissions : stacksPage.getItems()) {
          dao.remove(stackPermissions.getUserId(), stackPermissions.getInstanceId());
        }
      } while (stacksPage.hasNextPage());
    }
  }
}
