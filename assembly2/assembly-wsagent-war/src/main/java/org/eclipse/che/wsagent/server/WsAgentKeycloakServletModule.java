/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.wsagent.server;

import com.google.inject.servlet.ServletModule;

import org.eclipse.che.api.core.cors.CheCorsFilter;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.keycloak.server.KeycloakAuthenticationFilter;
import org.eclipse.che.machine.authentication.agent.MachineLoginFilter;
import org.everrest.guice.servlet.GuiceEverrestServlet;

import javax.inject.Singleton;

@DynaModule
public class WsAgentKeycloakServletModule extends ServletModule {
    @Override
    protected void configureServlets() {
        filter("/*").through(CheCorsFilter.class);
        serveRegex("^/api((?!(/(ws|eventbus)($|/.*)))/.*)").with(GuiceEverrestServlet.class);
        
        bind(KeycloakAuthenticationFilter.class).in(Singleton.class);
        filter("/*").through(MachineLoginFilter.class);
    }
}
