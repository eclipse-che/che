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
package org.eclipse.che.api.metrics;

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;
import org.eclipse.che.dto.server.DtoFactory;
import org.mockito.ArgumentCaptor;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class WorkspaceInterruptedStartAttemptsMeterBinderTest {

  private EventSubscriber<WorkspaceStatusEvent> events;
  private Counter interruptedCounter;

  @BeforeMethod
  public void setup() {
    MeterRegistry registry = new SimpleMeterRegistry();

    EventService eventService = mock(EventService.class);

    WorkspaceInterruptedStartAttemptsMeterBinder meterBinder =
        new WorkspaceInterruptedStartAttemptsMeterBinder(eventService);

    meterBinder.bindTo(registry);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<EventSubscriber<WorkspaceStatusEvent>> statusChangeEventCaptor =
        ArgumentCaptor.forClass(EventSubscriber.class);

    interruptedCounter = registry.find("che.workspace.start.interrupt.total").counter();

    verify(eventService)
        .subscribe(statusChangeEventCaptor.capture(), eq(WorkspaceStatusEvent.class));

    events = statusChangeEventCaptor.getValue();
  }

  @Test
  public void shouldCountWorkspaceInterruptedEvent() {
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
    Assert.assertEquals(interruptedCounter.count(), 1.0);
  }

  @Test
  public void shouldNotCountWorkspaceNonInterruptedEvent() {
    // given
    WorkspaceStatusEvent event =
        DtoFactory.newDto(WorkspaceStatusEvent.class)
            .withPrevStatus(WorkspaceStatus.STARTING)
            .withStatus(WorkspaceStatus.STOPPED)
            .withInitiatedByUser(false)
            .withError("interrupted")
            .withWorkspaceId("1");

    // when
    events.onEvent(event);

    // then
    Assert.assertEquals(interruptedCounter.count(), 0.0);
  }

  @Test(dataProvider = "allStatusTransitionsWithoutToStopped")
  public void shouldNotCollectInterruptionWhenNotTransitioningToStopped(
      WorkspaceStatus from, WorkspaceStatus to) {
    // This really doesn't make much sense because the codebase always transitions the workspace
    // to STOPPED on interruption. This is just a precaution that a potential bug in the
    // rest of the codebase doesn't affect the metric collection ;)

    events.onEvent(
        DtoFactory.newDto(WorkspaceStatusEvent.class)
            .withPrevStatus(from)
            .withStatus(to)
            .withInitiatedByUser(true)
            .withError("D'oh!")
            .withWorkspaceId("1"));

    Assert.assertEquals(interruptedCounter.count(), 0.0);
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
