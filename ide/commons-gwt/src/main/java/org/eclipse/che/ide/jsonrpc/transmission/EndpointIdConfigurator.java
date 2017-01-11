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
package org.eclipse.che.ide.jsonrpc.transmission;

import org.eclipse.che.ide.jsonrpc.JsonRpcFactory;
import org.eclipse.che.ide.jsonrpc.ResponseDispatcher;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.ng.WebSocketMessageTransmitter;

import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Endpoint ID configurator to defined endpoint id that the request
 * should be addressed to.
 */
public class EndpointIdConfigurator {
    private final ResponseDispatcher          dispatcher;
    private final WebSocketMessageTransmitter transmitter;
    private final JsonRpcFactory              factory;

    @Inject
    public EndpointIdConfigurator(ResponseDispatcher dispatcher, WebSocketMessageTransmitter transmitter, JsonRpcFactory factory) {
        this.dispatcher = dispatcher;
        this.transmitter = transmitter;
        this.factory = factory;
    }

    public MethodNameConfigurator endpointId(String id) {
        checkNotNull(id, "Endpoint ID must not be null");
        checkArgument(!id.isEmpty(), "Endpoint ID must not be empty");

        Log.debug(getClass(), "Configuring outgoing request endpoint ID: " + id);

        return new MethodNameConfigurator(dispatcher, transmitter, factory, id);
    }


}
