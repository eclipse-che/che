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
package org.eclipse.che.wsagent.server;

import com.google.common.collect.ImmutableMap;
import com.google.inject.servlet.ServletModule;

import org.eclipse.che.api.core.cors.CheCorsFilter;
import org.eclipse.che.filters.EnvironmentInitializationFilter;
import org.eclipse.che.inject.DynaModule;
import org.everrest.guice.servlet.GuiceEverrestServlet;
import org.everrest.websockets.WSConnectionTracker;

/** @author andrew00x */
@DynaModule
public class WsAgentServletModule extends ServletModule {
    @Override
    protected void configureServlets() {
        getServletContext().addListener(new WSConnectionTracker());

        filter("/*").through(CheCorsFilter.class);
        filter("/api/*").through(EnvironmentInitializationFilter.class);

        serveRegex("^/api((?!(/(ws|eventbus)($|/.*)))/.*)").with(GuiceEverrestServlet.class);

        bind(io.swagger.jaxrs.config.DefaultJaxrsConfig.class).asEagerSingleton();
        serve("/swaggerinit").with(io.swagger.jaxrs.config.DefaultJaxrsConfig.class, ImmutableMap
                .of("api.version", "1.0",
                    "swagger.api.title", "Eclipse Che",
                    "swagger.api.basepath", "/api"
                   ));
    }
}
