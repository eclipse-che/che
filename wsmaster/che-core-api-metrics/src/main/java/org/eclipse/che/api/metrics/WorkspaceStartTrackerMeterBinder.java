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
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link MeterBinder} that is providing metrics about workspace successful or failed start time.
 */
@Singleton
public class WorkspaceStartTrackerMeterBinder implements MeterBinder {

  private static final Logger LOG = LoggerFactory.getLogger(WorkspaceStartTrackerMeterBinder.class);

  private final EventService eventService;

  private final Map<String, Long> workspaceStartTime;
  private Timer successTimer;
  private Timer failTimer;

  @Inject
  public WorkspaceStartTrackerMeterBinder(EventService eventService) {
    this(eventService, new ConcurrentHashMap<>());
  }

  @VisibleForTesting
  WorkspaceStartTrackerMeterBinder(
      EventService eventService, Map<String, Long> workspaceStartTime) {
    this.eventService = eventService;
    this.workspaceStartTime = workspaceStartTime;
  }

  @Override
  public void bindTo(MeterRegistry registry) {

    successTimer =
        Timer.builder(workspaceMetric("start.time"))
            .description("The time of workspace start")
            .tags(withStandardTags("result", "success"))
            .publishPercentileHistogram()
            .sla(Duration.ofSeconds(10))
            .minimumExpectedValue(Duration.ofSeconds(10))
            .maximumExpectedValue(Duration.ofMinutes(15))
            .register(registry);

    failTimer =
        Timer.builder(workspaceMetric("start.time"))
            .description("The time of workspace start")
            .tags(withStandardTags("result", "fail"))
            .publishPercentileHistogram()
            .sla(Duration.ofSeconds(10))
            .minimumExpectedValue(Duration.ofSeconds(10))
            .maximumExpectedValue(Duration.ofMinutes(15))
            .register(registry);

    // only subscribe to the event once we have the counters ready
    eventService.subscribe(this::handleWorkspaceStatusChange, WorkspaceStatusEvent.class);
  }

  private void handleWorkspaceStatusChange(WorkspaceStatusEvent event) {
    if (event.getPrevStatus() == WorkspaceStatus.STOPPED
        && event.getStatus() == WorkspaceStatus.STARTING) {
      workspaceStartTime.put(event.getWorkspaceId(), System.currentTimeMillis());
    } else if (event.getPrevStatus() == WorkspaceStatus.STARTING) {
      Long startTime = workspaceStartTime.remove(event.getWorkspaceId());
      if (startTime == null) {
        // this situation is possible when starting workspace is recovered or some bug happened
        LOG.warn("No workspace start time recorded for workspace {}", event.getWorkspaceId());
        return;
      }

      if (event.getStatus() == WorkspaceStatus.RUNNING) {
        successTimer.record(Duration.ofMillis(System.currentTimeMillis() - startTime));
      } else if (event.getStatus() == WorkspaceStatus.STOPPED) {
        failTimer.record(Duration.ofMillis(System.currentTimeMillis() - startTime));
      }
    }
  }
}
