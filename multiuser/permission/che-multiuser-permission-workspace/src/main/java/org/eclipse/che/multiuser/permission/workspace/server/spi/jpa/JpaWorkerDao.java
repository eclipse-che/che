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
import org.eclipse.che.api.user.server.event.BeforeUserRemovedEvent;
import org.eclipse.che.api.workspace.server.event.BeforeWorkspaceRemovedEvent;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.core.db.cascade.CascadeEventSubscriber;
import org.eclipse.che.multiuser.api.permission.server.AbstractPermissionsDomain;
import org.eclipse.che.multiuser.api.permission.server.jpa.AbstractJpaPermissionsDao;
import org.eclipse.che.multiuser.permission.workspace.server.model.impl.WorkerImpl;
import org.eclipse.che.multiuser.permission.workspace.server.spi.WorkerDao;

/**
 * JPA based implementation of worker DAO.
 *
 * @author Max Shaposhnik
 */
@Singleton
public class JpaWorkerDao extends AbstractJpaPermissionsDao<WorkerImpl> implements WorkerDao {

  @Inject
  public JpaWorkerDao(AbstractPermissionsDomain<WorkerImpl> supportedDomain) {
    super(supportedDomain);
  }

  @Override
  public WorkerImpl getWorker(String workspaceId, String userId)
      throws ServerException, NotFoundException {
    return new WorkerImpl(get(userId, workspaceId));
  }

  @Override
  public void removeWorker(String workspaceId, String userId) throws ServerException {
    try {
      super.remove(userId, workspaceId);
    } catch (NotFoundException e) {
      throw new ServerException(e);
    }
  }

  @Override
  public Page<WorkerImpl> getWorkers(String workspaceId, int maxItems, long skipCount)
      throws ServerException {
    return getByInstance(workspaceId, maxItems, skipCount);
  }

  @Override
  public List<WorkerImpl> getWorkersByUser(String userId) throws ServerException {
    return getByUser(userId);
  }

  @Override
  public WorkerImpl get(String userId, String instanceId)
      throws ServerException, NotFoundException {
    requireNonNull(instanceId, "Workspace identifier required");
    requireNonNull(userId, "User identifier required");
    try {
      return new WorkerImpl(getEntity(wildcardToNull(userId), instanceId));
    } catch (RuntimeException x) {
      throw new ServerException(x.getLocalizedMessage(), x);
    }
  }

  @Override
  public List<WorkerImpl> getByUser(String userId) throws ServerException {
    requireNonNull(userId, "User identifier required");
    return doGetByUser(wildcardToNull(userId)).stream().map(WorkerImpl::new).collect(toList());
  }

  @Override
  @Transactional
  public Page<WorkerImpl> getByInstance(String instanceId, int maxItems, long skipCount)
      throws ServerException {
    requireNonNull(instanceId, "Workspace identifier required");
    checkArgument(
        skipCount <= Integer.MAX_VALUE,
        "The number of items to skip can't be greater than " + Integer.MAX_VALUE);

    try {
      final EntityManager entityManager = managerProvider.get();
      final List<WorkerImpl> workers =
          entityManager
              .createNamedQuery("Worker.getByWorkspaceId", WorkerImpl.class)
              .setParameter("workspaceId", instanceId)
              .setMaxResults(maxItems)
              .setFirstResult((int) skipCount)
              .getResultList()
              .stream()
              .map(WorkerImpl::new)
              .collect(toList());
      final Long workersCount =
          entityManager
              .createNamedQuery("Worker.getCountByWorkspaceId", Long.class)
              .setParameter("workspaceId", instanceId)
              .getSingleResult();
      return new Page<>(workers, skipCount, maxItems, workersCount);
    } catch (RuntimeException e) {
      throw new ServerException(e.getLocalizedMessage(), e);
    }
  }

  @Override
  protected WorkerImpl getEntity(String userId, String instanceId) throws NotFoundException {
    try {
      return doGet(userId, instanceId);
    } catch (NoResultException e) {
      throw new NotFoundException(
          format("Worker of workspace '%s' with id '%s' was not found.", instanceId, userId));
    }
  }

  @Transactional
  protected WorkerImpl doGet(String userId, String instanceId) {
    return managerProvider
        .get()
        .createNamedQuery("Worker.getByUserAndWorkspaceId", WorkerImpl.class)
        .setParameter("workspaceId", instanceId)
        .setParameter("userId", userId)
        .getSingleResult();
  }

  @Transactional
  protected List<WorkerImpl> doGetByUser(@Nullable String userId) throws ServerException {
    try {
      return managerProvider
          .get()
          .createNamedQuery("Worker.getByUserId", WorkerImpl.class)
          .setParameter("userId", userId)
          .getResultList();
    } catch (RuntimeException e) {
      throw new ServerException(e.getLocalizedMessage(), e);
    }
  }

  @Singleton
  public static class RemoveWorkersBeforeWorkspaceRemovedEventSubscriber
      extends CascadeEventSubscriber<BeforeWorkspaceRemovedEvent> {
    private static final int PAGE_SIZE = 100;

    @Inject private EventService eventService;
    @Inject private WorkerDao workerDao;

    @PostConstruct
    public void subscribe() {
      eventService.subscribe(this, BeforeWorkspaceRemovedEvent.class);
    }

    @PreDestroy
    public void unsubscribe() {
      eventService.unsubscribe(this, BeforeWorkspaceRemovedEvent.class);
    }

    @Override
    public void onCascadeEvent(BeforeWorkspaceRemovedEvent event) throws Exception {
      removeWorkers(event.getWorkspace().getId(), PAGE_SIZE);
    }

    @VisibleForTesting
    void removeWorkers(String workspaceId, int pageSize) throws ServerException {
      Page<WorkerImpl> workersPage;
      do {
        // skip count always equals to 0 because elements will be shifted after removing previous
        // items
        workersPage = workerDao.getWorkers(workspaceId, pageSize, 0);
        for (WorkerImpl worker : workersPage.getItems()) {
          workerDao.removeWorker(worker.getInstanceId(), worker.getUserId());
        }
      } while (workersPage.hasNextPage());
    }
  }

  @Singleton
  public static class RemoveWorkersBeforeUserRemovedEventSubscriber
      extends CascadeEventSubscriber<BeforeUserRemovedEvent> {
    @Inject private EventService eventService;
    @Inject private WorkerDao dao;

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
      for (WorkerImpl worker : dao.getWorkersByUser(event.getUser().getId())) {
        dao.removeWorker(worker.getInstanceId(), worker.getUserId());
      }
    }
  }
}
