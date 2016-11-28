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

import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Dispatches incoming json rpc responses
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class ResponseDispatcher {
    private static final Logger LOG = LoggerFactory.getLogger(ResponseDispatcher.class);

    private final Map<String, CompletableFuture> futures       = new HashMap<>();
    private final Map<String, Class<?>>          resultClasses = new HashMap<>();

    /**
     * Dispatches json rpc response received from endpoint identified by a high
     * level identifier and represented as a json object.
     *
     * @param endpointId
     *         high level endpoint identifier
     * @param incomingJson
     *         json object
     */
    public void dispatch(String endpointId, JsonObject incomingJson) {
        LOG.debug("Dispatching incoming response from: " + endpointId + ", json: " + incomingJson);

        final String id = incomingJson.get("id").getAsString();
        LOG.debug("Extracted response id: " + id);

        final String key = endpointId + '@' + id;
        LOG.debug("Combined response key: " + key);

        final Class resultClass = resultClasses.get(key);
        LOG.debug("Extracted result class: " + resultClass);

        final CompletableFuture completableFuture = futures.get(key);

        if (incomingJson.has("result")) {
            LOG.debug("Response contains result field, processing result");

            final JsonObject result = incomingJson.get("result").getAsJsonObject();
            final Object dto = DtoFactory.getInstance().createDtoFromJson(result.toString(), resultClass);

            completableFuture.complete(dto);
        } else {
            LOG.debug("Response contains error field, processing error");

            final String error = "Error processing is not yet supported";
            LOG.error(error);
            throw new UnsupportedOperationException(error);
        }

    }

    /**
     * Register and get a completable future that will be resolved when specified response
     * will be dispatched.
     *
     * @param endpointId
     *         high level endpoint identifier
     * @param requestId
     *         request identifier
     * @param resultClass
     *         class of request result that is contained within response
     *
     * @return completable future based on result represented by DTO
     */
    public <R> CompletableFuture<R> getCompletableFuture(String endpointId, String requestId, Class<R> resultClass) {
        final CompletableFuture<R> future = new CompletableFuture<>();
        futures.put(endpointId + '@' + requestId, future);
        resultClasses.put(endpointId + '@' + requestId, resultClass);
        return future;
    }
}
