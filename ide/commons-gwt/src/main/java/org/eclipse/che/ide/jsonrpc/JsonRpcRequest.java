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

import elemental.json.JsonFactory;
import elemental.json.JsonObject;
import elemental.json.JsonType;
import elemental.json.JsonValue;

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

    private final JsonFactory jsonFactory;

    @AssistedInject
    public JsonRpcRequest(@Assisted("message") String message, JsonFactory jsonFactory, JsonRpcFactory jsonRpcFactory) {
        checkNotNull(message, "Message must not be null");
        checkArgument(!message.isEmpty(), "Message must not be empty");

        this.jsonFactory = jsonFactory;

        JsonObject jsonObject = jsonFactory.parse(message);

        if (jsonObject.hasKey("id")) {
            JsonValue jsonValue = jsonObject.get("id");
            if (jsonValue.getType().equals(JsonType.STRING)) {
                id = jsonValue.asString();
            } else {
                id = Double.toString(jsonValue.asNumber());
            }
        } else {
            id = null;
        }

        method = jsonObject.getString("method");

        if (jsonObject.hasKey("params")) {
            params = jsonRpcFactory.createParams(jsonObject.get("params").toJson());
        } else {
            params = null;
        }
    }

    @AssistedInject
    public JsonRpcRequest(@Assisted("id") String id, @Assisted("method") String method, @Assisted("params") JsonRpcParams params,
                          JsonFactory jsonFactory) {
        checkNotNull(method, "Method name must not be null");
        checkArgument(!method.isEmpty(), "Method name must not be empty");

        this.jsonFactory = jsonFactory;

        this.id = id;
        this.method = method;
        this.params = params;
    }

    @AssistedInject
    public JsonRpcRequest(@Assisted("method") String method, @Assisted("params") JsonRpcParams params, JsonFactory jsonFactory) {
        this(null, method, params, jsonFactory);
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
        JsonObject request = jsonFactory.createObject();

        request.put("jsonrpc", "2.0");
        request.put("method", method);

        if (hasId()) {
            request.put("id", id);
        }

        if (hasParams()) {
            request.put("params", params.toJsonValue());
        }

        return request;
    }

    @Override
    public String toString() {
        return toJsonObject().toJson();
    }
}
