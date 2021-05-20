/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.deploy;

import com.google.inject.servlet.ServletModule;
import org.eclipse.che.api.core.cors.CheCorsFilter;
import org.eclipse.che.commons.logback.filter.RequestIdLoggerFilter;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.multiuser.keycloak.server.deploy.KeycloakServletModule;
import org.eclipse.che.multiuser.machine.authentication.server.MachineLoginFilter;
import org.everrest.guice.servlet.GuiceEverrestServlet;

/** @author andrew00x */
@DynaModule
public class WsMasterServletModule extends ServletModule {
  @Override
  protected void configureServlets() {

    if (Boolean.valueOf(System.getenv("CHE_TRACING_ENABLED"))) {
      install(new org.eclipse.che.core.tracing.web.TracingWebModule());
    }
    if (isCheCorsEnabled()) {
      filter("/*").through(CheCorsFilter.class);
    }

    filter("/*").through(RequestIdLoggerFilter.class);

    // Matching group SHOULD contain forward slash.
    serveRegex("^(?!/websocket.?)(.*)").with(GuiceEverrestServlet.class);
    install(new org.eclipse.che.swagger.deploy.BasicSwaggerConfigurationModule());

    if (Boolean.valueOf(System.getenv("CHE_MULTIUSER"))) {
      configureMultiUserMode();
    } else {
      configureSingleUserMode();
    }

    if (Boolean.valueOf(System.getenv("CHE_METRICS_ENABLED"))) {
      install(new org.eclipse.che.core.metrics.MetricsServletModule());
    }
  }

  private boolean isCheCorsEnabled() {
    String cheCorsEnabledEnvVar = System.getenv("CHE_CORS_ENABLED");
    if (cheCorsEnabledEnvVar == null) {
      // by default CORS should be disabled
      return false;
    } else {
      return Boolean.valueOf(cheCorsEnabledEnvVar);
    }
  }

  private void configureSingleUserMode() {
    filter("/*").through(org.eclipse.che.api.local.filters.EnvironmentInitializationFilter.class);
  }

  private void configureMultiUserMode() {
    filterRegex(".*").through(MachineLoginFilter.class);
    install(new KeycloakServletModule());
  }
}
