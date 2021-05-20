/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;
import org.eclipse.che.dto.server.DtoFactory;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class WorkspaceFailureMeterBinderTest {

  private Collection<Counter> failureCounters;
  private EventSubscriber<WorkspaceStatusEvent> events;

  @BeforeMethod
  public void setup() {
    MeterRegistry registry = new SimpleMeterRegistry();

    EventService eventService = mock(EventService.class);

    WorkspaceFailureMeterBinder meterBinder = new WorkspaceFailureMeterBinder(eventService);

    meterBinder.bindTo(registry);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<EventSubscriber<WorkspaceStatusEvent>> statusChangeEventCaptor =
        ArgumentCaptor.forClass(EventSubscriber.class);

    failureCounters = registry.find("che.workspace.failure.total").counters();

    verify(eventService)
        .subscribe(statusChangeEventCaptor.capture(), eq(WorkspaceStatusEvent.class));

    events = statusChangeEventCaptor.getValue();
  }

  @Test(dataProvider = "failureWhileInStatus")
  public void shouldCollectFailureCountsPerStatus(WorkspaceStatus failureStatus) {
    events.onEvent(
        DtoFactory.newDto(WorkspaceStatusEvent.class)
            .withPrevStatus(failureStatus)
            .withStatus(WorkspaceStatus.STOPPED)
            .withError("D'oh!")
            .withWorkspaceId("1"));

    List<Counter> restOfCounters = new ArrayList<>(failureCounters);

    Counter counter =
        failureCounters
            .stream()
            .filter(c -> failureStatus.name().equals(c.getId().getTag("while")))
            .findAny()
            .orElseThrow(
                () ->
                    new AssertionError(
                        "Could not find a counter for failure status " + failureStatus));

    restOfCounters.remove(counter);

    assertEquals(counter.count(), 1d);
    restOfCounters.forEach(c -> assertEquals(c.count(), 0d));
  }

  @Test(dataProvider = "failureWhileInStatus")
  public void shouldNotCollectFailureWhenNoErrorInEvent(WorkspaceStatus prevStatus) {
    events.onEvent(
        DtoFactory.newDto(WorkspaceStatusEvent.class)
            .withPrevStatus(prevStatus)
            .withStatus(WorkspaceStatus.STOPPED)
            .withWorkspaceId("1"));

    failureCounters.forEach(c -> assertEquals(c.count(), 0d));
  }

  @Test
  public void shouldNotCollectInterruptedEvent() {
    // given
    WorkspaceStatusEvent event =
        DtoFactory.newDto(WorkspaceStatusEvent.class)
            .withPrevStatus(WorkspaceStatus.STARTING)
            .withStatus(WorkspaceStatus.STOPPED)
            .withInitiatedByUser(true)
            .withError("interrupted")
            .withWorkspaceId("1");

    // when
    events.onEvent(event);

    // then
    failureCounters.forEach(c -> assertEquals(c.count(), 0d));
  }

  @Test(dataProvider = "allStatusTransitionsWithoutToStopped")
  public void shouldNotCollectFailureWhenNotTransitioningToStopped(
      WorkspaceStatus from, WorkspaceStatus to) {
    // This really doesn't make much sense because the codebase always transitions the workspace
    // to STOPPED on any kind of failure. This is just a precaution that a potential bug in the
    // rest of the codebase doesn't affect the metric collection ;)

    events.onEvent(
        DtoFactory.newDto(WorkspaceStatusEvent.class)
            .withPrevStatus(from)
            .withStatus(to)
            .withError("D'oh!")
            .withWorkspaceId("1"));

    failureCounters.forEach(c -> assertEquals(c.count(), 0d));
  }

  @DataProvider
  public Object[][] failureWhileInStatus() {
    return new Object[][] {
      new Object[] {WorkspaceStatus.STARTING},
      new Object[] {WorkspaceStatus.RUNNING},
      new Object[] {WorkspaceStatus.STOPPING},
    };
  }

  @DataProvider
  public Object[][] allStatusTransitionsWithoutToStopped() {
    List<List<WorkspaceStatus>> transitions = new ArrayList<>(9);

    for (WorkspaceStatus from : WorkspaceStatus.values()) {
      for (WorkspaceStatus to : WorkspaceStatus.values()) {
        if (from == to || to == WorkspaceStatus.STOPPED) {
          continue;
        }

        transitions.add(asList(from, to));
      }
    }

    return transitions.stream().map(List::toArray).toArray(Object[][]::new);
  }
}
