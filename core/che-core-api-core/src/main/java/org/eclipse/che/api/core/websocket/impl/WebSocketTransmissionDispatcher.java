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

import org.eclipse.che.api.core.websocket.WebSocketMessageReceiver;
import org.eclipse.che.api.core.websocket.shared.WebSocketTransmission;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

/**
 * Dispatches a {@link WebSocketTransmission} messages among registered receivers
 * ({@link WebSocketMessageReceiver}) according to WEB SOCKET transmission protocol
 * field value.
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class WebSocketTransmissionDispatcher {
    private static final Logger LOG = LoggerFactory.getLogger(WebSocketTransmissionDispatcher.class);

    private final Map<String, WebSocketMessageReceiver> receivers;
    private final WebSocketTransmissionValidator        validator;

    @Inject
    public WebSocketTransmissionDispatcher(Map<String, WebSocketMessageReceiver> receivers, WebSocketTransmissionValidator validator) {
        this.receivers = receivers;
        this.validator = validator;
    }

    public void dispatch(String rawTransmission, Integer endpointId) {
        final WebSocketTransmission transmission = DtoFactory.getInstance().createDtoFromJson(rawTransmission, WebSocketTransmission.class);
        validator.validate(transmission);

        final String protocol = transmission.getProtocol();
        final String message = transmission.getMessage();

        LOG.debug("Receiving a web socket transmission. Protocol: " + protocol + " Message: " + message);

        for (Entry<String, WebSocketMessageReceiver> entry : receivers.entrySet()) {
            final String protocolCandidate = entry.getKey();
            if (Objects.equals(protocolCandidate, protocol)) {
                final WebSocketMessageReceiver receiver = entry.getValue();
                LOG.debug("Matching web socket transmission receiver: {}", receiver.getClass());
                receiver.receive(message, endpointId);

                return;
            }
        }
    }
}
