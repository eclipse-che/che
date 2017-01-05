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
 * Represents JSON RPC error object. Can be constructed out of
 * stringified json object or by passing specific parameters.
 * Use {@link JsonRpcFactory#createError(int, String)} or
 * {@link JsonRpcFactory#createError(String)} to get an instance.
 */
public class JsonRpcError {
    private final int    code;
    private final String message;

    @AssistedInject
    public JsonRpcError(@Assisted("code") int code, @Assisted("message") String message) {
        this.code = code;
        this.message = message;
    }

    @AssistedInject
    public JsonRpcError(@Assisted("message") String message, JsonParser jsonParser) {
        checkNotNull(message, "Message must not be null");
        checkArgument(!message.isEmpty(), "Message must not be empty");

        this.code = getCode(message, jsonParser);
        this.message = getMessage(message, jsonParser);
    }

    private static String getMessage(String message, JsonParser jsonParser) {
        return jsonParser.parse(message).getAsJsonObject().get("message").getAsString();
    }

    private static int getCode(String message, JsonParser jsonParser) {
        return jsonParser.parse(message).getAsJsonObject().get("code").getAsInt();
    }

    public JsonObject toJsonObject() {
        JsonObject error = new JsonObject();
        error.addProperty("code", code);
        error.addProperty("message", message);
        return error;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return toJsonObject().toString();
    }
}
