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
package org.eclipse.che.core.metrics;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.CollectorRegistry;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * {@link javax.inject.Provider} of {@link io.micrometer.prometheus.PrometheusMeterRegistry}
 * instances. Used constructor with PrometheusConfig#DEFAULT and Clock.SYSTEM parameters.
 */
@Singleton
public class PrometheusMeterRegistryProvider implements Provider<PrometheusMeterRegistry> {
  private final PrometheusMeterRegistry prometheusMeterRegistry;

  @Inject
  public PrometheusMeterRegistryProvider(CollectorRegistry registry) {
    prometheusMeterRegistry =
        new PrometheusMeterRegistry(PrometheusConfig.DEFAULT, registry, Clock.SYSTEM);
    Metrics.addRegistry(prometheusMeterRegistry);
  }

  @Override
  public PrometheusMeterRegistry get() {
    return prometheusMeterRegistry;
  }
}
