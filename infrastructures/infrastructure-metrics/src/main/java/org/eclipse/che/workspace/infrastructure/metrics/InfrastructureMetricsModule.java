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
package org.eclipse.che.workspace.infrastructure.metrics;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import io.micrometer.core.instrument.binder.MeterBinder;

/**
 * A Guice module to bind infrastructure specific metric binders to a single multi-binder. The set
 * of all metric binders is used to produce the Prometheus metrics on request.
 */
public class InfrastructureMetricsModule extends AbstractModule {

  @Override
  protected void configure() {
    Multibinder<MeterBinder> meterMultibinder =
        Multibinder.newSetBinder(binder(), MeterBinder.class);

    meterMultibinder.addBinding().to(CurrentLogwatchersMeterBinder.class);
  }
}
