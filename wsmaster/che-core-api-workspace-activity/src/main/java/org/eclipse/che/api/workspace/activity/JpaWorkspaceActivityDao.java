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
package org.eclipse.che.api.workspace.activity;

import static java.util.Objects.requireNonNull;

import com.google.inject.persist.Transactional;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.core.db.jpa.DuplicateKeyException;
import org.eclipse.che.core.db.jpa.IntegrityConstraintViolationException;

/**
 * JPA workspaces expiration times storage.
 *
 * @author Max Shaposhnik (mshaposh@redhat.com)
 */
@Singleton
public class JpaWorkspaceActivityDao implements WorkspaceActivityDao {

  @Inject private Provider<EntityManager> managerProvider;

  @Override
  public void setExpirationTime(String workspaceId, long expirationTime) throws ServerException {
    requireNonNull(workspaceId, "Required non-null workspace id");
    doUpdate(workspaceId, a -> a.setExpiration(expirationTime));
  }

  @Override
  public void removeExpiration(String workspaceId) throws ServerException {
    requireNonNull(workspaceId, "Required non-null workspace id");
    doUpdateOptionally(workspaceId, a -> a.setExpiration(null));
  }

  /**
   * Finds any workspaces that have expired.
   *
   * <p>A workspace is expired when the expiration value is less than the current time or when the
   * difference between the current time and the last running time is greater than the run timeout
   *
   * @param timestamp expiration time
   * @param runTimeout time after which the workspace will be stopped regardless of activity
   * @return
   */
  @Override
  @Transactional(rollbackOn = ServerException.class)
  public List<String> findExpiredRunTimeout(long timestamp, long runTimeout)
      throws ServerException {
    try {
      return managerProvider
          .get()
          .createNamedQuery("WorkspaceActivity.getExpiredRunTimeout", String.class)
          .setParameter("timeDifference", timestamp - runTimeout)
          .getResultList()
          .stream()
          .collect(Collectors.toList());
    } catch (RuntimeException x) {
      throw new ServerException(x.getLocalizedMessage(), x);
    }
  }

  @Override
  @Transactional(rollbackOn = ServerException.class)
  public List<String> findExpiredIdle(long timestamp) throws ServerException {
    try {
      return managerProvider
          .get()
          .createNamedQuery("WorkspaceActivity.getExpiredIdle", String.class)
          .setParameter("expiration", timestamp)
          .getResultList()
          .stream()
          .collect(Collectors.toList());
    } catch (RuntimeException x) {
      throw new ServerException(x.getLocalizedMessage(), x);
    }
  }

  @Override
  public void removeActivity(String workspaceId) throws ServerException {
    try {
      doRemove(workspaceId);
    } catch (RuntimeException x) {
      throw new ServerException(x.getMessage(), x);
    }
  }

  @Transactional
  void doRemove(String workspaceId) {
    EntityManager em = managerProvider.get();
    WorkspaceActivity activity = em.find(WorkspaceActivity.class, workspaceId);
    if (activity != null) {
      em.remove(activity);
      em.flush();
    }
  }

  @Override
  public void setCreatedTime(String workspaceId, long createdTimestamp) throws ServerException {
    requireNonNull(workspaceId, "Required non-null workspace id");
    doUpdate(
        workspaceId,
        a -> {
          a.setCreated(createdTimestamp);

          // We might just have created the activity record and we need to initialize the status
          // to something. Since a created workspace is implicitly stopped, let's record it like
          // that.
          // If any status change event was already captured, the status would have been set
          // accordingly already.
          if (a.getStatus() == null) {
            a.setStatus(WorkspaceStatus.STOPPED);
            a.setLastStopped(createdTimestamp);
          }
        });
  }

  @Override
  public void setStatusChangeTime(String workspaceId, WorkspaceStatus status, long timestamp)
      throws ServerException {
    requireNonNull(workspaceId, "Required non-null workspace id");

    Consumer<WorkspaceActivity> update;
    switch (status) {
      case RUNNING:
        update =
            a -> {
              a.setStatus(status);
              a.setLastRunning(timestamp);
            };
        break;
      case STARTING:
        update =
            a -> {
              a.setStatus(status);
              a.setLastStarting(timestamp);
            };
        break;
      case STOPPED:
        update =
            a -> {
              a.setStatus(status);
              a.setLastStopped(timestamp);
            };
        break;
      case STOPPING:
        update =
            a -> {
              a.setStatus(status);
              a.setLastStopping(timestamp);
            };
        break;
      default:
        throw new ServerException("Unhandled workspace status: " + status);
    }

    doUpdate(workspaceId, update);
  }

