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

import org.eclipse.che.api.core.jsonrpc.shared.JsonRpcResponse;
import org.eclipse.che.ide.jsonrpc.JsonRpcResponseTransmitter;
import org.eclipse.che.ide.util.loging.Log;

import javax.inject.Inject;
import javax.inject.Singleton;


/**
 * Transmits JSON RPC responses to {@link WebSocketJsonRpcTransmitter}
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class WebSocketJsonRpcResponseTransmitter implements JsonRpcResponseTransmitter {
    private final WebSocketJsonRpcTransmitter transmitter;

    @Inject
    public WebSocketJsonRpcResponseTransmitter(WebSocketJsonRpcTransmitter transmitter) {
        this.transmitter = transmitter;
    }

    @Override
    public void transmit(JsonRpcResponse response) {
        Log.debug(getClass(), "Transmitting a response " + response.toString());
        transmitter.transmit("response", response.toString());
    }

}
