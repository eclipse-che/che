/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.languageserver.generator;

import static org.eclipse.che.api.languageserver.generator.DtoGenerator.INDENT;
import static org.eclipse.che.api.languageserver.generator.DtoGenerator.dtoName;

import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.messages.Either3;

/**
 * This class generates code to convert dto object fields to json properties.
 *
 * @author Thomas Mäder
 */
public class ToJsonGenerator extends ConversionGenerator {
  private JsonImpl json;

  public ToJsonGenerator(JsonImpl json) {
    this.json = json;
  }

  public void generateToJson(String indent, PrintWriter out, Class<?> receiverClass, Method m) {
    String varName = fieldName(m) + "Val";
    Type paramType = m.getGenericParameterTypes()[0];
    String valueAccess = getterName(receiverClass, m) + "()";

    if (getRawClass(paramType).isPrimitive()) {
      generateToJson(indent, out, varName, getterName(receiverClass, m) + "()", paramType);
      out.println(
          String.format(
              indent + INDENT + "result.%1$s(\"%2$s\", %3$s);", json.put(), fieldName(m), varName));
    } else {
      out.println(indent + String.format("if (%1$s == null) {", valueAccess));
      out.println(
          indent
              + INDENT
              + String.format("%1$s((%2$s)null);", m.getName(), paramType.getTypeName()));
      out.println(indent + "} else {");
      generateToJson(indent + INDENT, out, varName, getterName(receiverClass, m) + "()", paramType);
      out.println(
          String.format(
              indent + INDENT + "result.%1$s(\"%2$s\", %3$s);", json.put(), fieldName(m), varName));
      out.println(indent + "}");
    }
  }

  private void generateToJson(
      String indent, PrintWriter out, String varName, String valueAccess, Type paramType) {
    if (List.class.isAssignableFrom(getRawClass(paramType))) {
      new ListConverter(varName, valueAccess, paramType).generateListConversion(indent, out);
    } else if (Map.class.isAssignableFrom(getRawClass(paramType))) {
      generateMapConversion(indent, out, varName, valueAccess, paramType);
    } else if (Either3.class.isAssignableFrom(getRawClass(paramType))) {
      generateEither3Conversion(indent, out, varName, valueAccess, paramType);
    } else if (Either.class.isAssignableFrom(getRawClass(paramType))) {
      generateEitherConversion(indent, out, varName, valueAccess, paramType);
    } else {
      out.println(
          String.format(
              indent + "%1$s %2$s = %3$s;",
              json.element(),
              varName,
              jsonValueExpression(getRawClass(paramType), valueAccess)));
    }
  }

  private void generateEither3Conversion(
      String indent, PrintWriter out, String varName, String valueAccess, Type paramType) {
    String innerName = varName + "e";

    out.println(indent + String.format("%1$s %2$s;", json.element(), varName));
    out.println(indent + String.format("if (%1$s.isFirst()) {", valueAccess));
    generateToJson(
        indent + INDENT,
        out,
        innerName,
        valueAccess + ".getFirst()",
        EitherUtil.getFirstDisjointType(paramType));
    out.println(indent + INDENT + String.format("%1$s= %2$s;", varName, innerName));
    out.println(indent + String.format("} else if (%1$s.isSecond()) {", valueAccess));
    generateToJson(
        indent + INDENT,
        out,
        innerName,
        valueAccess + ".getSecond()",
        EitherUtil.getSecondDisjointType(paramType));
    out.println(indent + INDENT + String.format("%1$s= %2$s;", varName, innerName));
    out.println(indent + "} else  {");
    generateToJson(
        indent + INDENT,
        out,
        innerName,
        valueAccess + ".getThird()",
        EitherUtil.getThirdDisjointType(paramType));
    out.println(indent + INDENT + String.format("%1$s= %2$s;", varName, innerName));
    out.println(indent + "}");
  }