  @Override
  @Transactional(rollbackOn = ServerException.class)
  public Page<String> findInStatusSince(
      long timestamp, WorkspaceStatus status, int maxItems, long skipCount) throws ServerException {
    try {
      String queryName = "WorkspaceActivity.get" + firstUpperCase(status.name()) + "Since";
      String countQueryName = queryName + "Count";

      long count =
          managerProvider
              .get()
              .createNamedQuery(countQueryName, Long.class)
              .setParameter("time", timestamp)
              .getSingleResult();

      List<String> data =
          managerProvider
              .get()
              .createNamedQuery(queryName, String.class)
              .setParameter("time", timestamp)
              .setFirstResult((int) skipCount)
              .setMaxResults(maxItems)
              .getResultList();

      return new Page<>(data, skipCount, maxItems, count);
    } catch (RuntimeException e) {
      throw new ServerException(e.getMessage(), e);
    }
  }

  @Override
  @Transactional(rollbackOn = ServerException.class)
  public long countWorkspacesInStatus(WorkspaceStatus status, long timestamp)
      throws ServerException {
    try {
      String queryName = "WorkspaceActivity.get" + firstUpperCase(status.name()) + "SinceCount";

      return managerProvider
          .get()
          .createNamedQuery(queryName, Long.class)
          .setParameter("time", timestamp)
          .getSingleResult();
    } catch (RuntimeException e) {
      throw new ServerException(e.getMessage(), e);
    }
  }

  @Override
  @Transactional(rollbackOn = ServerException.class)
  public WorkspaceActivity findActivity(String workspaceId) throws ServerException {
    try {
      EntityManager em = managerProvider.get();
      return em.find(WorkspaceActivity.class, workspaceId);
    } catch (RuntimeException x) {
      throw new ServerException(x.getLocalizedMessage(), x);
    }
  }

  @Override
  @Transactional(rollbackOn = ServerException.class)
  public Page<WorkspaceActivity> getAll(int maxItems, long skipCount) throws ServerException {
    try {
      EntityManager em = managerProvider.get();
      long total =
          em.createNamedQuery("WorkspaceActivity.getAllCount", Long.class).getSingleResult();

      List<WorkspaceActivity> page =
          em.createNamedQuery("WorkspaceActivity.getAll", WorkspaceActivity.class)
              .setFirstResult((int) skipCount)
              .setMaxResults(maxItems)
              .getResultStream()
              .collect(Collectors.toList());

      return new Page<>(page, skipCount, maxItems, total);
    } catch (RuntimeException e) {
      throw new ServerException(e.getMessage(), e);
    }
  }

  @Override
  public void createActivity(WorkspaceActivity activity) throws ConflictException, ServerException {
    try {
      doCreate(activity);
    } catch (IntegrityConstraintViolationException e) {
      throw new ServerException(
          String.format(
              "Can not create activity record since the specified workspace with "
                  + "id '%s' does not exist.",
              activity.getWorkspaceId()),
          e);
    } catch (DuplicateKeyException e) {
      throw new ConflictException(
          "Activity record for workspace ID " + activity.getWorkspaceId() + " already exists.", e);
    } catch (RuntimeException e) {
      throw new ServerException(e.getMessage(), e);
    }
  }

  @Transactional
  void doCreate(WorkspaceActivity activity) {
    EntityManager em = managerProvider.get();
    em.persist(activity);
    em.flush();
  }

  private void doUpdate(String workspaceId, Consumer<WorkspaceActivity> updater)
      throws ServerException {
    try {
      doUpdate(false, workspaceId, updater);
    } catch (IntegrityConstraintViolationException e) {
      throw new ServerException(
          String.format(
              "Can not create activity record since the specified workspace with "
                  + "id '%s' does not exist.",
              workspaceId),
          e);
    } catch (RuntimeException x) {
      throw new ServerException(x.getMessage(), x);
    }
  }

  private void doUpdateOptionally(String workspaceId, Consumer<WorkspaceActivity> updater)
      throws ServerException {
    try {
      doUpdate(true, workspaceId, updater);
    } catch (IntegrityConstraintViolationException e) {
      throw new ServerException(
          String.format(
              "Can not create activity record since the specified workspace with "
                  + "id '%s' does not exist.",
              workspaceId),
          e);
    } catch (RuntimeException x) {
      throw new ServerException(x.getMessage(), x);
    }
  }

  @Transactional
  void doUpdate(boolean optional, String workspaceId, Consumer<WorkspaceActivity> updater) {
    EntityManager em = managerProvider.get();
    WorkspaceActivity activity = em.find(WorkspaceActivity.class, workspaceId);
    if (activity == null) {
      if (optional) {
        return;
      }
      activity = new WorkspaceActivity();
      activity.setWorkspaceId(workspaceId);

      updater.accept(activity);

      em.persist(activity);
    } else {
      updater.accept(activity);

      em.merge(activity);
    }

    em.flush();
  }

  private static String firstUpperCase(String str) {
    return Character.toUpperCase(str.charAt(0)) + str.substring(1).toLowerCase();
  }
}
