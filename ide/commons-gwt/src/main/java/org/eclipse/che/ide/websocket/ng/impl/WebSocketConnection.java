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

import com.google.inject.Inject;

import org.eclipse.che.api.core.websocket.shared.WebSocketTransmission;
import org.eclipse.che.ide.util.loging.Log;

import javax.inject.Singleton;

/**
 * Entry point for high level WEB SOCKET connection operations.
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class WebSocketConnection {
    public static final int IMMEDIATELY = 0;

    private final WebSocketCreator webSocketCreator;

    private WebSocket webSocket;
    private String    url;

    @Inject
    public WebSocketConnection(WebSocketCreator webSocketCreator) {
        this.webSocketCreator = webSocketCreator;
    }

    public WebSocketConnection initialize(String url){
        this.url = url;
        return this;
    }

    public void open(int delay) {
        if (url == null){
            Log.error(WebSocketConnection.class, "Cannot open connection because no URL is set");

            throw new IllegalStateException("No URL is set");
        }

        webSocket = webSocketCreator.create(url, delay);
        webSocket.open();

        Log.debug(getClass(), "Opening connection. Url: " + url);
    }

    public void close() {
        webSocket.close();

        Log.debug(WebSocketConnection.class, "Closing connection.");
    }

    public void send(WebSocketTransmission message) {
        webSocket.send(message.toString());

        Log.debug(getClass(), "Sending message: " + message);
    }

    public boolean isOpen() {
        return webSocket != null && webSocket.isOpen();
    }
}
