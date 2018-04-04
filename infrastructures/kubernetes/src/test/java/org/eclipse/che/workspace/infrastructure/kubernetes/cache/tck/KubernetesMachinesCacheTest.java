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
package org.eclipse.che.workspace.infrastructure.kubernetes.cache.tck;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static org.eclipse.che.workspace.infrastructure.kubernetes.cache.tck.TestObjects.*;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.model.workspace.runtime.MachineStatus;
import org.eclipse.che.api.core.model.workspace.runtime.ServerStatus;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeIdentityImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.commons.test.tck.TckListener;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.KubernetesMachineCache;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesMachineImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesRuntimeState;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesRuntimeState.RuntimeId;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link KubernetesMachineCache} contract.
 *
 * @author Sergii Leshchenko
 */
@Listeners(TckListener.class)
@Test(suiteName = KubernetesMachinesCacheTest.SUITE_NAME)
public class KubernetesMachinesCacheTest {

  public static final String SUITE_NAME = "KubernetesMachineCacheTck";

  @Inject private TckRepository<WorkspaceImpl> workspaceTckRepository;
  @Inject private TckRepository<AccountImpl> accountRepository;
  @Inject private TckRepository<KubernetesRuntimeState> runtimesRepository;

  @Inject private TckRepository<KubernetesMachineImpl> machineRepository;

  @Inject private KubernetesMachineCache machineCache;

  private KubernetesRuntimeState[] runtimeStates;
  private KubernetesMachineImpl[] machines;

  @BeforeMethod
  public void setUp() throws TckRepositoryException {
    WorkspaceImpl[] workspaces = new WorkspaceImpl[] {createWorkspace()};

    AccountImpl[] accounts = new AccountImpl[] {workspaces[0].getAccount()};

    runtimeStates = new KubernetesRuntimeState[] {createRuntimeState(workspaces[0])};

    accountRepository.createAll(asList(accounts));
    workspaceTckRepository.createAll(asList(workspaces));
    runtimesRepository.createAll(asList(runtimeStates));

    machines =
        new KubernetesMachineImpl[] {
          new KubernetesMachineImpl(
              workspaces[0].getId(),
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
    machineRepository.removeAll();

    runtimesRepository.removeAll();
    workspaceTckRepository.removeAll();
    accountRepository.removeAll();
  }

  // TODO Cover all methods

  @Test
  public void shouldAddMachine() throws Exception {
    machineCache.put(
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
    // given
    RuntimeId runtimeId = runtimeStates[0].getRuntimeId();

    // when
    machineCache.updateServerStatus(runtimeId, "machine1", "serverName", ServerStatus.RUNNING);

    // then
    Map<String, KubernetesMachineImpl> machines = machineCache.getMachines(runtimeId);
    assertEquals(machines.size(), 1);
  }

  @Test
  public void shouldRemoveMachine() throws Exception {
    // given
    RuntimeId runtimeId = runtimeStates[0].getRuntimeId();

    // when
    machineCache.remove(runtimeId);

    // then
    assertEquals(machineCache.getMachines(runtimeId).size(), 0);
  }
}
