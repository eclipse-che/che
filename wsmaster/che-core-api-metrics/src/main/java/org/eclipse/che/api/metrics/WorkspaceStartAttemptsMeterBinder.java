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

import static java.lang.Boolean.FALSE;
import static org.eclipse.che.api.metrics.WorkspaceBinders.withStandardTags;
import static org.eclipse.che.api.metrics.WorkspaceBinders.workspaceMetric;
import static org.eclipse.che.api.workspace.shared.Constants.DEBUG_WORKSPACE_START;

import com.google.inject.Inject;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;

/** Counts number of attempts to start workspace. */
@Singleton
public class WorkspaceStartAttemptsMeterBinder implements MeterBinder {
  private final EventService eventService;

  private Counter startingCounter;
  private Counter startingDebugCounter;

  @Inject
  public WorkspaceStartAttemptsMeterBinder(EventService eventService) {
    this.eventService = eventService;
  }

  @Override
  public void bindTo(MeterRegistry registry) {
    startingCounter =
        Counter.builder(workspaceMetric("starting_attempts.total"))
            .tags(withStandardTags("debug", "false"))
            .description("The count of workspaces start attempts")
            .register(registry);
    startingDebugCounter =
        Counter.builder(workspaceMetric("starting_attempts.total"))
            .tags(withStandardTags("debug", "true"))
            .description("The count of workspaces start attempts in debug mode")
            .register(registry);

    // only subscribe to the event once we have the counters ready
    eventService.subscribe(
        event -> {
          if (event.getPrevStatus() == WorkspaceStatus.STOPPED
              && event.getStatus() == WorkspaceStatus.STARTING) {
            if (event.getOptions() != null
                && Boolean.parseBoolean(
                    event.getOptions().getOrDefault(DEBUG_WORKSPACE_START, FALSE.toString()))) {
              startingDebugCounter.increment();
            } else {
              startingCounter.increment();
            }
          }
        },
        WorkspaceStatusEvent.class);
  }
}
