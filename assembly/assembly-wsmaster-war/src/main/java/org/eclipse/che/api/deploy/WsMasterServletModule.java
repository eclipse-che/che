/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.deploy;

import com.google.inject.servlet.ServletModule;

import org.apache.catalina.filters.CorsFilter;
import org.eclipse.che.inject.DynaModule;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

/** @author andrew00x */
@DynaModule
public class WsMasterServletModule extends ServletModule {
    @Override
    protected void configureServlets() {
        getServletContext().addListener(new org.everrest.websockets.WSConnectionTracker());

        final Map<String, String> corsFilterParams = new HashMap<>();
        corsFilterParams.put("cors.allowed.origins", "*");
        corsFilterParams.put("cors.allowed.methods", "GET," +
                                                     "POST," +
                                                     "HEAD," +
                                                     "OPTIONS," +
                                                     "PUT," +
                                                     "DELETE");
        corsFilterParams.put("cors.allowed.headers", "Content-Type," +
                                                     "X-Requested-With," +
                                                     "accept," +
                                                     "Origin," +
                                                     "Access-Control-Request-Method," +
                                                     "Access-Control-Request-Headers");
        corsFilterParams.put("cors.support.credentials", "true");
        // preflight cache is available for 10 minutes
        corsFilterParams.put("cors.preflight.maxage", "10");
        bind(CorsFilter.class).in(Singleton.class);

        filter("/*").through(CorsFilter.class, corsFilterParams);

        filter("/api/*").through(org.eclipse.che.api.local.filters.WsMasterEnvironmentInitializationFilter.class);
        serveRegex("^/api((?!(/(ws|eventbus)($|/.*)))/.*)").with(org.eclipse.che.api.local.CheGuiceEverrestServlet.class);
        install(new org.eclipse.che.swagger.deploy.BasicSwaggerConfigurationModule());
    }
}
