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
package org.eclipse.che.api.environment.server.compose.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

/**
 * Deserializer of environment section of Docker compose file.
 *
 * @author Dmytro Nochevnov
 */
public class EnvironmentDeserializer extends JsonDeserializer<Map<String, String>> {

    public static final String ARRAY_ITEM_DIVIDER        = "=";
    public static final String UNSUPPORTED_VALUE_MESSAGE = "Unsupported value '%s'.";

    @Override
    public Map<String, String> deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        Object environment = jsonParser.readValueAs(Object.class);

        try {
            /* Parse dictionary in view of:
               "environment:
                 key1: value1
                 key2: value2"
            */
            if (environment instanceof Map) {
                return (Map<String, String>) environment;
            }

            /* Parse array in view of:
               "environment:
                 - key1=value1
                 - key2=value2"
            */
            if (environment instanceof List) {
                // convert array to Map<String, String>{ key1: value1, key2: value2 }
                Map<String, String> map = new HashMap<>();
                for (String item : (List<String>) environment) {
                    map.putAll(parseEnvironmentItem(item));
                }

                return map;
            }
        } catch (IllegalArgumentException ie) {
            throw ctxt.mappingException(ie.getMessage());

        } catch (Exception e) {
            throw ctxt.mappingException(format(UNSUPPORTED_VALUE_MESSAGE, environment));
        }

        // work around empty environment
        if (environment instanceof String) {
            if (((String) environment).isEmpty()) {
                return new HashMap<>();
            }

            throw ctxt.mappingException(format(UNSUPPORTED_VALUE_MESSAGE, environment));
        }

        /* Work around unsupported type of environment content, for example, 'Boolean' in case of content:
           "environment:
             true"
        */
        throw ctxt.mappingException(format("Unsupported type '%s'.", environment.getClass()));
    }

    /**
     * Parse "key=value" item.
     * @param item - environment array item to parse
     * @return map of key/value which are divided by "=" sign in item.
     * @throws IllegalArgumentException if there is no "=" sign in item, or if there is empty key in item
     */
    private Map<String, String> parseEnvironmentItem(String item) throws IllegalArgumentException {
        if (item.indexOf(ARRAY_ITEM_DIVIDER) < 1) {
            throw new IllegalArgumentException(format(UNSUPPORTED_VALUE_MESSAGE, item));
        }

        String key = item.substring(0, item.indexOf(ARRAY_ITEM_DIVIDER));
        String value = item.substring(item.indexOf(ARRAY_ITEM_DIVIDER) + 1, item.length());

        return ImmutableMap.of(key, value);
    }
}
