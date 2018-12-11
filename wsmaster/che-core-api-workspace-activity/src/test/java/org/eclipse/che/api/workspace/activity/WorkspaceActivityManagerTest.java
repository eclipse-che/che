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
package org.eclipse.che.api.workspace.activity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.AssertJUnit.assertEquals;

import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;
import org.eclipse.che.dto.server.DtoFactory;
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
  private static final long DEFAULT_TIMEOUT = 60_000L; // 1 minute

  @Mock private WorkspaceManager workspaceManager;

  @Captor private ArgumentCaptor<EventSubscriber<WorkspaceStatusEvent>> captor;

  @Mock private Account account;
  @Mock private WorkspaceImpl workspace;
  @Mock private WorkspaceActivityDao workspaceActivityDao;

  @Mock private EventService eventService;

  private WorkspaceActivityManager activityManager;

  @BeforeMethod
  private void setUp() throws Exception {
    activityManager =
        new WorkspaceActivityManager(
            workspaceManager, workspaceActivityDao, eventService, DEFAULT_TIMEOUT);

    lenient().when(account.getName()).thenReturn("accountName");
    lenient().when(account.getId()).thenReturn("account123");

    lenient().when(workspaceManager.getWorkspace(anyString())).thenReturn(workspace);
    lenient().when(workspace.getNamespace()).thenReturn("accountName");
  }

  @Test
  public void shouldAddNewActiveWorkspace() throws Exception {
    final String wsId = "testWsId";
    final long activityTime = 1000L;

    activityManager.update(wsId, activityTime);

    verify(workspaceActivityDao, times(1))
        .setExpirationTime(eq(wsId), eq(activityTime + DEFAULT_TIMEOUT));
  }

  @Test
  public void shouldAddWorkspaceForTrackActivityWhenWorkspaceRunning() throws Exception {
    final String wsId = "testWsId";
    activityManager.subscribe();
    verify(eventService, times(2)).subscribe(captor.capture());
    final EventSubscriber<WorkspaceStatusEvent> subscriber = captor.getAllValues().get(0);
    subscriber.onEvent(
        DtoFactory.newDto(WorkspaceStatusEvent.class)
            .withStatus(WorkspaceStatus.RUNNING)
            .withWorkspaceId(wsId));
    ArgumentCaptor<String> wsIdCaptor = ArgumentCaptor.forClass(String.class);
    verify(workspaceActivityDao, times(1)).setExpirationTime(wsIdCaptor.capture(), any(long.class));
    assertEquals(wsIdCaptor.getValue(), wsId);
  }

  @Test
  public void shouldCeaseToTrackTheWorkspaceActivityAfterStopping() throws Exception {
    final String wsId = "testWsId";
    final long expiredTime = 1000L;
    activityManager.update(wsId, expiredTime);
    activityManager.subscribe();
    verify(eventService, times(2)).subscribe(captor.capture());
    final EventSubscriber<WorkspaceStatusEvent> subscriber = captor.getAllValues().get(0);

    subscriber.onEvent(
        DtoFactory.newDto(WorkspaceStatusEvent.class)
            .withStatus(WorkspaceStatus.STOPPED)
            .withWorkspaceId(wsId));

    verify(workspaceActivityDao, times(1)).removeExpiration(eq(wsId));
  }
}
