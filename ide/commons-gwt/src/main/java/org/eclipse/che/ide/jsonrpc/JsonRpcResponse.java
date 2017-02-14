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

import org.eclipse.che.ide.dto.DtoFactory;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents JSON RPC response object. Can be constructed out of
 * stringified json object or by passing specific parameters.
 * Use {@link JsonRpcFactory#createResponse(String)} or
 * {@link JsonRpcFactory#createResponse(String, JsonRpcResult, JsonRpcError)}
 * to get an instance of this entity.
 */
public class JsonRpcResponse {
    private final JsonFactory jsonFactory;

    private final String        id;
    private final JsonRpcResult result;
    private final JsonRpcError  error;

    @AssistedInject
    public JsonRpcResponse(@Assisted("message") String message, JsonFactory jsonFactory, DtoFactory dtoFactory) {
        checkNotNull(message, "Message must not be null");
        checkArgument(!message.isEmpty(), "Message must not be empty");

        this.jsonFactory = jsonFactory;

        JsonObject jsonObject = jsonFactory.parse(message);

        JsonValue jsonValue = jsonObject.get("id");
        if (jsonValue.getType().equals(JsonType.STRING)) {
            id = jsonValue.asString();
        } else {
            id = Double.toString(jsonValue.asNumber());
        }

        if (jsonObject.hasKey("result")) {
            this.result = new JsonRpcResult(jsonObject.get("result").toJson(), jsonFactory, dtoFactory);
            this.error = null;
        } else {
            this.error = new JsonRpcError(jsonObject.get("error").toJson(), jsonFactory);
            this.result = null;
        }
    }

    @AssistedInject
    public JsonRpcResponse(@Assisted("id") String id, @Assisted("result") JsonRpcResult result, @Assisted("error") JsonRpcError error,
                           JsonFactory jsonFactory) {
        checkNotNull(id, "ID must not be null");
        checkArgument(!id.isEmpty(), "ID must not be empty");
        checkArgument((result == null) != (error == null), "Must be either error or result");

        this.id = id;
        this.result = result;
        this.error = error;
        this.jsonFactory = jsonFactory;
    }

    public boolean hasError() {
        return error != null;
    }

    public boolean hasResult() {
        return result != null;
    }

    public JsonRpcError getError() {
        return error;
    }

    public String getId() {
        return id;
    }

    public JsonRpcResult getResult() {
        return result;
    }

    public JsonObject toJsonObject() {
        JsonObject response = jsonFactory.createObject();
        response.put("jsonrpc", "2.0");

        if (id != null) {
            response.put("id", id);
        }

        if (hasResult()) {
            response.put("result", result.toJsonValue());
        } else if (hasError()) {
            response.put("error", error.toJsonObject());
        }

        return response;
    }

    @Override
    public String toString() {
        return toJsonObject().toJson();
    }
}
