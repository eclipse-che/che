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

import static java.lang.String.format;
import static java.util.stream.Collectors.toMap;

import com.google.inject.persist.Transactional;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import org.eclipse.che.api.core.model.workspace.runtime.MachineStatus;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.model.workspace.runtime.ServerStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.core.db.cascade.CascadeEventSubscriber;
import org.eclipse.che.core.db.jpa.DuplicateKeyException;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.BeforeKubernetesRuntimeStateRemovedEvent;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.KubernetesMachineCache;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesMachineImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesMachineImpl.MachineId;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesServerImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesServerImpl.ServerId;

/**
 * JPA based implementation of {@link KubernetesMachineCache}.
 *
 * @author Sergii Leshchenko
 */
public class JpaKubernetesMachineCache implements KubernetesMachineCache {

  private final Provider<EntityManager> managerProvider;

  @Inject
  public JpaKubernetesMachineCache(Provider<EntityManager> managerProvider) {
    this.managerProvider = managerProvider;
  }

  @Override
  public void put(RuntimeIdentity runtimeIdentity, KubernetesMachineImpl machine)
      throws InfrastructureException {
    try {
      doPutMachine(machine);
    } catch (DuplicateKeyException e) {
      throw new InfrastructureException("Machine is already in cache", e);
    } catch (RuntimeException e) {
      throw new InfrastructureException(e.getMessage(), e);
    }
  }

  @Transactional(rollbackOn = InfrastructureException.class)
  @Override
  public Map<String, KubernetesMachineImpl> getMachines(RuntimeIdentity runtimeIdentity)
      throws InfrastructureException {
    try {
      return managerProvider
          .get()
          .createNamedQuery("KubernetesMachine.getByWorkspaceId", KubernetesMachineImpl.class)
          .setParameter("workspaceId", runtimeIdentity.getWorkspaceId())
          .getResultList()
          .stream()
          .collect(toMap(KubernetesMachineImpl::getName, Function.identity()));
    } catch (RuntimeException e) {
      throw new InfrastructureException(e.getMessage(), e);
    }
  }

  @Transactional(rollbackOn = InfrastructureException.class)
  @Override
  public KubernetesServerImpl getServer(
      RuntimeIdentity runtimeIdentity, String machineName, String serverName)
      throws InfrastructureException {
    try {
      String workspaceId = runtimeIdentity.getWorkspaceId();
      KubernetesServerImpl server =
          managerProvider
              .get()
              .find(KubernetesServerImpl.class, new ServerId(workspaceId, machineName, serverName));
      if (server == null) {
        throw new InfrastructureException(
            format("Server with name '%s' was not found", serverName));
      }
      return server;
    } catch (RuntimeException e) {
      throw new InfrastructureException(e.getMessage(), e);
    }
  }

  @Override
  public void updateMachineStatus(
      RuntimeIdentity runtimeIdentity, String machineName, MachineStatus newStatus)
      throws InfrastructureException {
    try {
      doUpdateMachineStatus(runtimeIdentity.getWorkspaceId(), machineName, newStatus);
    } catch (RuntimeException e) {
      throw new InfrastructureException(e.getMessage(), e);
    }
  }

  @Override
  public boolean updateServerStatus(
      RuntimeIdentity runtimeIdentity,
      String machineName,
      String serverName,
      ServerStatus newStatus)
      throws InfrastructureException {
    try {
      return doUpdateServerStatus(runtimeIdentity, machineName, serverName, newStatus);
    } catch (RuntimeException e) {
      throw new InfrastructureException(e.getMessage(), e);
    }
  }

  @Override
  public void remove(RuntimeIdentity runtimeIdentity) throws InfrastructureException {
    try {
      doRemove(runtimeIdentity);
    } catch (RuntimeException e) {
      throw new InfrastructureException(e.getMessage(), e);
    }
  }

  @Transactional(rollbackOn = {RuntimeException.class, InfrastructureException.class})
  protected void doRemove(RuntimeIdentity runtimeIdentity) throws InfrastructureException {
    EntityManager em = managerProvider.get();

    Collection<KubernetesMachineImpl> machines = getMachines(runtimeIdentity).values();

    for (KubernetesMachineImpl machine : machines) {
      em.remove(machine);
    }

    em.flush();
  }

  @Transactional
  protected void doPutMachine(KubernetesMachineImpl machine) {
    EntityManager em = managerProvider.get();
    em.persist(machine);
    em.flush();
  }

  @Transactional
  protected void doUpdateMachineStatus(String workspaceId, String machineName, MachineStatus status)
      throws InfrastructureException {
    EntityManager entityManager = managerProvider.get();

    KubernetesMachineImpl machine =
        entityManager.find(KubernetesMachineImpl.class, new MachineId(workspaceId, machineName));

    if (machine == null) {
      throw new InfrastructureException(
          format("Machine '%s:%s' was not found", workspaceId, machineName));
    }

    machine.setStatus(status);

    entityManager.flush();
  }

  @Transactional(rollbackOn = {RuntimeException.class, InfrastructureException.class})
  protected boolean doUpdateServerStatus(
      RuntimeIdentity runtimeIdentity, String machineName, String serverName, ServerStatus status)
      throws InfrastructureException {
    EntityManager entityManager = managerProvider.get();

    KubernetesServerImpl server = getServer(runtimeIdentity, machineName, serverName);

    if (server.getStatus() != status) {
      server.setStatus(status);
      entityManager.flush();
      return true;
    }
    return false;
  }

  @Singleton
  public static class RemoveKubernetesMachinesBeforeRuntimesRemoved
      extends CascadeEventSubscriber<BeforeKubernetesRuntimeStateRemovedEvent> {

    @Inject private EventService eventService;
    @Inject private JpaKubernetesMachineCache k8sMachines;

    @PostConstruct
    public void subscribe() {
      eventService.subscribe(this, BeforeKubernetesRuntimeStateRemovedEvent.class);
    }

    @Override
    public void onCascadeEvent(BeforeKubernetesRuntimeStateRemovedEvent event) throws Exception {
      k8sMachines.remove(event.getRuntimeState().getRuntimeId());
    }
  }
}
