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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.eclipse.che.api.core.websocket.WebSocketMessageReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Dispatches messages received from web socket endpoint throughout json rpc
 * inner infrastructure.
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class WebSocketToJsonRpcDispatcher implements WebSocketMessageReceiver {
    private static final Logger LOG = LoggerFactory.getLogger(WebSocketToJsonRpcDispatcher.class);

    private final RequestDispatcher      requestDispatcher;
    private final NotificationDispatcher notificationDispatcher;
    private final ResponseDispatcher     responseDispatcher;

    @Inject
    public WebSocketToJsonRpcDispatcher(RequestDispatcher requestDispatcher,
                                        NotificationDispatcher notificationDispatcher,
                                        ResponseDispatcher responseDispatcher) {
        this.requestDispatcher = requestDispatcher;
        this.notificationDispatcher = notificationDispatcher;
        this.responseDispatcher = responseDispatcher;
    }

    @Override
    public void receive(String endpointId, String message) {
        LOG.debug("Receiving a message from: " + endpointId + ", message" + message);
        final JsonParser jsonParser = new JsonParser();
        final JsonElement jsonElement = jsonParser.parse(message);

        if (!jsonElement.isJsonObject()) {
            final String error = "Json is an array, not supported yet";
            LOG.error(error);
            throw new UnsupportedOperationException(error);
        }

        final JsonObject incomingJson = jsonElement.getAsJsonObject();
        final boolean hasMethod = incomingJson.has("method");
        final boolean hasParams = incomingJson.has("params");
        final boolean hasId = incomingJson.has("id");
        final boolean hasResult = incomingJson.has("result");
        final boolean hasError = incomingJson.has("error");

        if (hasMethod && hasId && !hasResult && !hasError) {
            LOG.debug("It's a request, processing by request dispatcher.");
            requestDispatcher.dispatch(endpointId, incomingJson);
            return;
        }

        if (hasMethod && !hasId && !hasResult && !hasError) {
            LOG.debug("It's a notification, processing by notification dispatcher.");
            notificationDispatcher.dispatch(endpointId, incomingJson);
            return;
        }

        if (!hasMethod && !hasParams && hasId && (hasError != hasResult)) {
            LOG.debug("It's a response, processing by response dispatcher.");
            responseDispatcher.dispatch(endpointId, incomingJson);
            return;
        }

        throw new IllegalStateException("Improper json rpc message.");
    }
}
