/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.deploy;

import com.google.inject.servlet.ServletModule;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Singleton;
import org.apache.catalina.filters.CorsFilter;
import org.eclipse.che.commons.logback.filter.RequestIdLoggerFilter;
import org.eclipse.che.inject.DynaModule;
import org.everrest.guice.servlet.GuiceEverrestServlet;

/** @author andrew00x */
@DynaModule
public class WsMasterServletModule extends ServletModule {
  @Override
  protected void configureServlets() {
    getServletContext().addListener(new org.everrest.websockets.WSConnectionTracker());

    final Map<String, String> corsFilterParams = new HashMap<>();
    corsFilterParams.put("cors.allowed.origins", "*");
    corsFilterParams.put(
        "cors.allowed.methods", "GET," + "POST," + "HEAD," + "OPTIONS," + "PUT," + "DELETE");
    corsFilterParams.put(
        "cors.allowed.headers",
        "Content-Type,"
            + "X-Requested-With,"
            + "accept,"
            + "Origin,"
            + "Access-Control-Request-Method,"
            + "Access-Control-Request-Headers");
    corsFilterParams.put("cors.support.credentials", "true");
    // preflight cache is available for 10 minutes
    corsFilterParams.put("cors.preflight.maxage", "10");
    bind(CorsFilter.class).in(Singleton.class);

    filter("/*").through(CorsFilter.class, corsFilterParams);
    filter("/*").through(RequestIdLoggerFilter.class);

    serveRegex("^/api((?!(/(ws|eventbus)($|/.*)))/.*)").with(GuiceEverrestServlet.class);
    install(new org.eclipse.che.swagger.deploy.BasicSwaggerConfigurationModule());
  }
}
