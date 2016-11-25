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

import org.eclipse.che.api.core.jsonrpc.RequestTransmitter;
import org.eclipse.che.api.core.websocket.WebSocketMessageTransmitter;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

/**
 * Web socket based json rpc transmitter implementation
 *
 * @author Dmitry Kuleshov
 */
public class WebSocketTransmitter implements RequestTransmitter {
    private static final Logger LOG = LoggerFactory.getLogger(WebSocketTransmitter.class);

    private static long idCounter = 0L;

    private final ResponseDispatcher          responseDispatcher;
    private final WebSocketMessageTransmitter transmitter;

    @Inject
    public WebSocketTransmitter(ResponseDispatcher responseDispatcher, WebSocketMessageTransmitter transmitter) {
        this.responseDispatcher = responseDispatcher;
        this.transmitter = transmitter;
    }

    @Override
    public void transmitNotification(String endpointId, String method) {
        LOG.debug("Transmitting a parametrized notification to endpoint: " + endpointId + ", method: " + method);

        internalTransmit(endpointId, method, null, null);
    }

    @Override
    public void transmitNotification(String endpointId, String method, Object params) {
        LOG.debug("Transmitting a parametrized notification to endpoint: " + endpointId + ", method: " + method + ", params:" + params);

        internalTransmit(endpointId, method, params, null);
    }

    @Override
    public <R> CompletableFuture<R> transmitRequest(String endpointId, String method, Class<R> resultClass) {
        LOG.debug("Transmitting a request to endpoint: " + endpointId + ", method: " + method + ", result class:" + resultClass);

        final String id = Long.toString(++idCounter);
        internalTransmit(endpointId, method, null, id);
        return responseDispatcher.getCompletableFuture(endpointId, id, resultClass);
    }

    @Override
    public <R> CompletableFuture<R> transmitRequest(String endpointId, String method, Object params, Class<R> resultClass) {
        LOG.debug("Transmitting a parametrized request to endpoint: " + endpointId +
                  ", method: " + method +
                  ", params:" + params +
                  ", result class:" + resultClass);

        final String id = Long.toString(++idCounter);
        internalTransmit(endpointId, method, params, id);
        return responseDispatcher.getCompletableFuture(endpointId, id, resultClass);
    }

    @Override
    public void broadcast(String method, Object params) {
        final String id = null;
        final String endpointId = null;

        internalTransmit(endpointId, method, params, id);
    }

    private void internalTransmit(String endpointId, String method, Object dto, String id) {
        final JsonObject request = new JsonObject();

        request.addProperty("jsonrpc", "2.0");
        if (id != null) {
            request.addProperty("id", id);
        }
        request.addProperty("method", method);
        if (dto != null) {
            final String dtoString = DtoFactory.getInstance().toJson(dto);
            final JsonObject jsonParams = new JsonParser().parse(dtoString).getAsJsonObject();
            request.add("params", jsonParams);
        }

        if (endpointId == null) {
            transmitter.transmit(request.toString());
        } else {
            transmitter.transmit(endpointId, request.toString());
        }
    }

}
