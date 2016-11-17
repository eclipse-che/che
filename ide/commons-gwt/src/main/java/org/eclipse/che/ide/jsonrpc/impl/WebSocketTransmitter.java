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
import com.google.gwt.json.client.JSONValue;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.jsonrpc.RequestTransmitter;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.ng.WebSocketMessageTransmitter;

import javax.inject.Inject;
import java.util.List;

/**
 * Web socket based json rpc transmitter implementation
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class WebSocketTransmitter implements RequestTransmitter {
    private static long idCounter = 0L;

    private final ResponseDispatcher          responseDispatcher;
    private final WebSocketMessageTransmitter transmitter;
    private final DtoFactory                  dtoFactory;

    @Inject
    public WebSocketTransmitter(ResponseDispatcher responseDispatcher,
                                WebSocketMessageTransmitter transmitter,
                                DtoFactory dtoFactory) {
        this.responseDispatcher = responseDispatcher;
        this.transmitter = transmitter;
        this.dtoFactory = dtoFactory;
    }

    @Override
    public void transmitNotification(String endpointId, String method) {
        Log.debug(getClass(), "Transmitting a notification to: " + endpointId + ", method: " + method);
        internalTransmit(endpointId, method, null, null);
    }

    @Override
    public void transmitNotification(String endpointId, String method, Object params) {
        Log.debug(getClass(), "Transmitting a parametrized notification to: " + endpointId + ", method: " + method + ", params: " + params);
        internalTransmit(endpointId, method, params, null);
    }

    @Override
    public <T> Promise<T> transmitRequest(String endpointId, String method, Class<T> resultClass) {
        Log.debug(getClass(), "Transmitting a request to: " + endpointId + ", method: " + method + ", result class: " + resultClass);

        final String id = Long.toString(++idCounter);
        final Promise<T> promise = responseDispatcher.getPromise(endpointId, id, resultClass);

        internalTransmit(endpointId, method, null, id);
        return promise;
    }

    @Override
    public <T> Promise<T> transmitRequest(String endpointId, String method, Object params, Class<T> resultClass) {
        Log.debug(getClass(), "Transmitting a parametrized request to: " + endpointId +
                              ", method: " + method +
                              ", params: " + params +
                              ", result class: " + resultClass);

        final String id = Long.toString(++idCounter);
        final Promise<T> promise = responseDispatcher.getPromise(endpointId, id, resultClass);

        internalTransmit(endpointId, method, params, id);
        return promise;
    }

    @Override
    public <T> Promise<List<T>> transmitRequestForList(String endpointId, String method, Class<T> resultClass) {
        Log.debug(getClass(), "Transmitting a request for a list to: " + endpointId +
                              ", method: " + method +
                              ", result class: " + resultClass);

        final String id = Long.toString(++idCounter);
        final Promise<List<T>> promise = responseDispatcher.getListPromise(endpointId, id, resultClass);

        internalTransmit(endpointId, method, null, id);
        return promise;
    }

    @Override
    public <T> Promise<List<T>> transmitRequestForList(String endpointId, String method, Object params, Class<T> resultClass) {
        Log.debug(getClass(), "Transmitting a parametrized request for a list to: " + endpointId +
                              ", method: " + method +
                              ", params: " + params +
                              ", result class: " + resultClass);

        final String id = Long.toString(++idCounter);
        final Promise<List<T>> promise = responseDispatcher.getListPromise(endpointId, id, resultClass);

        internalTransmit(endpointId, method, params, id);
        return promise;
    }

    private void internalTransmit(String endpointId, String method, Object dto, String id) {
        final JSONObject request = new JSONObject();

        request.put("jsonrpc", new JSONString("2.0"));
        request.put("method", new JSONString(method));

        if (id != null) {
            request.put("id", new JSONString(id));
        }

        if (dto != null) {
            final String dtoString = dtoFactory.toJson(dto);
            final JSONValue jsonParams = JSONParser.parseStrict(dtoString);
            request.put("params", jsonParams);
        }

        transmitter.transmit(endpointId, request.toString());
    }
}
