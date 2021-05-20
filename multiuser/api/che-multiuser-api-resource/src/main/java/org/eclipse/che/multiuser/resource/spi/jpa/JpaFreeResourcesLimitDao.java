/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.resource.spi.jpa;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import com.google.inject.persist.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import org.eclipse.che.account.event.BeforeAccountRemovedEvent;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.core.db.jpa.IntegrityConstraintViolationException;
import org.eclipse.che.multiuser.resource.spi.FreeResourcesLimitDao;
import org.eclipse.che.multiuser.resource.spi.impl.FreeResourcesLimitImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JPA based implementation of {@link FreeResourcesLimitDao}.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class JpaFreeResourcesLimitDao implements FreeResourcesLimitDao {
  private static final Logger LOG = LoggerFactory.getLogger(JpaFreeResourcesLimitDao.class);

  @Inject private Provider<EntityManager> managerProvider;

  @Override
  public void store(FreeResourcesLimitImpl resourcesLimit)
      throws ConflictException, ServerException {
    requireNonNull(resourcesLimit, "Required non-null resource limit");
    try {
      doStore(resourcesLimit);
    } catch (RuntimeException e) {
      throw new ServerException(e.getMessage(), e);
    }
  }

  @Override
  @Transactional
  public FreeResourcesLimitImpl get(String accountId) throws NotFoundException, ServerException {
    requireNonNull(accountId, "Required non-null account id");
    try {
      return new FreeResourcesLimitImpl(doGet(accountId));
    } catch (NoResultException e) {
      throw new NotFoundException(
          "Free resources limit for account '" + accountId + "' was not found");
    } catch (RuntimeException e) {
      throw new ServerException(e.getMessage(), e);
    }
  }

  @Override
  @Transactional
  public Page<FreeResourcesLimitImpl> getAll(int maxItems, int skipCount) throws ServerException {
    try {
      final List<FreeResourcesLimitImpl> list =
          managerProvider
              .get()
              .createNamedQuery("FreeResourcesLimit.getAll", FreeResourcesLimitImpl.class)
              .setMaxResults(maxItems)
              .setFirstResult(skipCount)
              .getResultList()
              .stream()
              .map(FreeResourcesLimitImpl::new)
              .collect(Collectors.toList());
      return new Page<>(list, skipCount, maxItems, getTotalCount());
    } catch (RuntimeException e) {
      throw new ServerException(e.getMessage(), e);
    }
  }

  @Override
  public void remove(String id) throws ServerException {
    requireNonNull(id, "Required non-null id");
    try {
      doRemove(id);
    } catch (RuntimeException x) {
      throw new ServerException(x.getLocalizedMessage(), x);
    }
  }

  @Transactional
  protected void doRemove(String id) {
    final EntityManager manager = managerProvider.get();
    final FreeResourcesLimitImpl resourcesLimit = manager.find(FreeResourcesLimitImpl.class, id);
    if (resourcesLimit != null) {
      manager.remove(resourcesLimit);
      manager.flush();
    }
  }

  @Transactional(rollbackOn = {RuntimeException.class, ConflictException.class})
  protected void doStore(FreeResourcesLimitImpl resourcesLimit) throws ConflictException {
    EntityManager manager = managerProvider.get();
    try {
      final FreeResourcesLimitImpl existedLimit = doGet(resourcesLimit.getAccountId());
      existedLimit.setResources(resourcesLimit.getResources());
      manager.flush();
    } catch (NoResultException n) {
      try {
        manager.persist(resourcesLimit);
        manager.flush();
      } catch (IntegrityConstraintViolationException e) {
        throw new ConflictException(
            format("The specified account '%s' does not exist", resourcesLimit.getAccountId()));
      }
    }
  }

  @Transactional
  protected FreeResourcesLimitImpl doGet(String accountId) {
    return managerProvider
        .get()
        .createNamedQuery("FreeResourcesLimit.get", FreeResourcesLimitImpl.class)
        .setParameter("accountId", accountId)
        .getSingleResult();
  }

  private long getTotalCount() throws ServerException {
    return managerProvider
        .get()
        .createNamedQuery("FreeResourcesLimit.getTotalCount", Long.class)
        .getSingleResult();
  }

  @Singleton
  public static class RemoveFreeResourcesLimitSubscriber
      implements EventSubscriber<BeforeAccountRemovedEvent> {
    @Inject private EventService eventService;
    @Inject private FreeResourcesLimitDao limitDao;

    @PostConstruct
    public void subscribe() {
      eventService.subscribe(this);
    }

    @PreDestroy
    public void unsubscribe() {
      eventService.unsubscribe(this);
    }

    @Override
    public void onEvent(BeforeAccountRemovedEvent event) {
      try {
        limitDao.remove(event.getAccount().getId());
      } catch (Exception x) {
        LOG.error(
            format(
                "Couldn't remove free resources limit before account '%s' is removed",
                event.getAccount().getId()),
            x);
      }
    }
  }
}
