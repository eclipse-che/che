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

import com.google.gson.JsonObject;

import org.eclipse.che.api.core.jsonrpc.RequestHandler;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

/**
 * Dispatches incoming json rpc notification
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class NotificationDispatcher {
    private static final Logger LOG = LoggerFactory.getLogger(NotificationDispatcher.class);

    private final Map<String, RequestHandler> handlers;

    @Inject
    public NotificationDispatcher(Map<String, RequestHandler> handlers) {
        this.handlers = handlers;
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
    public void dispatch(String endpointId, JsonObject incomingJson) {
        LOG.debug("Dispatching incoming notification from: " + endpointId + ", json: " + incomingJson);

        final String method = incomingJson.get("method").getAsString();
        LOG.debug("Extracted notification method: " + method);

        final RequestHandler handler = handlers.get(method);

        if (incomingJson.has("params")) {
            final JsonObject params = incomingJson.get("params").getAsJsonObject();
            LOG.debug("Notification is parametrized, processing parameters: " + params);

            final Class paramsClass = handler.getParamsClass();
            LOG.debug("Extracted notification params class: " + paramsClass);

            dispatch(endpointId, handler, params, paramsClass);
        } else {
            LOG.debug("Notification is not parametrized");


            dispatch(endpointId, handler);
        }
    }

    private void dispatch(String endpointId, RequestHandler<Void, Void> handler) {
        handler.handleNotification(endpointId);
    }

    private <P> void dispatch(String endpointId, RequestHandler<P, Void> handler, JsonObject params, Class<P> paramClass) {
        final P param = DtoFactory.getInstance().createDtoFromJson(params.toString(), paramClass);
        handler.handleNotification(endpointId, param);
    }
}
