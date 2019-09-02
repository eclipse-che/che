/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
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
      if (Number.class.isAssignableFrom(type.getRawType())) {
        System.out.println("lol");
      }
      return (TypeAdapter<T>)
          new SerializableAdapter(gson.getAdapter(Object.class), gson.getAdapter(Number.class));
    }
    return null;
  }

  private static class SerializableAdapter extends TypeAdapter<Object> {

    TypeAdapter objectAdapter;
    TypeAdapter numberAdapter;

    public SerializableAdapter(TypeAdapter objectAdapter, TypeAdapter numberAdapter) {
      this.objectAdapter = objectAdapter;
      this.numberAdapter = numberAdapter;
    }

    @Override
    public void write(JsonWriter out, Object value) throws IOException {
      objectAdapter.write(out, value);
    }

    @Override
    public Object read(JsonReader in) throws IOException {
      JsonToken token = in.peek();
      if (token.equals(JsonToken.NUMBER)) {
        String str = in.nextString();
        try {
          return Integer.parseInt(str);
        } catch (NumberFormatException e) {
          try {
            return Long.parseLong(str);
          } catch (NumberFormatException e1) {
            try {
              return Double.parseDouble(str);
            } catch (NumberFormatException e3) {
              throw e3;
            }
          }
        }
      }
      return objectAdapter.read(in);
    }
  }
}
