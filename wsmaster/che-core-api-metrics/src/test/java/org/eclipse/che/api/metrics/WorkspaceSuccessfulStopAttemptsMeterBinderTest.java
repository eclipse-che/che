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

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;
import org.eclipse.che.dto.server.DtoFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class WorkspaceSuccessfulStopAttemptsMeterBinderTest {

  private EventService eventService;
  private MeterRegistry registry;

  @BeforeMethod
  public void setUp() {
    eventService = new EventService();
    registry = new SimpleMeterRegistry();
  }

  @Test(dataProvider = "allStatusTransitionsWithoutStopping")
  public void shouldNotCollectEvents(WorkspaceStatus from, WorkspaceStatus to) {
    // given
    WorkspaceSuccessfulStopAttemptsMeterBinder meterBinder =
        new WorkspaceSuccessfulStopAttemptsMeterBinder(eventService);
    meterBinder.bindTo(registry);

    // when

    eventService.publish(
        DtoFactory.newDto(WorkspaceStatusEvent.class)
            .withPrevStatus(from)
            .withStatus(to)
            .withWorkspaceId("id1"));

    // then
    Counter successful = registry.find("che.workspace.stopped.total").counter();
    Assert.assertEquals(successful.count(), 0.0);
  }

  @Test
  public void shouldCollectOnlyStoppedWithoutError() {
    // given
    WorkspaceSuccessfulStopAttemptsMeterBinder meterBinder =
        new WorkspaceSuccessfulStopAttemptsMeterBinder(eventService);
    meterBinder.bindTo(registry);

    // when
    eventService.publish(
        DtoFactory.newDto(WorkspaceStatusEvent.class)
            .withPrevStatus(WorkspaceStatus.STOPPING)
            .withStatus(WorkspaceStatus.STOPPED)
            .withWorkspaceId("id1"));
    // then
    Counter successful = registry.find("che.workspace.stopped.total").counter();
    Assert.assertEquals(successful.count(), 1.0);
  }

  @Test
  public void shouldNotCollectStoppedWithError() {
    // given
    WorkspaceSuccessfulStopAttemptsMeterBinder meterBinder =
        new WorkspaceSuccessfulStopAttemptsMeterBinder(eventService);
    meterBinder.bindTo(registry);

    // when
    eventService.publish(
        DtoFactory.newDto(WorkspaceStatusEvent.class)
            .withPrevStatus(WorkspaceStatus.STOPPING)
            .withStatus(WorkspaceStatus.STOPPED)
            .withError("Error during workspace stop")
            .withWorkspaceId("id1"));
    // then
    Counter successful = registry.find("che.workspace.stopped.total").counter();
    Assert.assertEquals(successful.count(), 0.0);
  }

  @DataProvider
  public Object[][] allStatusTransitionsWithoutStopping() {
    List<List<WorkspaceStatus>> transitions = new ArrayList<>(9);

    for (WorkspaceStatus from : WorkspaceStatus.values()) {
      for (WorkspaceStatus to : WorkspaceStatus.values()) {
        if (from == WorkspaceStatus.STOPPING && to == WorkspaceStatus.STOPPED) {
          continue;
        }

        transitions.add(asList(from, to));
      }
    }

    return transitions.stream().map(List::toArray).toArray(Object[][]::new);
  }
}
