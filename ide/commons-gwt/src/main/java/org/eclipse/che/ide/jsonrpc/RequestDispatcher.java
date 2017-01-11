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
package org.eclipse.che.ide.jsonrpc;

import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.ng.WebSocketMessageTransmitter;

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

        Log.debug(getClass(), "Dispatching request: " + request + ", " + endpointId);

        String method = request.getMethod();
        JsonRpcParams params = request.getParams();

        if (request.hasId()) {
            Log.debug(getClass(), "Request has ID");

            String id = request.getId();
            RequestHandler handler = registry.getRequestHandler(method);
            checkHandler(method, handler, id);
            JsonRpcResult result = handler.handle(endpointId, params);
            JsonRpcResponse response = factory.createResponse(id, result, null);

            Log.debug(getClass(), "Transmitting back a response: " + response);
            transmitter.transmit(endpointId, response.toString());
        } else {
            Log.debug(getClass(), "Request has no ID -> it is a notification");

            NotificationHandler handler = registry.getNotificationHandler(method);
            checkHandler(method, handler, null);
            handler.handle(endpointId, params);
        }
    }

    private void checkHandler(String method, Object handler, String id) throws JsonRpcException {
        if (handler == null) {
            Log.error(getClass(), "No corresponding to method: " + method + " handler is registered");
            throw new JsonRpcException(-32601, "Method '" + method + "' not registered", id);
        }
    }
}
