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
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Takes all {@link io.micrometer.core.instrument.binder.MeterBinder} from guice container, binded
 * with {@link com.google.inject.multibindings.Multibinder}, and bind them to {@link
 * io.micrometer.prometheus.PrometheusMeterRegistry} on PostConstruct.
 */
@Singleton
public class MetricsBinder {

  @Inject
  public void bindToRegistry(
      PrometheusMeterRegistry meterRegistry, Set<MeterBinder> meterBinderList) {
    meterBinderList.forEach(e -> e.bindTo(meterRegistry));
  }
}
