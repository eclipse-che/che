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

import org.eclipse.che.dto.server.DtoFactory;

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
    private final String        id;
    private final JsonRpcResult result;
    private final JsonRpcError  error;

    @AssistedInject
    public JsonRpcResponse(@Assisted("message") String message, JsonParser jsonParser, DtoFactory dtoFactory) {
        checkNotNull(message, "Message must not be null");
        checkArgument(!message.isEmpty(), "Message must not be empty");

        JsonObject response = jsonParser.parse(message).getAsJsonObject();

        this.id = response.has("id")
                  ? response.get("id").getAsString()
                  : null;

        this.result = response.has("result")
                      ? new JsonRpcResult(response.get("result").toString(), jsonParser, dtoFactory)
                      : null;

        this.error = response.has("error")
                     ? new JsonRpcError(response.get("error").toString(), jsonParser)
                     : null;
    }

    @AssistedInject
    public JsonRpcResponse(@Assisted("id") String id, @Assisted("result") JsonRpcResult result, @Assisted("error") JsonRpcError error) {
        checkNotNull(id, "ID must not be null");
        checkArgument(!id.isEmpty(), "ID must not be empty");
        checkArgument((result == null) != (error == null), "Must be either error or result");

        this.id = id;
        this.result = result;
        this.error = error;
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
        JsonObject response = new JsonObject();
        response.addProperty("jsonrpc", "2.0");

        if (hasResult()) {
            response.add("result", result.toJsonElement());
        } else if (hasError()) {
            response.add("error", error.toJsonObject());
        }

        if (id != null) {
            response.addProperty(
                    "id", id);
        }
        return response;
    }

    @Override
    public String toString() {
        return toJsonObject().toString();
    }
}
