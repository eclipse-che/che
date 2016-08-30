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

import org.eclipse.che.api.core.websocket.shared.WebSocketTransmission;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.ng.WebSocketMessageTransmitter;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Transmits messages over WEB SOCKET to a specific endpoint or broadcasts them.
 * If WEB SOCKET session is not opened adds messages to re-sender to try to send
 * them when session will be opened again.
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class BasicWebSocketMessageTransmitter implements WebSocketMessageTransmitter {
    private final WebSocketConnection            connection;
    private final PendingMessagesReSender        reSender;
    private final WebSocketTransmissionValidator validator;
    private final DtoFactory                     dtoFactory;

    @Inject
    public BasicWebSocketMessageTransmitter(WebSocketConnection connection,
                                            PendingMessagesReSender reSender,
                                            WebSocketTransmissionValidator validator,
                                            DtoFactory dtoFactory) {
        this.connection = connection;
        this.reSender = reSender;
        this.validator = validator;
        this.dtoFactory = dtoFactory;
    }

    @Override
    public void transmit(String protocol, String message) {
        final WebSocketTransmission transmission = dtoFactory.createDto(WebSocketTransmission.class).withProtocol(protocol).withMessage(message);
        validator.validate(transmission);

        if (connection.isOpen()) {
            Log.debug(getClass(), "Connection is opened, transmitting");

            connection.send(transmission);

        } else {
            Log.debug(getClass(), "Connection is closed, adding to pending");

            reSender.add(transmission);
        }
    }
}
