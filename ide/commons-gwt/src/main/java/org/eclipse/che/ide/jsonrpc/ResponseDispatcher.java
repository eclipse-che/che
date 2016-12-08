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
package org.eclipse.che.ide.jsonrpc;

import org.eclipse.che.api.promises.client.js.JsPromiseError;
import org.eclipse.che.api.promises.client.js.RejectFunction;
import org.eclipse.che.api.promises.client.js.ResolveFunction;
import org.eclipse.che.ide.util.loging.Log;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class ResponseDispatcher {
    private final Map<String, ResolveFunction> resolveFunctionsOfOne  = new HashMap<>();
    private final Map<String, ResolveFunction> resolveFunctionsOfMany = new HashMap<>();
    private final Map<String, RejectFunction>  rejectFunctions        = new HashMap<>();
    private final Map<String, Class<?>>        resultClasses          = new HashMap<>();

    public void dispatch(String endpointId, JsonRpcResponse response) {
        String id = response.getId();
        String key = endpointId + '@' + id;

        Class<?> resultClass = resultClasses.get(key);

        if (response.hasResult()) {
            JsonRpcResult result = response.getResult();
            if (result.isArray()) {
                processMany(response, resultClass, resolveFunctionsOfMany.get(key));
            } else {
                processOne(response, resultClass, resolveFunctionsOfOne.get(key));
            }
        } else if (response.hasError()) {
            JsonRpcError error = response.getError();
            RejectFunction rejectFunction = rejectFunctions.get(key);
            rejectFunction.apply(JsPromiseError.create(error.toString()));
        } else {
            Log.error(getClass(), "Received incorrect response: no error, no result");
        }
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

    public <R> void registerPromiseOfOne(String endpointId,
                                         String requestId,
                                         Class<R> resultClass,
                                         ResolveFunction<R> resolveFunction,
                                         RejectFunction rejectFunction) {
        String key = endpointId + '@' + requestId;


        resolveFunctionsOfOne.put(key, resolveFunction);
        resultClasses.put(key, resultClass);
        rejectFunctions.put(key, rejectFunction);
    }

    public <R> void registerPromiseOfMany(String endpointId,
                                          String requestId,
                                          Class<R> resultClass,
                                          ResolveFunction<List<R>> resolveFunction,
                                          RejectFunction rejectFunction) {
        String key = endpointId + '@' + requestId;


        resolveFunctionsOfMany.put(key, resolveFunction);
        resultClasses.put(key, resultClass);
        rejectFunctions.put(key, rejectFunction);
    }
}
