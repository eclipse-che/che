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

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import javax.inject.Singleton;

@Singleton
/** Bind server error metrics, such as HTTP 5xx status responses */
public class ServerErrorCounter implements MeterBinder {
  private Counter counter;

  @Override
  public void bindTo(MeterRegistry registry) {
    counter =
        Counter.builder("tomcat.server.errors")
            .description("Che Server Tomcat server errors (5xx responses)")
            .register(registry);
  }

  public void incrementCounter() {
    counter.increment();
  }
}
