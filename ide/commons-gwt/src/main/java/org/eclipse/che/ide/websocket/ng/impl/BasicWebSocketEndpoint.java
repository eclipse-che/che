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

/**
 * Duplex WEB SOCKET endpoint, handles messages, errors, session open/close events.
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class BasicWebSocketEndpoint implements WebSocketEndpoint {
    private final WebSocketConnectionSustainer    sustainer;
    private final PendingMessagesReSender         reSender;
    private final WebSocketTransmissionDispatcher dispatcher;

    @Inject
    public BasicWebSocketEndpoint(WebSocketConnectionSustainer sustainer,
                                  PendingMessagesReSender pending,
                                  WebSocketTransmissionDispatcher dispatcher) {
        this.sustainer = sustainer;
        this.reSender = pending;
        this.dispatcher = dispatcher;
    }

    @Override
    public void onOpen() {
        Log.debug(getClass(), "Session opened.");

        sustainer.reset();
        reSender.resend();
    }

    @Override
    public void onClose() {
        Log.debug(getClass(), "Session closed.");

        sustainer.sustain();
    }

    @Override
    public void onError() {
        Log.warn(getClass(), "Error occurred.");
    }

    @Override
    public void onMessage(String message) {
        Log.debug(getClass(), "Message received: " + message);

        dispatcher.dispatch(message);
    }
}
