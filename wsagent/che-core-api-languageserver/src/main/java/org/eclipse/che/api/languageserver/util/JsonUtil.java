/*******************************************************************************
 * Copyright (c) 2017 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.che.api.languageserver.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import org.eclipse.che.dto.server.JsonSerializable;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Utility to convert stuff that is not statically typed in lsp4j (java.lang.Object)
 *
 * @author Thomas Mäder
 */
public class JsonUtil {
    public static JsonElement convertToJson(Object value) {
        Class<?> t = value.getClass();
        if (Enum.class.isAssignableFrom(t)) {
            return new JsonPrimitive(((Enum<?>)value).name());
        } else if (String.class.isAssignableFrom(t)) {
            return new JsonPrimitive((String)value);
        } else if (Number.class.isAssignableFrom(t)) {
            return new JsonPrimitive(((Number)value).doubleValue());
        } else if (Boolean.class.isAssignableFrom(t)) {
            return new JsonPrimitive((boolean)value);
        } else if (JsonSerializable.class.isAssignableFrom(t)) {
            return ((JsonSerializable)value).toJsonElement();
        } else if (value instanceof JsonElement) {
            return (JsonElement)value;
        } else if (value instanceof Map) {
            // assumption here is that this is a json-like structure with map for object, list for array, etc.
            @SuppressWarnings("unchecked")
            Map<String, Object> object= (Map<String, Object>) value;
            JsonObject result= new JsonObject();
            for (Entry<String, Object> prop : object.entrySet()) {
                result.add(prop.getKey(), convertToJson(prop.getValue()));
            }
            return result;
        } else if (value instanceof List) {
            // assumption here is that this is a json-like structure with map for object, list for array, etc.
            @SuppressWarnings("unchecked")
            List<Object> array= (List<Object>) value;
            JsonArray result= new JsonArray();
            for (Object object : array) {
                result.add(convertToJson(object));
            }
            return result;
        }
        throw new RuntimeException("Unexpected runtime value: " + value);
    }

}
