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

import static org.eclipse.che.api.metrics.WorkspaceBinders.workspaceMetric;

import com.google.inject.Inject;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;

/** Counts number of successfully started workspaces. */
@Singleton
public class WorkspaceSuccessfulStartAttemptsMeterBinder implements MeterBinder {
  private final EventService eventService;

  private Counter startedCounter;

  @Inject
  public WorkspaceSuccessfulStartAttemptsMeterBinder(EventService eventService) {
    this.eventService = eventService;
  }

  @Override
  public void bindTo(MeterRegistry registry) {
    startedCounter =
        Counter.builder(workspaceMetric("started.total"))
            .description("The count of started workspaces")
            .register(registry);

    // only subscribe to the event once we have the counters ready
    eventService.subscribe(
        event -> {
          if (event.getPrevStatus() == WorkspaceStatus.STARTING
              && event.getStatus() == WorkspaceStatus.RUNNING) {
            startedCounter.increment();
          }
        },
        WorkspaceStatusEvent.class);
  }
}
