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
package org.eclipse.che.api.core.websocket.impl;

import org.eclipse.che.api.core.websocket.WebSocketMessageTransmitter;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.websocket.Session;
import java.io.IOException;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Transmits messages over WEB SOCKET to a specific endpoint or broadcasts them.
 * If WEB SOCKET session is not opened adds messages to re-sender to try to send
 * them when session will be opened again.
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class BasicWebSocketMessageTransmitter implements WebSocketMessageTransmitter {
    private static final Logger LOG = getLogger(BasicWebSocketMessageTransmitter.class);

    private final WebSocketSessionRegistry registry;
    private final MessagesReSender         reSender;

    @Inject
    public BasicWebSocketMessageTransmitter(WebSocketSessionRegistry registry, MessagesReSender reSender) {
        this.registry = registry;
        this.reSender = reSender;
    }

    @Override
    public synchronized void transmit(String endpointId, String message) {
        final Optional<Session> sessionOptional = registry.get(endpointId);

        if (!sessionOptional.isPresent() || !sessionOptional.get().isOpen()) {
            LOG.debug("Session is not registered or closed, adding message to pending");

            reSender.add(endpointId, message);
        } else {
            LOG.debug("Session registered and open, sending message");

            try {
                sessionOptional.get().getBasicRemote().sendText(message);
            } catch (IOException e) {
                LOG.error("Error while trying to send a message to a basic websocket remote endpoint", e);
            }
        }
    }

    @Override
    public synchronized void transmit(String message) {
        LOG.debug("Broadcasting a web socket transmission: ", message);

        registry.getSessions()
                .stream()
                .filter(Session::isOpen)
                .map(Session::getBasicRemote)
                .forEach(it -> {
                    try {
                        it.sendText(message);
                    } catch (IOException e) {
                        LOG.error("Error while trying to send a message to a basic websocket remote endpoint", e);
                    }
                });
    }

}
