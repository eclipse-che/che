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
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import org.eclipse.che.api.core.model.workspace.runtime.MachineStatus;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.model.workspace.runtime.ServerStatus;
import org.eclipse.che.api.workspace.server.model.impl.ServerImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.core.db.jpa.DuplicateKeyException;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesMachine;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.KubernetesMachineCache;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.jpa.entity.KubernetesMachineEntity;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.jpa.entity.KubernetesMachineEntity.KubernetesMachineId;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.jpa.entity.KubernetesServerEntity;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.jpa.entity.KubernetesServerEntity.KubernetesServerId;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespace;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespaceFactory;

/**
 * JPA based implementation of {@link KubernetesMachineCache}.
 *
 * @author Sergii Leshchenko
 */
public class JpaKubernetesMachineCache implements KubernetesMachineCache {

  private final Provider<EntityManager> managerProvider;
  private final KubernetesNamespaceFactory namespaceFactory;

  @Inject
  public JpaKubernetesMachineCache(
      Provider<EntityManager> managerProvider, KubernetesNamespaceFactory namespaceFactory) {
    this.managerProvider = managerProvider;
    this.namespaceFactory = namespaceFactory;
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
  public Map<String, KubernetesMachine> getMachines(RuntimeIdentity runtimeIdentity)
      throws InfrastructureException {
    KubernetesNamespace namespace = namespaceFactory.create(runtimeIdentity.getWorkspaceId());

    return doGetMachines(runtimeIdentity)
        .stream()
        .collect(
            Collectors.toMap(
                KubernetesMachineEntity::getMachineName,
                m ->
                    new KubernetesMachine(
                        m.getMachineName(),
                        m.getPodName(),
                        m.getContainerName(),
                        m.getAttributes(),
                        m.getServers()
                            .stream()
                            .collect(
                                toMap(
                                    KubernetesServerEntity::getName,
                                    s ->
                                        new ServerImpl(
                                            s.getUrl(), s.getStatus(), s.getAttributes()))),
                        m.getStatus(),
                        // TODO
                        namespace)));
  }

  @Override
  public void add(RuntimeIdentity runtimeIdentity, KubernetesMachine machine)
      throws InfrastructureException {
    try {
      doAddMachine(
          new KubernetesMachineEntity(
              runtimeIdentity.getWorkspaceId(),
              machine.getName(),
              machine.getPodName(),
              machine.getContainerName(),
              machine.getStatus(),
              machine.getAttributes(),
              machine
                  .getServers()
                  .entrySet()
                  .stream()
                  .map(
                      e ->
                          new KubernetesServerEntity(
                              runtimeIdentity.getWorkspaceId(),
                              machine.getName(),
                              e.getKey(),
                              e.getValue()))
                  .collect(Collectors.toList())));
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
      KubernetesServerEntity s =
          managerProvider
              .get()
              .find(
                  KubernetesServerEntity.class,
                  new KubernetesServerId(
                      runtimeIdentity.getWorkspaceId(), machineName, serverName));
      return new ServerImpl(s.getUrl(), s.getStatus(), s.getAttributes());
    } catch (RuntimeException e) {
      throw new InfrastructureException(e.getMessage(), e);
    }
  }

  @Transactional
  protected void doRemove(RuntimeIdentity runtimeIdentity) {
    EntityManager em = managerProvider.get();

    List<KubernetesMachineEntity> machines =
        em.createQuery(
                "SELECT m FROM KubernetesMachine m WHERE m.machineId.workspaceId = :workspaceId",
                KubernetesMachineEntity.class)
            .setParameter("workspaceId", runtimeIdentity.getWorkspaceId())
            .getResultList();

    for (KubernetesMachineEntity m : machines) {
      KubernetesMachineEntity remove =
          em.find(
              KubernetesMachineEntity.class,
              new KubernetesMachineId(m.getWorkspaceId(), m.getMachineName()));

      em.remove(remove);
    }

    em.flush();
  }

  @Transactional
  protected void doAddMachine(KubernetesMachineEntity machineMeta) {
    EntityManager em = managerProvider.get();
    em.persist(machineMeta);
    em.flush();
  }

  @Transactional(rollbackOn = {RuntimeException.class, InfrastructureException.class})
  protected List<KubernetesMachineEntity> doGetMachines(RuntimeIdentity runtimeIdentity)
      throws InfrastructureException {
    try {
      return managerProvider
          .get()
          .createQuery(
              "SELECT m FROM KubernetesMachine m WHERE m.machineId.workspaceId = :workspaceId",
              KubernetesMachineEntity.class)
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

    KubernetesMachineEntity meta =
        entityManager.find(
            KubernetesMachineEntity.class, new KubernetesMachineId(workspaceId, machineName));

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

    KubernetesServerEntity meta =
        entityManager.find(
            KubernetesServerEntity.class,
            new KubernetesServerId(workspaceId, machineName, serverName));

    if (meta.getStatus() != status) {
      meta.setStatus(status);

      entityManager.flush();
      return true;
    }
    return false;
  }
}
