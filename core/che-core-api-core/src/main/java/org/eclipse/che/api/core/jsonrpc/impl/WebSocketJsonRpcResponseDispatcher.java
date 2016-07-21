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

import org.eclipse.che.api.core.jsonrpc.JsonRpcResponseReceiver;
import org.eclipse.che.api.core.jsonrpc.shared.JsonRpcResponse;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Dispatches JSON RPC responses among all registered implementations of {@link JsonRpcResponseReceiver}
 * according to their method names.
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class WebSocketJsonRpcResponseDispatcher implements JsonRpcDispatcher {
    private static final Logger LOG = LoggerFactory.getLogger(WebSocketJsonRpcResponseDispatcher.class);

    private final JsonRpcRequestRegistry requestRegistry;

    private final Map<Pattern, JsonRpcResponseReceiver> receivers = new HashMap<>();

    @Inject
    public WebSocketJsonRpcResponseDispatcher(JsonRpcRequestRegistry requestRegistry, Map<String, JsonRpcResponseReceiver> receivers) {
        this.requestRegistry = requestRegistry;

        receivers.forEach((k, v) -> this.receivers.put(Pattern.compile(k), v));
    }

    @Override
    public void dispatch(String message, Integer endpointId) {
        final JsonRpcResponse response = DtoFactory.getInstance().createDtoFromJson(message, JsonRpcResponse.class);
        final String method = requestRegistry.extractFor(response.getId());

        for (Entry<Pattern, JsonRpcResponseReceiver> entry : receivers.entrySet()) {
            final Pattern pattern = entry.getKey();
            final Matcher matcher = pattern.matcher(method);
            if (matcher.matches()) {
                final JsonRpcResponseReceiver receiver = entry.getValue();
                LOG.debug("Matching json rpc response receiver: {}", receiver.getClass());
                receiver.receive(response, endpointId);
            }
        }
    }
}
