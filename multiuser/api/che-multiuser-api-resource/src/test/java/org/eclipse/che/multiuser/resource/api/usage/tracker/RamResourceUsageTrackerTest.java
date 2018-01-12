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
package org.eclipse.che.multiuser.resource.api.usage.tracker;

/*
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.inject.Provider;
import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.machine.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineLimitsImpl;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceRuntimeImpl;
import org.eclipse.che.multiuser.resource.api.type.RamResourceType;
import org.eclipse.che.multiuser.resource.model.Resource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
*/

import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;

/**
 * Tests for {@link org.eclipse.che.multiuser.resource.api.usage.tracker.RamResourceUsageTracker}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class RamResourceUsageTrackerTest {
  /*
  @Mock private Account account;
  @Mock private Provider<WorkspaceManager> workspaceManagerProvider;
  @Mock private WorkspaceManager workspaceManager;
  @Mock private AccountManager accountManager;

  @InjectMocks private RamResourceUsageTracker ramUsageTracker;

  @BeforeMethod
  public void setUp() throws Exception {
    when(workspaceManagerProvider.get()).thenReturn(workspaceManager);
  }

  @Test(
    expectedExceptions = NotFoundException.class,
    expectedExceptionsMessageRegExp = "Account was not found"
  )
  public void shouldThrowNotFoundExceptionWhenAccountDoesNotExistOnGettingUsedRam()
      throws Exception {
    when(accountManager.getById(any())).thenThrow(new NotFoundException("Account was not found"));

    ramUsageTracker.getUsedResource("account123");
  }

  @Test
  public void shouldReturnEmptyOptionalWhenAccountHasOnlyStoppedWorkspaces() throws Exception {
    when(accountManager.getById(any())).thenReturn(account);
    when(account.getName()).thenReturn("testAccount");

    when(workspaceManager.getByNamespace(anyString(), anyBoolean()))
        .thenReturn(singletonList(createWorkspace(WorkspaceStatus.STOPPED, 1000, 500, 500)));

    Optional<Resource> usedRamOpt = ramUsageTracker.getUsedResource("account123");

    assertFalse(usedRamOpt.isPresent());
  }

  @Test
  public void shouldReturnUsedRamForGivenAccount() throws Exception {
    when(accountManager.getById(any())).thenReturn(account);
    when(account.getName()).thenReturn("testAccount");

    when(workspaceManager.getByNamespace(anyString(), anyBoolean()))
        .thenReturn(singletonList(createWorkspace(WorkspaceStatus.RUNNING, 1000, 500, 500)));

    Optional<Resource> usedRamOpt = ramUsageTracker.getUsedResource("account123");

    assertTrue(usedRamOpt.isPresent());
    Resource usedRam = usedRamOpt.get();
    assertEquals(usedRam.getType(), RamResourceType.ID);
    assertEquals(usedRam.getAmount(), 2000L);
    assertEquals(usedRam.getUnit(), RamResourceType.UNIT);
    verify(accountManager).getById(eq("account123"));
    verify(workspaceManager).getByNamespace(eq("testAccount"), eq(true));
  }

  @Test
  public void shouldNotSumRamOfStoppedWorkspaceWhenGettingUsedRamForGivenAccount()
      throws Exception {
    when(accountManager.getById(any())).thenReturn(account);
    when(account.getName()).thenReturn("testAccount");

    when(workspaceManager.getByNamespace(anyString(), anyBoolean()))
        .thenReturn(singletonList(createWorkspace(WorkspaceStatus.STOPPED, 1000, 500, 500)));

    Optional<Resource> usedRamOpt = ramUsageTracker.getUsedResource("account123");

    assertFalse(usedRamOpt.isPresent());
    verify(accountManager).getById(eq("account123"));
    verify(workspaceManager).getByNamespace(eq("testAccount"), eq(true));
  }

  /** Creates users workspace object based on the status and machines RAM. */
  /*
  public static WorkspaceImpl createWorkspace(WorkspaceStatus status, Integer... machineRams) {
    final List<MachineImpl> machines = new ArrayList<>(machineRams.length - 1);
    for (Integer machineRam : machineRams) {
      machines.add(createMachine(machineRam));
    }
    return WorkspaceImpl.builder()
        .setRuntime(new WorkspaceRuntimeImpl(null, null, machines, null))
        .setStatus(status)
        .build();
  }

  private static MachineImpl createMachine(int memoryMb) {
    return MachineImpl.builder()
        .setConfig(MachineConfigImpl.builder().setLimits(new MachineLimitsImpl(memoryMb)).build())
        .build();
  }
  */
}
