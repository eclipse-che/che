/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.metrics;

import static java.util.Arrays.asList;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;
import org.eclipse.che.dto.server.DtoFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class WorkspaceStopTrackerMeterBinderTest {

  private EventService eventService;
  private MeterRegistry registry;
  private WorkspaceStopTrackerMeterBinder meterBinder;
  private Map<String, Long> workspaceStopTime;

  @BeforeMethod
  public void setUp() {
    eventService = new EventService();
    registry = new SimpleMeterRegistry();
    workspaceStopTime = new ConcurrentHashMap<>();
    meterBinder = new WorkspaceStopTrackerMeterBinder(eventService, workspaceStopTime);
    meterBinder.bindTo(registry);
  }

  @Test
  public void shouldRecordWorkspaceStopTime() {
    // given
    // when
    eventService.publish(
        DtoFactory.newDto(WorkspaceStatusEvent.class)
            .withPrevStatus(WorkspaceStatus.RUNNING)
            .withStatus(WorkspaceStatus.STOPPING)
            .withWorkspaceId("id1"));
    // then
    Assert.assertTrue(workspaceStopTime.containsKey("id1"));
  }

  @Test(dataProvider = "allStatusTransitionsWithoutStarting")
  public void shouldNotRecordWorkspaceStopTimeForNonStoppingStatuses(
      WorkspaceStatus from, WorkspaceStatus to) {
    // given
    // when
    eventService.publish(
        DtoFactory.newDto(WorkspaceStatusEvent.class)
            .withPrevStatus(from)
            .withStatus(to)
            .withWorkspaceId("id1"));
    // then
    Assert.assertTrue(workspaceStopTime.isEmpty());
  }

  @Test
  public void shouldCountSuccessfulStart() {
    // given
    workspaceStopTime.put("id1", System.currentTimeMillis() - 60 * 1000);
    // when
    eventService.publish(
        DtoFactory.newDto(WorkspaceStatusEvent.class)
            .withPrevStatus(WorkspaceStatus.STOPPING)
            .withStatus(WorkspaceStatus.STOPPED)
            .withWorkspaceId("id1"));

    // then

    Timer t = registry.find("che.workspace.stop.time").tag("result", "success").timer();
    Assert.assertEquals(t.count(), 1);
    Assert.assertTrue(t.totalTime(TimeUnit.MILLISECONDS) >= 60 * 1000);
  }

  @DataProvider
  public Object[][] allStatusTransitionsWithoutStarting() {
    List<List<WorkspaceStatus>> transitions = new ArrayList<>(9);

    for (WorkspaceStatus from : WorkspaceStatus.values()) {
      for (WorkspaceStatus to : WorkspaceStatus.values()) {
        if ((from == WorkspaceStatus.RUNNING || from == WorkspaceStatus.STARTING)
            && to == WorkspaceStatus.STOPPING) {
          continue;
        }

        transitions.add(asList(from, to));
      }
    }

    return transitions.stream().map(List::toArray).toArray(Object[][]::new);
  }
}
