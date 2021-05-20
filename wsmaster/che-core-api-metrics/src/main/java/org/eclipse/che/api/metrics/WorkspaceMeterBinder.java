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

import static org.eclipse.che.api.metrics.WorkspaceBinders.workspaceMetric;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.workspace.server.WorkspaceManager;

/** Provides metrics of workspace. */
@Singleton
public class WorkspaceMeterBinder implements MeterBinder {
  private final WorkspaceManager workspaceManager;

  @Inject
  public WorkspaceMeterBinder(WorkspaceManager workspaceManager) {
    this.workspaceManager = workspaceManager;
  }

  @Override
  public void bindTo(MeterRegistry registry) {
    Gauge.builder(workspaceMetric("total"), this::count)
        .description("Total number of workspaces")
        .register(registry);
  }

  private double count() {
    try {
      return workspaceManager.getWorkspacesTotalCount();
    } catch (ServerException e) {
      return Double.NaN;
    }
  }
}
