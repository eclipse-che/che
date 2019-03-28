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

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import javax.inject.Inject;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.UserManager;

public class UserMeterBinder implements MeterBinder {

  @Inject UserManager userManager;

  @Override
  public void bindTo(MeterRegistry registry) {
    Gauge.builder("che.user.total", this::count)
        .description("Total amount of users")
        .register(registry);
  }

  private double count() {
    try {
      return userManager.getTotalCount();
    } catch (ServerException e) {
      return Double.NaN;
    }
  }
}
