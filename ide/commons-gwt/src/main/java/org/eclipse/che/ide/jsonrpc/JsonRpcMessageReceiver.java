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

import org.eclipse.che.ide.jsonrpc.JsonRpcEntityQualifier.JsonRpcEntityType;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.ng.WebSocketMessageReceiver;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static org.eclipse.che.ide.jsonrpc.JsonRpcList.isArray;

@Singleton
public class JsonRpcMessageReceiver implements WebSocketMessageReceiver {
    private final RequestDispatcher       requestDispatcher;
    private final ResponseDispatcher      responseDispatcher;
    private final JsonRpcEntityQualifier  entityQualifier;
    private final JsonRpcEntityValidator  entityValidator;
    private final JsonRpcErrorTransmitter errorTransmitter;
    private final JsonRpcFactory          jsonRpcFactory;

    @Inject
    public JsonRpcMessageReceiver(RequestDispatcher requestDispatcher,
                                  ResponseDispatcher responseDispatcher,
                                  JsonRpcEntityQualifier entityQualifier,
                                  JsonRpcEntityValidator entityValidator,
                                  JsonRpcErrorTransmitter errorTransmitter,
                                  JsonRpcFactory jsonRpcFactory) {
        this.requestDispatcher = requestDispatcher;
        this.responseDispatcher = responseDispatcher;
        this.entityQualifier = entityQualifier;
        this.entityValidator = entityValidator;
        this.errorTransmitter = errorTransmitter;
        this.jsonRpcFactory = jsonRpcFactory;
    }

    @Override
    public void receive(String endpointId, String message) {
        try {
            entityValidator.validate(message);

            if (isArray(message)) {
                JsonRpcList list = jsonRpcFactory.createList(message);
                List<String> messages = list.toStringifiedList();
                for (String listMessage : messages) {
                    processMessage(endpointId, listMessage);
                }
            } else {
                processMessage(endpointId, message);
            }
        } catch (JsonRpcException e) {
            errorTransmitter.transmit(endpointId, e);
        }
    }

    private void processMessage(String endpointId, String message) throws JsonRpcException {
        JsonRpcEntityType type = entityQualifier.qualify(message);

        switch (type) {
            case REQUEST:
                JsonRpcRequest request = jsonRpcFactory.createRequest(message);
                requestDispatcher.dispatch(endpointId, request);
                break;
            case RESPONSE:
                JsonRpcResponse response = jsonRpcFactory.createResponse(message);
                responseDispatcher.dispatch(endpointId, response);
                break;
            case UNDEFINED:
            default:
                String msg = "The JSON sent is not a valid Request object";
                Log.error(getClass(), msg);
                throw new JsonRpcException(-32600, msg);
        }
    }
}
