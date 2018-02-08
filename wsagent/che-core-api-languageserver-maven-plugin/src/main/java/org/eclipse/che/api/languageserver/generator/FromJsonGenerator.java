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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.messages.Either3;

/**
 * This class generates property conversion code from json properties to dto fields.
 *
 * @author Thomas Mäder
 */
public class FromJsonGenerator extends ConversionGenerator {

  private final JsonImpl json;

  public FromJsonGenerator(JsonImpl json) {
    this.json = json;
  }

  public void generateFromJson(
      String indent, PrintWriter out, Method m, String objectName, String jsonName) {

    Type paramType = m.getGenericParameterTypes()[0];
    String fieldName = fieldName(m);
    String jsonValName = jsonName + fieldName + "Json";
    String valName = jsonName + fieldName + "Val";
    out.println(
        indent
            + String.format(
                "%1$s %2$s = %3$s.get(\"%4$s\");",
                json.element(), jsonValName, jsonName, fieldName));
    out.println(
        indent
            + String.format(
                "if (%1$s != null && !(%2$s)) {", jsonValName, json.isNull(jsonValName)));
    generateFromJson(indent + INDENT, out, valName, jsonValName, m.getGenericParameterTypes()[0]);
    out.println(
        String.format(
            indent + INDENT + "%1$s.%2$s((%3$s)%4$s);",
            objectName,
            m.getName(),
            paramType.getTypeName(),
            valName));
    out.println(indent + "}");
  }

  private void generateFromJson(
      String indent, PrintWriter out, String varName, String valueAccess, Type paramType) {
    if (List.class.isAssignableFrom(getRawClass(paramType))) {
      generateListConversion(indent, out, varName, valueAccess, paramType);
    } else if (Map.class.isAssignableFrom(getRawClass(paramType))) {
      generateMapConversion(indent, out, varName, valueAccess, paramType);
    } else if (Either3.class.isAssignableFrom(getRawClass(paramType))) {
      generateEither3Conversion(indent, out, varName, valueAccess, paramType);
    } else if (Either.class.isAssignableFrom(getRawClass(paramType))) {
      generateEitherConversion(indent, out, varName, valueAccess, paramType);
    } else {
      out.println(
          indent
              + String.format(
                  "%1$s %2$s = %3$s;",
                  getRawClass(paramType).getName(),
                  varName,
                  fromJsonConversion(getRawClass(paramType), valueAccess)));
    }
  }

  private void generateMapConversion(
      String indent, PrintWriter out, String varName, String jsonValName, Type paramType) {
    if (!(paramType instanceof ParameterizedType)) {
      paramType = ((Class<?>) paramType).getGenericSuperclass();
    }
    ParameterizedType genericType = (ParameterizedType) paramType;
    Type containedType = genericType.getActualTypeArguments()[1];
    String objectName = varName + "o";
    String containedName = objectName + "X";
    out.println(
        indent
            + String.format(
                "HashMap<String, %1$s> %2$s= new HashMap<String, %3$s>();",
                containedType.getTypeName(), varName, containedType.getTypeName()));
    out.println(
        indent
            + String.format(
                "%1$s %2$s = %3$s;", json.object(), objectName, json.objectValue(jsonValName)));

    json.iterateObject(
        indent,
        out,
        objectName,
        (String keyName, String valueName) -> {
          generateFromJson(indent + INDENT, out, containedName, valueName, containedType);
          out.println(
              indent
                  + INDENT
                  + String.format("%1$s.put(%2$s, %3$s);", varName, keyName, containedName));
        });
  }

  private void generateListConversion(
      String indent, PrintWriter out, String varName, String jsonValName, Type paramType) {
    Type containedType = containedType(paramType);
    out.println(
        indent
            + String.format(
                "ArrayList<%1$s> %2$s= new ArrayList<%3$s>();",
                containedType.getTypeName(), varName, containedType.getTypeName()));
    String arrayName = varName + "a";
    String containedName = arrayName + "X";
    String indexName = arrayName + "i";
    out.println(
        indent
            + String.format(
                "%1$s %2$s = %3$s;", json.array(), arrayName, json.arrayValue(jsonValName)));
    out.println(
        indent
            + String.format(
                "for(int %1$s= 0; %1$s < %2$s.size(); %1$s++) {", indexName, arrayName));

    generateFromJson(
        indent + INDENT, out, containedName, arrayName + ".get(" + indexName + ")", containedType);

    out.println(indent + INDENT + String.format("%1$s.add(%2$s);", varName, containedName));
    out.println(indent + "}");
  }

  private String getJsonDecision(Class<?> cls) {
    if (cls.isArray() || List.class.isAssignableFrom(cls)) {
      return "JsonDecision.LIST";
    }
    if (Boolean.class.isAssignableFrom(cls)) {
      return "JsonDecision.BOOLEAN";
    }
    if (Number.class.isAssignableFrom(cls)) {
      return "JsonDecision.NUMBER";
    }
    if (Character.class.isAssignableFrom(cls)
        || String.class.isAssignableFrom(cls)
        || Enum.class.isAssignableFrom(cls)) {
      return "JsonDecision.STRING";
    }
    return "JsonDecision.OBJECT";
  }