  private void generateEitherConversion(
      String indent, PrintWriter out, String varName, String valueAccess, Type paramType) {
    String innerName = varName + "e";

    out.println(indent + String.format("%1$s %2$s;", json.element(), varName));
    out.println(indent + String.format("if (%1$s.getLeft() != null) {", valueAccess));
    generateToJson(
        indent + INDENT,
        out,
        innerName,
        valueAccess + ".getLeft()",
        EitherUtil.getLeftDisjointType(paramType));
    out.println(indent + INDENT + String.format("%1$s= %2$s;", varName, innerName));
    out.println(indent + "} else  {");
    generateToJson(
        indent + INDENT,
        out,
        innerName,
        valueAccess + ".getRight()",
        EitherUtil.getRightDisjointType(paramType));
    out.println(indent + INDENT + String.format("%1$s= %2$s;", varName, innerName));
    out.println(indent + "}");
  }

  private void generateMapConversion(
      String indent, PrintWriter out, String varName, String valueAccess, Type paramType) {
    if (!(paramType instanceof ParameterizedType)) {
      paramType = ((Class<?>) paramType).getGenericSuperclass();
    }
    ParameterizedType genericType = (ParameterizedType) paramType;
    Type valueType = genericType.getActualTypeArguments()[1];
    String containedName = varName + "X";
    out.println(String.format(indent + "%1$s %2$s = new %1$s();", json.object(), varName));
    out.println(
        String.format(
            indent + "for (Entry<String, %1$s> %2$s : %3$s.entrySet()) {",
            valueType.getTypeName(),
            containedName,
            valueAccess));
    generateToJson(indent + INDENT, out, varName + "Y", containedName + ".getValue()", valueType);
    out.println(
        indent
            + INDENT
            + String.format(
                "%1$s.%2$s(%3$s.getKey().toString(), %4$s);",
                varName, json.put(), containedName, varName + "Y"));
    out.println(indent + "}");
  }

  private String jsonValueExpression(Class<?> paramType, String valueAccess) {
    if (paramType.isPrimitive()) {
      return String.format("%1$s;", toJsonConversion(paramType, valueAccess));
    } else {
      return String.format(
          "%1$s == null ? %2$s : %3$s;",
          valueAccess, json.nullValue(), toJsonConversion(paramType, valueAccess));
    }
  }

  private String toJsonConversion(Class<?> t, String value) {
    if (Enum.class.isAssignableFrom(t)) {
      return String.format("new %1$s(%2$s.name())", json.string(), value);
    } else if (String.class.isAssignableFrom(t)) {
      return String.format("new %1$s(%2$s)", json.string(), value);
    } else if (Number.class.isAssignableFrom(t)) {
      return String.format("new %1$s(%2$s.doubleValue())", json.number(), value);
    } else if (t == boolean.class || Boolean.class.isAssignableFrom(t)) {
      return json.boolValue(value);
    } else if (isSimpleNumberType(t)) {
      return String.format("new %1$s(%2$s)", json.number(), value);
    } else if (t == Object.class) {
      return String.format("JsonUtil.convertToJson(%1$s)", value);
    } else {
      return String.format("((%2$s)%1$s).toJsonElement()", value, dtoName(t));
    }
  }

  private class ListConverter {
    private String varName;
    private String valueAccess;
    private Type paramType;

    public ListConverter(String varName, String valueAccess, Type paramType) {
      this.varName = varName;
      this.valueAccess = valueAccess;
      this.paramType = paramType;
    }

    private void generateListConversion(String indent, PrintWriter out) {
      Type containedType = containedType(paramType);

      String containedName = varName + "X";
      out.println(String.format(indent + "%1$s %2$s = new %1$s();", json.array(), varName));
      out.println(
          String.format(
              indent + "for (%1$s %2$s : %3$s) {",
              containedType.getTypeName(),
              containedName,
              valueAccess));
      generateToJson(indent + INDENT, out, varName + "Y", containedName, containedType);
      out.println(indent + INDENT + json.add(varName, varName + "Y"));
      out.println(indent + "}");
    }
  }
}
