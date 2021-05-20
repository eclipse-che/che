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
package org.eclipse.che.plugin.typescript.dto;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.dto.shared.DelegateTo;

/**
 * Helper class
 *
 * @author Florent Benoit
 */
public class DTOHelper {

  /** Utility class. */
  private DTOHelper() {}

  /** Check is specified method is DTO getter. */
  public static boolean isDtoGetter(Method method) {
    if (method.isAnnotationPresent(DelegateTo.class)) {
      return false;
    }

    if (method.getParameterTypes().length > 0) {
      return false;
    }

    String methodName = method.getName();

    return methodName.startsWith("get")
        || (methodName.startsWith("is")
            && ((method.getReturnType() == Boolean.class
                || method.getReturnType() == boolean.class)));
  }

  /** Check is specified method is DTO setter. */
  public static boolean isDtoSetter(Method method) {
    if (method.isAnnotationPresent(DelegateTo.class)) {
      return false;
    }
    String methodName = method.getName();
    return methodName.startsWith("set") && method.getParameterTypes().length == 1;
  }

  /** Check is specified method is DTO with. */
  public static boolean isDtoWith(Method method) {
    if (method.isAnnotationPresent(DelegateTo.class)) {
      return false;
    }
    String methodName = method.getName();
    return methodName.startsWith("with") && method.getParameterTypes().length == 1;
  }

  /** Compute field name from the stringified string type */
  public static String getFieldName(String type) {
    char[] c = type.toCharArray();
    c[0] = Character.toLowerCase(c[0]);

    return new String(c);
  }

  /** Compute argument name from the stringified string type */
  public static String getArgumentName(String type) {

    String val = getFieldName(type);

    // replace reserved keyword
    if ("arguments".equals(val)) {
      val = "argumentsObj";
    }
    return val;
  }

  /** Extract field name from the getter method */
  public static Pair<String, String> getGetterFieldName(Method method) {
    String methodName = method.getName();
    if (methodName.startsWith("get")) {
      String name = methodName.substring(3);
      return Pair.of(getFieldName(name), getArgumentName(name));
    } else if (methodName.startsWith("is")) {
      String name = methodName.substring(2);
      return Pair.of(getFieldName(name), getArgumentName(name));
    }
    throw new IllegalArgumentException("Invalid getter method" + method.getName());
  }

  /** Extract field name from the setter method */
  public static Pair<String, String> getSetterFieldName(Method method) {
    String methodName = method.getName();
    if (methodName.startsWith("set")) {
      String name = methodName.substring(3);
      return Pair.of(getFieldName(name), getArgumentName(name));
    }
    throw new IllegalArgumentException("Invalid setter method" + method.getName());
  }

  /** Extract field name and argument name from the with method */
  public static Pair<String, String> getWithFieldName(Method method) {
    String methodName = method.getName();
    if (methodName.startsWith("with")) {
      String name = methodName.substring(4);
      return Pair.of(getFieldName(name), getArgumentName(name));
    }
    throw new IllegalArgumentException("Invalid with method" + method.getName());
  }

  /** Convert Java type to TypeScript type */
  public static String convertType(Type type) {
    if (type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) type;
      Type rawType = parameterizedType.getRawType();
      return convertParametrizedType(type, parameterizedType, rawType);
    } else if (String.class.equals(type) || (type instanceof Class && ((Class) type).isEnum())) {
      // Maybe find a better enum type for typescript
      return "string";
    } else if (Integer.class.equals(type)
        || Integer.TYPE.equals(type)
        || Long.class.equals(type)
        || Long.TYPE.equals(type)
        || Double.class.equals(type)
        || Double.TYPE.equals(type)
        || Float.TYPE.equals(type)) {
      return "number";
    } else if (Boolean.class.equals(type) || Boolean.TYPE.equals(type)) {
      return "boolean";
    } else if (Serializable.class.equals(type)) {
      return "string | number | boolean";
    }

