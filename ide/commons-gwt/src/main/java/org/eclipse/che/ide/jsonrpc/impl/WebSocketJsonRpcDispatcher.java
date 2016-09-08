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

import org.eclipse.che.api.core.jsonrpc.shared.JsonRpcObject;
import org.eclipse.che.api.core.websocket.shared.WebSocketTransmission;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.ng.WebSocketMessageReceiver;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

/**
 * Receives raw JSON RPC objects ({@link JsonRpcObject}) extracted from WEB SOCKET
 * transmissions ({@link WebSocketTransmission}) and dispatches them among more specific
 * dispatchers {@link JsonRpcDispatcher}) according to their type (e.g. JSON RPC
 * request/response dispatchers).
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class WebSocketJsonRpcDispatcher implements WebSocketMessageReceiver {
    private final Map<String, JsonRpcDispatcher> dispatchers;
    private final JsonRpcObjectValidator         validator;
    private final DtoFactory                     dtoFactory;

    @Inject
    public WebSocketJsonRpcDispatcher(Map<String, JsonRpcDispatcher> dispatchers, JsonRpcObjectValidator validator, DtoFactory dtoFactory) {
        this.dispatchers = dispatchers;
        this.validator = validator;
        this.dtoFactory = dtoFactory;
    }

    @Override
    public void receive(String rawJsonRpcObject) {
        final JsonRpcObject jsonRpcObject = dtoFactory.createDtoFromJson(rawJsonRpcObject, JsonRpcObject.class);
        validator.validate(jsonRpcObject);

        final String type = jsonRpcObject.getType();
        final String message = jsonRpcObject.getMessage();

        for (Entry<String, JsonRpcDispatcher> entry : dispatchers.entrySet()) {
            final String typeCandidate = entry.getKey();
            if (Objects.equals(typeCandidate, type)) {
                final JsonRpcDispatcher dispatcher = entry.getValue();
                Log.debug(getClass(), "Matching json rpc message dispatcher: " + dispatcher.getClass());
                dispatcher.dispatch(message);

                return;
            }
        }
    }
}
