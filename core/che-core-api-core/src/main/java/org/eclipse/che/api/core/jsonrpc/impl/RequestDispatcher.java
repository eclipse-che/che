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
package org.eclipse.che.api.core.jsonrpc.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.eclipse.che.api.core.jsonrpc.RequestHandler;
import org.eclipse.che.api.core.websocket.WebSocketMessageTransmitter;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.dto.server.JsonSerializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
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
        if (handler == null) {
            LOG.error("Handler not found: " + method);
            // TODO make a centralized standard errors structure
            transmitter.transmit(endpointId, error(-32601, "Method not found: " + method));
            return;
        }

        final Class resultClass = handler.getResultClass();
        LOG.debug("Extracted request result class: " + resultClass);


        JsonElement result;
        if (incomingJson.has("params")) {
            final JsonObject params = incomingJson.get("params").getAsJsonObject();
            LOG.debug("Request is parametrized, processing parameters: " + params);

            final Class paramsClass = handler.getParamsClass();
            LOG.debug("Extracted request params class: " + paramsClass);
            result = response(endpointId, handler, params, paramsClass, resultClass);
        } else {
            LOG.debug("Request is not parametrized.");
            result = response(endpointId, handler, null, null, resultClass);
        }

        final JsonElement response = prepareResponse(id, result);
        LOG.debug("Generated response: " + response);

        transmitter.transmit(endpointId, response.toString());
    }

    private <P, R> JsonElement response(String endpointId,
                                       RequestHandler<P, R> handler,
                                       JsonObject params,
                                       Class<P> paramClass,
                                       Class<R> resultClass) {

        final R result;

        if (paramClass != null) {
            final P param = DtoFactory.getInstance().createDtoFromJson(params.toString(), paramClass);
            result = handler.handleRequest(endpointId, param);
        } else {
            result = handler.handleRequest(endpointId);
        }

        LOG.debug("Dispatch response: ", result);

        if (result instanceof Void)
            return new JsonObject();
        else if (result instanceof String) {
            JsonObject response = new JsonObject();
            response.addProperty("text", (String)result);
            return response;
        } else if (result instanceof Collection) {   // list of DTO objects
            JsonArray valueArray = new JsonArray();
            ((Collection)result).stream().filter(r -> r instanceof JsonSerializable).forEach(r -> {
                String resultString = DtoFactory.getInstance().toJson(r);
                valueArray.add(new JsonParser().parse(resultString).getAsJsonObject());
            });
            return valueArray;
        }

        // DTO object otherwise
        final String resultString = DtoFactory.getInstance().toJson(result);
        return new JsonParser().parse(resultString).getAsJsonObject();
    }


    private JsonElement prepareResponse(String id, JsonElement result) {
        final JsonObject response = new JsonObject();

        response.addProperty("jsonrpc", "2.0");
        response.addProperty("id", id);
        response.add("result", result);

        return response;
    }

    private String error(int code, String message) {
        final JsonObject response = new JsonObject();

        response.addProperty("code", code);
        response.addProperty("message", message);

        return response.toString();
    }
}
