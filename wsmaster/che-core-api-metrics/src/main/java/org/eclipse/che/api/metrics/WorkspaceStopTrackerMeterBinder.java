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

import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.*;
import static org.eclipse.che.api.metrics.WorkspaceBinders.withStandardTags;
import static org.eclipse.che.api.metrics.WorkspaceBinders.workspaceMetric;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.MeterBinder;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Singleton;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link MeterBinder} that is providing metrics about workspace stop time.
 *
 * <p>We're measuring these state transitions:
 *
 * <pre>
 *   RUNNING -> STOPPING -> STOPPED
 *   STARTING -> STOPPING -> STOPPED
 * </pre>
 */
@Singleton
public class WorkspaceStopTrackerMeterBinder implements MeterBinder {

  private static final Logger LOG = LoggerFactory.getLogger(WorkspaceStopTrackerMeterBinder.class);

  private final EventService eventService;

  private final Map<String, Long> workspaceStopTime;
  private Timer stopTimer;

  @Inject
  public WorkspaceStopTrackerMeterBinder(EventService eventService) {
    this(eventService, new ConcurrentHashMap<>());
  }

  @VisibleForTesting
  WorkspaceStopTrackerMeterBinder(EventService eventService, Map<String, Long> workspaceStopTime) {
    this.eventService = eventService;
    this.workspaceStopTime = workspaceStopTime;
  }

  @Override
  public void bindTo(MeterRegistry registry) {

    stopTimer =
        Timer.builder(workspaceMetric("stop.time"))
            .description("The time of workspace stop")
            .tags(withStandardTags("result", "success"))
            .register(registry);

    // only subscribe to the event once we have the counters ready
    eventService.subscribe(this::handleWorkspaceStatusChange, WorkspaceStatusEvent.class);
  }

  private void handleWorkspaceStatusChange(WorkspaceStatusEvent event) {
    if ((event.getPrevStatus() == RUNNING || event.getPrevStatus() == STARTING)
        && event.getStatus() == STOPPING) {
      workspaceStopTime.put(event.getWorkspaceId(), System.currentTimeMillis());
    } else if (event.getPrevStatus() == STOPPING) {
      Long stopTime = workspaceStopTime.remove(event.getWorkspaceId());
      if (stopTime == null) {
        LOG.warn("No workspace stop time recorded for workspace {}", event.getWorkspaceId());
        return;
      }

      if (event.getStatus() == STOPPED) {
        stopTimer.record(Duration.ofMillis(System.currentTimeMillis() - stopTime));
      } else {
        LOG.error("Unexpected change of status from STOPPING to {}", event.getStatus());
        return;
      }
    }
  }
}
