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

import org.eclipse.che.api.core.jsonrpc.shared.JsonRpcObject;
import org.eclipse.che.api.core.websocket.WebSocketMessageReceiver;
import org.eclipse.che.api.core.websocket.shared.WebSocketTransmission;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

/**
 * Receives instances of raw {@link JsonRpcObject} extracted from {@link WebSocketTransmission}.
 * They are raw because they are presented as {@link String}. Those objects are dispatched among
 * more specific dispatchers {@link JsonRpcDispatcher}) according to their type (e.g. JSON RPC
 * request/response dispatchers).
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class WebSocketJsonRpcDispatcher implements WebSocketMessageReceiver {
    private static final Logger LOG = LoggerFactory.getLogger(WebSocketJsonRpcDispatcher.class);

    private final Map<String, JsonRpcDispatcher> dispatchers;
    private final JsonRpcObjectValidator         validator;

    @Inject
    public WebSocketJsonRpcDispatcher(Map<String, JsonRpcDispatcher> dispatchers, JsonRpcObjectValidator validator) {
        this.dispatchers = dispatchers;
        this.validator = validator;
    }

    @Override
    public void receive(String rawJsonRpcObject, Integer endpointId) {
        final JsonRpcObject jsonRpcObject = DtoFactory.getInstance().createDtoFromJson(rawJsonRpcObject, JsonRpcObject.class);
        validator.validate(jsonRpcObject);

        final String type = jsonRpcObject.getType();
        final String message = jsonRpcObject.getMessage();

        for (Entry<String, JsonRpcDispatcher> entry : dispatchers.entrySet()) {
            final String typeCandidate = entry.getKey();
            if (Objects.equals(typeCandidate, type)) {
                final JsonRpcDispatcher dispatcher = entry.getValue();
                LOG.debug("Matching json rpc message dispatcher: {}", dispatcher.getClass());
                dispatcher.dispatch(message, endpointId);

                return;
            }
        }
    }
}
