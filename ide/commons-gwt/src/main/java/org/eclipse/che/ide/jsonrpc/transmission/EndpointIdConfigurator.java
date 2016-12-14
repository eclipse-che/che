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
package org.eclipse.che.ide.jsonrpc.transmission;

import org.eclipse.che.ide.jsonrpc.JsonRpcFactory;
import org.eclipse.che.ide.jsonrpc.ResponseDispatcher;
import org.eclipse.che.ide.websocket.ng.WebSocketMessageTransmitter;

import javax.inject.Inject;

/**
 * Endpoint ID configurator to defined endpoint id that the request
 * should be addressed to.
 */
public class EndpointIdConfigurator {
    private final ResponseDispatcher          responseDispatcher;
    private final WebSocketMessageTransmitter transmitter;
    private final JsonRpcFactory              jsonRpcFactory;

    @Inject
    public EndpointIdConfigurator(ResponseDispatcher responseDispatcher, WebSocketMessageTransmitter transmitter,
                                  JsonRpcFactory jsonRpcFactory) {
        this.responseDispatcher = responseDispatcher;
        this.transmitter = transmitter;
        this.jsonRpcFactory = jsonRpcFactory;
    }

    public MethodNameConfigurator endpointId(String endpointId) {
        return new MethodNameConfigurator(responseDispatcher, transmitter, jsonRpcFactory, endpointId);
    }


}
