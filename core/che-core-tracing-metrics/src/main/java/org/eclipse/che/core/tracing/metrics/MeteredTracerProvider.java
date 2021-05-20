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
package org.eclipse.che.core.tracing.metrics;

import io.jaegertracing.Configuration;
import io.jaegertracing.micrometer.MicrometerMetricsFactory;
import io.opentracing.Tracer;
import io.opentracing.contrib.metrics.Metrics;
import io.opentracing.contrib.metrics.MetricsReporter;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.eclipse.che.core.tracing.TracerProvider;

/**
 * Provider of {@link Tracer}, that is integrated with metrics.
 *
 * <p>Tracer instance is created with custom metrics factory, so it would expose internal metrics to
 * prometheus server
 *
 * <p>Tracer is also wrapped in custom metrics reporter, which would report data about traced spans
 * and expose them as prometheus metrics
 */
@Singleton
public class MeteredTracerProvider implements Provider<TracerProvider> {
  private TracerProvider tracerProvider;

  @Inject
  public MeteredTracerProvider(Set<MetricsReporter> metricsReporter) {
    MicrometerMetricsFactory internalMetricsFactory = new MicrometerMetricsFactory();
    Configuration configuration = Configuration.fromEnv();
    Tracer tracer =
        configuration.getTracerBuilder().withMetricsFactory(internalMetricsFactory).build();

    this.tracerProvider = new TracerProvider(Metrics.decorate(tracer, metricsReporter));
  }

  @Override
  public TracerProvider get() {
    return tracerProvider;
  }
}
