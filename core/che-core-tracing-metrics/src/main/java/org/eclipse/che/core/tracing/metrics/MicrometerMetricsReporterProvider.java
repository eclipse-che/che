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
package org.eclipse.che.core.tracing.metrics;

import io.opentracing.contrib.metrics.micrometer.MicrometerMetricsReporter;
import io.opentracing.tag.Tags;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Provider of {@link MicrometerMetricsReporter}, which is responsible for reporting metrics of
 * traced spans to prometheus server. Here is also specified configuration as for metrics name and
 * tags.
 *
 * <p>This reporter is configured to report all spans with "span.kind" server, as well as provide
 * additional label "http.status_code", if such tag is available in the span.
 *
 * <p>When defining tags , if "Default value" will be null, then the spans which don't have such
 * tag, then will not be reported.
 *
 * <p>Visit https://github.com/opentracing-contrib/java-metrics to find out about how to configure
 * reporter.
 */
@Singleton
public class MicrometerMetricsReporterProvider implements Provider<MicrometerMetricsReporter> {

  private static final String TRACING_METRIC_NAME = "che_server_api_tracing_span";

  private final MicrometerMetricsReporter micrometerMetricsReporter;

  public MicrometerMetricsReporterProvider() {
    micrometerMetricsReporter =
        MicrometerMetricsReporter.newMetricsReporter()
            .withName(TRACING_METRIC_NAME)
            .withTagLabel(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER)
            .withTagLabel(Tags.HTTP_STATUS.getKey(), "undefined")
            .build();
  }

  @Override
  public MicrometerMetricsReporter get() {
    return micrometerMetricsReporter;
  }
}
