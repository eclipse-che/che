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

import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.jsonrpc.RequestHandler;
import org.eclipse.che.ide.util.loging.Log;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

/**
 * Dispatches incoming json rpc notification
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class NotificationDispatcher extends AbstractJsonRpcDispatcher {
    private final DtoFactory dtoFactory;

    @Inject
    public NotificationDispatcher(Map<String, RequestHandler> handlers, DtoFactory dtoFactory) {
        super(handlers);
        this.dtoFactory = dtoFactory;
    }

    /**
     * Dispatches json rpc notification received from endpoint identified by a
     * high level identifier and represented as a json object.
     *
     * @param endpointId
     *         high level endpoint identifier
     * @param incomingJson
     *         json object
     */
    public void dispatch(String endpointId, JSONObject incomingJson) {
        Log.debug(getClass(), "Dispatching a notification from endpoint: " + endpointId + ", json: " + incomingJson);

        final String method = incomingJson.get("method").isString().stringValue();
        Log.debug(getClass(), "Extracted notification method: " + method);

        final RequestHandler handler = getRequestHandler(endpointId, method);

        if (incomingJson.containsKey("params")) {
            final JSONObject params = incomingJson.get("params").isObject();
            Log.debug(getClass(), "Notification is parametrized, processing parameters: " + params);

            final Class paramsClass = handler.getParamsClass();
            Log.debug(getClass(), "Extracted notification params class: " + paramsClass);

            dispatch(endpointId, handler, params, paramsClass);
        } else {
            dispatch(endpointId, handler);
        }

    }

    private <P> void dispatch(String endpointId, RequestHandler<P, Void> handler, JSONObject params, Class<P> paramsClass) {
        final P param = dtoFactory.createDtoFromJson(params.toString(), paramsClass);
        handler.handleNotification(endpointId, param);
    }

    private void dispatch(String endpointId, RequestHandler handler) {
        handler.handleNotification(endpointId);
    }
}
