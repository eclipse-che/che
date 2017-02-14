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
import elemental.json.JsonBoolean;
import elemental.json.JsonFactory;
import elemental.json.JsonNumber;
import elemental.json.JsonString;
import elemental.json.JsonValue;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import org.eclipse.che.ide.dto.DtoFactory;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static elemental.json.JsonType.ARRAY;

/**
 * Represents list object that is related to any JSON RPC entity like incoming
 * list of requests or params value represented by a list. Can be constructed
 * out of a stringified json object or by passing specific parameters.
 * Use {@link JsonRpcFactory#createList(String)} or
 * {@link JsonRpcFactory#createList(List)}} to get an instance.
 */
public class JsonRpcList {
    private final DtoFactory       dtoFactory;
    private final List<JsonValue> jsonObjectList;
    private final JsonFactory      jsonFactory;

    @AssistedInject
    public JsonRpcList(@Assisted("message") String message, JsonFactory jsonFactory, DtoFactory dtoFactory) {
        checkNotNull(message, "Message must not be null");
        checkArgument(!message.isEmpty(), "Message must not be empty");

        this.dtoFactory = dtoFactory;
        this.jsonFactory = jsonFactory;

        JsonArray jsonArray = jsonFactory.parse(message);
        this.jsonObjectList = new ArrayList<>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            jsonObjectList.add(i, jsonArray.get(i));
        }
    }

    @AssistedInject
    public JsonRpcList(@Assisted("dtoObjectList") List<?> dtoObjectList, JsonFactory jsonFactory, DtoFactory dtoFactory) {
        checkNotNull(dtoObjectList, "List must not be null");
        checkArgument(!dtoObjectList.isEmpty(), "List must not be empty");

        this.dtoFactory = dtoFactory;
        this.jsonFactory = jsonFactory;

        this.jsonObjectList = new ArrayList<>(dtoObjectList.size());
        for (int i = 0; i < dtoObjectList.size(); i++) {
            Object dtoObject = dtoObjectList.get(i);
            JsonValue jsonValue;
            if (dtoObject instanceof String) {
                jsonValue = jsonFactory.create((String)dtoObject);
            } else if (dtoObject instanceof Double) {
                jsonValue = jsonFactory.create((Double)dtoObject);
            } else if (dtoObject instanceof Boolean) {
                jsonValue = jsonFactory.create((Boolean)dtoObject);
            } else {
                jsonValue = jsonFactory.parse(dtoObject.toString());
            }
            this.jsonObjectList.add(i, jsonValue);
        }
    }

    public static boolean isArray(String message) {
        return isArray(message, Json.instance());
    }

    public static boolean isArray(String message, JsonFactory jsonFactory) {
        checkNotNull(message, "Message must not be null");
        checkArgument(!message.isEmpty(), "Message must not be empty");

        return ARRAY.equals(jsonFactory.parse(message).getType());
    }

    private <T> T getDto(int i, Class<T> type) {
        checkNotNull(type, "DTO type must not be null");
        checkArgument(i >= 0, "Index must not be negative");

        return dtoFactory.createDtoFromJson(jsonObjectList.get(i).toJson(), type);
    }

    private String getString(int i) {
        checkArgument(i >= 0, "Index must not be negative");

        return ((JsonString)jsonObjectList.get(i)).getString();
    }

    private Double getNumber(int i) {
        checkArgument(i >= 0, "Index must not be negative");

        return ((JsonNumber)jsonObjectList.get(i)).getNumber();
    }

    private Boolean getBoolean(int i) {
        checkArgument(i >= 0, "Index must not be negative");

        return ((JsonBoolean)jsonObjectList.get(i)).getBoolean();
    }

    private int size() {
        return this.jsonObjectList.size();
    }

    public <T> T get(int i, Class<T> type) {
        checkNotNull(type, "Item type must not be null");
        checkArgument(i >= 0, "Index must not be negative");

        if (type.equals(String.class)) {
            return (T)getString(i);
        } else if (type.equals(Double.class)) {
            return (T)getNumber(i);
        } else if (type.equals(Boolean.class)) {
            return (T)getBoolean(i);
        } else {
            return getDto(i, type);
        }
    }

    public <T> List<T> toList(Class<T> type) {
        checkNotNull(type, "List Item type must not be null");

        List<T> list = new ArrayList<>(size());
        for (int i = 0; i < size(); i++) {
            list.add(get(i, type));
        }
        return list;
    }

    public List<String> toStringifiedList() {
        List<String> stringifiedList = new ArrayList<>(size());
        for (int i = 0; i < size(); i++) {
            stringifiedList.add(i, jsonObjectList.get(i).toJson());
        }
        return stringifiedList;
    }

    public JsonArray toJsonArray() {
        JsonArray array = jsonFactory.createArray();
        for (int i = 0; i < jsonObjectList.size(); i++) {
            array.set(i, jsonObjectList.get(i));
        }
        return array;
    }

    @Override
    public String toString() {
        return toJsonArray().toJson();
    }
}