    return type.getTypeName();
  }

  /** Handle convert of a parametrized Java type to TypeScript type */
  public static String convertParametrizedType(
      Type type, ParameterizedType parameterizedType, Type rawType) {

    if (List.class.equals(rawType)) {
      return "Array<" + convertType(parameterizedType.getActualTypeArguments()[0]) + ">";
    } else if (Map.class.equals(rawType)) {
      return "Map<"
          + convertType(parameterizedType.getActualTypeArguments()[0])
          + ","
          + convertType(parameterizedType.getActualTypeArguments()[1])
          + ">";
    } else {
      throw new IllegalArgumentException("Invalid type" + type);
    }
  }

  /**
   * Same as {@link #convertParametrizedType(Type, ParameterizedType, Type)} but use [] instead if
   * 'Array[' for arrays
   */
  public static String convertParametrizedTypeDTS(
      Type type, ParameterizedType parameterizedType, Type rawType, Type containerType) {

    if (List.class.equals(rawType)) {
      return convertTypeForDTS(containerType, parameterizedType.getActualTypeArguments()[0]) + "[]";
    } else if (Map.class.equals(rawType)) {
      return "Map<"
          + convertTypeForDTS(containerType, parameterizedType.getActualTypeArguments()[0])
          + ","
          + convertTypeForDTS(containerType, parameterizedType.getActualTypeArguments()[1])
          + ">";
    } else {
      throw new IllegalArgumentException("Invalid type" + type);
    }
  }

  /**
   * Convert Java type to TypeScript type for .d.ts Same as {@link #convertType(Type)} but in
   * addition check if dto and its container dto in same package than skip adding namespace
   */
  public static String convertTypeForDTS(Type containerType, Type type) {
    if (type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) type;
      Type rawType = parameterizedType.getRawType();
      return convertParametrizedTypeDTS(type, parameterizedType, rawType, containerType);
    } else if (String.class.equals(type)) {
      // Maybe find a better enum type for typescript
      return "string";

    } else if ((type instanceof Class && ((Class) type).isEnum())) {

      Object[] constants = ((Class) type).getEnumConstants();
      return Arrays.stream(constants)
          .map(o -> "\'" + o.toString() + "\'")
          .collect(Collectors.joining(" | "));
    } else if (Integer.class.equals(type)
        || Integer.TYPE.equals(type)
        || Long.class.equals(type)
        || Long.TYPE.equals(type)
        || Double.class.equals(type)
        || Double.TYPE.equals(type)
        || Float.TYPE.equals(type)) {
      return "number";
    } else if (Boolean.class.equals(type) || Boolean.TYPE.equals(type)) {
      return "boolean";
    } else if (Serializable.class.equals(type)) {
      return "string | number | boolean";
    }

    String declarationPackage = convertToDTSPackageName((Class) containerType);
    String typePackage = convertToDTSPackageName((Class) type);
    if (declarationPackage.equals(typePackage)) {
      return convertToDTSName((Class) type);
    }

    return typePackage + "." + convertToDTSName((Class) type);
  }

  /** Remove 'Dto' suffix from class name */
  public static String convertToDTSName(Class type) {
    String name = type.getSimpleName();
    if (name.toLowerCase().endsWith("dto")) {
      name = name.substring(0, name.length() - 3);
    }

    return name;
  }

  /**
   * Convert Java package to TypeScript namespace. This method deletes "org.eclipse.", ".api",
   * ".dto", ".shared" segments from dto package name
   *
   * @param dto
   * @return the TS namespace name
   */
  public static String convertToDTSPackageName(Class dto) {
    return removeSubStrings(dto.getPackage().getName(), "org.eclipse.", ".api", ".dto", ".shared");
  }

  /** Remove all substrings from original string */
  private static String removeSubStrings(String str, String... subStrings) {
    StringBuilder builder = new StringBuilder(str);
    for (String subString : subStrings) {
      int index = builder.indexOf(subString);
      if (index != -1) {
        builder.delete(index, index + subString.length());
      }
    }

    return builder.toString();
  }
}
