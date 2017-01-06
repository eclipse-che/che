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
import com.google.gson.JsonParser;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import org.eclipse.che.dto.server.DtoFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents list object that is related to any JSON RPC entity like incoming
 * list of requests or params value represented by a list. Can be constructed
 * out of a stringified json object or by passing specific parameters.
 * Use {@link JsonRpcFactory#createList(String)} or
 * {@link JsonRpcFactory#createList(List)}} to get an instance.
 */
public class JsonRpcList {
    private final DtoFactory        dtoFactory;
    private final List<JsonElement> jsonElementList;
    private final JsonParser        jsonParser;

    @AssistedInject
    public JsonRpcList(@Assisted("message") String message, JsonParser jsonParser, DtoFactory dtoFactory) {
        checkNotNull(message, "Message must not be null");
        checkArgument(!message.isEmpty(), "Message must not be empty");

        this.dtoFactory = dtoFactory;
        this.jsonParser = jsonParser;

        JsonArray jsonArray = jsonParser.parse(message).getAsJsonArray();
        this.jsonElementList = new ArrayList<>(jsonArray.size());
        jsonArray.forEach(jsonElementList::add);
    }

    @AssistedInject
    public JsonRpcList(@Assisted("dtoObjectList") List<?> dtoObjectList, JsonParser jsonParser, DtoFactory dtoFactory) {
        checkNotNull(dtoObjectList, "List must not be null");
        checkArgument(!dtoObjectList.isEmpty(), "List must not be empty");

        this.dtoFactory = dtoFactory;
        this.jsonParser = jsonParser;

        this.jsonElementList = dtoObjectList.stream().map(Object::toString).map(jsonParser::parse).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private static <T> T cast(Object object) {
        return (T)object;
    }

    public static boolean isArray(String message) {
        return isArray(message, new JsonParser());
    }

    public static boolean isArray(String message, JsonParser jsonParser) {
        checkNotNull(message, "Message must not be null");
        checkArgument(!message.isEmpty(), "Message must not be empty");

        return jsonParser.parse(message).isJsonArray();
    }

    public <T> T get(int i, Class<T> type) {
        checkNotNull(type, "Item type must not be null");
        checkArgument(i >= 0, "Index must not be negative");

        return JsonRpcUtils.getAs(jsonElementList.get(i), type, dtoFactory);
    }

    public <T> List<T> toList(Class<T> type) {
        checkNotNull(type, "List Item type must not be null");

        return JsonRpcUtils.getAsListOf(jsonElementList, type, dtoFactory);
    }

    public List<String> toStringifiedList() {
        return jsonElementList.stream().map(JsonElement::toString).collect(Collectors.toList());
    }

    public JsonArray toJsonArray() {
        JsonArray jsonArray = new JsonArray();
        jsonElementList.forEach(jsonArray::add);
        return jsonArray;
    }

    @Override
    public String toString() {
        return toJsonArray().toString();
    }
}
