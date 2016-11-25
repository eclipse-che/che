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
package org.eclipse.che.api.core.jsonrpc.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.eclipse.che.api.core.jsonrpc.RequestHandler;
import org.eclipse.che.api.core.websocket.WebSocketMessageTransmitter;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

/**
 * Dispatches incoming json rpc requests
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class RequestDispatcher {
    private static final Logger LOG = LoggerFactory.getLogger(RequestDispatcher.class);

    private final Map<String, RequestHandler> handlers;
    private final WebSocketMessageTransmitter transmitter;

    @Inject
    public RequestDispatcher(Map<String, RequestHandler> handlers, WebSocketMessageTransmitter transmitter) {
        this.handlers = handlers;
        this.transmitter = transmitter;
    }

    /**
     * Dispatches json rpc request received from endpoint identified by a high
     * level identifier and represented as a json object.
     *
     * @param endpointId
     *         high level endpoint identifier
     * @param incomingJson
     *         json object
     */
    public void dispatch(String endpointId, JsonObject incomingJson) {
        LOG.debug("Dispatching incoming request from: " + endpointId + ", json: " + incomingJson);

        final String id = incomingJson.get("id").getAsString();
        LOG.debug("Extracted request id: " + id);

        final String method = incomingJson.get("method").getAsString();
        LOG.debug("Extracted request method: " + method);

        final RequestHandler handler = handlers.get(method);

        final Class resultClass = handler.getResultClass();
        LOG.debug("Extracted request result class: " + resultClass);


        JsonObject result;
        if (incomingJson.has("params")) {
            final JsonObject params = incomingJson.get("params").getAsJsonObject();
            LOG.debug("Request is parametrized, processing parameters: " + params);

            final Class paramsClass = handler.getParamsClass();
            LOG.debug("Extracted request params class: " + paramsClass);

            result = dispatch(endpointId, handler, params, paramsClass, resultClass);
        } else {
            LOG.debug("Request is parametrized.");

            result = dispatch(endpointId, handler, resultClass);
        }

        final JsonObject response = prepareResponse(id, result);
        LOG.debug("Generated response: " + response);

        transmitter.transmit(endpointId, response.toString());
    }

    private <P, R> JsonObject dispatch(String endpointId,
                                       RequestHandler<P, R> handler,
                                       JsonObject params,
                                       Class<P> paramClass,
                                       Class<R> resultClass) {
        final P param = DtoFactory.getInstance().createDtoFromJson(params.toString(), paramClass);
        final R result = handler.handleRequest(endpointId, param);
        final String resultString = DtoFactory.getInstance().toJson(result);
        return new JsonParser().parse(resultString).getAsJsonObject();
    }

    private <R> JsonObject dispatch(String endpointId, RequestHandler<Void, R> handler, Class<R> resultClass) {
        final R result = handler.handleRequest(endpointId);
        final String resultString = DtoFactory.getInstance().toJson(result);
        return new JsonParser().parse(resultString).getAsJsonObject();
    }

    private JsonObject prepareResponse(String id, JsonObject result) {
        final JsonObject response = new JsonObject();

        response.addProperty("jsonrpc", "2.0");
        response.addProperty("id", id);
        response.add("result", result);

        return response;
    }
}
