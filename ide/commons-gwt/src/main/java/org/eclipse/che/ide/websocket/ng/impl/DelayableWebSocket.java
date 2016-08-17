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
 * Yet another {@link WebSocketJsoWrapper} wrapper to benefit from
 * dependency injection provided by Gin. This implementation allows
 * setting a delay for opening a connection. It is convenient when
 * you are reconnecting.
 *
 * @author Dmitry Kuleshov
 */
public class DelayableWebSocket implements WebSocket {
    private final String            url;
    private final Integer           delay;
    private final WebSocketEndpoint endpoint;

    private WebSocketJsoWrapper webSocketJsoWrapper;

    @Inject
    public DelayableWebSocket(@Assisted String url, @Assisted Integer delay, WebSocketEndpoint endpoint) {
        this.url = url;
        this.delay = delay;
        this.endpoint = endpoint;
    }

    @Override
    public void open() {
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
