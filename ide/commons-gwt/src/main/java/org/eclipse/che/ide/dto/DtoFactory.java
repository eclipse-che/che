/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.dto;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONParser;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides implementations of all registered DTO interfaces.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class DtoFactory {
  private final Map<Class<?>, DtoProvider<?>> dtoInterface2Providers = new HashMap<>();

  /**
   * Creates new instance of class which implements specified DTO interface.
   *
   * @param dtoInterface DTO interface
   * @return new instance of DTO implementation
   * @throws IllegalArgumentException if can't provide any implementation for specified interface
   */
  public <T> T createDto(Class<T> dtoInterface) {
    return getDtoProvider(dtoInterface).newInstance();
  }

  /**
   * Creates new instance of class which implements specified DTO interface, parses specified JSON
   * string and uses parsed data for initializing fields of DTO object.
   *
   * @param json JSON data
   * @param dtoInterface DTO interface
   * @return new instance of DTO implementation
   * @throws IllegalArgumentException if can't provide any implementation for specified interface
   */
  public <T> T createDtoFromJson(String json, Class<T> dtoInterface) {
    return getDtoProvider(dtoInterface).fromJson(json);
  }

  /**
   * Parses the JSON data from the specified sting into list of objects of the specified type.
   *
   * @param json JSON data
   * @param dtoInterface DTO interface
   * @return list of DTO
   * @throws IllegalArgumentException if can't provide any implementation for specified interface
   */
  public <T> List<T> createListDtoFromJson(String json, Class<T> dtoInterface) {
    final DtoProvider<T> dtoProvider = getDtoProvider(dtoInterface);
    final JSONArray jsonArray = JSONParser.parseStrict(json).isArray();
    final List<T> result = new ArrayList<>();
    for (int i = 0; i < jsonArray.size(); i++) {
      String payload = jsonArray.get(i).isObject().toString();
      result.add(dtoProvider.fromJson(payload));
    }
    return result;
  }

  /** Serializes dto to JSON format. */
  public <T> String toJson(T dto) {
    if (dto instanceof JsonSerializable) {
      return ((JsonSerializable) dto).toJson();
    }
    throw new IllegalArgumentException("JsonSerializable instance required. ");
  }

  /** Serializes array of DTO objects to JSON format. */
  public <T> String toJson(List<T> array) {
    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < array.size(); i++) {
      T dto = array.get(i);
      if (dto instanceof JsonSerializable) {
        if (i > 0) {
          sb.append(", ");
        }
        sb.append(((JsonSerializable) dto).toJson());
      } else {
        throw new IllegalArgumentException("JsonSerializable instance required. ");
      }
    }
    sb.append("]");
    return sb.toString();
  }

  /**
   * Registers DtoProvider for DTO interface.
   *
   * @param dtoInterface DTO interface
   * @param provider provider for DTO interface
   * @see DtoProvider
   */
  public void registerProvider(Class<?> dtoInterface, DtoProvider<?> provider) {
    dtoInterface2Providers.put(dtoInterface, provider);
  }

  private <T> DtoProvider<T> getDtoProvider(Class<T> dtoInterface) {
    DtoProvider<?> dtoProvider = dtoInterface2Providers.get(dtoInterface);
    if (dtoProvider == null) {
      throw new IllegalArgumentException("Unknown DTO type " + dtoInterface);
    }
    return (DtoProvider<T>) dtoProvider;
  }
}
