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
package org.eclipse.che.multiuser.api.permission.server.jpa;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import com.google.inject.persist.Transactional;
import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.user.server.event.BeforeUserRemovedEvent;
import org.eclipse.che.core.db.cascade.CascadeEventSubscriber;
import org.eclipse.che.multiuser.api.permission.server.SystemDomain;
import org.eclipse.che.multiuser.api.permission.server.model.impl.SystemPermissionsImpl;

/**
 * JPA based implementation of system permissions DAO.
 *
 * @author Max Shaposhnik
 */
@Singleton
public class JpaSystemPermissionsDao extends AbstractJpaPermissionsDao<SystemPermissionsImpl> {

  @Inject
  public JpaSystemPermissionsDao(
      @Named(SystemDomain.SYSTEM_DOMAIN_ACTIONS) Set<String> allowedActions) {
    super(new SystemDomain(allowedActions));
  }

  @Override
  public SystemPermissionsImpl get(String userId, String instanceId)
      throws ServerException, NotFoundException {
    requireNonNull(userId, "Required non-null user id");
    try {
      return new SystemPermissionsImpl(getEntity(wildcardToNull(userId), instanceId));
    } catch (RuntimeException x) {
      throw new ServerException(x.getLocalizedMessage(), x);
    }
  }

  @Override
  @Transactional
  public Page<SystemPermissionsImpl> getByInstance(String instanceId, int maxItems, long skipCount)
      throws ServerException {
    checkArgument(
        skipCount <= Integer.MAX_VALUE,
        "The number of items to skip can't be greater than " + Integer.MAX_VALUE);
    // instanceId is ignored because system domain doesn't require it
    try {
      final EntityManager entityManager = managerProvider.get();
      final List<SystemPermissionsImpl> permissions =
          entityManager
              .createNamedQuery("SystemPermissions.getAll", SystemPermissionsImpl.class)
              .setMaxResults(maxItems)
              .setFirstResult((int) skipCount)
              .getResultList()
              .stream()
              .map(SystemPermissionsImpl::new)
              .collect(toList());
      final Long totalCount =
          entityManager
              .createNamedQuery("SystemPermissions.getTotalCount", Long.class)
              .getSingleResult();
      return new Page<>(permissions, skipCount, maxItems, totalCount);
    } catch (RuntimeException e) {
      throw new ServerException(e.getLocalizedMessage(), e);
    }
  }

  @Override
  public List<SystemPermissionsImpl> getByUser(String userId) throws ServerException {
    requireNonNull(userId, "User identifier required");
    try {
      return doGetByUser(userId).stream().map(SystemPermissionsImpl::new).collect(toList());
    } catch (RuntimeException e) {
      throw new ServerException(e.getLocalizedMessage(), e);
    }
  }

  @Override
  protected SystemPermissionsImpl getEntity(String userId, String instanceId)
      throws NotFoundException, ServerException {
    try {
      final List<SystemPermissionsImpl> existent = doGetByUser(userId);
      if (existent.isEmpty()) {
        throw new NotFoundException(format("System permissions for user '%s' not found", userId));
      }
      return existent.get(0);
    } catch (RuntimeException e) {
      throw new ServerException(e.getMessage(), e);
    }
  }

  @Transactional
  protected List<SystemPermissionsImpl> doGetByUser(String userId) {
    return managerProvider
        .get()
        .createNamedQuery("SystemPermissions.getByUserId", SystemPermissionsImpl.class)
        .setParameter("userId", userId)
        .getResultList();
  }

  @Override
  public void remove(String userId, String instanceId) throws ServerException, NotFoundException {
    requireNonNull(userId, "User identifier required");
    try {
      doRemove(userId, instanceId);
    } catch (RuntimeException x) {
      throw new ServerException(x.getLocalizedMessage(), x);
    }
  }

  @Singleton
  public static class RemoveSystemPermissionsBeforeUserRemovedEventSubscriber
      extends CascadeEventSubscriber<BeforeUserRemovedEvent> {
    @Inject private EventService eventService;
    @Inject JpaSystemPermissionsDao dao;

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
      for (SystemPermissionsImpl permissions : dao.getByUser(event.getUser().getId())) {
        dao.remove(permissions.getUserId(), permissions.getInstanceId());
      }
    }
  }
}
