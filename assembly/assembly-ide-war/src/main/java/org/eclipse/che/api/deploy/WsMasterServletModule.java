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

import org.eclipse.che.api.local.CheGuiceEverrestServlet;
import org.eclipse.che.api.local.filters.WsMasterEnvironmentInitializationFilter;
import org.eclipse.che.inject.DynaModule;
import org.everrest.websockets.WSConnectionTracker;

/** @author andrew00x */
@DynaModule
public class WsMasterServletModule extends ServletModule {
    @Override
    protected void configureServlets() {
        getServletContext().addListener(new WSConnectionTracker());

        filter("/api/*").through(WsMasterEnvironmentInitializationFilter.class);

        serveRegex("^/api((?!(/(ws|eventbus)($|/.*)))/.*)").with(CheGuiceEverrestServlet.class);
        install(new org.eclipse.che.swagger.deploy.BasicSwaggerConfigurationModule());
    }
}
