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
package org.eclipse.che.ide.websocket.ng.impl;

import org.eclipse.che.ide.util.loging.Log;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

import static org.eclipse.che.ide.websocket.ng.impl.WebSocketConnection.IMMEDIATELY;

/**
 * @author Dmitry Kuleshov
 */
@Singleton
public class SessionWebSocketInitializer implements WebSocketInitializer {
    private final WebSocketConnection          connection;
    private final WebSocketConnectionSustainer sustainer;

    @Inject
    public SessionWebSocketInitializer(WebSocketConnection connection, WebSocketConnectionSustainer sustainer) {
        this.connection = connection;
        this.sustainer = sustainer;
    }

    @Override
    public void initialize(Map<String, String> properties) {
        Log.debug(getClass(), "Initializing with properties: " + properties);

        final String url = properties.get("url");

        sustainer.enable();
        connection.initialize(url).open(IMMEDIATELY);
    }

    @Override
    public void terminate() {
        Log.debug(getClass(), "Stopping");

        sustainer.disable();
        connection.close();
    }
}
