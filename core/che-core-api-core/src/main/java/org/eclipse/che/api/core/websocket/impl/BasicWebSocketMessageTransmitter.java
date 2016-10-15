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
import org.eclipse.che.api.core.websocket.shared.WebSocketTransmission;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.websocket.Session;
import java.io.IOException;
import java.util.Optional;

import static org.eclipse.che.dto.server.DtoFactory.newDto;
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

    private final WebSocketSessionRegistry       registry;
    private final PendingMessagesReSender        resender;
    private final WebSocketTransmissionValidator validator;

    @Inject
    public BasicWebSocketMessageTransmitter(WebSocketSessionRegistry registry,
                                            PendingMessagesReSender resender,
                                            WebSocketTransmissionValidator validator) {
        this.registry = registry;
        this.resender = resender;
        this.validator = validator;
    }

    @Override
    public synchronized void transmit(String protocol, String message, Integer endpointId) {
        final WebSocketTransmission transmission = newDto(WebSocketTransmission.class).withProtocol(protocol).withMessage(message);
        validator.validate(transmission);

        final Optional<Session> sessionOptional = registry.get(endpointId);

        if (!sessionOptional.isPresent() || !sessionOptional.get().isOpen()) {
            LOG.debug("Session is not registered or closed, adding message to pending");

            resender.add(endpointId, transmission);
        } else {
            LOG.debug("Session registered and open, sending message");

            try {
                sessionOptional.get().getBasicRemote().sendText(transmission.toString());
            } catch (IOException e) {
                LOG.error("Error while trying to send a message to a basic websocket remote endpoint", e);
            }
        }
    }

    @Override
    public synchronized void transmit(String protocol, String message) {
        final WebSocketTransmission transmission = newDto(WebSocketTransmission.class).withProtocol(protocol).withMessage(message);
        validator.validate(transmission);

        LOG.debug("Broadcasting a web socket transmission: ", transmission.toString());

        registry.getSessions()
                .stream()
                .filter(Session::isOpen)
                .map(Session::getBasicRemote)
                .forEach(it -> {
                    try {
                        it.sendText(transmission.toString());
                    } catch (IOException e) {
                        LOG.error("Error while trying to send a message to a basic websocket remote endpoint", e);
                    }
                });
    }

}
