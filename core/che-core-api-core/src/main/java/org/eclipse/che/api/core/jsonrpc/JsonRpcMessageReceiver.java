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
package org.eclipse.che.api.core.jsonrpc;

import org.eclipse.che.api.core.jsonrpc.JsonRpcEntityQualifier.JsonRpcEntityType;
import org.eclipse.che.api.core.websocket.WebSocketMessageReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.eclipse.che.api.core.jsonrpc.JsonRpcList.isArray;

/**
 * Receives and process messages coming from web socket service. Basically
 * it validates, qualifies and transforms a raw web socket message to a JSON
 * RPC known structure and pass it further to appropriate dispatchers. In case
 * of any {@link JsonRpcException} happens during request/response processing
 * this class is also responsible for an error transmission.
 */
@Singleton
public class JsonRpcMessageReceiver implements WebSocketMessageReceiver {
    private static final Logger LOG = LoggerFactory.getLogger(JsonRpcErrorTransmitter.class);

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
        checkNotNull(endpointId, "Endpoint ID must not be null");
        checkArgument(!endpointId.isEmpty(), "Endpoint ID name must not be empty");
        checkNotNull(message, "Message must not be null");
        checkArgument(!message.isEmpty(), "Message must not be empty");

        LOG.debug("Receiving message: " + message + ", from endpoint: " + endpointId);
        try {
            entityValidator.validate(message);

            if (isArray(message)) {
                LOG.debug("Message is an array, processing an array");

                JsonRpcList list = jsonRpcFactory.createList(message);
                List<String> messages = list.toStringifiedList();
                for (String listMessage : messages) {
                    processObject(endpointId, listMessage);
                }
            } else {
                LOG.debug("Message is not an array");

                processObject(endpointId, message);
            }
        } catch (JsonRpcException e) {
            errorTransmitter.transmit(endpointId, e);
        }
    }

    private void processObject(String endpointId, String message) throws JsonRpcException {
        LOG.debug("Processing end object: " + message);

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
                LOG.error(msg);
                throw new JsonRpcException(-32600, msg);
        }
    }
}
