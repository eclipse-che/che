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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * This class will prevent serialization of null or empty fields of DTOs with Map types
 *
 * @author Mykhailo Kuznietsov
 */
public class NullOrEmptyMapAdapter implements JsonSerializer<Map<?, ?>> {
  @Override
  public JsonElement serialize(Map<?, ?> src, Type typeOfSrc, JsonSerializationContext context) {
    if (src == null || src.isEmpty()) {
      return null;
    }

    JsonObject object = new JsonObject();
    for (Map.Entry<?, ?> entry : src.entrySet()) {
      object.add(entry.getKey().toString(), context.serialize(entry.getValue()));
    }
    return object;
  }
}
