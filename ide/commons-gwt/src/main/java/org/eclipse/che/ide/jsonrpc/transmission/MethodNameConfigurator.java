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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Method name configurator to defined method name that the request
 * will have.
 */
public class MethodNameConfigurator {
    public static AtomicInteger id = new AtomicInteger(0);

    private final ResponseDispatcher          responseDispatcher;
    private final WebSocketMessageTransmitter transmitter;
    private final JsonRpcFactory              jsonRpcFactory;
    private final String                      endpointId;

    @Inject
    public MethodNameConfigurator(ResponseDispatcher responseDispatcher, WebSocketMessageTransmitter transmitter,
                                  JsonRpcFactory jsonRpcFactory, String endpointId) {
        this.responseDispatcher = responseDispatcher;
        this.transmitter = transmitter;
        this.jsonRpcFactory = jsonRpcFactory;
        this.endpointId = endpointId;
    }

    public ParamsConfigurator methodName(String method) {
        return new ParamsConfigurator(responseDispatcher, transmitter, jsonRpcFactory, method, endpointId);
    }


}
