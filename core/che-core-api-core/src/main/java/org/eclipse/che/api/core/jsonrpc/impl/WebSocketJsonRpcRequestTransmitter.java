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


import org.eclipse.che.api.core.jsonrpc.JsonRpcRequestTransmitter;
import org.eclipse.che.api.core.jsonrpc.shared.JsonRpcRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Transmits JSON RPC requests through to {@link WebSocketJsonRpcTransmitter}
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class WebSocketJsonRpcRequestTransmitter implements JsonRpcRequestTransmitter {
    private static final Logger LOG = LoggerFactory.getLogger(WebSocketJsonRpcRequestTransmitter.class);

    private final WebSocketJsonRpcTransmitter transmitter;
    private final JsonRpcRequestRegistry      registry;

    @Inject
    public WebSocketJsonRpcRequestTransmitter(WebSocketJsonRpcTransmitter transmitter, JsonRpcRequestRegistry registry) {
        this.transmitter = transmitter;
        this.registry = registry;
    }

    @Override
    public void transmit(JsonRpcRequest request, Integer endpoint) {
        internalTransmit(request, endpoint);
    }

    @Override
    public void transmit(JsonRpcRequest request) {
        internalTransmit(request, null);
    }

    private void internalTransmit(JsonRpcRequest request, Integer endpointId) {
        final Integer id = request.getId();
        final String method = request.getMethod();

        if (id != null) {
            registry.add(id, method);
        }

        LOG.debug("Transmitting a request\n {}", request);

        if (endpointId == null) {
            transmitter.transmit("request", request.toString());
        } else {
            transmitter.transmit("request", request.toString(), endpointId);
        }
    }
}
