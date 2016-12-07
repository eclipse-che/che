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

import com.google.gwt.user.client.Timer;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.ide.util.loging.Log;

import javax.inject.Inject;

/**
 * Web socket connection that can be established or reestablished with a delay
 *
 * @author Dmitry Kuleshov
 */
public class DelayableWebSocketConnection implements WebSocketConnection {
    private final WebSocketPropertyManager propertyManager;
    private final WebSocketEndpoint        endpoint;
    private final String                   url;

    private WebSocketJsoWrapper webSocketJsoWrapper;


    @Inject
    public DelayableWebSocketConnection(WebSocketPropertyManager propertyManager, WebSocketEndpoint endpoint, @Assisted String url) {
        this.propertyManager = propertyManager;
        this.endpoint = endpoint;
        this.url = url;
    }

    @Override
    public void open() {
        final int delay = propertyManager.getConnectionDelay(url);

        if (isClosed() || isClosing()) {
            if (delay == 0) {
                webSocketJsoWrapper = WebSocketJsoWrapper.connect(url, endpoint);
            } else {
                new Timer() {
                    @Override
                    public void run() {
                        webSocketJsoWrapper = WebSocketJsoWrapper.connect(url, endpoint);
                    }
                }.schedule(delay);
            }
        } else {
            Log.warn(getClass(), "Opening already opened or connecting web socket.");
        }
    }

    @Override
    public void close() {
        if (isOpen()) {
            webSocketJsoWrapper.close();
        } else {
            Log.warn(getClass(), "Closing connection that is not opened.");
        }
    }

    @Override
    public void send(final String message) {
        if (isOpen()) {
            webSocketJsoWrapper.send(message);
        } else {
            Log.error(getClass(), "Sending message to not opened connection.");
        }
    }

    @Override
    public boolean isClosed() {
        return webSocketJsoWrapper == null || webSocketJsoWrapper.isClosed();
    }

    @Override
    public boolean isClosing() {
        return webSocketJsoWrapper == null || webSocketJsoWrapper.isClosing();
    }

    @Override
    public boolean isOpen() {
        return webSocketJsoWrapper != null && webSocketJsoWrapper.isOpen();
    }

    @Override
    public boolean isConnecting() {
        return webSocketJsoWrapper != null && webSocketJsoWrapper.isConnecting();
    }
}
