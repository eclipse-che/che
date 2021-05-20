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

import static org.eclipse.che.api.metrics.WorkspaceBinders.workspaceMetric;

import com.google.inject.Inject;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;

/** Counts number of successfully stopped workspaces. */
@Singleton
public class WorkspaceSuccessfulStopAttemptsMeterBinder implements MeterBinder {
  private final EventService eventService;

  private Counter stoppedCounter;

  @Inject
  public WorkspaceSuccessfulStopAttemptsMeterBinder(EventService eventService) {
    this.eventService = eventService;
  }

  @Override
  public void bindTo(MeterRegistry registry) {
    stoppedCounter =
        Counter.builder(workspaceMetric("stopped.total"))
            .description("The count of stopped workspaces")
            .register(registry);

    // only subscribe to the event once we have the counters ready
    eventService.subscribe(
        event -> {
          if ((event.getError() == null)
              && (event.getPrevStatus() == WorkspaceStatus.STOPPING
                  && event.getStatus() == WorkspaceStatus.STOPPED)) {
            stoppedCounter.increment();
          }
        },
        WorkspaceStatusEvent.class);
  }
}
