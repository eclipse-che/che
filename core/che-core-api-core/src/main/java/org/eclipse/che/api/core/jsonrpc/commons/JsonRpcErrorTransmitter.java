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
package org.eclipse.che.api.core.jsonrpc.commons;

import org.eclipse.che.api.core.logger.commons.Logger;
import org.eclipse.che.api.core.logger.commons.LoggerFactory;
import org.eclipse.che.api.core.websocket.commons.WebSocketMessageTransmitter;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Transmits an instance of {@link JsonRpcException} to an endpoint
 */
@Singleton
public class JsonRpcErrorTransmitter {
    private final Logger                      logger;
    private final WebSocketMessageTransmitter transmitter;
    private final JsonRpcMarshaller           marshaller;

    private final static org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("gggg");

    @Inject
    public JsonRpcErrorTransmitter(LoggerFactory loggerFactory, WebSocketMessageTransmitter transmitter, JsonRpcMarshaller marshaller) {
        this.logger = loggerFactory.get(getClass());
        this.transmitter = transmitter;
        this.marshaller = marshaller;
    }

    public void transmit(String endpointId, JsonRpcException e) {
        checkNotNull(endpointId, "Endpoint ID must not be null");
        checkArgument(!endpointId.isEmpty(), "Endpoint ID must not be empty");

        logger.debug("Transmitting a JSON RPC error: " + e.getMessage());
        LOGGER.debug("Dshdfjsfhsdjfhdsjfhdj");

        JsonRpcError error = new JsonRpcError(e.getCode(), e.getMessage());
        JsonRpcResponse response = new JsonRpcResponse(e.getId(), null, error);
        String message = marshaller.marshall(response);
        transmitter.transmit(endpointId, message);
    }
}
