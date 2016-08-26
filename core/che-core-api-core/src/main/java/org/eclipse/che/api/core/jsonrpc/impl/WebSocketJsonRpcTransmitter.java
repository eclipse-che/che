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
package org.eclipse.che.api.core.jsonrpc.impl;


import org.eclipse.che.api.core.jsonrpc.shared.JsonRpcObject;
import org.eclipse.che.api.core.websocket.WebSocketMessageTransmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * Transmits JSON RPC objects to {@link WebSocketMessageTransmitter}
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class WebSocketJsonRpcTransmitter {
    private static final Logger LOG = LoggerFactory.getLogger(WebSocketJsonRpcTransmitter.class);

    private final WebSocketMessageTransmitter transmitter;
    private final JsonRpcObjectValidator      validator;

    @Inject
    public WebSocketJsonRpcTransmitter(WebSocketMessageTransmitter transmitter, JsonRpcObjectValidator validator) {
        this.transmitter = transmitter;
        this.validator = validator;
    }

    public void transmit(String type, String message, Integer endpointId) {
        internalTransmit(message, type, endpointId);
    }

    public void transmit(String type, String message) {
        internalTransmit(message, type, null);
    }

    private void internalTransmit(String message, String type, Integer endpointId) {
        LOG.debug("Transmitting a json rpc object. Message: {} of type: {}", message);
        final JsonRpcObject jsonRpcObject = newDto(JsonRpcObject.class).withType(type).withMessage(message);
        validator.validate(jsonRpcObject);

        if (endpointId == null) {
            transmitter.transmit("jsonrpc-2.0", jsonRpcObject.toString());
        } else {
            transmitter.transmit("jsonrpc-2.0", jsonRpcObject.toString(), endpointId);
        }
    }
}
