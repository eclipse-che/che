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

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.WorkspaceRuntimes;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.shared.Constants;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(value = MockitoTestNGListener.class)
public class WorkspaceActivityCheckerTest {
  private static final long DEFAULT_TIMEOUT = 60_000L; // 1 minute
  private static final long DEFAULT_RUN_TIMEOUT = 0; // No default run timeout

  private ManualClock clock;
  private WorkspaceActivityChecker checker;
  @Mock private WorkspaceManager workspaceManager;
  @Mock private WorkspaceRuntimes workspaceRuntimes;
  @Mock private WorkspaceActivityDao workspaceActivityDao;
  @Mock private EventService eventService;

  @BeforeMethod
  public void setUp() throws Exception {
    clock = new ManualClock();

    WorkspaceActivityManager activityManager =
        new WorkspaceActivityManager(
            workspaceManager,
            workspaceActivityDao,
            eventService,
            DEFAULT_TIMEOUT,
            DEFAULT_RUN_TIMEOUT,
            clock);

    lenient()
        .when(workspaceActivityDao.getAll(anyInt(), anyLong()))
        .thenAnswer(
            inv -> {
              int maxItems = inv.getArgument(0);
              long skipCount = inv.getArgument(1);

              return new Page<WorkspaceActivity>(emptyList(), skipCount, maxItems, 0);
            });

    checker =
        new WorkspaceActivityChecker(
            workspaceActivityDao, workspaceManager, workspaceRuntimes, activityManager, clock);
  }

  @Test
  public void shouldStopAllExpiredWorkspaces() throws Exception {
    when(workspaceActivityDao.findExpiredIdle(anyLong())).thenReturn(asList("1", "2", "3"));

    checker.expire();

    verify(workspaceActivityDao, times(3)).removeExpiration(anyString());
    verify(workspaceActivityDao).removeExpiration(eq("1"));
    verify(workspaceActivityDao).removeExpiration(eq("2"));
    verify(workspaceActivityDao).removeExpiration(eq("3"));
  }

  @Test
  public void shouldRecreateMissingActivityRecord() throws Exception {
    // given
    String id = "1";
    when(workspaceRuntimes.getRunning()).thenReturn(singleton(id));
    when(workspaceActivityDao.findActivity(eq(id))).thenReturn(null);
    when(workspaceManager.getWorkspace(eq(id)))
        .thenReturn(
            WorkspaceImpl.builder()
                .setId(id)
                .setAttributes(ImmutableMap.of(Constants.CREATED_ATTRIBUTE_NAME, "15"))
                .build());

    // when
    clock.forward(Duration.of(1, ChronoUnit.SECONDS));
    checker.cleanup();

    // then
    ArgumentCaptor<WorkspaceActivity> captor = ArgumentCaptor.forClass(WorkspaceActivity.class);
    verify(workspaceActivityDao).createActivity(captor.capture());
    WorkspaceActivity created = captor.getValue();
    assertEquals(id, created.getWorkspaceId());
    assertEquals(Long.valueOf(15), created.getCreated());
    assertEquals(WorkspaceStatus.RUNNING, created.getStatus());
    assertNotNull(created.getLastRunning());
    assertEquals(clock.millis(), (long) created.getLastRunning());
    assertNotNull(created.getExpiration());
    assertEquals(clock.millis() + DEFAULT_TIMEOUT, (long) created.getExpiration());
  }

  @Test
  public void shouldRestoreCreatedTimeOnInvalidActivityRecord() throws Exception {
    // given
    String id = "1";
    WorkspaceActivity invalidActivity = new WorkspaceActivity();
    invalidActivity.setWorkspaceId(id);
    when(workspaceRuntimes.getRunning()).thenReturn(singleton(id));
    when(workspaceActivityDao.findActivity(eq(id))).thenReturn(invalidActivity);
    when(workspaceManager.getWorkspace(eq(id)))
        .thenReturn(
            WorkspaceImpl.builder()
                .setId(id)
                .setAttributes(ImmutableMap.of(Constants.CREATED_ATTRIBUTE_NAME, "15"))
                .build());

    // when
    checker.cleanup();

    // then
    verify(workspaceActivityDao).setCreatedTime(eq(id), eq(15L));
  }

