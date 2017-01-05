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


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import org.eclipse.che.dto.server.DtoFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents JSON RPC params object. Can be constructed out of
 * stringified json object or by passing specific parameters.
 * Use {@link JsonRpcFactory#createParams(Object)},
 * {@link JsonRpcFactory#createParamsList(List)} or
 * {@link JsonRpcFactory#createParams(String)} to get an instance.
 */
public class JsonRpcParams {
    private final static JsonObject EMPTY_OBJECT = new JsonObject();

    private final DtoFactory dtoFactory;

    private List<JsonElement> paramsList;
    private JsonElement       params;

    @AssistedInject
    public JsonRpcParams(@Assisted("message") String message, JsonParser jsonParser, DtoFactory dtoFactory) {
        this.dtoFactory = dtoFactory;

        checkNotNull(message, "Message must not be null");
        checkArgument(!message.isEmpty(), "Message must not be empty");


        JsonElement jsonElement = jsonParser.parse(message);
        if (jsonElement.isJsonArray()) {
            JsonArray jsonArray = jsonParser.parse(message).getAsJsonArray();
            paramsList = new ArrayList<>(jsonArray.size());
            jsonArray.forEach(it -> paramsList.add(it));
        } else {
            params = jsonParser.parse(message);
        }
    }

    @AssistedInject
    public JsonRpcParams(@Assisted("params") Object params, DtoFactory dtoFactory, JsonParser jsonParser) {
        this.dtoFactory = dtoFactory;

        this.params = params == null ? EMPTY_OBJECT : jsonParser.parse(params.toString());
    }

    @AssistedInject
    public JsonRpcParams(@Assisted("params") List<?> params, JsonParser jsonParser, DtoFactory dtoFactory) {
        this.dtoFactory = dtoFactory;

        if (params == null || params.isEmpty()) {
            this.paramsList = Collections.emptyList();
        } else {
            this.paramsList = params.stream().map(Object::toString).map(jsonParser::parse).collect(Collectors.toList());
        }
    }

    public boolean emptyOrAbsent() {
        return (paramsList == null || paramsList.isEmpty()) && (params == null || EMPTY_OBJECT.equals(params));
    }

    public JsonElement toJsonElement() {
        if (params != null) {
            return params;
        }

        JsonArray array = new JsonArray();
        paramsList.forEach(array::add);
        return array;
    }

    public <T> T getAs(Class<T> type) {
        checkNotNull(params, "Type must not be null");

        return JsonRpcUtils.getAs(params, type, dtoFactory);
    }

    public <T> List<T> getAsListOf(Class<T> type) {
        checkNotNull(type, "Type must not be null");

        return JsonRpcUtils.getAsListOf(paramsList, type, dtoFactory);
    }

    @Override
    public String toString() {
        return toJsonElement().toString();
    }
}
