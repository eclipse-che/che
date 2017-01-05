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
package org.eclipse.che.api.core.jsonrpc;


import org.eclipse.che.api.core.websocket.WebSocketMessageTransmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Dispatches incoming JSON RPC requests and notifications. If during
 * dispatching happens any kind of error related to JSON RPC it throws
 * appropriate exception {@link JsonRpcException}.
 */
@Singleton
public class RequestDispatcher {
    private static final Logger LOG = LoggerFactory.getLogger(RequestDispatcher.class);

    private final RequestHandlerRegistry      registry;
    private final JsonRpcFactory              factory;
    private final WebSocketMessageTransmitter transmitter;

    @Inject
    public RequestDispatcher(RequestHandlerRegistry registry, WebSocketMessageTransmitter transmitter, JsonRpcFactory factory) {
        this.registry = registry;
        this.transmitter = transmitter;
        this.factory = factory;
    }

    public void dispatch(String endpointId, JsonRpcRequest request) throws JsonRpcException {
        checkNotNull(endpointId, "Endpoint ID must not be null");
        checkArgument(!endpointId.isEmpty(), "Endpoint ID must not be empty");
        checkNotNull(request, "Request must not be null");

        LOG.debug("Dispatching request: {}, endpoint: {}", request, endpointId);

        String method = request.getMethod();
        JsonRpcParams params = request.getParams();

        if (request.hasId()) {
            LOG.debug("Request has ID");

            String id = request.getId();
            RequestHandler handler = registry.getRequestHandler(method);
            checkHandler(method, handler, id);
            JsonRpcResult result = handler.handle(endpointId, params);
            JsonRpcResponse response = factory.createResponse(id, result, null);

            LOG.debug("Transmitting back a response: {}", response);
            transmitter.transmit(endpointId, response.toString());
        } else {
            LOG.debug("Request has no ID -> it is a notification");

            NotificationHandler handler = registry.getNotificationHandler(method);
            checkHandler(method, handler, null);
            handler.handle(endpointId, params);
        }
    }

    private void checkHandler(String method, Object handler, String id) throws JsonRpcException {
        if (handler == null) {
            LOG.error("No corresponding to method '{}' handler is registered", method);
            throw new JsonRpcException(-32601, "Method '" + method + "' not registered", id);
        }
    }
}
