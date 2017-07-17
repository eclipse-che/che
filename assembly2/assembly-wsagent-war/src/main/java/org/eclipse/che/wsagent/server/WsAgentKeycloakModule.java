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

import com.google.inject.AbstractModule;

import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.machine.authentication.agent.token.HeaderRequestTokenExtractor;
import org.eclipse.che.machine.authentication.agent.token.RequestTokenExtractor;

/**
 *
 */
@DynaModule
public class WsAgentKeycloakModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(HttpJsonRequestFactory.class).to(AgentKeycloakHttpJsonRequestFactory.class);
        bind(RequestTokenExtractor.class).to(HeaderRequestTokenExtractor.class);
    }
}
