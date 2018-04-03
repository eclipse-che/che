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

import static java.util.stream.Collectors.toMap;

import com.google.inject.persist.Transactional;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import org.eclipse.che.api.core.model.workspace.runtime.MachineStatus;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.model.workspace.runtime.ServerStatus;
import org.eclipse.che.api.workspace.server.model.impl.ServerImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.core.db.jpa.DuplicateKeyException;
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
  public void delete(RuntimeIdentity runtimeIdentity) throws InfrastructureException {
    try {
      doRemove(runtimeIdentity);
    } catch (RuntimeException e) {
      throw new InfrastructureException(e.getMessage(), e);
    }
  }

  @Override
  public Map<String, KubernetesMachineImpl> getMachines(RuntimeIdentity runtimeIdentity)
      throws InfrastructureException {
    return doGetMachines(runtimeIdentity)
        .stream()
        .collect(toMap(KubernetesMachineImpl::getName, Function.identity()));
  }

  @Override
  public void add(RuntimeIdentity runtimeIdentity, KubernetesMachineImpl machine)
      throws InfrastructureException {
    try {
      doAddMachine(machine);
    } catch (DuplicateKeyException e) {
      // TODO
      throw new InfrastructureException("Machine is already is cache", e);
    } catch (RuntimeException e) {
      throw new InfrastructureException(e.getMessage(), e);
    }
  }

  @Override
  public void updateMachineStatus(
      RuntimeIdentity runtimeIdentity, String machineName, MachineStatus status)
      throws InfrastructureException {
    try {
      doUpdateMachineStatus(runtimeIdentity.getWorkspaceId(), machineName, status);
    } catch (RuntimeException e) {
      throw new InfrastructureException(e.getMessage(), e);
    }
  }

  @Override
  public boolean updateServerStatus(
      RuntimeIdentity runtimeIdentity, String machineName, String serverName, ServerStatus status)
      throws InfrastructureException {
    try {
      return doUpdateServerStatus(
          runtimeIdentity.getWorkspaceId(), machineName, serverName, status);
    } catch (RuntimeException e) {
      throw new InfrastructureException(e.getMessage(), e);
    }
  }

  @Transactional(rollbackOn = {RuntimeException.class, InfrastructureException.class})
  @Override
  public ServerImpl getServer(
      RuntimeIdentity runtimeIdentity, String machineName, String serverName)
      throws InfrastructureException {
    try {
      KubernetesServerImpl s =
          managerProvider
              .get()
              .find(
                  KubernetesServerImpl.class,
                  new ServerId(runtimeIdentity.getWorkspaceId(), machineName, serverName));
      return new ServerImpl(s.getUrl(), s.getStatus(), s.getAttributes());
    } catch (RuntimeException e) {
      throw new InfrastructureException(e.getMessage(), e);
    }
  }

  @Transactional
  protected void doRemove(RuntimeIdentity runtimeIdentity) {
    EntityManager em = managerProvider.get();

    List<KubernetesMachineImpl> machines =
        em.createQuery(
                "SELECT m FROM KubernetesMachine m WHERE m.machineId.workspaceId = :workspaceId",
                KubernetesMachineImpl.class)
            .setParameter("workspaceId", runtimeIdentity.getWorkspaceId())
            .getResultList();

    for (KubernetesMachineImpl m : machines) {
      KubernetesMachineImpl remove =
          em.find(KubernetesMachineImpl.class, new MachineId(m.getWorkspaceId(), m.getName()));

      em.remove(remove);
    }

    em.flush();
  }

  @Transactional
  protected void doAddMachine(KubernetesMachineImpl machineMeta) {
    EntityManager em = managerProvider.get();
    em.persist(machineMeta);
    em.flush();
  }

  @Transactional(rollbackOn = {RuntimeException.class, InfrastructureException.class})
  protected List<KubernetesMachineImpl> doGetMachines(RuntimeIdentity runtimeIdentity)
      throws InfrastructureException {
    try {
      return managerProvider
          .get()
          .createQuery(
              "SELECT m FROM KubernetesMachine m WHERE m.machineId.workspaceId = :workspaceId",
              KubernetesMachineImpl.class)
          .setParameter("workspaceId", runtimeIdentity.getWorkspaceId())
          .getResultList();
    } catch (RuntimeException e) {
      throw new InfrastructureException(e.getMessage(), e);
    }
  }

  @Transactional(rollbackOn = {RuntimeException.class, InfrastructureException.class})
  protected void doUpdateMachineStatus(String workspaceId, String machineName, MachineStatus status)
      throws InfrastructureException {
    EntityManager entityManager = managerProvider.get();

    KubernetesMachineImpl meta =
        entityManager.find(KubernetesMachineImpl.class, new MachineId(workspaceId, machineName));

    if (meta == null) {
      throw new InfrastructureException("Can't update machine status");
    }

    meta.setStatus(status);

    entityManager.flush();
  }

  @Transactional
  protected boolean doUpdateServerStatus(
      String workspaceId, String machineName, String serverName, ServerStatus status) {
    EntityManager entityManager = managerProvider.get();

    KubernetesServerImpl meta =
        entityManager.find(
            KubernetesServerImpl.class, new ServerId(workspaceId, machineName, serverName));

    if (meta.getStatus() != status) {
      meta.setStatus(status);

      entityManager.flush();
      return true;
    }
    return false;
  }
}
