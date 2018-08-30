/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.cache.tck;

import static java.util.Arrays.asList;
import static org.eclipse.che.workspace.infrastructure.kubernetes.cache.tck.TestObjects.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import javax.inject.Inject;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.model.workspace.runtime.MachineStatus;
import org.eclipse.che.api.core.model.workspace.runtime.ServerStatus;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.test.tck.TckListener;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.KubernetesMachineCache;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesMachineImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesRuntimeState;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesRuntimeState.RuntimeId;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesServerImpl;
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

  private WorkspaceImpl[] workspaces;
  private KubernetesRuntimeState[] runtimeStates;

  private KubernetesMachineImpl[] machines;

  @BeforeMethod
  public void setUp() throws TckRepositoryException {
    workspaces = new WorkspaceImpl[] {createWorkspace(), createWorkspace()};

    AccountImpl[] accounts =
        new AccountImpl[] {workspaces[0].getAccount(), workspaces[1].getAccount()};

    runtimeStates =
        new KubernetesRuntimeState[] {
          createRuntimeState(workspaces[0]), createRuntimeState(workspaces[1])
        };

    accountRepository.createAll(asList(accounts));
    workspaceTckRepository.createAll(asList(workspaces));
    runtimesRepository.createAll(asList(runtimeStates));

    machines =
        new KubernetesMachineImpl[] {
          createMachine(
              workspaces[0].getId(),
              "machine1",
              MachineStatus.STARTING,
              ImmutableMap.of("server1", createServer(ServerStatus.UNKNOWN))),
          createMachine(
              workspaces[0].getId(),
              "machine2",
              MachineStatus.RUNNING,
              ImmutableMap.of("server1", createServer(ServerStatus.UNKNOWN))),
          createMachine(
              workspaces[1].getId(),
              "machine1",
              MachineStatus.STARTING,
              ImmutableMap.of("server1", createServer(ServerStatus.UNKNOWN)))
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

  @Test
  public void shouldPutMachine() throws Exception {
    // given
    KubernetesMachineImpl machine =
        createMachine(
            workspaces[1].getId(),
            "machine2",
            MachineStatus.RUNNING,
            ImmutableMap.of("myServer", createServer(ServerStatus.RUNNING)));

    // when
    machineCache.put(runtimeStates[1].getRuntimeId(), machine);

    // then
    Map<String, KubernetesMachineImpl> fetched =
        machineCache.getMachines(runtimeStates[1].getRuntimeId());
    assertEquals(2, fetched.size());
    assertTrue(fetched.containsKey("machine2"));
    assertTrue(fetched.containsValue(machine));
  }

  @Test(
      expectedExceptions = InfrastructureException.class,
      expectedExceptionsMessageRegExp = "Machine is already in cache")
  public void shouldThrowExceptionIfMachineIsAlreadyInCacheOnTryToPutMachine() throws Exception {
    // given
    KubernetesMachineImpl machine =
        createMachine(
            workspaces[1].getId(),
            machines[0].getName(),
            MachineStatus.RUNNING,
            ImmutableMap.of("myServer", createServer(ServerStatus.RUNNING)));

    // when
    machineCache.put(runtimeStates[1].getRuntimeId(), machine);
  }

  @Test
  public void shouldGetMachines() throws Exception {
    // given
    RuntimeId runtimeId = runtimeStates[0].getRuntimeId();

    // when
    machineCache.getMachines(runtimeId);

    // then
    Map<String, KubernetesMachineImpl> fetched = machineCache.getMachines(runtimeId);
    assertEquals(fetched.size(), 2);
    assertTrue(fetched.keySet().containsAll(asList("machine1", "machine2")));
    assertTrue(fetched.values().containsAll(asList(machines[0], machines[1])));
  }

  @Test
  public void shouldGetServer() throws Exception {
    // given
    RuntimeId runtimeId = runtimeStates[0].getRuntimeId();
    KubernetesMachineImpl machine = machines[0];
    Entry<String, KubernetesServerImpl> serverToFetch =
        machine.getServers().entrySet().iterator().next();

    // when
    KubernetesServerImpl fetched =
        machineCache.getServer(runtimeId, machine.getName(), serverToFetch.getKey());

    // then
    assertEquals(fetched, serverToFetch.getValue());
  }

  @Test(
      expectedExceptions = InfrastructureException.class,
      expectedExceptionsMessageRegExp = "Server with name 'non-existing' was not found")
  public void shouldThrowExceptionWhenServerWasNotFoundOnGetting() throws Exception {
    // given
    RuntimeId runtimeId = runtimeStates[0].getRuntimeId();
    KubernetesMachineImpl machine = machines[0];

    // when
    machineCache.getServer(runtimeId, machine.getName(), "non-existing");
  }

  @Test
  public void shouldUpdateMachineStatusServerStatus() throws Exception {
    // given
    RuntimeId runtimeId = runtimeStates[0].getRuntimeId();

    // when
    machineCache.updateServerStatus(
        runtimeId, machines[0].getName(), "server1", ServerStatus.RUNNING);

    // then
    KubernetesServerImpl fetchedServer = machineCache.getServer(runtimeId, "machine1", "server1");
    assertEquals(fetchedServer.getStatus(), ServerStatus.RUNNING);
  }

  @Test(
      expectedExceptions = InfrastructureException.class,
      expectedExceptionsMessageRegExp = "Server with name 'non-existing' was not found")
  public void shouldThrowExceptionWhenServerWasNotFoundOnStatusUpdating() throws Exception {
    // given
    RuntimeId runtimeId = runtimeStates[0].getRuntimeId();
    KubernetesMachineImpl machine = machines[0];

    // when
    machineCache.updateServerStatus(
        runtimeId, machine.getName(), "non-existing", ServerStatus.RUNNING);
  }

  @Test
  public void shouldUpdateMachineStatus() throws Exception {
    // given
    RuntimeId runtimeId = runtimeStates[0].getRuntimeId();
    KubernetesMachineImpl machine = machines[0];
    String machineName = machine.getName();

    // when
    machineCache.updateMachineStatus(runtimeId, machineName, MachineStatus.RUNNING);

    // then
    Optional<KubernetesMachineImpl> machineOpt =
        machineCache
            .getMachines(runtimeId)
            .entrySet()
            .stream()
            .filter(e -> e.getKey().equals(machineName))
            .map(Map.Entry::getValue)
            .findAny();
    assertTrue(machineOpt.isPresent());
    assertEquals(machineOpt.get().getStatus(), MachineStatus.RUNNING);
  }

  @Test
  public void shouldUpdateServerStatus() throws Exception {
    // given
    RuntimeId runtimeId = runtimeStates[0].getRuntimeId();

    // when
    machineCache.updateServerStatus(
        runtimeId, machines[0].getName(), "server1", ServerStatus.RUNNING);

    // then
    KubernetesServerImpl fetchedServer = machineCache.getServer(runtimeId, "machine1", "server1");
    assertEquals(fetchedServer.getStatus(), ServerStatus.RUNNING);
  }

  @Test
  public void shouldRemoveMachines() throws Exception {
    // given
    RuntimeId runtimeId = runtimeStates[0].getRuntimeId();

    // when
    machineCache.remove(runtimeId);

    // then
    assertEquals(machineCache.getMachines(runtimeId).size(), 0);
  }
}
