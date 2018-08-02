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

import static org.eclipse.che.api.languageserver.generator.DtoGenerator.INDENT;
import static org.eclipse.che.api.languageserver.generator.DtoGenerator.dtoName;

import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.messages.Either3;

/**
 * This class generates property conversions from regular lsp4j classes to dto classes. Used to
 * convert responses from the backend language servers for che rmi usage.
 *
 * @author Thomas Mäder
 */
public class ToDtoGenerator extends ConversionGenerator {

  private Set<Class<? extends Object>> classes;

  public ToDtoGenerator(Set<Class<? extends Object>> classes) {
    this.classes = classes;
  }

  public static void generateMakeDto(
      String indent, PrintWriter out, Set<Class<? extends Object>> classes) {
    out.println(indent + "public static final Object makeDto(Object obj) {");
    for (Class<? extends Object> clazz : classes) {
      out.println(indent + INDENT + String.format("if (obj instanceof %1$s) {", clazz.getName()));
      out.println(
          indent
              + INDENT
              + INDENT
              + String.format("return new %1$s((%2$s)obj);", dtoName(clazz), clazz.getName()));
      out.println(indent + INDENT + "}");
    }
    out.println(indent + INDENT + "return obj;");
    out.println(indent + "}");
  }

  public void generateToDto(
      String indent, PrintWriter out, Class<?> receiverClass, Method m, String objectName) {
    String varName = fieldName(m) + "Val";
    String valueAccess = objectName + "." + getterName(receiverClass, m) + "()";
    Type paramType = m.getGenericParameterTypes()[0];
    if (getRawClass(paramType).isPrimitive()) {
      generateToDto(indent + INDENT, out, varName, valueAccess, paramType);
      out.println(
          String.format(
              indent + INDENT + "%1$s((%2$s)%3$s);",
              m.getName(),
              paramType.getTypeName(),
              varName));
    } else {
      out.println(indent + String.format("if (%1$s == null) {", valueAccess));
      out.println(
          indent
              + INDENT
              + String.format("%1$s((%2$s)null);", m.getName(), paramType.getTypeName()));
      out.println(indent + "} else {");
      generateToDto(indent + INDENT, out, varName, valueAccess, paramType);
      out.println(
          String.format(
              indent + INDENT + "%1$s((%2$s)%3$s);",
              m.getName(),
              paramType.getTypeName(),
              varName));
      out.println(indent + "}");
    }
  }

  private void generateToDto(
      String indent, PrintWriter out, String varName, String valueAccess, Type paramType) {
    Class<?> rawClass = getRawClass(paramType);
    if (List.class.isAssignableFrom(rawClass)) {
      generateListConversion(indent + INDENT, out, varName, valueAccess, paramType);
    } else if (Map.class.isAssignableFrom(rawClass)) {
      generateMapConversion(indent + INDENT, out, varName, valueAccess, paramType);
    } else if (Either3.class.isAssignableFrom(getRawClass(paramType))) {
      generateEither3Conversion(indent + INDENT, out, varName, valueAccess, paramType);
    } else if (Either.class.isAssignableFrom(rawClass)) {
      generateEitherConversion(indent + INDENT, out, varName, valueAccess, paramType);
    } else {
      out.println(
          String.format(
              indent + "%1$s %2$s = %3$s;",
              rawClass.getName(),
              varName,
              dtoValueExpression(rawClass, valueAccess)));
    }
  }

  private void generateEither3Conversion(
      String indent, PrintWriter out, String varName, String valueAccess, Type paramType) {
    String innerName = varName + "e";

    out.println(indent + String.format("%1$s %2$s;", paramType.getTypeName(), varName));
    out.println(indent + String.format("if (%1$s.isFirst()) {", valueAccess));
    generateToDto(
        indent + INDENT,
        out,
        innerName,
        valueAccess + ".getFirst()",
        EitherUtil.getFirstDisjointType(paramType));
    out.println(
        indent + INDENT + String.format("%1$s= Either3.forFirst(%2$s);", varName, innerName));
    out.println(indent + String.format("} else if (%1$s.isSecond()) {", valueAccess));
    generateToDto(
        indent + INDENT,
        out,
        innerName,
        valueAccess + ".getSecond()",
        EitherUtil.getSecondDisjointType(paramType));
    out.println(
        indent + INDENT + String.format("%1$s= Either3.forSecond(%2$s);", varName, innerName));
    out.println(indent + "} else  {");
    generateToDto(
        indent + INDENT,
        out,
        innerName,
        valueAccess + ".getThird()",
        EitherUtil.getThirdDisjointType(paramType));
    out.println(
        indent + INDENT + String.format("%1$s= Either3.forThird(%2$s);", varName, innerName));
    out.println(indent + "}");
  }

