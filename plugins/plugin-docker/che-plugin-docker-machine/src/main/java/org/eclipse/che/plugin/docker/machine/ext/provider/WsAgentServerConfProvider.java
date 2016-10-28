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
package org.eclipse.che.plugin.docker.machine.ext.provider;

import org.eclipse.che.api.core.model.machine.ServerConf;
import org.eclipse.che.api.machine.server.model.impl.ServerConfImpl;
import org.eclipse.che.api.machine.shared.Constants;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.net.URI;

import static org.eclipse.che.api.machine.shared.Constants.WSAGENT_REFERENCE;

/**
 * Provides server conf that describes workspace agent server
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class WsAgentServerConfProvider implements Provider<ServerConf> {

    @Inject
    @Named("che.api")
    private URI apiEndpoint;

    @Override
    public ServerConf get() {
        return new ServerConfImpl(WSAGENT_REFERENCE,
                                  Constants.WS_AGENT_PORT,
                                  apiEndpoint.getScheme(),
                                  "/wsagent/ext");
    }

}
