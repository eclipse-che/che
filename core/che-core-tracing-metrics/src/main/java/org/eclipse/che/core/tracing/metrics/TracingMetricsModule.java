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

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import io.opentracing.contrib.metrics.MetricsReporter;
import org.eclipse.che.core.tracing.TracerProvider;

public class TracingMetricsModule extends AbstractModule {
  @Override
  protected void configure() {

    bind(TracerProvider.class).toProvider(MeteredTracerProvider.class);

    Multibinder<MetricsReporter> metricsReporterBinder =
        Multibinder.newSetBinder(binder(), MetricsReporter.class);
    metricsReporterBinder.addBinding().toProvider(MicrometerMetricsReporterProvider.class);
  }
}
