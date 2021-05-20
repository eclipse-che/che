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
package org.eclipse.che.api.workspace.activity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import com.google.common.collect.ImmutableMap;
import java.util.stream.Stream;
import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.event.BeforeWorkspaceRemovedEvent;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.shared.Constants;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;
import org.eclipse.che.api.workspace.shared.event.WorkspaceCreatedEvent;
import org.eclipse.che.dto.server.DtoFactory;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** Tests for {@link WorkspaceActivityManager} */
@Listeners(value = MockitoTestNGListener.class)
public class WorkspaceActivityManagerTest {

  private static final long DEFAULT_TIMEOUT = 60_000L; // 1 minute
  private static final long DEFAULT_RUN_TIMEOUT = 0; // No run timeout

  @Mock private WorkspaceManager workspaceManager;

  @Captor private ArgumentCaptor<EventSubscriber<WorkspaceCreatedEvent>> createEventCaptor;
  @Captor private ArgumentCaptor<EventSubscriber<WorkspaceStatusEvent>> statusChangeEventCaptor;
  @Captor private ArgumentCaptor<EventSubscriber<BeforeWorkspaceRemovedEvent>> removeEventCaptor;

  @Mock private Account account;
  @Mock private WorkspaceImpl workspace;
  @Mock private WorkspaceActivityDao workspaceActivityDao;

  @Mock private EventService eventService;

  private WorkspaceActivityManager activityManager;

  @BeforeMethod
  private void setUp() throws Exception {
    activityManager =
        new WorkspaceActivityManager(
            workspaceManager,
            workspaceActivityDao,
            eventService,
            DEFAULT_TIMEOUT,
            DEFAULT_RUN_TIMEOUT);

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
    final EventSubscriber<WorkspaceStatusEvent> subscriber = subscribeAndGetStatusEventSubscriber();

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
    final EventSubscriber<WorkspaceStatusEvent> subscriber = subscribeAndGetStatusEventSubscriber();

    subscriber.onEvent(
        DtoFactory.newDto(WorkspaceStatusEvent.class)
            .withStatus(WorkspaceStatus.STOPPED)
            .withWorkspaceId(wsId));

    verify(workspaceActivityDao, times(1)).removeExpiration(eq(wsId));
  }

  @Test
  public void shouldRecordWorkspaceCreation() throws Exception {
    String wsId = "1";

    EventSubscriber<WorkspaceCreatedEvent> subscriber = subscribeAndGetCreatedSubscriber();

    subscriber.onEvent(
        new WorkspaceCreatedEvent(
            DtoFactory.newDto(WorkspaceDto.class)
                .withId(wsId)
                .withAttributes(ImmutableMap.of(Constants.CREATED_ATTRIBUTE_NAME, "15"))));

    verify(workspaceActivityDao, times(1)).setCreatedTime(eq(wsId), eq(15L));
  }

  @Test(dataProvider = "wsStatus")
  public void shouldRecordWorkspaceStatusChange(WorkspaceStatus status) throws Exception {
    String wsId = "1";

    EventSubscriber<WorkspaceStatusEvent> subscriber = subscribeAndGetStatusEventSubscriber();

    subscriber.onEvent(
        DtoFactory.newDto(WorkspaceStatusEvent.class).withStatus(status).withWorkspaceId(wsId));

    verify(workspaceActivityDao, times(1)).setStatusChangeTime(eq(wsId), eq(status), anyLong());
  }

  @Test
  public void shouldRemoveActivityWhenWorkspaceRemoved() throws Exception {
    String wsId = "1";

    EventSubscriber<BeforeWorkspaceRemovedEvent> subscriber = subscribeAndGetRemoveSubscriber();

    subscriber.onEvent(
        new BeforeWorkspaceRemovedEvent(
            new WorkspaceImpl(DtoFactory.newDto(WorkspaceDto.class).withId(wsId), null)));

    verify(workspaceActivityDao, times(1)).removeActivity(eq(wsId));
  }

  @Test
  public void shouldCountWorkspacesInStatus() throws Exception {
    // given
    when(workspaceActivityDao.countWorkspacesInStatus(eq(WorkspaceStatus.STARTING), eq(0L)))
        .thenReturn(15L);

    // when
    long count = activityManager.countWorkspacesInStatus(WorkspaceStatus.STARTING, 0L);

    // then
    verify(workspaceActivityDao).countWorkspacesInStatus(eq(WorkspaceStatus.STARTING), eq(0L));
    assertEquals(15L, count);
  }

  @DataProvider(name = "wsStatus")
  public Object[][] getWorkspaceStatus() {
    return Stream.of(WorkspaceStatus.values())
        .map(s -> new WorkspaceStatus[] {s})
        .toArray(Object[][]::new);
  }

  private EventSubscriber<WorkspaceStatusEvent> subscribeAndGetStatusEventSubscriber() {
    subscribeToEventService();
    return statusChangeEventCaptor.getValue();
  }

  private EventSubscriber<WorkspaceCreatedEvent> subscribeAndGetCreatedSubscriber() {
    subscribeToEventService();
    return createEventCaptor.getValue();
  }

  private EventSubscriber<BeforeWorkspaceRemovedEvent> subscribeAndGetRemoveSubscriber() {
    subscribeToEventService();
    return removeEventCaptor.getValue();
  }

  private void subscribeToEventService() {
    activityManager.subscribe();
    verify(eventService).subscribe(createEventCaptor.capture(), eq(WorkspaceCreatedEvent.class));
    verify(eventService)
        .subscribe(statusChangeEventCaptor.capture(), eq(WorkspaceStatusEvent.class));
    verify(eventService)
        .subscribe(removeEventCaptor.capture(), eq(BeforeWorkspaceRemovedEvent.class));
  }
}
