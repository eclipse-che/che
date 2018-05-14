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
package org.eclipse.che.workspace.infrastructure.kubernetes.cache.jpa;

import com.google.inject.persist.Transactional;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.core.db.jpa.DuplicateKeyException;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.KubernetesRuntimeStateCache;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesRuntimeState;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesRuntimeState.RuntimeId;

/**
 * JPA based implementation of {@link KubernetesRuntimeStateCache}.
 *
 * @author Sergii Leshchenko
 */
public class JpaKubernetesRuntimeStateCache implements KubernetesRuntimeStateCache {

  private final Provider<EntityManager> managerProvider;

  @Inject
  public JpaKubernetesRuntimeStateCache(Provider<EntityManager> managerProvider) {
    this.managerProvider = managerProvider;
  }

  @Override
  public boolean putIfAbsent(KubernetesRuntimeState runtimeState) throws InfrastructureException {
    try {
      doPutIfAbsent(runtimeState);
      return true;
    } catch (DuplicateKeyException | EntityExistsException e) {
      return false;
    } catch (RuntimeException e) {
      throw new InfrastructureException(e.getMessage(), e);
    }
  }

  @Transactional(rollbackOn = InfrastructureException.class)
  @Override
  public Set<RuntimeIdentity> getIdentities() throws InfrastructureException {
    try {
      return managerProvider
          .get()
          .createNamedQuery("KubernetesRuntime.getAll", KubernetesRuntimeState.class)
          .getResultList()
          .stream()
          .map(KubernetesRuntimeState::getRuntimeId)
          .collect(Collectors.toSet());
    } catch (RuntimeException x) {
      throw new InfrastructureException(x.getMessage(), x);
    }
  }

  @Transactional(rollbackOn = InfrastructureException.class)
  @Override
  public WorkspaceStatus getStatus(RuntimeIdentity id) throws InfrastructureException {
    try {
      Optional<KubernetesRuntimeState> runtimeStateOpt = get(id);

      if (!runtimeStateOpt.isPresent()) {
        throw new InfrastructureException(
            "Runtime state for workspace with id '" + id.getWorkspaceId() + "' was not found");
      }

      return runtimeStateOpt.get().getStatus();
    } catch (RuntimeException x) {
      throw new InfrastructureException(x.getMessage(), x);
    }
  }

  @Transactional(rollbackOn = InfrastructureException.class)
  @Override
  public Optional<KubernetesRuntimeState> get(RuntimeIdentity runtimeId)
      throws InfrastructureException {
    try {
      return Optional.ofNullable(
          managerProvider.get().find(KubernetesRuntimeState.class, new RuntimeId(runtimeId)));
    } catch (RuntimeException x) {
      throw new InfrastructureException(x.getMessage(), x);
    }
  }

  @Override
  public void updateStatus(RuntimeIdentity runtimeId, WorkspaceStatus newStatus)
      throws InfrastructureException {
    try {
      doUpdateStatus(runtimeId, newStatus);
    } catch (RuntimeException x) {
      throw new InfrastructureException(x.getMessage(), x);
    }
  }

  @Override
  public boolean updateStatus(
      RuntimeIdentity identity, Predicate<WorkspaceStatus> predicate, WorkspaceStatus newStatus)
      throws InfrastructureException {
    try {
      doUpdateStatus(identity, predicate, newStatus);
      return true;
    } catch (IllegalStateException e) {
      return false;
    } catch (RuntimeException e) {
      throw new InfrastructureException(e.getMessage(), e);
    }
  }

  @Override
  public void remove(RuntimeIdentity runtimeId) throws InfrastructureException {
    try {
      doRemove(runtimeId);
    } catch (RuntimeException x) {
      throw new InfrastructureException(x.getMessage(), x);
    }
  }

  @Transactional
  protected void doRemove(RuntimeIdentity runtimeIdentity) {
    EntityManager em = managerProvider.get();

    KubernetesRuntimeState runtime =
        em.find(KubernetesRuntimeState.class, new RuntimeId(runtimeIdentity));

    if (runtime != null) {
      em.remove(runtime);
    }
  }

  @Transactional(rollbackOn = {RuntimeException.class, InfrastructureException.class})
  protected void doUpdateStatus(RuntimeIdentity id, WorkspaceStatus status)
      throws InfrastructureException {
    Optional<KubernetesRuntimeState> runtimeStateOpt = get(id);

    if (!runtimeStateOpt.isPresent()) {
      throw new InfrastructureException(
          "Runtime state for workspace with id '" + id.getWorkspaceId() + "' was not found");
    }

    runtimeStateOpt.get().setStatus(status);

    managerProvider.get().flush();
  }

  @Transactional(rollbackOn = {RuntimeException.class, InfrastructureException.class})
  protected void doUpdateStatus(
      RuntimeIdentity id, Predicate<WorkspaceStatus> predicate, WorkspaceStatus newStatus)
      throws InfrastructureException {
    EntityManager entityManager = managerProvider.get();

    Optional<KubernetesRuntimeState> existingStateOpt = get(id);

    if (!existingStateOpt.isPresent()) {
      throw new InfrastructureException(
          "Runtime state for workspace with id '" + id.getWorkspaceId() + "' was not found");
    }

    KubernetesRuntimeState existingState = existingStateOpt.get();
    if (!predicate.test(existingState.getStatus())) {
      throw new IllegalStateException("Runtime status doesn't match to the specified predicate");
    }

    existingState.setStatus(newStatus);
    entityManager.flush();
  }

  @Transactional
  protected void doPutIfAbsent(KubernetesRuntimeState runtimeState) {
    EntityManager em = managerProvider.get();
    em.persist(runtimeState);
    em.flush();
  }
}
