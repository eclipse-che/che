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

import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MetricsBinder {
  private final Set<MeterBinder> meterBinderList;

  private final PrometheusMeterRegistry meterRegistry;

  @Inject
  public MetricsBinder(PrometheusMeterRegistry meterRegistry, Set<MeterBinder> meterBinderList) {
    this.meterBinderList = meterBinderList;
    this.meterRegistry = meterRegistry;
  }

  @PostConstruct
  public void bindToRegistry() {
    meterBinderList.forEach(e -> e.bindTo(meterRegistry));
  }
}
