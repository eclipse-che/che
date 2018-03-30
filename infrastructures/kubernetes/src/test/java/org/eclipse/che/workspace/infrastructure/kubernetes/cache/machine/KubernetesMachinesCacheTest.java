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
package org.eclipse.che.workspace.infrastructure.kubernetes.cache.machine;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.model.workspace.runtime.MachineStatus;
import org.eclipse.che.api.core.model.workspace.runtime.ServerStatus;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeIdentityImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerImpl;
import org.eclipse.che.commons.test.tck.TckListener;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesMachine;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.jpa.JpaKubernetesMachineCache;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.jpa.entity.KubernetesMachineEntity;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.jpa.entity.KubernetesRuntimeEntity;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.jpa.entity.KubernetesRuntimeEntity.Id;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.jpa.entity.KubernetesServerEntity;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Sergii Leshchenko */
@Listeners(TckListener.class)
@Test(suiteName = "K8sRuntimeTest2")
public class KubernetesMachinesCacheTest {

  @Inject private JpaKubernetesMachineCache machineCache;

  @Inject private TckRepository<KubernetesRuntimeEntity> runtimesRepository;
  @Inject private TckRepository<KubernetesMachineEntity> machineRepository;
  @Inject private TckRepository<KubernetesServerEntity> serverRepository;

  private KubernetesMachineEntity[] machines;
  private KubernetesServerEntity[] servers;

  @BeforeMethod
  public void setUp() throws TckRepositoryException {
    runtimesRepository.createAll(
        singletonList(
            new KubernetesRuntimeEntity(
                new Id("ws123", "envname", "ownerId"), "namespace", WorkspaceStatus.STARTING)));

    machines =
        new KubernetesMachineEntity[] {
          new KubernetesMachineEntity(
              "ws123",
              "machine1",
              "pod1",
              "c1",
              MachineStatus.STARTING,
              ImmutableMap.of("m.attr", "value"),
              //                emptyList()

              singletonList(
                  new KubernetesServerEntity(
                      "ws123",
                      "machine1",
                      "serverName",
                      "url",
                      ImmutableMap.of("attr", "value"),
                      ServerStatus.UNKNOWN)))
        };

    machineRepository.createAll(asList(machines));

    //    servers =
    //        new KubernetesServerEntity[]{
    //            new KubernetesServerEntity(
    //                "ws123", "machine1", "serverName2", "url", emptyMap(), ServerStatus.UNKNOWN)
    //        };
    //
    //    serverRepository.createAll(asList(servers));
  }

  @AfterMethod
  public void removeEntities() throws TckRepositoryException {
    serverRepository.removeAll();
    machineRepository.removeAll();
    runtimesRepository.removeAll();
  }

  @Test
  public void shouldAddMachine() throws Exception {
    machineCache.add(
        new RuntimeIdentityImpl("ws123", "env", "ownerId"),
        new KubernetesMachine(
            "machine2",
            "pod1",
            "c1",
            ImmutableMap.of("created", "123123123"),
            ImmutableMap.of("server2", new ServerImpl("url", ServerStatus.UNKNOWN, emptyMap())),
            MachineStatus.STARTING,
            null));
  }

  @Test
  public void shouldGetMachines() throws Exception {
    RuntimeIdentityImpl runtimeIdentity = new RuntimeIdentityImpl("ws123", "env", "ownerId");
    machineCache.updateServerStatus(
        runtimeIdentity, "machine1", "serverName", ServerStatus.RUNNING);

    Map<String, KubernetesMachine> machines = machineCache.getMachines(runtimeIdentity);

    assertEquals(machines.size(), 1);
  }

  @Test
  public void shouldRemoveMachine() throws Exception {
    RuntimeIdentityImpl runtimeId = new RuntimeIdentityImpl("ws123", "env", "ownerId");

    machineCache.delete(runtimeId);

    assertEquals(machineCache.getMachines(runtimeId).size(), 0);
  }
}
