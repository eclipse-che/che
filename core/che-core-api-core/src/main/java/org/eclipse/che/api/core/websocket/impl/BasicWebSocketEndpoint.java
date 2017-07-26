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
package org.eclipse.che.api.core.websocket.impl;

import org.eclipse.che.api.core.websocket.commons.WebSocketMessageReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

/**
 * Duplex WEB SOCKET endpoint, handles messages, errors, session open/close events.
 *
 * @author Dmitry Kuleshov
 */
abstract public class BasicWebSocketEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(BasicWebSocketEndpoint.class);

    private final WebSocketSessionRegistry registry;
    private final MessagesReSender         reSender;
    private final WebSocketMessageReceiver receiver;
    private final WebsocketIdService       identificationService;


    public BasicWebSocketEndpoint(WebSocketSessionRegistry registry,
                                  MessagesReSender reSender,
                                  WebSocketMessageReceiver receiver,
                                  WebsocketIdService identificationService) {

        this.registry = registry;
        this.reSender = reSender;
        this.receiver = receiver;
        this.identificationService = identificationService;
    }

    @OnOpen
    public void onOpen(Session session) {
        String combinedEndpointId = getCombinedEndpointId(session);

        LOG.debug("Web socket session opened");
        LOG.debug("Endpoint: {}", combinedEndpointId);

        session.setMaxIdleTimeout(0);

        registry.add(combinedEndpointId, session);
        reSender.resend(combinedEndpointId);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        String combinedEndpointId = getCombinedEndpointId(session);

        LOG.debug("Receiving a web socket message.");
        LOG.debug("Endpoint: {}", combinedEndpointId);
        LOG.debug("Message: {}", message);

        receiver.receive(combinedEndpointId, message);
    }

    @OnClose
    public void onClose(CloseReason closeReason, Session session) {
        String combinedEndpointId = getCombinedEndpointId(session);

        LOG.debug("Web socket session closed");
        LOG.debug("Endpoint: {}", combinedEndpointId);
        LOG.debug("Close reason: {}:{}", closeReason.getReasonPhrase(), closeReason.getCloseCode());

        registry.remove(combinedEndpointId);
    }

    @OnError
    public void onError(Throwable t) {
        LOG.debug("Web socket session error");
        LOG.debug("Error: {}", t);
    }

    private String getCombinedEndpointId(Session session) {
        return registry.get(session).orElseGet(() -> identificationService.getCombinedId(getEndpointId()));
    }

    protected abstract String getEndpointId();
}
