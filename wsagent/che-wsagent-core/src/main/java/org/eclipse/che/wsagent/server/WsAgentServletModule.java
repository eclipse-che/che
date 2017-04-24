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
import org.everrest.websockets.WSConnectionTracker;

/**
 * General binding that may be reused by other basic assembly
 *
 * @author Sergii Kabashiuk
 */
@DynaModule
public class WsAgentServletModule extends ServletModule {
    @Override
    protected void configureServlets() {
        getServletContext().addListener(new WSConnectionTracker());
        filter("/*").through(CheCorsFilter.class);

    }
}
