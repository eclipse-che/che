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
public class ApiResponseCounter implements MeterBinder {
  private Counter successResponseCounter;
  private Counter redirectResponseCounter;
  private Counter clientErrorResponseCounter;
  private Counter serverErrorResponseCounter;

  @Override
  public void bindTo(MeterRegistry registry) {
    successResponseCounter =
        Counter.builder("che.server.api.response.success")
            .description("Che Server Tomcat success (2xx responses)")
            .tags("code=200", "area=http")
            .register(registry);
    redirectResponseCounter =
        Counter.builder("che.server.api.response.redirect")
            .description("Che Server Tomcat redirects (3xx responses)")
            .tags("code=300", "area=http")
            .register(registry);
    clientErrorResponseCounter =
        Counter.builder("che.server.api.response.client_error")
            .description("Che Server Tomcat client errors (4xx responses)")
            .tags("code=400", "area=http")
            .register(registry);
    serverErrorResponseCounter =
        Counter.builder("che.server.api.response.server_error")
            .description("Che Server Tomcat server errors (5xx responses)")
            .tags("code=500", "area=http")
            .register(registry);
  }

  public void incrementSuccessResponseCounter() {
    successResponseCounter.increment();
  }

  public void incrementRedirectResonseCounter() {
    redirectResponseCounter.increment();
  }

  public void incrementClientErrorResponseCounter() {
    clientErrorResponseCounter.increment();
  }

  public void incrementServerErrorResponceCounter() {
    serverErrorResponseCounter.increment();
  }
}
