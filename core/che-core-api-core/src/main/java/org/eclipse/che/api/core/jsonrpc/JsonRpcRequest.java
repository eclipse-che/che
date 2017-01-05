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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents JSON RPC request object. Can be constructed out of
 * stringified json object or by passing specific parameters.
 * Use {@link JsonRpcFactory#createRequest(String, String, JsonRpcParams)}
 * {@link JsonRpcFactory#createRequest(String)} or
 * {@link JsonRpcFactory#createRequest(String, JsonRpcParams)} to get an instance.
 */
public class JsonRpcRequest {
    private final String        id;
    private final String        method;
    private final JsonRpcParams params;

    private final JsonParser jsonParser;

    @AssistedInject
    public JsonRpcRequest(@Assisted("message") String message, JsonParser jsonParser, JsonRpcFactory jsonRpcFactory) {
        this.jsonParser = jsonParser;

        checkNotNull(message, "Message must not be null");
        checkArgument(!message.isEmpty(), "Message must not be empty");


        JsonObject jsonObject = jsonParser.parse(message).getAsJsonObject();

        method = jsonObject.get("method").getAsString();

        if (jsonObject.has("id")) {
            id = jsonObject.get("id").getAsString();
        } else {
            id = null;
        }

        if (jsonObject.has("params")) {
            params = jsonRpcFactory.createParams(jsonObject.get("params").toString());
        } else {
            params = null;
        }
    }

    @AssistedInject
    public JsonRpcRequest(@Assisted("id") String id, @Assisted("method") String method, @Assisted("params") JsonRpcParams params,
                          JsonParser jsonParser) {
        this.jsonParser = jsonParser;

        checkNotNull(method, "Method name must not be null");
        checkArgument(!method.isEmpty(), "Method name must not be empty");


        this.id = id;
        this.method = method;
        this.params = params;
    }

    @AssistedInject
    public JsonRpcRequest(@Assisted("method") String method, @Assisted("params") JsonRpcParams params, JsonParser jsonParser) {
        this(null, method, params, jsonParser);
    }

    public boolean hasParams() {
        return params != null && !params.emptyOrAbsent();
    }

    public boolean hasId() {
        return id != null;
    }

    public String getMethod() {
        return method;
    }

    public String getId() {
        return id;
    }

    public JsonRpcParams getParams() {
        return params;
    }

    public JsonObject toJsonObject() {
        JsonObject request = new JsonObject();

        request.addProperty("jsonrpc", "2.0");
        request.addProperty("method", method);

        if (hasId()) {
            request.addProperty("id", id);
        }

        if (hasParams()) {
            request.add("params", params.toJsonElement());
        }

        return request;
    }

    @Override
    public String toString() {
        return toJsonObject().toString();
    }
}
