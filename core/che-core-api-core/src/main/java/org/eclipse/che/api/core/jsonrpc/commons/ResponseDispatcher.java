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

import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Dispatches JSON RPC responses
 */
@Singleton
public class ResponseDispatcher {
    private final static Logger LOGGER = getLogger(ResponseDispatcher.class);

    private final JsonRpcComposer composer;

    private final Map<String, JsonRpcPromise> promises = new ConcurrentHashMap<>();
    private final Map<String, Class<?>>       rClasses = new ConcurrentHashMap<>();

    @Inject
    public ResponseDispatcher(JsonRpcComposer composer) {
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
        LOGGER.debug("Result is a single object - processing single object...");

        R result = composer.composeOne(jsonRpcResult, resultClass);
        if (consumer == null) {
            LOGGER.debug("No consumer is not found, skipping.");
        } else {
            LOGGER.debug("Consumer is found, processing.");

            consumer.accept(endpointId, result);
        }
    }

    private <R> void processMany(String endpointId, JsonRpcResult jsonRpcResult, Class<R> resultClass, BiConsumer<String, List> consumer) {
        LOGGER.debug("Result is an array - processing array...");

        List<R> result = composer.composeMany(jsonRpcResult, resultClass);
        if (consumer == null) {
            LOGGER.debug("No consumer is not found, skipping.");
        } else {
            LOGGER.debug("Consumer is found, processing.");

            consumer.accept(endpointId, result);
        }
    }

    public void dispatch(String endpointId, JsonRpcResponse response) {
        checkNotNull(endpointId, "Endpoint ID name must not be null");
        checkArgument(!endpointId.isEmpty(), "Endpoint ID name must not be empty");
        checkNotNull(response, "Response name must not be null");

        LOGGER.debug("Dispatching a response: {} from endpoint: {}", response, endpointId);

        String responseId = response.getId();
        if (responseId == null) {
            LOGGER.debug("Response ID is not defined, skipping...");
            return;
        }
        LOGGER.debug("Fetching response ID: {}", responseId);

        String key = combine(endpointId, responseId);
        LOGGER.debug("Generating key: {} ", key);

        if (response.hasResult()) {
            processResult(endpointId, response, key);
        } else if (response.hasError()) {
            processError(endpointId, response, key);
        } else {
            LOGGER.error("Received incorrect response: no error, no result");
        }
    }

    private void processError(String endpointId, JsonRpcResponse response, String key) {
        LOGGER.debug("Response has error. Proceeding...");

        if (!promises.containsKey(key)) {
            LOGGER.debug("No action is associated for the key: {}, skipping.", key);
            return;
        }

        JsonRpcError error = response.getError();

        JsonRpcPromise<?> promise = promises.remove(key);
        LOGGER.debug("Fetching promise: {}", promise);

        BiConsumer<String, JsonRpcError> consumer = promise.getFailureConsumer();
        if (consumer != null) {
            LOGGER.debug("Failure consumer is found, accepting...");
            consumer.accept(endpointId, error);
        } else {
            LOGGER.debug("Reject function is not found, skipping");
        }
    }

    private void processResult(String endpointId, JsonRpcResponse response, String key) {
        LOGGER.debug("Response has result. Proceeding...");

        if (!promises.containsKey(key)) {
            LOGGER.debug("No promise is associated with the key: {}, skipping.", key);
            return;
        }

        if (!rClasses.containsKey(key)) {
            LOGGER.debug("No result class is associated for the key: {}, skipping.", key);
            return;
        }

        JsonRpcResult result = response.getResult();

        Class<?> rClass = rClasses.remove(key);
        LOGGER.debug("Fetching result class: {}", rClass);

        JsonRpcPromise promise = promises.remove(key);
        LOGGER.debug("Fetching promise: {}", promise);

        if (result.isSingle()) {
            processOne(endpointId, result, rClass, cast(promise.getSuccessConsumer()));
        } else {
            processMany(endpointId, result, rClass, cast(promise.getSuccessConsumer()));
        }
    }

    public synchronized <R> JsonRpcPromise<R> registerPromiseOfOne(String endpointId, String requestId, Class<R> rClass) {
        return cast(registerInternal(endpointId, requestId, rClass, new JsonRpcPromise<R>()));
    }

    public synchronized <R> JsonRpcPromise<List<R>> registerPromiseOfMany(String endpointId, String requestId, Class<R> rClass) {
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
