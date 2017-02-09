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
package org.eclipse.che.api.core.jsonrpc.transmission;

import org.eclipse.che.api.core.jsonrpc.JsonRpcFactory;
import org.eclipse.che.api.core.jsonrpc.ResponseDispatcher;
import org.eclipse.che.api.core.websocket.WebSocketMessageTransmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Endpoint ID configurator to defined endpoint id that the request
 * should be addressed to.
 */
public class EndpointIdConfigurator {
    private static final Logger LOG = LoggerFactory.getLogger(EndpointIdConfigurator.class);

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

        LOG.debug("Configuring outgoing request endpoint ID: {}", id);

        return new MethodNameConfigurator(dispatcher, transmitter, factory, id);
    }
}
