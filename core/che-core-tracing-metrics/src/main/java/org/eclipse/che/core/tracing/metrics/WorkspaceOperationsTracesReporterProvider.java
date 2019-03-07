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
package org.eclipse.che.core.tracing.metrics;

import io.opentracing.contrib.metrics.micrometer.MicrometerMetricsReporter;
import io.opentracing.tag.Tags;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.eclipse.che.commons.tracing.TracingTags;

@Singleton
/**
 * Provider of {@link MicrometerMetricsReporter}, which is responsible for reporting metrics of
 * traced spans to prometheus server. Here is also specified configuration as for metrics name and
 * tags.
 */
public class WorkspaceOperationsTracesReporterProvider
    implements Provider<MicrometerMetricsReporter> {

  private static final String TRACING_METRIC_NAME = "che_server_api_workspace_tracing_span";

  private final MicrometerMetricsReporter workspaceOperationsTracesReporterProvider;

  public WorkspaceOperationsTracesReporterProvider() {
    workspaceOperationsTracesReporterProvider =
        MicrometerMetricsReporter.newMetricsReporter()
            .withName(TRACING_METRIC_NAME)
            .withTagLabel(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER)
            .withTagLabel(TracingTags.WORKSPACE_ID.getKey(), "null")
            .withTagLabel(TracingTags.MACHINE_NAME.getKey(), "null")
            .withTagLabel(TracingTags.STOPPED_BY.getKey(), "null")
            .withBaggageLabel(TracingTags.USER_ID.getKey(), "null")
            .build();
  }

  @Override
  public MicrometerMetricsReporter get() {
    return workspaceOperationsTracesReporterProvider;
  }
}
