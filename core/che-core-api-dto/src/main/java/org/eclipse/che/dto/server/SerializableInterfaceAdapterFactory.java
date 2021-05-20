/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
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

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.Serializable;

/**
 * This adapter is required for fields of {@link java.io.Serializable} type to be treated as {@link
 * Object}
 */
public class SerializableInterfaceAdapterFactory implements TypeAdapterFactory {

  @Override
  public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
    if (Serializable.class.equals(type.getRawType())) {
      return (TypeAdapter<T>) new SerializableAdapter(gson.getAdapter(Object.class));
    }
    return null;
  }

  private static class SerializableAdapter extends TypeAdapter<Object> {

    TypeAdapter objectAdapter;

    public SerializableAdapter(TypeAdapter objectAdapter) {
      this.objectAdapter = objectAdapter;
    }

    @Override
    public void write(JsonWriter out, Object value) throws IOException {
      objectAdapter.write(out, value);
    }

    @Override
    public Object read(JsonReader in) throws IOException {
      JsonToken token = in.peek();
      if (token.equals(JsonToken.NUMBER)) {
        try {
          return in.nextLong();
        } catch (NumberFormatException e) {
          return in.nextDouble();
        }
      }
      return objectAdapter.read(in);
    }
  }
}
