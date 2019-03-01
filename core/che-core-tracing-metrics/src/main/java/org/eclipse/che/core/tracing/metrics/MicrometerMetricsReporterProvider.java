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

@Singleton
public class MicrometerMetricsReporterProvider implements Provider<MicrometerMetricsReporter> {

  private final MicrometerMetricsReporter micrometerMetricsReporter;

  public MicrometerMetricsReporterProvider() {
    micrometerMetricsReporter =
        MicrometerMetricsReporter.newMetricsReporter()
            .withName("MyName")
            .withConstLabel("span.kind", Tags.SPAN_KIND_CLIENT)
            .build();
    ;
  }

  @Override
  public MicrometerMetricsReporter get() {
    return micrometerMetricsReporter;
  }
}
