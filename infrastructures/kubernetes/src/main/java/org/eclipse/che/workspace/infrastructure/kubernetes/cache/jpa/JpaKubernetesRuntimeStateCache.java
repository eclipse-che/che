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

  @Transactional(rollbackOn = {RuntimeException.class, InfrastructureException.class})
  @Override
  public Set<RuntimeIdentity> getIdentities() throws InfrastructureException {
    try {
      return managerProvider
          .get()
          .createQuery("SELECT r FROM KubernetesRuntime r", KubernetesRuntimeState.class)
          .getResultList()
          .stream()
          .map(KubernetesRuntimeState::getRuntimeId)
          .collect(Collectors.toSet());
    } catch (RuntimeException x) {
      throw new InfrastructureException(x.getLocalizedMessage(), x);
    }
  }

  @Override
  public boolean put(KubernetesRuntimeState runtimeState) throws InfrastructureException {
    try {
      doInit(runtimeState);
      return true;
    } catch (DuplicateKeyException | EntityExistsException e) {
      return false;
    } catch (RuntimeException e) {
      throw new InfrastructureException(e.getMessage(), e);
    }
  }

  @Override
  public void updateStatus(RuntimeIdentity runtimeId, WorkspaceStatus newStatus)
      throws InfrastructureException {
    try {
      doUpdateStatus(runtimeId, newStatus);
    } catch (RuntimeException x) {
      throw new InfrastructureException(x.getLocalizedMessage(), x);
    }
  }

  @Override
  public void remove(RuntimeIdentity runtimeId) throws InfrastructureException {
    try {
      doRemove(runtimeId);
    } catch (RuntimeException x) {
      throw new InfrastructureException(x.getLocalizedMessage(), x);
    }
  }

  @Transactional(rollbackOn = {RuntimeException.class, InfrastructureException.class})
  @Override
  public WorkspaceStatus getStatus(RuntimeIdentity runtimeId) throws InfrastructureException {
    try {
      return managerProvider
          .get()
          .find(KubernetesRuntimeState.class, new RuntimeId(runtimeId))
          .getStatus();
    } catch (RuntimeException x) {
      throw new InfrastructureException(x.getLocalizedMessage(), x);
    }
  }

  @Transactional(rollbackOn = {RuntimeException.class, InfrastructureException.class})
  @Override
  public Optional<KubernetesRuntimeState> get(RuntimeIdentity runtimeId)
      throws InfrastructureException {
    try {
      return Optional.ofNullable(
          managerProvider.get().find(KubernetesRuntimeState.class, new RuntimeId(runtimeId)));
    } catch (RuntimeException x) {
      throw new InfrastructureException(x.getLocalizedMessage(), x);
    }
  }

  @Override
  public boolean updateStatus(
      RuntimeIdentity identity, Predicate<WorkspaceStatus> predicate, WorkspaceStatus newStatus)
      throws InfrastructureException {
    try {
      doReplaceStatus(identity, predicate, newStatus);
      return true;
    } catch (IllegalStateException e) {
      return false;
    } catch (RuntimeException e) {
      throw new InfrastructureException(e.getMessage(), e);
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

  @Transactional
  protected void doUpdateStatus(RuntimeIdentity runtimeIdentity, WorkspaceStatus status) {
    EntityManager entityManager = managerProvider.get();

    KubernetesRuntimeState meta =
        entityManager.find(KubernetesRuntimeState.class, new RuntimeId(runtimeIdentity));

    meta.setStatus(status);

    entityManager.flush();
  }

  @Transactional(rollbackOn = IllegalStateException.class)
  protected void doReplaceStatus(
      RuntimeIdentity identity, Predicate<WorkspaceStatus> predicate, WorkspaceStatus newStatus)
      throws IllegalStateException {
    EntityManager entityManager = managerProvider.get();
    KubernetesRuntimeState kubernetesRuntimeState =
        entityManager.find(KubernetesRuntimeState.class, new RuntimeId(identity));

    KubernetesRuntimeState kubernetesMachineMeta =
        new KubernetesRuntimeState(kubernetesRuntimeState).withStatus(newStatus);

    KubernetesRuntimeState old = entityManager.merge(kubernetesMachineMeta);

    if (predicate.test(old.getStatus())) {
      throw new IllegalStateException(
          "Runtime state is not corresponding to the specified predicate");
    }
  }

  @Transactional
  protected void doInit(KubernetesRuntimeState runtimeState) {
    EntityManager em = managerProvider.get();
    em.persist(runtimeState);
    em.flush();
  }
}
