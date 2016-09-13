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
package org.eclipse.che.ide.jsonrpc.impl;

import org.eclipse.che.api.core.jsonrpc.shared.JsonRpcRequest;
import org.eclipse.che.ide.jsonrpc.JsonRpcRequestTransmitter;
import org.eclipse.che.ide.util.loging.Log;

import javax.inject.Inject;
import javax.inject.Singleton;


/**
 * Transmits JSON RPC requests through to {@link WebSocketJsonRpcTransmitter}
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class WebSocketJsonRpcRequestTransmitter implements JsonRpcRequestTransmitter {
    private final WebSocketJsonRpcTransmitter transmitter;
    private final JsonRpcRequestRegistry      requestRegistry;

    @Inject
    public WebSocketJsonRpcRequestTransmitter(WebSocketJsonRpcTransmitter transmitter,
                                              JsonRpcRequestRegistry requestRegistry) {
        this.transmitter = transmitter;
        this.requestRegistry = requestRegistry;
    }

    @Override
    public void transmit(JsonRpcRequest request) {
        final Integer id = request.getId();
        final String method = request.getMethod();

        if (id != null) {
            requestRegistry.add(id, method);
        }

        Log.debug(getClass(), "Transmitting a request " + request.toString());
        transmitter.transmit("request", request.toString());
    }
}
