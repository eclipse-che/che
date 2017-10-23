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
package org.eclipse.che.plugin.activity;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.Map;
import org.eclipse.che.account.api.AccountManager;
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
  private static final long EXPIRE_PERIOD_MS = 60_000L; // 1 minute

  @Mock private AccountManager accountManager;

  @Mock private WorkspaceManager workspaceManager;

  @Captor private ArgumentCaptor<EventSubscriber<WorkspaceStatusEvent>> captor;

  @Mock private Account account;
  @Mock private WorkspaceImpl workspace;

  @Mock private EventService eventService;

  private WorkspaceActivityManager activityManager;

  @BeforeMethod
  private void setUp() throws Exception {
    activityManager =
        new WorkspaceActivityManager(workspaceManager, eventService, EXPIRE_PERIOD_MS);

    when(account.getName()).thenReturn("accountName");
    when(account.getId()).thenReturn("account123");
    when(accountManager.getByName(anyString())).thenReturn(account);

    when(workspaceManager.getWorkspace(anyString())).thenReturn(workspace);
    when(workspace.getNamespace()).thenReturn("accountName");
  }

  @Test
  public void shouldAddNewActiveWorkspace() throws Exception {
    final String wsId = "testWsId";
    final long activityTime = 1000L;
    final Map<String, Long> activeWorkspaces = getActiveWorkspaces(activityManager);
    boolean wsAlreadyAdded = activeWorkspaces.containsKey(wsId);

    activityManager.update(wsId, activityTime);

    assertFalse(wsAlreadyAdded);
    assertEquals((long) activeWorkspaces.get(wsId), activityTime + EXPIRE_PERIOD_MS);
    assertFalse(activeWorkspaces.isEmpty());
  }

  @Test
  public void shouldUpdateTheWorkspaceExpirationIfItWasPreviouslyActive() throws Exception {
    final String wsId = "testWsId";
    final long activityTime = 1000L;
    final long newActivityTime = 2000L;
    final Map<String, Long> activeWorkspaces = getActiveWorkspaces(activityManager);
    boolean wsAlreadyAdded = activeWorkspaces.containsKey(wsId);
    activityManager.update(wsId, activityTime);

    activityManager.update(wsId, newActivityTime);
    final long workspaceStopTime = activeWorkspaces.get(wsId);

    assertFalse(wsAlreadyAdded);
    assertFalse(activeWorkspaces.isEmpty());
    assertEquals(newActivityTime + EXPIRE_PERIOD_MS, workspaceStopTime);
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
    final Map<String, Long> activeWorkspaces = getActiveWorkspaces(activityManager);

    assertTrue(activeWorkspaces.containsKey(wsId));
  }

  @Test
  public void shouldCeaseToTrackTheWorkspaceActivityAfterStopping() throws Exception {
    final String wsId = "testWsId";
    final long expiredTime = 1000L;
    activityManager.update(wsId, expiredTime);
    activityManager.subscribe();
    verify(eventService).subscribe(captor.capture());
    final EventSubscriber<WorkspaceStatusEvent> subscriber = captor.getValue();

    final Map<String, Long> activeWorkspaces = getActiveWorkspaces(activityManager);
    final boolean contains = activeWorkspaces.containsKey(wsId);
    subscriber.onEvent(
        DtoFactory.newDto(WorkspaceStatusEvent.class)
            .withStatus(WorkspaceStatus.STOPPED)
            .withWorkspaceId(wsId));

    assertTrue(contains);
    assertTrue(activeWorkspaces.isEmpty());
  }

  @SuppressWarnings("unchecked")
  private Map<String, Long> getActiveWorkspaces(WorkspaceActivityManager workspaceActivityManager)
      throws Exception {
    for (Field field : workspaceActivityManager.getClass().getDeclaredFields()) {
      field.setAccessible(true);
      if (field.getName().equals("activeWorkspaces")) {
        return (Map<String, Long>) field.get(workspaceActivityManager);
      }
    }
    throw new IllegalAccessException();
  }
}
