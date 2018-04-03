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
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.model.workspace.runtime.MachineStatus;
import org.eclipse.che.api.core.model.workspace.runtime.ServerStatus;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeIdentityImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.commons.test.tck.TckListener;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.jpa.JpaKubernetesMachineCache;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesMachineImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesRuntimeState;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesRuntimeState.RuntimeId;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesServerImpl;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Sergii Leshchenko */
@Listeners(TckListener.class)
@Test(suiteName = "K8sRuntimeTest2")
public class KubernetesMachinesCacheTest {

  @Inject private JpaKubernetesMachineCache machineCache;

  @Inject private TckRepository<WorkspaceImpl> workspaceTckRepository;
  @Inject private TckRepository<AccountImpl> accountRepository;
  @Inject private TckRepository<KubernetesRuntimeState> runtimesRepository;
  @Inject private TckRepository<KubernetesMachineImpl> machineRepository;
  @Inject private TckRepository<KubernetesServerImpl> serverRepository;

  private KubernetesMachineImpl[] machines;
  private KubernetesServerImpl[] servers;

  @BeforeMethod
  public void setUp() throws TckRepositoryException {
    AccountImpl account = new AccountImpl("id", "name", "type");
    accountRepository.createAll(singletonList(account));
    workspaceTckRepository.createAll(
        singletonList(
            new WorkspaceImpl(
                "ws123",
                account,
                new WorkspaceConfigImpl(
                    "name", "description", "defEnv", emptyList(), emptyList(), emptyMap()))));

    runtimesRepository.createAll(
        singletonList(
            new KubernetesRuntimeState(
                new RuntimeId("ws123", "envname", "ownerId"),
                "namespace",
                WorkspaceStatus.STARTING)));

    machines =
        new KubernetesMachineImpl[] {
          new KubernetesMachineImpl(
              "ws123",
              "machine1",
              "pod1",
              "c1",
              MachineStatus.STARTING,
              ImmutableMap.of("m.attr", "value"),
              ImmutableMap.of(
                  "serverName",
                  new ServerImpl("url", ServerStatus.UNKNOWN, ImmutableMap.of("attr", "value"))))
        };

    machineRepository.createAll(asList(machines));
  }

  @AfterMethod
  public void removeEntities() throws TckRepositoryException {
    serverRepository.removeAll();
    machineRepository.removeAll();
    runtimesRepository.removeAll();
    workspaceTckRepository.removeAll();
    accountRepository.removeAll();
  }

  @Test
  public void shouldAddMachine() throws Exception {
    machineCache.add(
        new RuntimeIdentityImpl("ws123", "env", "ownerId"),
        new KubernetesMachineImpl(
            "ws123",
            "machine2",
            "pod1",
            "c1",
            MachineStatus.STARTING,
            ImmutableMap.of("created", "123123123"),
            ImmutableMap.of("server2", new ServerImpl("url", ServerStatus.UNKNOWN, emptyMap()))));
  }

  @Test
  public void shouldGetMachines() throws Exception {
    RuntimeIdentityImpl runtimeIdentity = new RuntimeIdentityImpl("ws123", "env", "ownerId");
    machineCache.updateServerStatus(
        runtimeIdentity, "machine1", "serverName", ServerStatus.RUNNING);

    Map<String, KubernetesMachineImpl> machines = machineCache.getMachines(runtimeIdentity);

    assertEquals(machines.size(), 1);
  }

  @Test
  public void shouldRemoveMachine() throws Exception {
    RuntimeIdentityImpl runtimeId = new RuntimeIdentityImpl("ws123", "env", "ownerId");

    machineCache.delete(runtimeId);

    assertEquals(machineCache.getMachines(runtimeId).size(), 0);
  }
}
