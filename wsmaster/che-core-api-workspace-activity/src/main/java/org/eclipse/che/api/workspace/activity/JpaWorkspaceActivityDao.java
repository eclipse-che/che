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
package org.eclipse.che.api.workspace.activity;

import static java.util.Objects.requireNonNull;

import com.google.inject.persist.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import org.eclipse.che.api.core.ServerException;

/**
 * JPA workspaces expiration times storage.
 *
 * @author Max Shaposhnik (mshaposh@redhat.com)
 */
@Singleton
public class JpaWorkspaceActivityDao implements WorkspaceActivityDao {

  @Inject private Provider<EntityManager> managerProvider;

  @Override
  public void setExpiration(WorkspaceExpiration expiration) throws ServerException {
    requireNonNull(expiration, "Required non-null expiration object");
    try {
      doCreateOrUpdate(expiration);
    } catch (RuntimeException x) {
      throw new ServerException(x.getLocalizedMessage(), x);
    }
  }

  @Override
  public void removeExpiration(String workspaceId) throws ServerException {
    requireNonNull(workspaceId, "Required non-null id");
    try {
      doRemove(workspaceId);
    } catch (RuntimeException x) {
      throw new ServerException(x.getLocalizedMessage(), x);
    }
  }

  @Override
  public List<String> findExpired(long timestamp) throws ServerException {
    try {
      return doFindExpired(timestamp);
    } catch (RuntimeException x) {
      throw new ServerException(x.getLocalizedMessage(), x);
    }
  }

  @Transactional
  protected List<String> doFindExpired(long timestamp) {
    return managerProvider
        .get()
        .createNamedQuery("WorkspaceExpiration.getExpired", WorkspaceExpiration.class)
        .setParameter("expiration", timestamp)
        .getResultList()
        .stream()
        .map(WorkspaceExpiration::getWorkspaceId)
        .collect(Collectors.toList());
  }

  @Transactional
  protected void doCreateOrUpdate(WorkspaceExpiration expiration) {
    final EntityManager manager = managerProvider.get();
    if (manager.find(WorkspaceExpiration.class, expiration.getWorkspaceId()) == null) {
      manager.persist(expiration);
    } else {
      manager.merge(expiration);
    }
    manager.flush();
  }

  @Transactional
  protected void doRemove(String workspaceId) {
    final EntityManager manager = managerProvider.get();
    final WorkspaceExpiration expiration = manager.find(WorkspaceExpiration.class, workspaceId);
    if (expiration != null) {
      manager.remove(expiration);
      manager.flush();
    }
  }
}
