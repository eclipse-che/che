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

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import org.eclipse.che.dto.server.JsonSerializable;

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
        }
        throw new RuntimeException("Unexpected runtime value: " + value);
    }

}
