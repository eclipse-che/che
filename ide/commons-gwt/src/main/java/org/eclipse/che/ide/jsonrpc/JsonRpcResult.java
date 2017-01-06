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
 * Represents JSON RPC result object. Can be constructed out of
 * stringified json object or by passing specific parameters.
 * Use {@link JsonRpcFactory#createResult(Object)},
 * {@link JsonRpcFactory#createResult(String)} or
 * {@link JsonRpcFactory#createResultList(List)}
 * to get an instance of this entity.
 */
public class JsonRpcResult {
    private static final JsonValue EMPTY_OBJECT = Json.instance().parse("{}");
    private static final JsonValue EMPTY_ARRAY  = Json.instance().parse("[]");

    private final JsonFactory jsonFactory;
    private final DtoFactory  dtoFactory;

    private List<JsonValue> resultList;
    private JsonValue       result;

    @AssistedInject
    public JsonRpcResult(@Assisted("message") String message, JsonFactory jsonFactory, DtoFactory dtoFactory) {
        checkNotNull(message, "Message must not be null");
        checkArgument(!message.isEmpty(), "Message must not be empty");

        this.jsonFactory = jsonFactory;
        this.dtoFactory = dtoFactory;

        JsonValue jsonValue = jsonFactory.parse(message);
        if (jsonValue.getType().equals(JsonType.ARRAY)) {
            JsonArray jsonArray = jsonFactory.parse(message);
            this.resultList = new ArrayList<>(jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                this.resultList.add(i, jsonArray.get(i));
            }
        } else {
            this.result = jsonFactory.parse(message);
        }
    }

    @AssistedInject
    public JsonRpcResult(@Assisted("result") Object result, DtoFactory dtoFactory, JsonFactory jsonFactory) {
        this.jsonFactory = jsonFactory;
        this.dtoFactory = dtoFactory;

        if (result instanceof String) {
            this.result = jsonFactory.create((String)result);
        } else if (result instanceof Double) {
            this.result = jsonFactory.create((Double)result);
        } else if (result instanceof Boolean) {
            this.result = jsonFactory.create((Boolean)result);
        } else if (result == null) {
            this.result = jsonFactory.createObject();
        } else {
            this.result = jsonFactory.parse(result.toString());
        }
    }

    @AssistedInject
    public JsonRpcResult(@Assisted("result") List<?> result, JsonFactory jsonFactory, DtoFactory dtoFactory) {
        checkNotNull(result, "Result must not be null");
        checkArgument(!result.isEmpty(), "Result must not be empty");

        this.jsonFactory = jsonFactory;
        this.dtoFactory = dtoFactory;

        this.resultList = new ArrayList<>(result.size());
        for (int i = 0; i < result.size(); i++) {
            Object item = result.get(i);
            if (item instanceof String) {
                resultList.add(i, jsonFactory.create((String)item));
            } else if (item instanceof Double) {
                resultList.add(i, jsonFactory.create((Double)item));
            } else if (item instanceof Boolean) {
                resultList.add(i, jsonFactory.create((Boolean)item));
            } else {
                resultList.add(i, jsonFactory.parse(item.toString()));
            }
        }
    }

    public boolean isEmptyOrAbsent() {
        return (result == null || EMPTY_OBJECT.jsEquals(toJsonValue())) && (resultList == null || EMPTY_ARRAY.jsEquals(toJsonValue()));
    }

    public boolean isArray() {
        return result == null && resultList != null;
    }

    public JsonValue toJsonValue() {
        if (result != null) {
            return result;
        } else {
            JsonArray array = jsonFactory.createArray();
            for (int i = 0; i < resultList.size(); i++) {
                array.set(i, resultList.get(i));
            }
            return array;
        }
    }

    public <T> T getAs(Class<T> type) {
        if (type.equals(String.class)) {
            return (T)result.asString();
        } else if (type.equals(Double.class)) {
            return (T)(Double)result.asNumber();
        } else if (type.equals(Boolean.class)) {
            return (T)(Boolean)result.asBoolean();
        } else if (type.equals(Void.class)) {
            return (T)null;
        } else {
            return dtoFactory.createDtoFromJson(result.toJson(), type);
        }
    }

    public <T> List<T> getAsListOf(Class<T> type) {
        List<T> list = new ArrayList<>(resultList.size());

        for (int i = 0; i < resultList.size(); i++) {
            JsonValue jsonValue = resultList.get(i);
            T item;
            if (type.equals(String.class)) {
                item = (T)jsonValue.asString();
            } else if (type.equals(Double.class)) {
                item = (T)(Double)jsonValue.asNumber();
            } else if (type.equals(Boolean.class)) {
                item = (T)(Boolean)jsonValue.asBoolean();
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