  private void generateEitherConversion(
      String indent, PrintWriter out, String varName, String valueAccess, Type paramType) {
    String innerName = varName + "e";

    out.println(indent + String.format("%1$s %2$s;", paramType.getTypeName(), varName));
    out.println(indent + String.format("if (%1$s.getLeft() != null) {", valueAccess));
    generateToDto(
        indent + INDENT,
        out,
        innerName,
        valueAccess + ".getLeft()",
        EitherUtil.getLeftDisjointType(paramType));
    out.println(indent + INDENT + String.format("%1$s= Either.forLeft(%2$s);", varName, innerName));
    out.println(indent + "} else  {");
    generateToDto(
        indent + INDENT,
        out,
        innerName,
        valueAccess + ".getRight()",
        EitherUtil.getRightDisjointType(paramType));
    out.println(
        indent + INDENT + String.format("%1$s= Either.forRight(%2$s);", varName, innerName));
    out.println(indent + "}");
  }

  private void generateMapConversion(
      String indent, PrintWriter out, String varName, String valueAccess, Type inputParamType) {
    Type paramType = inputParamType;
    if (!(paramType instanceof ParameterizedType)) {
      paramType = ((Class<?>) paramType).getGenericSuperclass();
    }
    ParameterizedType genericType = (ParameterizedType) paramType;
    Type containedType = genericType.getActualTypeArguments()[1];
    Type valueType = genericType.getActualTypeArguments()[1];
    String containedName = varName + "X";
    String typeName = inputParamType.getTypeName();
    String instantiateTypeName = typeName;
    if (getRawClass(inputParamType).isInterface()) {
      if (inputParamType instanceof ParameterizedType) {
        instantiateTypeName = String.format("HashMap<String, %1$s>", containedType.getTypeName());
      } else {
        throw new RuntimeException(
            "Unsupported Map Conversion. Generator needs to be updated for new LSP4J construct");
      }
    }
    out.println(
        indent + String.format("%1$s %2$s= new %3$s();", typeName, varName, instantiateTypeName));
    out.println(
        String.format(
            indent + "for (Entry<String, %1$s> %2$s : %3$s.entrySet()) {",
            valueType.getTypeName(),
            containedName,
            valueAccess));
    generateToDto(indent + INDENT, out, varName + "Y", containedName + ".getValue()", valueType);
    out.println(
        indent
            + INDENT
            + String.format(
                "%1$s.put(%2$s, %3$s);", varName, containedName + ".getKey()", varName + "Y"));
    out.println(indent + "}");
  }

  private void generateListConversion(
      String indent, PrintWriter out, String varName, String valueAccess, Type paramType) {
    Type containedType = containedType(paramType);
    out.println(
        indent
            + String.format(
                "ArrayList<%1$s> %2$s= new ArrayList<%3$s>();",
                containedType.getTypeName(), varName, containedType.getTypeName()));
    String containedName = varName + "X";
    out.println(
        String.format(
            indent + "for (%1$s %2$s : %3$s) {",
            containedType.getTypeName(),
            containedName,
            valueAccess));
    generateToDto(indent + INDENT, out, varName + "Y", containedName, containedType);
    out.println(indent + INDENT + String.format("%1$s.add(%2$s);", varName, varName + "Y"));
    out.println(indent + "}");
  }

  private String dtoValueExpression(Class<?> t, String value) {
    if (isDtoClass(t)) {
      return String.format("new %1$s(%2$s)", dtoName(t), value);
    } else {
      return String.format("(%1$s)makeDto(%2$s);", t.getName(), value);
    }
  }

  private boolean isDtoClass(Class<?> t) {
    return classes.contains(t);
  }
}