  @Test
  public void shouldContinueCheckActivitiesValidityIfExceptionOccurredOnRestoringOne()
      throws Exception {
    // given
    String id = "1";
    WorkspaceActivity invalidActivity = new WorkspaceActivity();
    invalidActivity.setWorkspaceId(id);
    when(workspaceRuntimes.getRunning()).thenReturn(ImmutableSet.of("problematic", id));
    doThrow(new ServerException("error"))
        .when(workspaceActivityDao)
        .findActivity(eq("problematic"));
    doReturn(invalidActivity).when(workspaceActivityDao).findActivity(eq(id));
    when(workspaceManager.getWorkspace(eq(id)))
        .thenReturn(
            WorkspaceImpl.builder()
                .setId(id)
                .setAttributes(ImmutableMap.of(Constants.CREATED_ATTRIBUTE_NAME, "15"))
                .build());

    // when
    checker.cleanup();

    // then
    verify(workspaceActivityDao).setCreatedTime(eq(id), eq(15L));
  }

  @Test
  public void shouldRestoreLastRunningTimeOnInvalidActivityRecordUsingCreatedTime()
      throws Exception {
    // given
    String id = "1";
    WorkspaceActivity invalidActivity = new WorkspaceActivity();
    invalidActivity.setWorkspaceId(id);
    invalidActivity.setCreated(15);
    when(workspaceRuntimes.getRunning()).thenReturn(singleton(id));
    when(workspaceActivityDao.findActivity(eq(id))).thenReturn(invalidActivity);

    // when
    clock.forward(Duration.of(1, ChronoUnit.SECONDS));
    checker.cleanup();

    // then
    verify(workspaceActivityDao, never()).setCreatedTime(eq(id), anyLong());
    verify(workspaceActivityDao)
        .setStatusChangeTime(eq(id), eq(WorkspaceStatus.RUNNING), eq(clock.millis()));
  }

  @Test
  public void shouldRestoreLastRunningTimeOnInvalidActivityRecordUsingLastStartingTime()
      throws Exception {
    // given
    String id = "1";
    WorkspaceActivity invalidActivity = new WorkspaceActivity();
    invalidActivity.setWorkspaceId(id);
    invalidActivity.setLastStarting(10);
    when(workspaceRuntimes.getRunning()).thenReturn(singleton(id));
    when(workspaceActivityDao.findActivity(eq(id))).thenReturn(invalidActivity);
    when(workspaceManager.getWorkspace(eq(id)))
        .thenReturn(
            WorkspaceImpl.builder()
                .setId(id)
                .setAttributes(ImmutableMap.of(Constants.CREATED_ATTRIBUTE_NAME, "15"))
                .build());

    // when
    clock.forward(Duration.of(1, ChronoUnit.SECONDS));
    checker.cleanup();

    // then
    verify(workspaceActivityDao).setCreatedTime(eq(id), eq(15L));
    verify(workspaceActivityDao)
        .setStatusChangeTime(eq(id), eq(WorkspaceStatus.RUNNING), eq(clock.millis()));
  }

  @Test
  public void shouldRestoreExpirationTimeMoreThanASecondAfterRunning() throws Exception {
    long lastRunning = clock.millis();
    String id = "1";
    WorkspaceActivity invalidActivity = new WorkspaceActivity();
    invalidActivity.setWorkspaceId(id);
    invalidActivity.setCreated(clock.millis());
    invalidActivity.setLastRunning(lastRunning);
    when(workspaceRuntimes.getRunning()).thenReturn(singleton(id));
    when(workspaceActivityDao.findActivity(eq(id))).thenReturn(invalidActivity);

    // when
    clock.forward(Duration.of(1500, ChronoUnit.MILLIS));
    checker.cleanup();

    // then
    verify(workspaceActivityDao).setExpirationTime(eq(id), eq(lastRunning + DEFAULT_TIMEOUT));
  }

