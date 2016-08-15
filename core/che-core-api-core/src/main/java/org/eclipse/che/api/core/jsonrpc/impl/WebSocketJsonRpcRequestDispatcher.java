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

import org.eclipse.che.api.core.jsonrpc.JsonRpcRequestReceiver;
import org.eclipse.che.api.core.jsonrpc.shared.JsonRpcRequest;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Dispatches JSON RPC requests among all registered implementations of {@link JsonRpcRequestReceiver}
 * according to their method names.
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class WebSocketJsonRpcRequestDispatcher implements JsonRpcDispatcher {
    private static final Logger LOG = LoggerFactory.getLogger(WebSocketJsonRpcRequestDispatcher.class);

    private final Map<String, JsonRpcRequestReceiver> receivers;

    @Inject
    public WebSocketJsonRpcRequestDispatcher(Map<String, JsonRpcRequestReceiver> receivers) {
        this.receivers = receivers;
    }

    @Override
    public void dispatch(String message, Integer endpointId) {
        final JsonRpcRequest request = DtoFactory.getInstance().createDtoFromJson(message, JsonRpcRequest.class);
        final String method = request.getMethod();

        for (Entry<String, JsonRpcRequestReceiver> entry : receivers.entrySet()) {
            final String candidate = entry.getKey();
            if (Objects.equals(candidate, method)) {
                final JsonRpcRequestReceiver receiver = entry.getValue();
                LOG.debug("Matching json rpc request receiver: {}", receiver.getClass());
                receiver.receive(request, endpointId);
            }
        }
    }
}
