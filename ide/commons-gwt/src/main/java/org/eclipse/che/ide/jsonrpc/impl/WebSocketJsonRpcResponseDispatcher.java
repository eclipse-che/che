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

import com.google.gwt.regexp.shared.RegExp;

import org.eclipse.che.api.core.jsonrpc.shared.JsonRpcResponse;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.jsonrpc.JsonRpcResponseReceiver;
import org.eclipse.che.ide.util.loging.Log;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

/**
 * Dispatches JSON RPC responses among all registered implementations of {@link JsonRpcResponseReceiver}
 * according to their mappings.
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class WebSocketJsonRpcResponseDispatcher implements JsonRpcDispatcher {
    private final Map<RegExp, JsonRpcResponseReceiver> receivers = new HashMap<>();

    private final JsonRpcRequestRegistry registry;
    private final DtoFactory             dtoFactory;

    @Inject
    public WebSocketJsonRpcResponseDispatcher(Map<String, JsonRpcResponseReceiver> receivers,
                                              JsonRpcRequestRegistry registry,
                                              DtoFactory dtoFactory) {

        for (Map.Entry<String, JsonRpcResponseReceiver> entry : receivers.entrySet()) {
            final RegExp regExp = RegExp.compile(entry.getKey());
            final JsonRpcResponseReceiver receiver = entry.getValue();
            this.receivers.put(regExp, receiver);
        }

        this.registry = registry;
        this.dtoFactory = dtoFactory;
    }

    @Override
    public void dispatch(String message) {
        final JsonRpcResponse response = dtoFactory.createDtoFromJson(message, JsonRpcResponse.class);
        final String method = registry.extractFor(response.getId());

        for (Map.Entry<RegExp, JsonRpcResponseReceiver> entry : receivers.entrySet()) {
            final RegExp regExp = entry.getKey();
            if (regExp.test(method)) {
                final JsonRpcResponseReceiver receiver = entry.getValue();
                Log.debug(getClass(), "Matching response receiver: ", receiver.getClass());
                receiver.receive(response);
            }
        }
    }
}
