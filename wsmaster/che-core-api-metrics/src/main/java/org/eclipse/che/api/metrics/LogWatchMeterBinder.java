/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
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
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.log.event.WorkspaceStartedInDebugModeEvent;

public class LogWatchMeterBinder implements MeterBinder {

  private Counter workspaceStartDebugMode;

  @Inject
  public LogWatchMeterBinder(EventService eventService) {
    eventService.subscribe(
        event -> workspaceStartDebugMode.increment(), WorkspaceStartedInDebugModeEvent.class);
  }

  @Override
  public void bindTo(MeterRegistry registry) {
    workspaceStartDebugMode =
        Counter.builder(workspaceMetric("starting_attempts.total"))
            .tags(withStandardTags("debug", "true"))
            .description("The count of workspaces start attempts in debug mode")
            .register(registry);
  }
}
