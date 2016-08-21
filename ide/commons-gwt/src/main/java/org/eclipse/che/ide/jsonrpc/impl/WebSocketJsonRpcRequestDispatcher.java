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

import org.eclipse.che.api.core.jsonrpc.shared.JsonRpcRequest;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.jsonrpc.JsonRpcRequestReceiver;
import org.eclipse.che.ide.util.loging.Log;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import static com.google.gwt.regexp.shared.RegExp.compile;

/**
 * Dispatches JSON RPC requests among all registered implementations of {@link JsonRpcRequestReceiver}
 * according to their mappings.
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class WebSocketJsonRpcRequestDispatcher implements JsonRpcDispatcher {
    private final Map<String, JsonRpcRequestReceiver> receivers;

    private final DtoFactory dtoFactory;

    @Inject
    public WebSocketJsonRpcRequestDispatcher(Map<String, JsonRpcRequestReceiver> receivers, DtoFactory dtoFactory) {
        this.receivers = receivers;
        this.dtoFactory = dtoFactory;
    }

    @Override
    public void dispatch(String message) {
        final JsonRpcRequest request = dtoFactory.createDtoFromJson(message, JsonRpcRequest.class);
        final String method = request.getMethod();

        for (Entry<String, JsonRpcRequestReceiver> entry : receivers.entrySet()) {
            final String candidate = entry.getKey();
            if (Objects.equals(candidate, method)) {
                final JsonRpcRequestReceiver receiver = entry.getValue();
                Log.debug(getClass(), "Matching request receiver: ", receiver.getClass());
                receiver.receive(request);
            }
        }
    }
}
