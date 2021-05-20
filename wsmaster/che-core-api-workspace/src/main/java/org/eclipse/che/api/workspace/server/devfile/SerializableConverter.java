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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import org.eclipse.che.api.core.model.workspace.devfile.Component;

/**
 * Helps to store and read serializable values of the preferences map in {@link Component} to/from
 * database.
 *
 * @author Max Shaposhnyk
 */
@Converter(autoApply = true)
public class SerializableConverter implements AttributeConverter<Serializable, String> {

  private ObjectMapper objectMapper;

  public SerializableConverter() {
    this.objectMapper = new ObjectMapper();
    objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);
  }

  @Override
  public String convertToDatabaseColumn(Serializable value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Unable to serialize preference value:" + e.getMessage(), e);
    }
  }

  @Override
  public Serializable convertToEntityAttribute(String dbData) {
    try {
      JsonNode node = objectMapper.readTree(dbData);
      if (node.isValueNode()) {
        return serializableNodeValue(node);
      } else if (node.isArray()) {
        List<Serializable> values = new ArrayList<>();
        node.elements().forEachRemaining(n -> values.add(serializableNodeValue(n)));
        return values.toArray();
      } else {
        throw new RuntimeException("Unable to deserialize preference value:" + dbData);
      }
    } catch (IOException e) {
      throw new RuntimeException("Unable to deserialize preference value:" + e.getMessage(), e);
    }
  }

  private Serializable serializableNodeValue(JsonNode node) {
    if (node.isNumber()) {
      return node.numberValue();
    } else if (node.isBoolean()) {
      return node.booleanValue();
    } else if (node.isTextual()) {
      return node.textValue();
    } else {
      throw new RuntimeException("Unable to deserialize preference value:" + node.asText());
    }
  }
}
