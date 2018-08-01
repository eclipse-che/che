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

import static java.util.Objects.requireNonNull;

import com.google.inject.persist.Transactional;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.multiuser.api.permission.server.AbstractPermissionsDomain;
import org.eclipse.che.multiuser.api.permission.server.model.impl.AbstractPermissions;
import org.eclipse.che.multiuser.api.permission.server.spi.PermissionsDao;

/**
 * Basic JPA DAO implementation for {@link Permissions} objects.
 *
 * @author Max Shaposhnik
 */
public abstract class AbstractJpaPermissionsDao<T extends AbstractPermissions>
    implements PermissionsDao<T> {

  private final AbstractPermissionsDomain<T> supportedDomain;

  @Inject protected Provider<EntityManager> managerProvider;

  public AbstractJpaPermissionsDao(AbstractPermissionsDomain<T> supportedDomain) {
    this.supportedDomain = supportedDomain;
  }

  @Override
  public AbstractPermissionsDomain<T> getDomain() {
    return supportedDomain;
  }

  @Override
  public Optional<T> store(T permissions) throws ServerException {
    requireNonNull(permissions, "Permissions instance required");
    try {
      return doCreate(permissions);
    } catch (RuntimeException e) {
      throw new ServerException(e.getMessage(), e);
    }
  }

  @Override
  @Transactional
  public boolean exists(String userId, String instanceId, String action) throws ServerException {
    requireNonNull(userId, "User identifier required");
    requireNonNull(action, "Action name required");
    T permissions;
    try {
      permissions = get(userId, instanceId);
    } catch (NotFoundException e) {
      return false;
    }
    return permissions.getActions().contains(action);
  }

  @Override
  public void remove(String userId, String instanceId) throws ServerException, NotFoundException {
    requireNonNull(instanceId, "Instance identifier required");
    requireNonNull(userId, "User identifier required");
    try {
      doRemove(userId, instanceId);
    } catch (RuntimeException x) {
      throw new ServerException(x.getLocalizedMessage(), x);
    }
  }

  @Override
  public abstract T get(String userId, String instanceId) throws ServerException, NotFoundException;

  @Override
  public abstract List<T> getByUser(String userId) throws ServerException;

  @Override
  public abstract Page<T> getByInstance(String instanceId, int maxItems, long skipCount)
      throws ServerException;

  /**
   * Must return jpa managed entity or throw {@link NotFoundException} when there is no such entity.
   * Parameters {@code userId} and {@code instanceId} are the same to {@link #get(String, String)}
   * method parameters.
   */
  protected abstract T getEntity(String userId, String instanceId) throws NotFoundException;

  @Transactional
  protected Optional<T> doCreate(T permissions) throws ServerException {
    EntityManager manager = managerProvider.get();
    try {
      final T result =
          getEntity(wildcardToNull(permissions.getUserId()), permissions.getInstanceId());
      final T existing =
          getDomain().newInstance(result.getUserId(), result.getInstanceId(), result.getActions());
      result.getActions().clear();
      result.getActions().addAll(permissions.getActions());
      manager.flush();
      return Optional.of(existing);
    } catch (NotFoundException n) {
      manager.persist(permissions);
      manager.flush();
      return Optional.empty();
    }
  }

  @Transactional
  protected void doRemove(String userId, String instanceId)
      throws ServerException, NotFoundException {
    final T entity = getEntity(wildcardToNull(userId), instanceId);
    EntityManager manager = managerProvider.get();
    manager.remove(entity);
    manager.flush();
  }

  /**
   * Converts '*' user wildcard to {@code null}
   *
   * @return {@code null} when user identifier equal to '*', either user identifier will be returned
   */
  public static String wildcardToNull(String userId) {
    return !"*".equals(userId) ? userId : null;
  }
}
