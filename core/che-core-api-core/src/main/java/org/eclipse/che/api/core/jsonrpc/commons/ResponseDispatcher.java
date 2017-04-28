/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.core.jsonrpc.commons;

import org.eclipse.che.api.core.logger.commons.Logger;
import org.eclipse.che.api.core.logger.commons.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Dispatches JSON RPC responses
 */
@Singleton
public class ResponseDispatcher {
    private final Logger          logger;
    private final JsonRpcComposer composer;

    private final Map<String, JsonRpcPromise> promises = new HashMap<>();
    private final Map<String, Class<?>>       rClasses = new HashMap<>();

    @Inject
    public ResponseDispatcher(LoggerFactory loggerFactory, JsonRpcComposer composer) {
        this.logger = loggerFactory.get(getClass());
        this.composer = composer;
    }

    private static void checkArguments(String endpointId, String requestId, Class<?> rClass, JsonRpcPromise success) {
        checkNotNull(endpointId, "Endpoint ID must not be null");
        checkArgument(!endpointId.isEmpty(), "Endpoint ID must not be empty");

        checkNotNull(requestId, "Request ID must not be null");
        checkArgument(!requestId.isEmpty(), "Request ID must not be empty");

        checkNotNull(rClass, "Result class must not be null");

        checkNotNull(success, "Json rpc promise must not be null");
    }

    private static String combine(String endpointId, String requestId) {
        return endpointId + '@' + requestId;
    }

    @SuppressWarnings("unchecked")
    private static <T> T cast(Object object) {
        return (T)object;
    }

    private <R> void processOne(String endpointId, JsonRpcResult jsonRpcResult, Class<R> resultClass, BiConsumer<String, R> consumer) {
        logger.debug("Result is a single object - processing single object...");

        R result = composer.composeOne(jsonRpcResult, resultClass);
        consumer.accept(endpointId, result);
    }

    private <R> void processMany(String endpointId, JsonRpcResult jsonRpcResult, Class<R> resultClass, BiConsumer<String, List> consumer) {
        logger.debug("Result is an array - processing array...");

        List<R> result = composer.composeMany(jsonRpcResult, resultClass);
        consumer.accept(endpointId, result);
    }

    public void dispatch(String endpointId, JsonRpcResponse response) {
        checkNotNull(endpointId, "Endpoint ID name must not be null");
        checkArgument(!endpointId.isEmpty(), "Endpoint ID name must not be empty");
        checkNotNull(response, "Response name must not be null");

        logger.debug("Dispatching a response: " + response + ", from endpoint: " + endpointId);

        String responseId = response.getId();
        if (responseId == null) {
            logger.debug("Response ID is not defined, skipping...");
            return;
        }
        logger.debug("Fetching response ID: " + responseId);

        String key = combine(endpointId, responseId);
        logger.debug("Generating key: " + key);

        Class<?> rClass = rClasses.get(key);
        logger.debug("Fetching result class:" + rClass);

        if (response.hasResult()) {
            processResult(endpointId, response, key, rClass);
        } else if (response.hasError()) {
            processError(endpointId, response, key);
        } else {
            logger.error("Received incorrect response: no error, no result");
        }
    }

    private void processError(String endpointId, JsonRpcResponse response, String key) {
        logger.debug("Response has error. Proceeding...");

        JsonRpcError error = response.getError();
        JsonRpcPromise<JsonRpcError> jsonRpcPromise = cast(promises.get(key));
        BiConsumer<String, JsonRpcError> consumer = jsonRpcPromise.getFailureConsumer();
        if (consumer != null) {
            logger.debug("Failure consumer is found, accepting...");
            consumer.accept(endpointId, error);
        } else {
            logger.debug("Reject function is not found, skipping");
        }
    }

    private void processResult(String endpointId, JsonRpcResponse response, String key, Class<?> rClass) {
        logger.debug("Response has result. Proceeding...");

        JsonRpcResult result = response.getResult();
        if (result.isSingle()) {
            processOne(endpointId, result, rClass, cast(promises.get(key).getSuccessConsumer()));
        } else {
            processMany(endpointId, result, rClass, cast(promises.get(key).getSuccessConsumer()));
        }
    }

    public <R> JsonRpcPromise<R> registerPromiseOfOne(String endpointId, String requestId, Class<R> rClass) {
        return cast(registerInternal(endpointId, requestId, rClass, new JsonRpcPromise<R>()));
    }

    public <R> JsonRpcPromise<List<R>> registerPromiseOfMany(String endpointId, String requestId, Class<R> rClass) {
        return cast(registerInternal(endpointId, requestId, rClass, new JsonRpcPromise<List<R>>()));
    }

    private <R> JsonRpcPromise registerInternal(String endpointId, String requestId, Class<R> rClass, JsonRpcPromise promise) {
        checkArguments(endpointId, requestId, rClass, promise);

        String key = combine(endpointId, requestId);

        promises.put(key, promise);
        rClasses.put(key, rClass);

        return promise;
    }
}
