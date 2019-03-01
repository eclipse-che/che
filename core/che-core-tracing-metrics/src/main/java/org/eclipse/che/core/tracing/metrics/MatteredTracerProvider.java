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

import io.opentracing.contrib.metrics.Metrics;
import io.opentracing.contrib.metrics.MetricsReporter;
import io.opentracing.contrib.tracerresolver.TracerResolver;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.eclipse.che.core.tracing.TracerProvider;

@Singleton
public class MatteredTracerProvider implements Provider<TracerProvider> {
  private TracerProvider tracerProvider;

  @Inject
  public MatteredTracerProvider(Set<MetricsReporter> metricsReporter) {
    this.tracerProvider =
        new TracerProvider(Metrics.decorate(TracerResolver.resolveTracer(), metricsReporter));
  }

  @Override
  public TracerProvider get() {
    return tracerProvider;
  }
}
