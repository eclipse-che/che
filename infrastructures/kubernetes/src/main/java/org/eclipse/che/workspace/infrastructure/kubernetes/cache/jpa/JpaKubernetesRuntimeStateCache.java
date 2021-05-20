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
package org.eclipse.che.workspace.infrastructure.kubernetes.cache.jpa;

import static java.lang.String.format;
import static java.util.Collections.emptyList;

import com.google.inject.persist.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.model.workspace.config.Command;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.event.BeforeWorkspaceRemovedEvent;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.core.db.cascade.CascadeEventSubscriber;
import org.eclipse.che.core.db.jpa.DuplicateKeyException;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.BeforeKubernetesRuntimeStateRemovedEvent;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.KubernetesRuntimeStateCache;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesRuntimeCommandImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesRuntimeState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JPA based implementation of {@link KubernetesRuntimeStateCache}.
 *
 * @author Sergii Leshchenko
 */
public class JpaKubernetesRuntimeStateCache implements KubernetesRuntimeStateCache {
  private static final Logger LOG = LoggerFactory.getLogger(JpaKubernetesRuntimeStateCache.class);

  private final Provider<EntityManager> managerProvider;
  private final EventService eventService;

  @Inject
  public JpaKubernetesRuntimeStateCache(
      Provider<EntityManager> managerProvider, EventService eventService) {
    this.managerProvider = managerProvider;
    this.eventService = eventService;
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
  public Optional<WorkspaceStatus> getStatus(RuntimeIdentity id) throws InfrastructureException {
    try {
      Optional<KubernetesRuntimeState> runtimeStateOpt = get(id);
      return runtimeStateOpt.map(KubernetesRuntimeState::getStatus);
    } catch (RuntimeException x) {
      throw new InfrastructureException(x.getMessage(), x);
    }
  }

  @Override
  public List<? extends Command> getCommands(RuntimeIdentity runtimeId)
      throws InfrastructureException {
    Optional<KubernetesRuntimeState> k8sRuntimeState = get(runtimeId);
    if (k8sRuntimeState.isPresent()) {
      return k8sRuntimeState.get().getCommands();
    } else {
      // runtime is not started yet
      return emptyList();
    }
  }

  @Transactional(rollbackOn = InfrastructureException.class)
  @Override
  public Optional<KubernetesRuntimeState> get(RuntimeIdentity runtimeId)
      throws InfrastructureException {
    try {
      return Optional.ofNullable(
          managerProvider.get().find(KubernetesRuntimeState.class, runtimeId.getWorkspaceId()));
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
  public void updateCommands(RuntimeIdentity identity, List<? extends Command> commands)
      throws InfrastructureException {
    try {
      doUpdateCommands(
          identity,
          commands.stream().map(KubernetesRuntimeCommandImpl::new).collect(Collectors.toList()));
    } catch (RuntimeException e) {
      throw new InfrastructureException(e.getMessage(), e);
    }
  }

  @Override
  public void remove(RuntimeIdentity runtimeId) throws InfrastructureException {
    try {
      doRemove(runtimeId);
    } catch (ServerException | RuntimeException x) {
      throw new InfrastructureException(x.getMessage(), x);
    }
  }

  @Transactional(rollbackOn = InfrastructureException.class)
  protected Optional<KubernetesRuntimeState> find(String workspaceId)
      throws InfrastructureException {
    try {
      return Optional.ofNullable(
          managerProvider.get().find(KubernetesRuntimeState.class, workspaceId));
    } catch (RuntimeException x) {
      throw new InfrastructureException(x.getMessage(), x);
    }
  }

  @Transactional(rollbackOn = {RuntimeException.class, ServerException.class})
  protected void doRemove(RuntimeIdentity runtimeIdentity) throws ServerException {
    EntityManager em = managerProvider.get();

    KubernetesRuntimeState runtime =
        em.find(KubernetesRuntimeState.class, runtimeIdentity.getWorkspaceId());

    if (runtime != null) {
      eventService
          .publish(
              new BeforeKubernetesRuntimeStateRemovedEvent(new KubernetesRuntimeState(runtime)))
          .propagateException();

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

  @Transactional(rollbackOn = {RuntimeException.class, InfrastructureException.class})
  protected void doUpdateCommands(RuntimeIdentity id, List<KubernetesRuntimeCommandImpl> commands)
      throws InfrastructureException {
    Optional<KubernetesRuntimeState> runtimeStateOpt = get(id);

    if (!runtimeStateOpt.isPresent()) {
      throw new InfrastructureException(
          "Runtime state for workspace with id '" + id.getWorkspaceId() + "' was not found");
    }

    runtimeStateOpt.get().setCommands(commands);

    managerProvider.get().flush();
  }

  @Transactional
  protected void doPutIfAbsent(KubernetesRuntimeState runtimeState) {
    EntityManager em = managerProvider.get();
    em.persist(runtimeState);
    em.flush();
  }

  @Singleton
  public static class RemoveKubernetesRuntimeBeforeWorkspaceRemoved
      extends CascadeEventSubscriber<BeforeWorkspaceRemovedEvent> {

    @Inject private EventService eventService;
    @Inject private JpaKubernetesRuntimeStateCache k8sRuntimes;

    @PostConstruct
    public void subscribe() {
      eventService.subscribe(this, BeforeWorkspaceRemovedEvent.class);
    }

    @Override
    public void onCascadeEvent(BeforeWorkspaceRemovedEvent event) throws Exception {
      Optional<KubernetesRuntimeState> k8sRuntimeStateOpt =
          k8sRuntimes.find(event.getWorkspace().getId());
      if (k8sRuntimeStateOpt.isPresent()) {
        KubernetesRuntimeState existingK8sRuntimeState = k8sRuntimeStateOpt.get();
        RuntimeIdentity runtimeId = existingK8sRuntimeState.getRuntimeId();

        // It is not normal case when non STOPPED workspace is going to be removed.
        // Need to log error to investigate why it may happen
        // and clean up existing runtime not to lock removing of workspace.
        LOG.error(
            format(
                "Workspace is being removed while Kubernetes runtime state '%s:%s:%s' exists. "
                    + "This situation indicates a bug that needs to be reported. Runtime state "
                    + "will be removed from DB, but Kubernetes resources (pods, pvcs, etc.) "
                    + "won't be cleaned up.",
                runtimeId.getWorkspaceId(), runtimeId.getEnvName(), runtimeId.getOwnerId()));
        k8sRuntimes.remove(runtimeId);
      }
    }
  }
}
