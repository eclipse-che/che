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
import org.eclipse.che.ide.websocket.ng.WebSocketMessageTransmitter;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Web socket transmitter implementation that can transmit a message into opened connection
 * or send a message to pending message re-sender so it could be possible to send it later
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class BasicWebSocketMessageTransmitter implements WebSocketMessageTransmitter {
    private final WebSocketConnectionManager connectionManager;
    private final MessagesReSender           reSender;
    private final UrlResolver                urlResolver;

    @Inject
    public BasicWebSocketMessageTransmitter(WebSocketConnectionManager connectionManager, MessagesReSender reSender, UrlResolver resolver) {
        this.connectionManager = connectionManager;
        this.reSender = reSender;
        this.urlResolver = resolver;
    }

    @Override
    public void transmit(String endpointId, String message) {
        final String url = urlResolver.getUrl(endpointId);

        if (connectionManager.isConnectionOpen(url)) {
            Log.debug(getClass(), "Connection is opened, transmitting: " + message);
            connectionManager.sendMessage(url, message);

        } else {
            Log.debug(getClass(), "Connection is closed, adding to pending: " + message);
            reSender.add(url, message);
        }
    }
}
