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
package org.eclipse.che.ide.jsonrpc;

import org.eclipse.che.api.promises.client.js.JsPromiseError;
import org.eclipse.che.api.promises.client.js.RejectFunction;
import org.eclipse.che.api.promises.client.js.ResolveFunction;
import org.eclipse.che.ide.util.loging.Log;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Dispatches JSON RPC responses
 */
@Singleton
public class ResponseDispatcher {
    private final Map<String, ResolveFunction> resolveFunctionsOfOne  = new HashMap<>();
    private final Map<String, ResolveFunction> resolveFunctionsOfMany = new HashMap<>();
    private final Map<String, RejectFunction>  rejectFunctions        = new HashMap<>();
    private final Map<String, Class<?>>        resultClasses          = new HashMap<>();

    private static void checkArguments(String endpointId, String requestId, Class<?> rClass, ResolveFunction<?> resolve) {
        checkNotNull(endpointId, "Endpoint ID must not be null");
        checkArgument(!endpointId.isEmpty(), "Endpoint ID must not be empty");

        checkNotNull(requestId, "Request ID must not be null");
        checkArgument(!requestId.isEmpty(), "Request ID must not be empty");

        checkNotNull(rClass, "Result class must not be null");

        checkNotNull(resolve, "Resolve function must not be null");
    }

    private <R> void processOne(JsonRpcResponse response, Class<R> resultClass,
                                ResolveFunction<R> resultBiOperation) {
        JsonRpcResult jsonRpcResult = response.getResult();
        R result = jsonRpcResult.getAs(resultClass);
        resultBiOperation.apply(result);
    }

    private <R> void processMany(JsonRpcResponse response, Class<R> resultClass,
                                 ResolveFunction<List<R>> resultListBiOperation) {
        JsonRpcResult jsonRpcResult = response.getResult();
        List<R> result = jsonRpcResult.getAsListOf(resultClass);
        resultListBiOperation.apply(result);
    }

    public void dispatch(String endpointId, JsonRpcResponse response) {
        checkNotNull(endpointId, "Endpoint ID name must not be null");
        checkArgument(!endpointId.isEmpty(), "Endpoint ID name must not be empty");
        checkNotNull(response, "Response name must not be null");

        Log.debug(getClass(), "Dispatching a response: " + response + ", form endpoint: " + endpointId);

        String responseId = response.getId();
        Log.debug(getClass(), "Fetching response ID: " + responseId);

        String key = endpointId + '@' + responseId;
        Log.debug(getClass(), "Generating key: " + key);

        Class<?> rClass = resultClasses.get(key);
        Log.debug(getClass(), "Fetching result class: " + rClass);

        if (response.hasResult()) {
            Log.debug(getClass(), "Response has result. Proceeding...");

            JsonRpcResult result = response.getResult();
            if (result.isArray()) {
                Log.debug(getClass(), "Result is an array - processing array...");

                processMany(response, rClass, resolveFunctionsOfMany.get(key));
            } else {
                Log.debug(getClass(), "Result is a single object - processing single object...");

                processOne(response, rClass, resolveFunctionsOfOne.get(key));
            }
        } else if (response.hasError()) {
            Log.debug(getClass(), "Response has error. Proceeding...");

            JsonRpcError error = response.getError();
            RejectFunction rejectFunction = rejectFunctions.get(key);
            if (rejectFunction != null) {
                Log.debug(getClass(), "Reject function is found, applying");

                rejectFunction.apply(JsPromiseError.create(error.toString()));
            } else {
                Log.debug(getClass(), "Reject function is not found, skipping");
            }
        } else {
            Log.error(getClass(), "Received incorrect response: no error, no result");
        }
    }

    public <R> void registerPromiseOfOne(String endpointId, String requestId, Class<R> rClass, ResolveFunction<R> resolve,
                                         RejectFunction reject) {
        checkArguments(endpointId, requestId, rClass, resolve);

        String key = endpointId + '@' + requestId;

        resolveFunctionsOfOne.put(key, resolve);
        resultClasses.put(key, rClass);
        rejectFunctions.put(key, reject);
    }

    public <R> void registerPromiseOfMany(String endpointId, String requestId, Class<R> rClass, ResolveFunction<List<R>> resolve,
                                          RejectFunction reject) {
        checkArguments(endpointId, requestId, rClass, resolve);

        String key = endpointId + '@' + requestId;

        resolveFunctionsOfMany.put(key, resolve);
        resultClasses.put(key, rClass);
        rejectFunctions.put(key, reject);
    }
}
