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

import static org.eclipse.che.api.metrics.WorkspaceBinders.withStandardTags;
import static org.eclipse.che.api.metrics.WorkspaceBinders.workspaceMetric;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;

/**
 * Counts errors in workspaces while in different statuses. I.e. the errors while starting, running
 * or stopping are counted separately. The counter IDs only differ in the "while" tag which
 * specifies the workspace status in which the failure occurred.
 */
@Singleton
public class WorkspaceFailureMeterBinder implements MeterBinder {

  private final EventService eventService;

  private Counter startingStoppedFailureCounter;
  private Counter stoppingStoppedFailureCounter;
  private Counter runningStoppedFailureCounter;

  @Inject
  public WorkspaceFailureMeterBinder(EventService eventService) {
    this.eventService = eventService;
  }

  @Override
  public void bindTo(MeterRegistry registry) {
    startingStoppedFailureCounter = bindFailureFrom(WorkspaceStatus.STARTING, registry);
    runningStoppedFailureCounter = bindFailureFrom(WorkspaceStatus.RUNNING, registry);
    stoppingStoppedFailureCounter = bindFailureFrom(WorkspaceStatus.STOPPING, registry);

    // only subscribe to the event once we have the counters ready
    eventService.subscribe(
        event -> {
          if (event.getError() == null
              || event.getStatus() != WorkspaceStatus.STOPPED
              || event.isInitiatedByUser()) {
            return;
          }
          Counter counter;
          switch (event.getPrevStatus()) {
            case STARTING:
              counter = startingStoppedFailureCounter;
              break;
            case RUNNING:
              counter = runningStoppedFailureCounter;
              break;
            case STOPPING:
              counter = stoppingStoppedFailureCounter;
              break;
            default:
              return;
          }

          counter.increment();
        },
        WorkspaceStatusEvent.class);
  }

  private Counter bindFailureFrom(WorkspaceStatus previousState, MeterRegistry registry) {
    // there's apparently a convention to suffix the counters with "_total" (which is what the name
    // will end up looking like).
    return Counter.builder(workspaceMetric("failure.total"))
        .tags(withStandardTags("while", previousState.name()))
        .description("The count of failed workspaces")
        .register(registry);
  }
}
