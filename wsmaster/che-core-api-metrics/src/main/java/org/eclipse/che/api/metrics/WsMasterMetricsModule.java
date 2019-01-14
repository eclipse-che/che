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
package org.eclipse.che.api.metrics;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import io.micrometer.core.instrument.binder.MeterBinder;

/**
 * A Guice module to bind all our metric binders to a single multi-binder. The set of all metric
 * binders is used to produce the Prometheus metrics on request.
 */
public class WsMasterMetricsModule extends AbstractModule {

  @Override
  protected void configure() {
    Multibinder<MeterBinder> meterMultibinder =
        Multibinder.newSetBinder(binder(), MeterBinder.class);

    meterMultibinder.addBinding().to(WorkspaceActivityMeterBinder.class);
    meterMultibinder.addBinding().to(WorkspaceFailureMeterBinder.class);
  }
}
