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
import org.eclipse.che.ide.websocket.ng.WebSocketMessageReceiver;

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
    private final Map<String, WebSocketMessageReceiver> receivers;
    private final WebSocketTransmissionValidator        validator;
    private final DtoFactory                            dtoFactory;


    @Inject
    public WebSocketTransmissionDispatcher(Map<String, WebSocketMessageReceiver> receivers,
                                           WebSocketTransmissionValidator validator,
                                           DtoFactory dtoFactory) {
        this.receivers = receivers;
        this.dtoFactory = dtoFactory;
        this.validator = validator;
    }

    public void dispatch(String rawTransmission) {
        final WebSocketTransmission transmission = dtoFactory.createDtoFromJson(rawTransmission, WebSocketTransmission.class);
        validator.validate(transmission);

        final String protocol = transmission.getProtocol();
        final String message = transmission.getMessage();

        Log.debug(getClass(), "Receiving a web socket transmission. Type: " + protocol + " Message: " + message);

        for (Entry<String, WebSocketMessageReceiver> entry : receivers.entrySet()) {
            final String protocolCandidate = entry.getKey();
            if (Objects.equals(protocol, protocolCandidate)) {
                final WebSocketMessageReceiver receiver = entry.getValue();
                Log.debug(getClass(), "Matching web socket transmission receiver: " + receiver.getClass());
                receiver.receive(message);

                return;
            }
        }
    }
}
