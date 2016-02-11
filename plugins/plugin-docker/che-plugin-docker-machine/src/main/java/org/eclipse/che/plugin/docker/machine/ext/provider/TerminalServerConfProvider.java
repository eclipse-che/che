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

import org.eclipse.che.plugin.docker.machine.ServerConf;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.net.URI;

/**
 * Provides server conf that describes websocket terminal server
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class TerminalServerConfProvider implements Provider<ServerConf> {
    public static final String TERMINAL_SERVER_REFERENCE = "terminal";

    @Inject
    @Named("api.endpoint")
    private URI apiEndpoint;

    @Override
    public ServerConf get() {
        return new ServerConf(TERMINAL_SERVER_REFERENCE, "4411", apiEndpoint.getScheme());
    }
}
