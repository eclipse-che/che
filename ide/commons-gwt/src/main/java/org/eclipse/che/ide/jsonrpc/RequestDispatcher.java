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
public class RequestDispatcher {
    private final RequestHandlerRegistry      requestHandlerRegistry;
    private final JsonRpcFactory              jsonRpcFactory;
    private final WebSocketMessageTransmitter transmitter;

    @Inject
    public RequestDispatcher(RequestHandlerRegistry requestHandlerRegistry,
                             WebSocketMessageTransmitter transmitter,
                             JsonRpcFactory jsonRpcFactory) {
        this.requestHandlerRegistry = requestHandlerRegistry;
        this.transmitter = transmitter;
        this.jsonRpcFactory = jsonRpcFactory;
    }

    public void dispatch(String endpointId, JsonRpcRequest request) throws JsonRpcException {
        String method = request.getMethod();
        JsonRpcParams params = request.getParams();

        if (request.hasId()) {
            String id = request.getId();
            RequestHandler handler = requestHandlerRegistry.getRequestHandler(method);
            checkHandler(method, handler, id);
            JsonRpcResult result = handler.handle(endpointId, params);
            JsonRpcResponse response = jsonRpcFactory.createResponse(id, result, null);
            transmitter.transmit(endpointId, response.toString());
        } else {
            NotificationHandler handler = requestHandlerRegistry.getNotificationHandler(method);
            checkHandler(method, handler, null);
            handler.handle(endpointId, params);
        }
    }

    private void checkHandler(String method, Object handler, String id) throws JsonRpcException {
        if (handler == null) {
            throw new JsonRpcException(-32601, "Method '" + method + "' not registered", id);
        }
    }
}
