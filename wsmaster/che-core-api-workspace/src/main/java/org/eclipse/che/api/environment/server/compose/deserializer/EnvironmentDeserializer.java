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
import com.fasterxml.jackson.databind.JsonMappingException;

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
    @Override
    public Map<String, String> deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        Object environment = jsonParser.readValueAs(Object.class);

        JsonMappingException jsonMappingException = ctxt.mappingException(format("Unsupported value '%s'.", environment.toString()));

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
                    String[] splitResult = item.split("=");
                    if (splitResult.length < 2) {
                        throw ctxt.mappingException(format("Unsupported value '%s'.", item));
                    }

                    map.put(splitResult[0].trim(), splitResult[1].trim());
                }

                return map;
            }
        } catch (JsonMappingException je) {
            throw je;
        } catch (Exception e) {
            throw jsonMappingException;
        }

        // work around empty environment
        if (environment instanceof String) {
            if (((String) environment).isEmpty()) {
                return new HashMap<>();
            }

            throw jsonMappingException;
        }

        /* Work around unsupported type of environment content, for example, 'Boolean' in case of content:
           "environment:
             true"
        */
        throw ctxt.mappingException(format("Unsupported type '%s'.", environment.getClass()));
    }
}
