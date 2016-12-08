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

import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.ng.WebSocketMessageReceiver;

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
        Log.debug(getClass(), "Dispatching message: " + message + " form endpoint: " + endpointId);

        final JSONObject incomingJson = JSONParser.parseStrict(message).isObject();

        final boolean hasMethod = incomingJson.containsKey("method");
        final boolean hasParams = incomingJson.containsKey("params");
        final boolean hasId = incomingJson.containsKey("id");
        final boolean hasResult = incomingJson.containsKey("result");
        final boolean hasError = incomingJson.containsKey("error");

        if (hasMethod && hasId && !hasResult && !hasError) {
            Log.debug(getClass(), "It is a request, calling request dispatcher.");
            requestDispatcher.dispatch(endpointId, incomingJson);
            return;
        }

        if (hasMethod && !hasId &&  !hasResult && !hasError) {
            Log.debug(getClass(), "It is a notification, calling notification dispatcher.");
            notificationDispatcher.dispatch(endpointId, incomingJson);
            return;
        }

        if (!hasMethod && !hasParams && hasId && (hasError != hasResult)) {
            Log.debug(getClass(), "It is a response, calling response dispatcher.");
            responseDispatcher.dispatch(endpointId, incomingJson);
            return;
        }

        final String error = "Malformed Json RPC message, could not define message type or parse it properly.";
        Log.error(getClass(), error);
        throw new IllegalStateException(error);
    }
}
