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
package org.eclipse.che.ide.jsonrpc.impl;

import org.eclipse.che.ide.jsonrpc.JsonRpcInitializer;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.ng.impl.WebSocketInitializer;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

/**
 * Web socket based json rpc initializer.
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class WebSocketJsonRpcInitializer implements JsonRpcInitializer {
    private final WebSocketInitializer webSocketInitializer;

    @Inject
    public WebSocketJsonRpcInitializer(WebSocketInitializer webSocketInitializer) {
        this.webSocketInitializer = webSocketInitializer;
    }

    @Override
    public void initialize(String endpointId, Map<String, String> properties) {
        Log.debug(getClass(), "Initializing with properties: " + properties);
        final String url = properties.get("url");
        webSocketInitializer.initialize(endpointId, url);
    }

    @Override
    public void terminate(String endpointId) {
        Log.debug(getClass(), "Terminating");
        webSocketInitializer.terminate(endpointId);
    }
}
