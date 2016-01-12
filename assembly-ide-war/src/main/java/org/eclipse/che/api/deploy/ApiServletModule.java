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
import org.eclipse.che.api.machine.server.proxy.MachineExtensionProxyServlet;
import org.eclipse.che.env.local.server.SingleEnvironmentFilter;
import org.eclipse.che.inject.DynaModule;
import org.everrest.websockets.WSConnectionTracker;

import javax.inject.Singleton;

/** @author andrew00x */
@DynaModule
public class ApiServletModule extends ServletModule {
    @Override
    protected void configureServlets() {
        getServletContext().addListener(new WSConnectionTracker());

        bind(SingleEnvironmentFilter.class).in(Singleton.class);

        filter("/*").through(SingleEnvironmentFilter.class);
        serve("/ext/*").with(MachineExtensionProxyServlet.class);
        serveRegex("^/api((?!(/(ws|eventbus)($|/.*)))/.*)").with(CheGuiceEverrestServlet.class);


    }
}