  @Test
  public void shouldNotRestoreExpirationTimeLessThanASecondAfterRunning() throws Exception {
    String id = "1";
    WorkspaceActivity invalidActivity = new WorkspaceActivity();
    invalidActivity.setWorkspaceId(id);
    invalidActivity.setCreated(clock.millis());
    invalidActivity.setLastRunning(clock.millis());
    when(workspaceRuntimes.getRunning()).thenReturn(singleton(id));
    when(workspaceActivityDao.findActivity(eq(id))).thenReturn(invalidActivity);

    // when
    clock.forward(Duration.of(900, ChronoUnit.MILLIS));
    checker.cleanup();

    // then
    verify(workspaceActivityDao, never()).setExpirationTime(anyString(), anyLong());
  }

  @Test
  public void shouldRestoreTrueStateOfWorkspaceIfActivityDoesntReflectThat() throws Exception {
    // given
    String wsId = "1";
    WorkspaceActivity activity = new WorkspaceActivity();
    activity.setCreated(clock.millis());
    activity.setWorkspaceId(wsId);
    activity.setStatus(WorkspaceStatus.STARTING);
    activity.setLastStarting(clock.millis());
    doAnswer(
            inv -> {
              int maxItems = inv.getArgument(0);
              long skipCount = inv.getArgument(1);

              if (skipCount < 1) {
                return new Page<>(singleton(activity), skipCount, maxItems, 1);
              } else {
                return new Page<>(emptyList(), skipCount, maxItems, 1);
              }
            })
        .when(workspaceActivityDao)
        .getAll(anyInt(), anyLong());

    when(workspaceRuntimes.getStatus(eq(wsId))).thenReturn(WorkspaceStatus.STOPPED);

    // when
    checker.cleanup();

    // then
    verify(workspaceActivityDao)
        .setStatusChangeTime(eq(wsId), eq(WorkspaceStatus.STOPPED), eq(clock.millis()));
  }

  @Test
  public void shouldContinueReconcileStatusesWhenExceptionOccurredOnOne() throws Exception {
    // given
    String wsId1 = "1";
    WorkspaceActivity activity1 = new WorkspaceActivity();
    activity1.setCreated(clock.millis());
    activity1.setWorkspaceId(wsId1);
    activity1.setStatus(WorkspaceStatus.STARTING);
    activity1.setLastStarting(clock.millis());

    String wsId2 = "2";
    WorkspaceActivity activity2 = new WorkspaceActivity();
    activity2.setCreated(clock.millis());
    activity2.setWorkspaceId(wsId2);
    activity2.setStatus(WorkspaceStatus.STARTING);
    activity2.setLastStarting(clock.millis());
    doAnswer(
            inv -> {
              int maxItems = inv.getArgument(0);
              long skipCount = inv.getArgument(1);

              switch ((int) skipCount) {
                case 0:
                  return new Page<>(singleton(activity1), 0, 1, 2);
                case 1:
                  return new Page<>(singleton(activity2), 1, 1, 2);
                default:
                  return new Page<>(emptyList(), skipCount, maxItems, 2);
              }
            })
        .when(workspaceActivityDao)
        .getAll(anyInt(), anyLong());

    doReturn(WorkspaceStatus.STOPPED).when(workspaceRuntimes).getStatus(any());
    doThrow(new ServerException("Error"))
        .when(workspaceActivityDao)
        .setStatusChangeTime(eq(wsId1), any(WorkspaceStatus.class), anyLong());

    // when
    checker.cleanup();

    // then
    verify(workspaceActivityDao)
        .setStatusChangeTime(eq(wsId2), eq(WorkspaceStatus.STOPPED), eq(clock.millis()));
  }

  @Test
  public void shouldNotThrowExceptionWhenErrorOccurredDuringActivitiesListingOnReconciling()
      throws Exception {
    // given
    doThrow(new ServerException("error")).when(workspaceActivityDao).getAll(anyInt(), anyLong());

    // when
    checker.cleanup();
  }

  private static final class ManualClock extends Clock {

    private Instant instant;

    private ManualClock() {
      instant = Instant.now();
    }

    @Override
    public ZoneId getZone() {
      return ZoneId.systemDefault();
    }

    @Override
    public Clock withZone(ZoneId zone) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Instant instant() {
      return instant;
    }

    public void forward(Duration duration) {
      instant = instant.plus(duration);
    }
  }
}
