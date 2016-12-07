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

import org.eclipse.che.ide.jsonrpc.RequestHandler;
import org.eclipse.che.ide.util.loging.Log;

import javax.inject.Inject;
import java.util.Map;

/**
 * @author Dmitry Kuleshov
 */
public abstract class AbstractJsonRpcDispatcher {
    private final Map<String, RequestHandler> handlers;

    @Inject
    public AbstractJsonRpcDispatcher(Map<String, RequestHandler> handlers) {
        this.handlers = handlers;
    }

    RequestHandler getRequestHandler(String endpointId, String method) {
        if (handlers.containsKey(method)) {
            return handlers.get(method);
        }

        final String error = "No handler registered for method: " + method + ", and endpoint: " + endpointId;
        Log.error(getClass(), error);
        throw new IllegalStateException(error);
    }
}
