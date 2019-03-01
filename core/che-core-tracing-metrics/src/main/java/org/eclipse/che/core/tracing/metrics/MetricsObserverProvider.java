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

import io.opentracing.contrib.metrics.MetricsObserver;
import io.opentracing.contrib.metrics.MetricsReporter;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class MetricsObserverProvider implements Provider<MetricsObserver> {
  private final MetricsObserver metricsObserver;

  @Inject
  public MetricsObserverProvider(Set<MetricsReporter> metricsReporters) {
    this.metricsObserver = new MetricsObserver(metricsReporters);
  }

  @Override
  public MetricsObserver get() {
    return metricsObserver;
  }
}
