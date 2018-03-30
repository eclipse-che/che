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
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.jpa.entity.KubernetesRuntimeEntity;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.jpa.entity.KubernetesRuntimeEntity.Id;

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
          .createQuery("SELECT r FROM KubernetesRuntime r", KubernetesRuntimeEntity.class)
          .getResultList()
          .stream()
          .map(KubernetesRuntimeEntity::getRuntimeId)
          .collect(Collectors.toSet());
    } catch (RuntimeException x) {
      throw new InfrastructureException(x.getLocalizedMessage(), x);
    }
  }

  @Override
  public boolean initStatus(RuntimeIdentity identity, String namespace, WorkspaceStatus newStatus)
      throws InfrastructureException {
    try {
      doInitStatus(identity, namespace, newStatus);
      return true;
    } catch (DuplicateKeyException | EntityExistsException e) {
      return false;
    } catch (RuntimeException e) {
      throw new InfrastructureException(e.getMessage(), e);
    }
  }

  @Override
  public void updateStatus(RuntimeIdentity runtimeIdentity, WorkspaceStatus status)
      throws InfrastructureException {
    try {
      doUpdateStatus(runtimeIdentity, status);
    } catch (RuntimeException x) {
      throw new InfrastructureException(x.getLocalizedMessage(), x);
    }
  }

  @Override
  public void delete(RuntimeIdentity runtimeIdentity) throws InfrastructureException {
    try {
      doDelete(runtimeIdentity);
    } catch (RuntimeException x) {
      throw new InfrastructureException(x.getLocalizedMessage(), x);
    }
  }

  @Transactional(rollbackOn = {RuntimeException.class, InfrastructureException.class})
  @Override
  public WorkspaceStatus getStatus(RuntimeIdentity identity) throws InfrastructureException {
    try {
      return managerProvider
          .get()
          .find(KubernetesRuntimeEntity.class, new KubernetesRuntimeEntity.Id(identity))
          .getStatus();
    } catch (RuntimeException x) {
      throw new InfrastructureException(x.getLocalizedMessage(), x);
    }
  }

  @Override
  public boolean replaceStatus(
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
  protected void doDelete(RuntimeIdentity runtimeIdentity) {
    EntityManager em = managerProvider.get();

    KubernetesRuntimeEntity runtime =
        em.find(KubernetesRuntimeEntity.class, new Id(runtimeIdentity));

    if (runtime != null) {
      em.remove(runtime);
    }
  }

  @Transactional
  protected void doUpdateStatus(RuntimeIdentity runtimeIdentity, WorkspaceStatus status) {
    EntityManager entityManager = managerProvider.get();

    KubernetesRuntimeEntity meta =
        entityManager.find(
            KubernetesRuntimeEntity.class, new KubernetesRuntimeEntity.Id(runtimeIdentity));

    meta.setStatus(status);

    entityManager.flush();
  }

  @Transactional(rollbackOn = IllegalStateException.class)
  protected void doReplaceStatus(
      RuntimeIdentity identity, Predicate<WorkspaceStatus> predicate, WorkspaceStatus newStatus)
      throws IllegalStateException {
    EntityManager entityManager = managerProvider.get();
    KubernetesRuntimeEntity kubernetesRuntimeEntity =
        entityManager.find(KubernetesRuntimeEntity.class, new KubernetesRuntimeEntity.Id(identity));

    KubernetesRuntimeEntity kubernetesMachineMeta =
        new KubernetesRuntimeEntity(kubernetesRuntimeEntity).withStatus(newStatus);

    KubernetesRuntimeEntity old = entityManager.merge(kubernetesMachineMeta);

    if (predicate.test(old.getStatus())) {
      throw new IllegalStateException(
          "Runtime state is not corresponding to the specified predicate");
    }
  }

  @Transactional
  protected void doInitStatus(RuntimeIdentity identity, String namespace, WorkspaceStatus status) {
    managerProvider
        .get()
        .persist(
            new KubernetesRuntimeEntity(
                new KubernetesRuntimeEntity.Id(identity), namespace, status));
  }
}
