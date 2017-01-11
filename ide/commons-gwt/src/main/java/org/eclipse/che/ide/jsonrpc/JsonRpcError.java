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
    private final int         code;
    private final String      message;
    private final JsonFactory jsonFactory;

    @AssistedInject
    public JsonRpcError(@Assisted("code") int code, @Assisted("message") String message, JsonFactory jsonFactory) {

        this.code = code;
        this.message = message;
        this.jsonFactory = jsonFactory;
    }

    @AssistedInject
    public JsonRpcError(@Assisted("message") String message, JsonFactory jsonFactory) {
        checkNotNull(message, "Message must not be null");
        checkArgument(!message.isEmpty(), "Message must not be empty");

        JsonObject error = jsonFactory.parse(message);
        this.code = Double.valueOf(error.getNumber("code")).intValue();
        this.message = error.getString("message");
        this.jsonFactory = jsonFactory;
    }

    public JsonObject toJsonObject() {
        JsonObject error = jsonFactory.createObject();
        error.put("code", code);
        error.put("message", message);
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
        return toJsonObject().toJson();
    }
}
