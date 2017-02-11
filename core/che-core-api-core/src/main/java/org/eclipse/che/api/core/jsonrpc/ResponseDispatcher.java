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
package org.eclipse.che.api.core.jsonrpc;

import org.eclipse.che.api.core.jsonrpc.transmission.MethodNameConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger LOG = LoggerFactory.getLogger(MethodNameConfigurator.class);

    private final Map<String, JsonRpcPromise> promises = new HashMap<>();
    private final Map<String, Class<?>>       rClasses = new HashMap<>();

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

    private <R> void processOne(String endpointId, JsonRpcResponse response, Class<R> resultClass, BiConsumer<String, R> consumer) {
        LOG.debug("Result is a single object - processing single object...");

        JsonRpcResult jsonRpcResult = response.getResult();
        R result = jsonRpcResult.getAs(resultClass);
        consumer.accept(endpointId, result);
    }

    private <R> void processMany(String endpointId, JsonRpcResponse response, Class<R> resultClass, BiConsumer<String, List> consumer) {
        LOG.debug("Result is an array - processing array...");

        JsonRpcResult jsonRpcResult = response.getResult();
        List<R> result = jsonRpcResult.getAsListOf(resultClass);
        consumer.accept(endpointId, result);
    }

    public void dispatch(String endpointId, JsonRpcResponse response) {
        checkNotNull(endpointId, "Endpoint ID name must not be null");
        checkArgument(!endpointId.isEmpty(), "Endpoint ID name must not be empty");
        checkNotNull(response, "Response name must not be null");

        LOG.debug("Dispatching a response: {}, from endpoint: {}", response, endpointId);

        String responseId = response.getId();
        LOG.debug("Fetching response ID: {}", responseId);

        String key = combine(endpointId, responseId);
        LOG.debug("Generating key: {}", key);

        Class<?> rClass = rClasses.get(key);
        LOG.debug("Fetching result class: {}", rClass);

        if (response.hasResult()) {
            processResult(endpointId, response, key, rClass);
        } else if (response.hasError()) {
            processError(endpointId, response, key);
        } else {
            LOG.error("Received incorrect response: no error, no result");
        }
    }

    private void processError(String endpointId, JsonRpcResponse response, String key) {
        LOG.debug("Response has error. Proceeding...");

        JsonRpcError error = response.getError();
        BiConsumer<String, JsonRpcError> consumer = cast(promises.get(key).getFailureConsumer());
        if (consumer != null) {
            LOG.debug("Failure consumer is found, accepting...");
            consumer.accept(endpointId, error);
        } else {
            LOG.debug("Reject function is not found, skipping");
        }
    }

    private void processResult(String endpointId, JsonRpcResponse response, String key, Class<?> rClass) {
        LOG.debug("Response has result. Proceeding...");

        JsonRpcResult result = response.getResult();
        if (result.isArray()) {
            processMany(endpointId, response, rClass, cast(promises.get(key).getSuccessConsumer()));
        } else {
            processOne(endpointId, response, rClass, cast(promises.get(key).getSuccessConsumer()));
        }
    }

    public <R> JsonRpcPromise<R> registerPromiseOfOne(String endpointId, String requestId, Class<R> rClass, JsonRpcPromise<R> promise) {
        return cast(registerInternal(endpointId, requestId, rClass, promise));
    }

    public <R> JsonRpcPromise<List<R>> registerPromiseOfMany(String endpointId, String requestId, Class<R> rClass,
                                                             JsonRpcPromise<List<R>> promise) {
        return cast(registerInternal(endpointId, requestId, rClass, promise));
    }

    private <R> JsonRpcPromise registerInternal(String endpointId, String requestId, Class<R> rClass, JsonRpcPromise promise) {
        checkArguments(endpointId, requestId, rClass, promise);

        String key = combine(endpointId, requestId);

        promises.put(key, promise);
        rClasses.put(key, rClass);

        return promise;
    }
}