  private void generateEither3Conversion(
      String indent, PrintWriter out, String varName, String valueAccess, Type paramType) {
    String innerName = varName + "e";

    out.println(indent + String.format("%1$s %2$s;", paramType.getTypeName(), varName));
    String firstDecisions = getJsonDecisions(EitherUtil.getFirstDisjointType(paramType));
    String secondDecisions = getJsonDecisions(EitherUtil.getSecondDisjointType(paramType));
    out.println(
        indent
            + String.format("if (EitherUtil.matches(%1$s, %2$s)) {", valueAccess, firstDecisions));

    generateFromJson(
        indent + INDENT, out, innerName, valueAccess, EitherUtil.getFirstDisjointType(paramType));

    out.println(
        indent + INDENT + String.format("%1$s= Either3.forFirst(%2$s);", varName, innerName));
    out.println(
        indent
            + String.format(
                "} else if (EitherUtil.matches(%1$s, %2$s)) {", valueAccess, secondDecisions));

    generateFromJson(
        indent + INDENT, out, innerName, valueAccess, EitherUtil.getSecondDisjointType(paramType));

    out.println(
        indent + INDENT + String.format("%1$s= Either3.forSecond(%2$s);", varName, innerName));
    out.println(indent + "} else  {");

    generateFromJson(
        indent + INDENT, out, innerName, valueAccess, EitherUtil.getThirdDisjointType(paramType));
    out.println(
        indent + INDENT + String.format("%1$s= Either3.forThird(%2$s);", varName, innerName));
    out.println(indent + "}");
  }

  private String getJsonDecisions(Type firstDisjointType) {
    String decisionNames =
        EitherUtil.getAllDisjoinTypes(firstDisjointType)
            .stream()
            .map(t -> getJsonDecision(getRawClass(t)))
            .collect(Collectors.joining(","));
    return String.format("new JsonDecision[] { %1$s }", decisionNames);
  }

  private void generateEitherConversion(
      String indent, PrintWriter out, String varName, String valueAccess, Type paramType) {
    String innerName = varName + "e";
    String typesName = innerName + "cls";

    Collection<Type> allDisjoinTypes =
        EitherUtil.getAllDisjoinTypes(EitherUtil.getLeftDisjointType(paramType));
    Set<Class<?>> leftClasses =
        allDisjoinTypes.stream().map(t -> getRawClass(t)).collect(Collectors.toSet());

    out.println(indent + String.format("JsonDecision[] %1$s= new JsonDecision[] {", typesName));
    boolean first = true;
    for (Class<?> startClass : leftClasses) {
      out.print(indent + INDENT + getJsonDecision(startClass));
      if (first) {
        first = false;
      } else {
        out.print(",");
      }
      out.println();
    }
    out.println(indent + "};");

    out.println(indent + String.format("%1$s %2$s;", paramType.getTypeName(), varName));
    out.println(
        indent + String.format("if (EitherUtil.matches(%1$s, %2$s)) {", valueAccess, typesName));

    generateFromJson(
        indent + INDENT, out, innerName, valueAccess, EitherUtil.getLeftDisjointType(paramType));

    out.println(indent + INDENT + String.format("%1$s= Either.forLeft(%2$s);", varName, innerName));
    out.println(indent + "} else  {");

    generateFromJson(
        indent + INDENT, out, innerName, valueAccess, EitherUtil.getRightDisjointType(paramType));
    out.println(
        indent + INDENT + String.format("%1$s= Either.forRight(%2$s);", varName, innerName));
    out.println(indent + "}");
  }

  @SuppressWarnings("unchecked")
  private String fromJsonConversion(Class<?> t, String valueName) {
    if (Enum.class.isAssignableFrom(t)) {
      return String.format("%1$s.valueOf(%2$s);", t.getName(), json.asString(valueName));
    } else if (String.class.isAssignableFrom(t)) {
      return String.format("%1$s;", json.asString(valueName));
    } else if (Number.class.isAssignableFrom(t)) {
      return String.format(
          "(%1$s)%2$s;", primitiveCast((Class<? extends Number>) t), json.asDouble(valueName));
    } else if (isSimpleNumberType(t)) {
      return String.format("(%1$s)%2$s;", t.getSimpleName(), json.asDouble(valueName));
    } else if (t == boolean.class || Boolean.class.isAssignableFrom(t)) {
      return json.asBoolean(valueName);
    } else if (t == Object.class) {
      return valueName;
    } else {
      return String.format("%1$s.fromJson((%2$s)%3$s);", dtoName(t), json.object(), valueName);
    }
  }

  private Object primitiveCast(Class<? extends Number> t) {
    if (t == Integer.class) {
      return "int";
    } else if (t == Number.class) {
      return "Number";
    } else {
      return t.getSimpleName().toLowerCase();
    }
  }
}
