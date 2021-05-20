/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.devfile;

import static java.lang.String.format;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.devfile.Component;

/**
 * Helps to deserialize multi-type preferences into {@link Component} as a {@code Map}.
 *
 * @author Max Shaposhnyk
 */
public class PreferencesDeserializer extends JsonDeserializer<Map<String, Serializable>> {

  @Override
  public Map<String, Serializable> deserialize(JsonParser jsonParser, DeserializationContext ctxt)
      throws IOException {
    Map<String, Serializable> result = new HashMap<>();
    jsonParser.nextToken();
    while (!JsonToken.END_OBJECT.equals(jsonParser.getCurrentToken())) {
      JsonToken currentToken = jsonParser.nextValue();
      switch (currentToken) {
        case VALUE_NUMBER_INT:
        case VALUE_NUMBER_FLOAT:
          result.put(jsonParser.getCurrentName(), jsonParser.getNumberValue());
          break;
        case VALUE_FALSE:
        case VALUE_TRUE:
          result.put(jsonParser.getCurrentName(), jsonParser.getValueAsBoolean());
          break;
        case VALUE_STRING:
          result.put(jsonParser.getCurrentName(), jsonParser.getValueAsString());
          break;
        case START_ARRAY:
          String key = jsonParser.getCurrentName();
          ArrayList<Serializable> array = new ArrayList<>();
          // Iterate over nested array
          JsonToken nextValue = jsonParser.nextValue();
          while (!JsonToken.END_ARRAY.equals(nextValue)) {

            switch (nextValue) {
              case VALUE_NUMBER_INT:
              case VALUE_NUMBER_FLOAT:
                array.add(jsonParser.getNumberValue());
                break;
              case VALUE_FALSE:
              case VALUE_TRUE:
                array.add(jsonParser.getValueAsBoolean());
                break;
              case VALUE_STRING:
                array.add(jsonParser.getValueAsString());
                break;
              default:
                throw new JsonParseException(
                    jsonParser,
                    format(
                        "Unexpected value in the nested array of the preference with key '%s'.",
                        key));
            }
            nextValue = jsonParser.nextValue();
          }
          result.put(key, array.toArray());
          break;
        default:
          throw new JsonParseException(
              jsonParser,
              format(
                  "Unexpected value of the preference with key '%s'.",
                  jsonParser.getCurrentName()));
      }
      jsonParser.nextToken();
    }
    return result;
  }
}
