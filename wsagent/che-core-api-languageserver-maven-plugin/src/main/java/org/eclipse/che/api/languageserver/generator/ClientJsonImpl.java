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
package org.eclipse.che.api.languageserver.generator;

import java.io.PrintWriter;

public class ClientJsonImpl implements JsonImpl {

  @Override
  public String element() {
    return "JSONValue";
  }

  @Override
  public String object() {
    return "JSONObject";
  }

  @Override
  public String array() {
    return "JSONArray";
  }

  @Override
  public String nullValue() {
    return "JSONNull.getInstance()";
  }

  @Override
  public Object string() {
    return "JSONString";
  }

  @Override
  public Object number() {
    return "JSONNumber";
  }

  @Override
  public String boolValue(String value) {
    return String.format("JSONBoolean.getInstance(%1$s)", value);
  }

  @Override
  public String arrayValue(String jsonValName) {
    return String.format("%1$s.isArray()", jsonValName);
  }

  @Override
  public Object objectValue(String value) {
    return String.format("%1$s.isObject()", value);
  }

  @Override
  public String asBoolean(String valueName) {
    return String.format("%1$s.isBoolean().booleanValue()", valueName);
  }

  @Override
  public String asDouble(String valueName) {
    return String.format("%1$s.isNumber().doubleValue()", valueName);
  }

  @Override
  public String asString(String valueName) {
    return String.format("%1$s.isString().stringValue()", valueName);
  }

  @Override
  public String parse(String valueName) {
    return String.format("JSONParser.parseStrict(%1$s)", valueName);
  }

  @Override
  public String put() {
    return "put";
  }

  public String add(String arrayName, String valueName) {
    return String.format("%1$s.set(%1$s.size(), %2$s);", arrayName, valueName);
  }

  @Override
  public void iterateObject(
      String indent, PrintWriter out, String objectName, PropertyHandler handler) {
    String keyName = objectName + "k";
    out.println(indent + String.format("for(String %1$s : %2$s.keySet()) {", keyName, objectName));
    handler.handle(keyName, String.format("%1$s.get(%2$s)", objectName, keyName));
    out.println(indent + "}");
  }

  @Override
  public String isNull(String jsonValName) {
    return jsonValName + ".isNull() != null";
  }

  @Override
  public void writeImports(PrintWriter out) {
    out.println("import com.google.gwt.json.client.JSONParser;");
    out.println("import com.google.gwt.json.client.JSONObject;");
    out.println("import com.google.gwt.json.client.JSONArray;");
    out.println("import com.google.gwt.json.client.JSONString;");
    out.println("import com.google.gwt.json.client.JSONNumber;");
    out.println("import com.google.gwt.json.client.JSONBoolean;");
    out.println("import com.google.gwt.json.client.JSONValue;");
    out.println("import com.google.gwt.json.client.JSONNull;");
  }
}
