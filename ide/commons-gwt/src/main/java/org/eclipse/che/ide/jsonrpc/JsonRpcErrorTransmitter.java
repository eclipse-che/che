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
package org.eclipse.che.ide.jsonrpc;

import org.eclipse.che.ide.websocket.ng.WebSocketMessageTransmitter;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class JsonRpcErrorTransmitter {
    private final WebSocketMessageTransmitter transmitter;
    private final JsonRpcFactory              jsonRpcFactory;

    @Inject
    public JsonRpcErrorTransmitter(WebSocketMessageTransmitter transmitter, JsonRpcFactory jsonRpcFactory) {
        this.transmitter = transmitter;
        this.jsonRpcFactory = jsonRpcFactory;
    }

    public void transmit(String endpointId, JsonRpcException e) {
        JsonRpcError error = jsonRpcFactory.createError(e.getCode(), e.getMessage());
        JsonRpcResponse response = jsonRpcFactory.createResponse(e.getId(), null, error);
        transmitter.transmit(endpointId, response.toString());
    }
}
