/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
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
import elemental.json.JsonObject;
import elemental.json.JsonString;
import elemental.json.JsonValue;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import org.eclipse.che.ide.dto.DtoFactory;

import java.util.ArrayList;
import java.util.List;

import static elemental.json.JsonType.ARRAY;

public class JsonRpcList {
    private final DtoFactory       dtoFactory;
    private final List<JsonValue> jsonObjectList;
    private final JsonFactory      jsonFactory;

    @AssistedInject
    public JsonRpcList(@Assisted("message") String message, JsonFactory jsonFactory, DtoFactory dtoFactory) {
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
        return ARRAY.equals(jsonFactory.parse(message).getType());
    }

    private <T> T getDto(int i, Class<T> type) {
        return dtoFactory.createDtoFromJson(jsonObjectList.get(i).toJson(), type);
    }

    private String getString(int i) {
        return ((JsonString)jsonObjectList.get(0)).getString();
    }

    private Double getNumber(int i) {
        return ((JsonNumber)jsonObjectList.get(0)).getNumber();
    }

    private Boolean getBoolean(int i) {
        return ((JsonBoolean)jsonObjectList.get(0)).getBoolean();
    }

    private int size() {
        return this.jsonObjectList.size();
    }

    public <T> T get(int i, Class<T> type) {
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
