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
package org.eclipse.che.multiuser.resource.api.usage.tracker;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Provider;
import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.multiuser.resource.api.type.RuntimeResourceType;
import org.eclipse.che.multiuser.resource.model.Resource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link RuntimeResourceUsageTracker}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class RuntimeResourceUsageTrackerTest {
  @Mock private Provider<WorkspaceManager> workspaceManagerProvider;
  @Mock private WorkspaceManager workspaceManager;
  @Mock private AccountManager accountManager;
  @Mock private Account account;

  @InjectMocks private RuntimeResourceUsageTracker runtimeResourceUsageTracker;

  @BeforeMethod
  public void setUp() throws Exception {
    when(workspaceManagerProvider.get()).thenReturn(workspaceManager);
  }

  @Test(
      expectedExceptions = NotFoundException.class,
      expectedExceptionsMessageRegExp = "Account was not found")
  public void shouldThrowNotFoundExceptionWhenAccountDoesNotExistOnGettingUsedRuntimes()
      throws Exception {
    when(accountManager.getById(any())).thenThrow(new NotFoundException("Account was not found"));

    runtimeResourceUsageTracker.getUsedResource("account123");
  }

  @Test
  public void shouldReturnEmptyOptionalWhenAccountDoesNotUseRuntimes() throws Exception {
    when(accountManager.getById(any())).thenReturn(account);
    when(account.getName()).thenReturn("testAccount");

    when(workspaceManager.getByNamespace(anyString(), anyBoolean(), anyInt(), anyLong()))
        .thenReturn(new Page<>(singletonList(createWorkspace(WorkspaceStatus.STOPPED)), 0, 1, 1));

    Optional<Resource> usedRuntimesOpt = runtimeResourceUsageTracker.getUsedResource("account123");

    assertFalse(usedRuntimesOpt.isPresent());
  }

  @Test
  public void shouldReturnUsedRuntimesForGivenAccount() throws Exception {
    when(accountManager.getById(any())).thenReturn(account);
    when(account.getName()).thenReturn("testAccount");

    List<WorkspaceImpl> runtimes =
        Stream.of(WorkspaceStatus.values())
            .map(RuntimeResourceUsageTrackerTest::createWorkspace)
            .collect(Collectors.toList());
    when(workspaceManager.getByNamespace(anyString(), anyBoolean(), anyInt(), anyLong()))
        .thenReturn(new Page<>(runtimes, 0, runtimes.size(), runtimes.size()));

    Optional<Resource> usedRuntimesOpt = runtimeResourceUsageTracker.getUsedResource("account123");

    assertTrue(usedRuntimesOpt.isPresent());
    Resource usedRuntimes = usedRuntimesOpt.get();
    assertEquals(usedRuntimes.getType(), RuntimeResourceType.ID);
    assertEquals(
        usedRuntimes.getAmount(), WorkspaceStatus.values().length - 1); // except stopped workspaces
    assertEquals(usedRuntimes.getUnit(), RuntimeResourceType.UNIT);
    verify(accountManager).getById(eq("account123"));
    verify(workspaceManager).getByNamespace(eq("testAccount"), eq(false), anyInt(), anyLong());
  }

  /** Creates users workspace object based on the status. */
  public static WorkspaceImpl createWorkspace(WorkspaceStatus status) {
    return WorkspaceImpl.builder().setStatus(status).build();
  }
}
