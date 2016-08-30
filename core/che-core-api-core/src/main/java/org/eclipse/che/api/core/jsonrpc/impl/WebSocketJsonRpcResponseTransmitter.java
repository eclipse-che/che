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


import org.eclipse.che.api.core.jsonrpc.JsonRpcResponseTransmitter;
import org.eclipse.che.api.core.jsonrpc.shared.JsonRpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Transmits JSON RPC responses to {@link WebSocketJsonRpcTransmitter}
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class WebSocketJsonRpcResponseTransmitter implements JsonRpcResponseTransmitter {
    private static final Logger LOG = LoggerFactory.getLogger(WebSocketJsonRpcResponseTransmitter.class);

    private final WebSocketJsonRpcTransmitter transmitter;

    @Inject
    public WebSocketJsonRpcResponseTransmitter(WebSocketJsonRpcTransmitter transmitter) {
        this.transmitter = transmitter;
    }

    @Override
    public void transmit(JsonRpcResponse response, Integer endpoint) {
        internalTransmit(response, endpoint);
    }

    @Override
    public void transmit(JsonRpcResponse response) {
        internalTransmit(response, null);
    }

    private void internalTransmit(JsonRpcResponse response, Integer endpointId) {
        LOG.debug("Transmitting a response\n {}", response);

        if (endpointId == null) {
            transmitter.transmit("response", response.toString());
        } else {
            transmitter.transmit("response", response.toString(), endpointId);
        }
    }
}
