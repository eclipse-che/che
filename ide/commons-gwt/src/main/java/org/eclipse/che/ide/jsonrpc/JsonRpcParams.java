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

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonFactory;
import elemental.json.JsonType;
import elemental.json.JsonValue;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import org.eclipse.che.ide.dto.DtoFactory;

import java.util.ArrayList;
import java.util.List;

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
    private static final JsonValue EMPTY_OBJECT = Json.instance().parse("{}");
    private static final JsonValue EMPTY_ARRAY  = Json.instance().parse("[]");

    private final JsonFactory jsonFactory;
    private final DtoFactory  dtoFactory;

    private List<JsonValue> paramsList;
    private JsonValue       params;

    @AssistedInject
    public JsonRpcParams(@Assisted("message") String message, JsonFactory jsonFactory, DtoFactory dtoFactory) {
        checkNotNull(message, "Message must not be null");
        checkArgument(!message.isEmpty(), "Message must not be empty");

        this.jsonFactory = jsonFactory;
        this.dtoFactory = dtoFactory;

        JsonValue jsonValue = jsonFactory.parse(message);
        if (jsonValue.getType().equals(JsonType.ARRAY)) {
            JsonArray jsonArray = jsonFactory.parse(message);
            this.paramsList = new ArrayList<>(jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                this.paramsList.add(i, jsonArray.get(i));
            }
        } else {
            this.params = jsonFactory.parse(message);
        }
    }

    @AssistedInject
    public JsonRpcParams(@Assisted("params") Object params, DtoFactory dtoFactory, JsonFactory jsonFactory) {
        this.jsonFactory = jsonFactory;
        this.dtoFactory = dtoFactory;

        if (params instanceof String) {
            this.params = jsonFactory.create((String)params);
        } else if (params instanceof Double) {
            this.params = jsonFactory.create((Double)params);
        } else if (params instanceof Boolean) {
            this.params = jsonFactory.create((Boolean)params);
        } else if (params == null) {
            this.params = jsonFactory.createObject();
        } else {
            this.params = jsonFactory.parse(params.toString());
        }
    }

    @AssistedInject
    public JsonRpcParams(@Assisted("params") List<?> params, JsonFactory jsonFactory, DtoFactory dtoFactory) {
        checkNotNull(params, "Params must not be null");
        checkArgument(!params.isEmpty(), "Params must not be empty");

        this.jsonFactory = jsonFactory;
        this.dtoFactory = dtoFactory;

        this.paramsList = new ArrayList<>(params.size());
        for (int i = 0; i < params.size(); i++) {
            Object item = params.get(i);
            if (item instanceof String) {
                paramsList.add(i, jsonFactory.create((String)item));
            } else if (item instanceof Double) {
                paramsList.add(i, jsonFactory.create((Double)item));
            } else if (item instanceof Boolean) {
                paramsList.add(i, jsonFactory.create((Boolean)item));
            } else {
                paramsList.add(i, jsonFactory.parse(item.toString()));
            }
        }
    }

    public boolean emptyOrAbsent() {
        return (paramsList == null || EMPTY_ARRAY.jsEquals(toJsonValue())) && (params == null || EMPTY_OBJECT.jsEquals(toJsonValue()));
    }

    public JsonValue toJsonValue() {
        if (params != null) {
            return params;
        } else {
            JsonArray array = jsonFactory.createArray();
            for (int i = 0; i < paramsList.size(); i++) {
                JsonValue value = paramsList.get(i);
                array.set(i, value);
            }
            return array;
        }
    }

    public <T> T getAs(Class<T> type) {
        checkNotNull(params, "Type must not be null");

        if (type.equals(String.class)) {
            String s = params.asString();
            return (T)s;
        } else if (type.equals(Double.class)) {
            Double d = params.asNumber();
            return (T)d;
        } else if (type.equals(Boolean.class)) {
            Boolean b = params.asBoolean();
            return (T)b;
        } else if (type.equals(Void.class)) {
            return (T)null;
        } else {
            return dtoFactory.createDtoFromJson(params.toJson(), type);
        }
    }

    public <T> List<T> getAsListOf(Class<T> type) {
        checkNotNull(type, "Type must not be null");

        List<T> list = new ArrayList<>(paramsList.size());

        for (int i = 0; i < paramsList.size(); i++) {
            JsonValue jsonValue = paramsList.get(i);
            T item;
            if (type.equals(String.class)) {
                item = (T)jsonValue.asString();
            } else if (type.equals(Double.class)) {
                Double d = jsonValue.asNumber();
                item = (T)d;
            } else if (type.equals(Boolean.class)) {
                Boolean b = jsonValue.asBoolean();
                item = (T)b;
            } else {
                item = dtoFactory.createDtoFromJson(jsonValue.toJson(), type);
            }
            list.add(i, item);
        }

        return list;
    }

    @Override
    public String toString() {
        return toJsonValue().toJson();
    }
}
