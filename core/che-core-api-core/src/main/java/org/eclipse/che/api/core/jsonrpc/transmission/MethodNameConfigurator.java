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
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Method name configurator to defined method name that the request
 * will have.
 */
public class MethodNameConfigurator {
    private static final Logger LOG = LoggerFactory.getLogger(MethodNameConfigurator.class);

    public static AtomicInteger id = new AtomicInteger(0);

    private final ResponseDispatcher          dispatcher;
    private final WebSocketMessageTransmitter transmitter;
    private final JsonRpcFactory              factory;
    private final String                      endpointId;

    @Inject
    MethodNameConfigurator(ResponseDispatcher dispatcher, WebSocketMessageTransmitter transmitter, JsonRpcFactory factory,
                                  String endpointId) {
        this.dispatcher = dispatcher;
        this.transmitter = transmitter;
        this.factory = factory;
        this.endpointId = endpointId;
    }

    public ParamsConfigurator methodName(String name) {
        checkNotNull(name, "Method name must not be null");
        checkArgument(!name.isEmpty(), "Method name must not be empty");

        LOG.debug("Configuring outgoing request method name name: {}", name);

        return new ParamsConfigurator(dispatcher, transmitter, factory, name, endpointId);
    }

    public static int getIdAndIncrement() {
        return id.getAndIncrement();
    }
}
