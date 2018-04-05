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
package org.eclipse.che.api.workspace.activity;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.multiuser.resource.api.type.TimeoutResourceType;
import org.eclipse.che.multiuser.resource.api.usage.ResourceManager;
import org.eclipse.che.multiuser.resource.spi.impl.ResourceImpl;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(value = MockitoTestNGListener.class)
/** Tests for {@link WorkspaceActivityNotifier} */
public class WorkspaceActivityManagerTest {
  private static final long EXPIRE_PERIOD_MS = 60_000L; // 1 minute

  @Mock private AccountManager accountManager;

  @Mock private WorkspaceManager workspaceManager;

  @Captor private ArgumentCaptor<EventSubscriber<WorkspaceStatusEvent>> captor;

  @Mock private Account account;
  @Mock private WorkspaceImpl workspace;
  @Mock private WorkspaceActivityDao workspaceActivityDao;
  @Mock private ResourceManager resourceManager;

  @Mock private EventService eventService;

  private WorkspaceActivityManager activityManager;

  @BeforeMethod
  private void setUp() throws Exception {
    activityManager =
        new WorkspaceActivityManager(
            workspaceManager,
            workspaceActivityDao,
            accountManager,
            resourceManager,
            eventService,
            EXPIRE_PERIOD_MS);

    when(account.getName()).thenReturn("accountName");
    when(account.getId()).thenReturn("account123");
    when(accountManager.getByName(anyString())).thenReturn(account);

    when(workspaceManager.getWorkspace(anyString())).thenReturn(workspace);
    when(workspace.getNamespace()).thenReturn("accountName");

    doReturn(
            singletonList(
                new ResourceImpl(
                    TimeoutResourceType.ID,
                    EXPIRE_PERIOD_MS / 60 / 1000,
                    TimeoutResourceType.UNIT)))
        .when(resourceManager)
        .getAvailableResources(anyString());
  }

  @Test
  public void shouldAddNewActiveWorkspace() throws Exception {
    final String wsId = "testWsId";
    final String wsName = "testWsName";
    final long activityTime = 1000L;

    activityManager.update(wsId, activityTime);

    WorkspaceExpiration expected = new WorkspaceExpiration(wsId, activityTime + EXPIRE_PERIOD_MS);
    verify(workspaceActivityDao, times(1)).setExpiration(eq(expected));
  }

  @Test
  public void shouldAddWorkspaceForTrackActivityWhenWorkspaceRunning() throws Exception {
    final String wsId = "testWsId";
    activityManager.subscribe();
    verify(eventService).subscribe(captor.capture());
    final EventSubscriber<WorkspaceStatusEvent> subscriber = captor.getValue();
    subscriber.onEvent(
        DtoFactory.newDto(WorkspaceStatusEvent.class)
            .withStatus(WorkspaceStatus.RUNNING)
            .withWorkspaceId(wsId));
    ArgumentCaptor<WorkspaceExpiration> captor = ArgumentCaptor.forClass(WorkspaceExpiration.class);
    verify(workspaceActivityDao, times(1)).setExpiration(captor.capture());
    assertEquals(captor.getValue().getWorkspaceId(), wsId);
  }

  @Test
  public void shouldCeaseToTrackTheWorkspaceActivityAfterStopping() throws Exception {
    final String wsId = "testWsId";
    final long expiredTime = 1000L;
    activityManager.update(wsId, expiredTime);
    activityManager.subscribe();
    verify(eventService).subscribe(captor.capture());
    final EventSubscriber<WorkspaceStatusEvent> subscriber = captor.getValue();

    subscriber.onEvent(
        DtoFactory.newDto(WorkspaceStatusEvent.class)
            .withStatus(WorkspaceStatus.STOPPED)
            .withWorkspaceId(wsId));

    verify(workspaceActivityDao, times(1)).removeExpiration(eq(wsId));
  }
}
