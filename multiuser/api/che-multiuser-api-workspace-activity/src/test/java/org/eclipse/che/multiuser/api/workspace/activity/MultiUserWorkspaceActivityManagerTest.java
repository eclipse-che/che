/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.api.workspace.activity;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.activity.WorkspaceActivityDao;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.multiuser.resource.api.type.TimeoutResourceType;
import org.eclipse.che.multiuser.resource.api.usage.ResourceManager;
import org.eclipse.che.multiuser.resource.spi.impl.ResourceImpl;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class MultiUserWorkspaceActivityManagerTest {
  private static final long DEFAULT_TIMEOUT = 60_000L; // 1 minute
  private static final long USER_LIMIT_TIMEOUT = 120_000L; // 2 minutes
  private static final long DEFAULT_RUN_TIMEOUT = 0; // No default run timeout

  @Mock private AccountManager accountManager;
  @Mock private ResourceManager resourceManager;

  @Mock private WorkspaceManager workspaceManager;

  @Mock private Account account;
  @Mock private WorkspaceImpl workspace;
  @Mock private WorkspaceActivityDao workspaceActivityDao;

  @Mock private EventService eventService;

  private MultiUserWorkspaceActivityManager activityManager;

  @BeforeMethod
  private void setUp() throws Exception {
    activityManager =
        new MultiUserWorkspaceActivityManager(
            workspaceManager,
            workspaceActivityDao,
            eventService,
            accountManager,
            resourceManager,
            DEFAULT_TIMEOUT,
            DEFAULT_RUN_TIMEOUT);

    when(account.getId()).thenReturn("account123");
    when(accountManager.getByName(anyString())).thenReturn(account);

    when(workspaceManager.getWorkspace(anyString())).thenReturn(workspace);
    when(workspace.getNamespace()).thenReturn("accountName");
  }

  @Test
  public void shouldAddNewActiveWorkspaceWithUserTimeoutIfPresent() throws Exception {
    final String wsId = "testWsId";
    final long activityTime = 1000L;
    doReturn(
            singletonList(
                new ResourceImpl(
                    TimeoutResourceType.ID,
                    USER_LIMIT_TIMEOUT / 60 / 1000,
                    TimeoutResourceType.UNIT)))
        .when(resourceManager)
        .getAvailableResources(anyString());

    activityManager.update(wsId, activityTime);

    verify(workspaceActivityDao, times(1))
        .setExpirationTime(eq(wsId), eq(activityTime + USER_LIMIT_TIMEOUT));
    verify(resourceManager).getAvailableResources(eq("account123"));
  }

  @Test
  public void shouldAddNewActiveWorkspaceWithDefaultTimeoutIfThereAreNoLimintsOnAccount()
      throws Exception {
    final String wsId = "testWsId";
    final long activityTime = 1000L;

    activityManager.update(wsId, activityTime);

    verify(workspaceActivityDao, times(1))
        .setExpirationTime(eq(wsId), eq(activityTime + DEFAULT_TIMEOUT));
    verify(resourceManager).getAvailableResources(eq("account123"));
  }
}
