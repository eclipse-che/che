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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.String.format;

/**
 * @author Dmytro Nochevnov
 */
public class EnvironmentDeserializer extends JsonDeserializer<Map<String, String>> {
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
                return ((List<String>) environment).stream()
                                                   .collect(Collectors.toMap(item -> item.split("=")[0],
                                                                             item -> item.split("=")[1]));
            }
        } catch (Exception e) {
            throw new RuntimeException(format("Unsupported value '%s'.", environment.toString()));
        }

        /* Work around empty environment */
        if (environment instanceof String) {
            if (((String) environment).isEmpty()) {
                return new HashMap<>();
            }

            throw new RuntimeException(format("Unsupported value '%s'.", environment.toString()));
        }

        /* Work around unsupported type of environment content, for example, 'Boolean' in case of content:
           "environment:
             true"
        */
        throw new RuntimeException(format("Unsupported type '%s'.", environment.getClass()));
    }
}
