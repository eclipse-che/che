/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.dto.server;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.List;

/**
 * This class will prevent serialization of null or empty fields of DTOs with Collection types
 *
 * @author Mykhailo Kuznietsov
 */
public class NullOrEmptyCollectionAdapter implements JsonSerializer<List<?>> {
  @Override
  public JsonElement serialize(List<?> src, Type typeOfSrc, JsonSerializationContext context) {
    if (src == null || src.isEmpty()) {
      return null;
    }

    JsonArray array = new JsonArray();

    for (Object child : src) {
      JsonElement element = context.serialize(child);
      array.add(element);
    }

    return array;
  }
}
