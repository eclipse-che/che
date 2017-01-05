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
package org.eclipse.che.api.core.jsonrpc;


import org.eclipse.che.api.core.websocket.WebSocketMessageTransmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Transmits an instance of {@link JsonRpcException} to specific endpoint
 */
@Singleton
public class JsonRpcErrorTransmitter {
    private static final Logger LOG = LoggerFactory.getLogger(JsonRpcErrorTransmitter.class);

    private final WebSocketMessageTransmitter transmitter;
    private final JsonRpcFactory              jsonRpcFactory;

    @Inject
    public JsonRpcErrorTransmitter(WebSocketMessageTransmitter transmitter, JsonRpcFactory jsonRpcFactory) {
        this.transmitter = transmitter;
        this.jsonRpcFactory = jsonRpcFactory;
    }

    public void transmit(String endpointId, JsonRpcException e) {
        checkNotNull(endpointId, "Endpoint ID must not be null");
        checkArgument(!endpointId.isEmpty(), "Endpoint ID must not be empty");

        LOG.debug("Transmitting a JSON RPC error out of: " + e.getMessage());

        JsonRpcError error = jsonRpcFactory.createError(e.getCode(), e.getMessage());
        JsonRpcResponse response = jsonRpcFactory.createResponse(e.getId(), null, error);
        transmitter.transmit(endpointId, response.toString());
    }
}
