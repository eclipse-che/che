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
package org.eclipse.che.ide.jsonrpc.impl;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;

import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.jsonrpc.RequestHandler;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.ng.WebSocketMessageTransmitter;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

/**
 * Dispatches incoming json rpc requests
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class RequestDispatcher extends AbstractJsonRpcDispatcher {
    private final WebSocketMessageTransmitter transmitter;
    private final DtoFactory                  dtoFactory;

    @Inject
    public RequestDispatcher(Map<String, RequestHandler> handlers, WebSocketMessageTransmitter transmitter, DtoFactory dtoFactory) {
        super(handlers);
        this.transmitter = transmitter;
        this.dtoFactory = dtoFactory;
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
    public void dispatch(String endpointId, JSONObject incomingJson) {
        Log.debug(getClass(), "Dispatching a request from endpoint: " + endpointId + ", jso: " + incomingJson);

        final String method = incomingJson.get("method").isString().stringValue();
        Log.debug(getClass(), "Extracted request method: " + method);

        final RequestHandler handler = getRequestHandler(endpointId, method);

        final String id = incomingJson.get("id").toString();
        Log.debug(getClass(), "Extracted request id: " + id);

        final Class resultClass = handler.getResultClass();
        Log.debug(getClass(), "Extracted request result class: " + resultClass);


        JSONObject result;
        if (incomingJson.containsKey("params")) {
            final JSONObject params = incomingJson.get("params").isObject();
            Log.debug(getClass(), "Request is parametrized, processing parameters: " + params);

            final Class paramsClass = handler.getParamsClass();
            Log.debug(getClass(), "Extracted request params class: " + paramsClass);

            result = dispatch(endpointId, handler, params, paramsClass);
        } else {

            Log.debug(getClass(), "Request is not parametrized");
            result = dispatch(endpointId, handler);
        }

        final JSONObject response = prepareResponse(id, result);
        Log.debug(getClass(), "Prepared a response: " + response);

        transmitter.transmit(endpointId, response.toString());
    }

    private JSONObject prepareResponse(String id, JSONObject result) {
        final JSONObject response = new JSONObject();

        response.put("jsonrpc", new JSONString("2.0"));
        response.put("id", new JSONString(id));
        response.put("result", result);

        return response;
    }

    private <P, R> JSONObject dispatch(String endpointId, RequestHandler<P, R> handler, JSONObject params, Class<P> paramClass) {
        final P param = dtoFactory.createDtoFromJson(params.toString(), paramClass);
        final R result = handler.handleRequest(endpointId, param);

        final String resultString = dtoFactory.toJson(result);
        return JSONParser.parseStrict(resultString).isObject();
    }

    private <R> JSONObject dispatch(String endpointId, RequestHandler<Void, R> handler) {
        final R result = handler.handleRequest(endpointId);

        final String resultString = dtoFactory.toJson(result);
        return JSONParser.parseStrict(resultString).isObject();
    }
}
