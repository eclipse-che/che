/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.resource.api.usage.tracker;

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

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import javax.inject.Provider;
import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.multiuser.resource.api.type.WorkspaceResourceType;
import org.eclipse.che.multiuser.resource.model.Resource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link
 * org.eclipse.che.multiuser.resource.api.usage.tracker.WorkspaceResourceUsageTracker}
 */
@Listeners(MockitoTestNGListener.class)
public class WorkspaceResourceUsageTrackerTest {
  @Mock private Provider<WorkspaceManager> workspaceManagerProvider;
  @Mock private WorkspaceManager workspaceManager;
  @Mock private AccountManager accountManager;
  @Mock private Account account;

  @InjectMocks private WorkspaceResourceUsageTracker workspaceResourceUsageTracker;

  @BeforeMethod
  public void setUp() throws Exception {
    when(workspaceManagerProvider.get()).thenReturn(workspaceManager);
  }

  @Test(
    expectedExceptions = NotFoundException.class,
    expectedExceptionsMessageRegExp = "Account was not found"
  )
  public void shouldThrowNotFoundExceptionWhenAccountDoesNotExistOnGettingUsedWorkspaces()
      throws Exception {
    when(accountManager.getById(any())).thenThrow(new NotFoundException("Account was not found"));

    workspaceResourceUsageTracker.getUsedResource("account123");
  }

  @Test
  public void shouldReturnEmptyOptionalWhenAccountDoesNotUseWorkspaces() throws Exception {
    when(accountManager.getById(any())).thenReturn(account);
    when(account.getName()).thenReturn("testAccount");

    when(workspaceManager.getByNamespace(anyString(), anyBoolean(), anyInt(), anyLong()))
        .thenReturn(new Page<>(Collections.emptyList(), 0, 1, 0));

    Optional<Resource> usedWorkspacesOpt =
        workspaceResourceUsageTracker.getUsedResource("account123");

    assertFalse(usedWorkspacesOpt.isPresent());
  }

  @Test
  public void shouldReturnUsedWorkspacesForGivenAccount() throws Exception {
    when(accountManager.getById(any())).thenReturn(account);
    when(account.getName()).thenReturn("testAccount");

    when(workspaceManager.getByNamespace(anyString(), anyBoolean(), anyInt(), anyLong()))
        .thenReturn(
            new Page<>(
                Arrays.asList(new WorkspaceImpl(), new WorkspaceImpl(), new WorkspaceImpl()),
                0,
                3,
                3));

    Optional<Resource> usedWorkspacesOpt =
        workspaceResourceUsageTracker.getUsedResource("account123");

    assertTrue(usedWorkspacesOpt.isPresent());
    Resource usedWorkspaces = usedWorkspacesOpt.get();
    assertEquals(usedWorkspaces.getType(), WorkspaceResourceType.ID);
    assertEquals(usedWorkspaces.getAmount(), 3);
    assertEquals(usedWorkspaces.getUnit(), WorkspaceResourceType.UNIT);
    verify(accountManager).getById(eq("account123"));
    verify(workspaceManager).getByNamespace(eq("testAccount"), eq(false), anyInt(), anyLong());
  }
}
