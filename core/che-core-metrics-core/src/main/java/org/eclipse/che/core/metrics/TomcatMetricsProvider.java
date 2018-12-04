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

import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.tomcat.TomcatMetrics;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.apache.catalina.Manager;

/**
 * {@link javax.inject.Provider} of {@link
 * io.micrometer.core.instrument.binder.tomcat.TomcatMetrics} instance. Used constructor with empty
 * {@link io.micrometer.core.instrument.Tags}
 */
@Singleton
public class TomcatMetricsProvider implements Provider<TomcatMetrics> {

  private final Manager manager;

  @Inject
  public TomcatMetricsProvider(Manager manager) {
    this.manager = manager;
  }

  @Override
  public TomcatMetrics get() {
    return new TomcatMetrics(manager, Tags.empty());
  }
}
