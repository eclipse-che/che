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

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.js.Executor;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.api.promises.client.js.RejectFunction;
import org.eclipse.che.api.promises.client.js.ResolveFunction;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.util.loging.Log;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dispatches incoming json rpc responses
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class ResponseDispatcher {
    private final Map<String, ResolveFunction> promises        = new HashMap<>();
    private final Map<String, RejectFunction>  rejectFunctions = new HashMap<>();
    private final Map<String, Class<?>>        resultClasses   = new HashMap<>();

    private final DtoFactory dtoFactory;

    @Inject
    public ResponseDispatcher(DtoFactory dtoFactory) {
        this.dtoFactory = dtoFactory;
    }

    /**
     * Dispatches json rpc response received from endpoint identified by a high
     * level identifier and represented as a json object.
     *
     * @param endpointId
     *         high level endpoint identifier
     * @param incomingJson
     *         json object
     */
    public void dispatch(String endpointId, JSONObject incomingJson) {
        Log.debug(getClass(), "Dispatching a response from: " + endpointId + ", json: " + incomingJson);

        final String id = getId(incomingJson);
        Log.debug(getClass(), "Extracted response id: " + id);

        final String key = endpointId + '@' + id;
        Log.debug(getClass(), "Combined response key: " + key);

        if (incomingJson.containsKey("result")) {
            Log.debug(getClass(), "It's a response with result field, processing result");

            final Class resultClass = resultClasses.get(key);
            Log.debug(getClass(), "Extracted response result class: " + resultClass);

            processResult(endpointId, id, resultClass, incomingJson.get("result"));
        } else {
            Log.debug(getClass(), "It's a response with error field, processing error");

            processError(endpointId, id, incomingJson.get("error"));
        }
    }

    /**
     * Register and get a promise that will be resolved when specified response
     * will be dispatched.
     *
     * @param endpointId
     *         high level endpoint identifier
     * @param id
     *         request identifier
     * @param resultClass
     *         class of request result that is contained within response
     *
     * @return promise with result dto
     */
    public <T> Promise<T> getPromise(final String endpointId, final String id, final Class<T> resultClass) {
        Log.debug(getClass(), "Registering single promise for: " + endpointId + ", request id: " + id + "result class: " + resultClass);

        return Promises.create(new Executor.ExecutorBody<T>() {
            @Override
            public void apply(ResolveFunction<T> resolve, RejectFunction reject) {
                promises.put(endpointId + '@' + id, resolve);
                resultClasses.put(endpointId + '@' + id, resultClass);
                rejectFunctions.put(endpointId + '@' + id, reject);
            }
        });
    }

    /**
     * Register and get a promise that will be resolved when specified response
     * will be dispatched. The response result is represented as list of objects
     * that has a type defined in a corresponding parameter.
     *
     * @param endpointId
     *         high level endpoint identifier
     * @param id
     *         request identifier
     * @param resultClass
     *         class of request result that is contained within response
     *
     * @return promise with result dto
     */
    public <T> Promise<List<T>> getListPromise(final String endpointId, final String id, final Class<T> resultClass) {
        Log.debug(getClass(), "Registering list of promises for: " + endpointId + ", request id: " + id + "result class: " + resultClass);

        return Promises.create(new Executor.ExecutorBody<List<T>>() {
            @Override
            public void apply(ResolveFunction<List<T>> resolve, RejectFunction reject) {
                promises.put(endpointId + '@' + id, resolve);
                resultClasses.put(endpointId + '@' + id, resultClass);
                rejectFunctions.put(endpointId + '@' + id, reject);
            }
        });
    }

    private <R> void applyObject(String combinedId, JSONObject result, Class<R> resultClass) {
        ResolveFunction<R> resolveFunction = promises.get(combinedId);
        final R dto = dtoFactory.createDtoFromJson(result.toString(), resultClass);
        resolveFunction.apply(dto);
    }

    private <R> void applyArray(String combinedId, JSONArray result, Class<R> resultClass) {
        ResolveFunction<List<R>> resolveFunction = promises.get(combinedId);
        final List<R> dto = dtoFactory.createListDtoFromJson(result.toString(), resultClass);
        resolveFunction.apply(dto);
    }

    private void processResult(String endpointId, String id, Class resultClass, JSONValue result) {
        final String combinedId = endpointId + '@' + id;
        final JSONObject resultObject = result.isObject();

        if (resultObject != null) {
            applyObject(combinedId, resultObject, resultClass);
        } else {
            final JSONArray resultArray = result.isArray();
            applyArray(combinedId, resultArray, resultClass);
        }
    }

    private void processError(final String endpointId, final String id, final JSONValue error) {
        rejectFunctions.get(endpointId + '@' + id)
                       .apply(new PromiseError() {
                           @Override
                           public String getMessage() {
                               return error.toString();
                           }

                           @Override
                           public Throwable getCause() {
                               return null;
                           }
                       });
    }

    private String getId(JSONObject incomingJson) {
        final JSONValue idValue = incomingJson.get("id");
        final JSONString idString = idValue.isString();
        if (idString == null) {
            return Long.toString((long)idValue.isNumber().doubleValue());
        } else {
            return idString.stringValue();
        }
    }
}
