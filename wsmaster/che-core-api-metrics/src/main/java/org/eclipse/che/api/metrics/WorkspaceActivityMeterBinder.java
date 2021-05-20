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

import static org.eclipse.che.api.metrics.WorkspaceBinders.withStandardTags;
import static org.eclipse.che.api.metrics.WorkspaceBinders.workspaceMetric;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.workspace.activity.WorkspaceActivityManager;

/** Provides metrics of workspace activity. */
@Singleton
public class WorkspaceActivityMeterBinder implements MeterBinder {

  private final WorkspaceActivityManager activityManager;

  @Inject
  public WorkspaceActivityMeterBinder(WorkspaceActivityManager activityManager) {
    this.activityManager = activityManager;
  }

  @Override
  public void bindTo(MeterRegistry registry) {
    for (WorkspaceStatus s : WorkspaceStatus.values()) {
      Gauge.builder(workspaceMetric("status"), () -> count(s))
          .tags(withStandardTags("status", s.name()))
          .description("The number of workspaces in a given status")
          .register(registry);
    }
  }

  private double count(WorkspaceStatus status) {
    try {
      return activityManager.countWorkspacesInStatus(status, System.currentTimeMillis());
    } catch (ServerException e) {
      return Double.NaN;
    }
  }
}
