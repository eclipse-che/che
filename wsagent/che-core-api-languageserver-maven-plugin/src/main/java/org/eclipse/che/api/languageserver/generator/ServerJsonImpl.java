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
package org.eclipse.che.api.languageserver.generator;

import java.io.PrintWriter;

public class ServerJsonImpl implements JsonImpl {

  @Override
  public String element() {
    return "JsonElement";
  }

  @Override
  public String object() {
    return "JsonObject";
  }

  @Override
  public String array() {
    return "JsonArray";
  }

  @Override
  public String nullValue() {
    return "JsonNull.INSTANCE";
  }

  @Override
  public Object string() {
    return "JsonPrimitive";
  }

  @Override
  public Object number() {
    return "JsonPrimitive";
  }

  @Override
  public String boolValue(String value) {
    return String.format("new JsonPrimitive(%1$s)", value);
  }

  @Override
  public Object objectValue(String value) {
    return String.format("%1$s.getAsJsonObject()", value);
  }

  @Override
  public String arrayValue(String valueName) {
    return String.format("%1$s.getAsJsonArray()", valueName);
  }

  @Override
  public String asBoolean(String valueName) {
    return String.format("%1$s.getAsBoolean()", valueName);
  }

  @Override
  public String asDouble(String valueName) {
    return String.format("%1$s.getAsDouble()", valueName);
  }

  @Override
  public String asString(String valueName) {
    return String.format("%1$s.getAsString()", valueName);
  }

  @Override
  public String parse(String valueName) {
    return String.format("new JsonParser().parse(%1$s)", valueName);
  }

  @Override
  public String put() {
    return "add";
  }

  public String add(String arrayName, String valueName) {
    return String.format("%1$s.add(%2$s);", arrayName, valueName);
  }

  @Override
  public void iterateObject(
      String indent, PrintWriter out, String objectName, PropertyHandler handler) {
    String entryName = objectName + "e";
    out.println(
        indent
            + String.format(
                "for(Entry<String, JsonElement> %1$s : %2$s.entrySet()) {", entryName, objectName));
    handler.handle(
        String.format("%1$s.getKey()", entryName), String.format("%1$s.getValue()", entryName));
    out.println(indent + "}");
  }

  @Override
  public String isNull(String jsonValName) {
    return jsonValName + ".isJsonNull()";
  }

  @Override
  public void writeImports(PrintWriter out) {
    out.println("import com.google.gson.JsonParser;");
    out.println("import com.google.gson.JsonObject;");
    out.println("import com.google.gson.JsonArray;");
    out.println("import com.google.gson.JsonPrimitive;");
    out.println("import com.google.gson.JsonElement;");
    out.println("import com.google.gson.JsonNull;");
  }
}
