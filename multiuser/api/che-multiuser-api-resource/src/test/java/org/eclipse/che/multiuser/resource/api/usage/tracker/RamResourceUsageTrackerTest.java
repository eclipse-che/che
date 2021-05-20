/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.resource.api.usage.tracker;

import static java.lang.String.valueOf;
import static java.util.Arrays.asList;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_LIMIT_ATTRIBUTE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.inject.Provider;
import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.Runtime;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.MachineImpl;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.multiuser.resource.api.type.RamResourceType;
import org.eclipse.che.multiuser.resource.model.Resource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link org.eclipse.che.multiuser.resource.api.usage.tracker.RamResourceUsageTracker}
 *
 * @author Sergii Leschenko
 * @author Anton Korneta
 */
@Listeners(MockitoTestNGListener.class)
public class RamResourceUsageTrackerTest {

  public static final String ACCOUNT_ID = "account_119";
  public static final String ACCOUNT_NAME = "testAccount";
  public static final String ACTIVE_ENV_NAME = "default";

  @Mock private Account account;
  @Mock private Provider<WorkspaceManager> workspaceManagerProvider;
  @Mock private WorkspaceManager workspaceManager;
  @Mock private AccountManager accountManager;
  @Mock private EnvironmentRamCalculator envRamCalculator;

  @InjectMocks private RamResourceUsageTracker ramUsageTracker;

  @BeforeMethod
  public void setUp() throws Exception {
    when(workspaceManagerProvider.get()).thenReturn(workspaceManager);
    lenient().when(accountManager.getById(ACCOUNT_ID)).thenReturn(account);
    when(account.getName()).thenReturn(ACCOUNT_NAME);
  }

  @Test(
      expectedExceptions = NotFoundException.class,
      expectedExceptionsMessageRegExp = "Account was not found")
  public void shouldThrowNotFoundExceptionWhenAccountDoesNotExistOnGettingUsedRam()
      throws Exception {
    when(accountManager.getById(any())).thenThrow(new NotFoundException("Account was not found"));

    ramUsageTracker.getUsedResource(ACCOUNT_ID);
  }

  @Test
  public void shouldReturnEmptyOptionalWhenAccountHasOnlyStoppedWorkspaces() throws Exception {
    mockWorkspaces(createWorkspace(WorkspaceStatus.STOPPED, 1000, 500, 500));

    final Optional<Resource> usedRamOpt = ramUsageTracker.getUsedResource(ACCOUNT_ID);

    assertFalse(usedRamOpt.isPresent());
  }

  @Test
  public void shouldReturnUsedRamOfRunningWorkspaceForGivenAccount() throws Exception {
    mockWorkspaces(createWorkspace(WorkspaceStatus.RUNNING, 1000, 500, 500));
    when(envRamCalculator.calculate(any(Runtime.class))).thenReturn(2000L);

    final Optional<Resource> usedRamOpt = ramUsageTracker.getUsedResource(ACCOUNT_ID);

    assertTrue(usedRamOpt.isPresent());
    final Resource usedRam = usedRamOpt.get();
    assertEquals(usedRam.getType(), RamResourceType.ID);
    assertEquals(usedRam.getAmount(), 2000L);
    assertEquals(usedRam.getUnit(), RamResourceType.UNIT);
    verify(accountManager).getById(ACCOUNT_ID);
    verify(workspaceManager).getByNamespace(anyString(), anyBoolean(), anyInt(), anyLong());
  }

  @Test
  public void shouldNotSumRamOfStoppedWorkspaceWhenGettingUsedRamForGivenAccount()
      throws Exception {
    final WorkspaceImpl stoppedWs = createWorkspace(WorkspaceStatus.STOPPED, 3500);
    final WorkspaceImpl runningWs = createWorkspace(WorkspaceStatus.RUNNING, 2500);
    mockWorkspaces(stoppedWs, runningWs);
    when(envRamCalculator.calculate(runningWs.getRuntime())).thenReturn(2500L);

    final Optional<Resource> usedRamOpt = ramUsageTracker.getUsedResource(ACCOUNT_ID);

    assertTrue(usedRamOpt.isPresent());
    final Resource usedRam = usedRamOpt.get();
    assertEquals(usedRam.getType(), RamResourceType.ID);
    assertEquals(usedRam.getAmount(), 2500L);
    assertEquals(usedRam.getUnit(), RamResourceType.UNIT);
    verify(accountManager).getById(ACCOUNT_ID);
    verify(workspaceManager).getByNamespace(anyString(), anyBoolean(), anyInt(), anyLong());
  }

  @Test
  public void returnUsedRamOfStartingWorkspaceForGivenAccount() throws Exception {
    mockWorkspaces(createWorkspace(WorkspaceStatus.STARTING, 1000, 500, 500));
    when(envRamCalculator.calculate(any(Environment.class))).thenReturn(2000L);

    final Optional<Resource> usedRamOpt = ramUsageTracker.getUsedResource(ACCOUNT_ID);

    assertTrue(usedRamOpt.isPresent());
    final Resource usedRam = usedRamOpt.get();
    assertEquals(usedRam.getType(), RamResourceType.ID);
    assertEquals(usedRam.getAmount(), 2000L);
    assertEquals(usedRam.getUnit(), RamResourceType.UNIT);
    verify(accountManager).getById(ACCOUNT_ID);
    verify(workspaceManager).getByNamespace(anyString(), anyBoolean(), anyInt(), anyLong());
  }

  private void mockWorkspaces(WorkspaceImpl... workspaces) throws ServerException {
    when(workspaceManager.getByNamespace(anyString(), anyBoolean(), anyInt(), anyLong()))
        .thenReturn(new Page<>(asList(workspaces), 0, workspaces.length, workspaces.length));
  }

  /** Creates users workspace object based on the status and machines RAM. */
  private static WorkspaceImpl createWorkspace(WorkspaceStatus status, Integer... machineRams) {
    final Map<String, MachineImpl> machines = new HashMap<>(machineRams.length - 1);
    final Map<String, MachineConfigImpl> machineConfigs = new HashMap<>(machineRams.length - 1);
    byte i = 1;
    for (Integer machineRam : machineRams) {
      final String machineName = "machine_" + i++;
      machines.put(machineName, createMachine(machineRam));
      machineConfigs.put(machineName, createMachineConfig(machineRam));
    }
    return WorkspaceImpl.builder()
        .setConfig(
            WorkspaceConfigImpl.builder()
                .setEnvironments(
                    ImmutableBiMap.of(ACTIVE_ENV_NAME, new EnvironmentImpl(null, machineConfigs)))
                .build())
        .setRuntime(new RuntimeImpl(ACTIVE_ENV_NAME, machines, null))
        .setStatus(status)
        .build();
  }

  private static MachineImpl createMachine(long memoryMb) {
    return new MachineImpl(
        ImmutableMap.of(MEMORY_LIMIT_ATTRIBUTE, valueOf(memoryMb)), new HashMap<>(), null);
  }

  private static MachineConfigImpl createMachineConfig(long memoryMb) {
    return new MachineConfigImpl(
        null, null, ImmutableMap.of(MEMORY_LIMIT_ATTRIBUTE, valueOf(memoryMb)), null);
  }
}
