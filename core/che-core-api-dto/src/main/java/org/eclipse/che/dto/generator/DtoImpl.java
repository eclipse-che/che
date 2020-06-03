/*
 * Copyright (c) 2012-2016 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.dto.generator;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.che.dto.shared.CompactJsonDto;
import org.eclipse.che.dto.shared.DTO;
import org.eclipse.che.dto.shared.DelegateTo;
import org.eclipse.che.dto.shared.JsonFieldName;
import org.eclipse.che.dto.shared.SerializationIndex;

/** Abstract base class for the source generating template for a single DTO. */
abstract class DtoImpl {
  protected static final String COPY_JSONS_PARAM = "copyJsons";

  private final Class<?> dtoInterface;
  private final DtoTemplate enclosingTemplate;
  private final boolean compactJson;
  private final String implClassName;
  private final List<Method> dtoMethods;

  DtoImpl(DtoTemplate enclosingTemplate, Class<?> dtoInterface) {
    this.enclosingTemplate = enclosingTemplate;
    this.dtoInterface = dtoInterface;
    this.implClassName = dtoInterface.getSimpleName() + "Impl";
    this.compactJson = DtoTemplate.implementsInterface(dtoInterface, CompactJsonDto.class);
    this.dtoMethods = ImmutableList.copyOf(calcDtoMethods());
  }

  protected boolean isCompactJson() {
    return compactJson;
  }

  public Class<?> getDtoInterface() {
    return dtoInterface;
  }

  public DtoTemplate getEnclosingTemplate() {
    return enclosingTemplate;
  }

  protected String getJavaFieldName(String getterName) {
    String fieldName;
    if (getterName.startsWith("get")) {
      fieldName = getterName.substring(3);
    } else {
      // starts with "is", see method '#ignoreMethod(Method)'
      fieldName = getterName.substring(2);
    }
    return normalizeIdentifier(Character.toLowerCase(fieldName.charAt(0)) + fieldName.substring(1));
  }

  private String normalizeIdentifier(String fieldName) {
    // use $ prefix according to http://docs.oracle.com/javase/specs/jls/se7/html/jls-3.html#jls-3.8
    switch (fieldName) {
      case "default":
        fieldName = "$" + fieldName;
        break;
        // add other keywords here
    }
    return fieldName;
  }

  private String getCamelCaseName(String fieldName) {
    // see normalizeIdentifier method
    if (fieldName.charAt(0) == '$') {
      fieldName = fieldName.substring(1);
    }
    return Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
  }

  protected String getImplClassName() {
    return implClassName;
  }

  protected String getSetterName(String fieldName) {
    return "set" + getCamelCaseName(fieldName);
  }

  protected String getWithName(String fieldName) {
    return "with" + getCamelCaseName(fieldName);
  }

  protected String getListAdderName(String fieldName) {
    return "add" + getCamelCaseName(fieldName);
  }

  protected String getMapPutterName(String fieldName) {
    return "put" + getCamelCaseName(fieldName);
  }

  protected String getClearName(String fieldName) {
    return "clear" + getCamelCaseName(fieldName);
  }

  protected String getEnsureName(String fieldName) {
    return "ensure" + getCamelCaseName(fieldName);
  }

  /** Get the canonical name of the field by deriving it from a getter method's name. */
  protected String getFieldNameFromGetterName(String getterName) {
    String fieldName;
    if (getterName.startsWith("get")) {
      fieldName = getterName.substring(3);
    } else {
      // starts with "is", see method '#ignoreMethod(Method)'
      fieldName = getterName.substring(2);
    }
    return Character.toLowerCase(fieldName.charAt(0)) + fieldName.substring(1);
  }

  /**
   * Get the name of the JSON field that corresponds to the given getter method in a DTO-annotated
   * type.
   */
  protected String getJsonFieldName(Method getterMethod) {
    // First, check if a custom field name is defined for the getter
    JsonFieldName fieldNameAnn = getterMethod.getAnnotation(JsonFieldName.class);
    if (fieldNameAnn != null) {
      String customFieldName = fieldNameAnn.value();
      if (customFieldName != null && !customFieldName.isEmpty()) {
        return customFieldName;
      }
    }
    // If no custom name is given for the field, deduce it from the camel notation
    return getFieldNameFromGetterName(getterMethod.getName());
  }

  /**
   * Our super interface may implement some other interface (or not). We need to know because if it
   * does then we need to directly extend said super interfaces impl class.
   */
  protected Class<?> getSuperDtoInterface(Class<?> dto) {
    Class<?>[] superInterfaces = dto.getInterfaces();
    if (superInterfaces.length > 0) {
      for (Class<?> superInterface : superInterfaces) {
        if (superInterface.isAnnotationPresent(DTO.class)) {
          return superInterface;
        }
      }
    }
    return null;
  }

  protected List<Method> getDtoGetters(Class<?> dto) {
    final Map<String, Method> getters = new HashMap<>();
    if (enclosingTemplate.isDtoInterface(dto)) {
      addDtoGetters(dto, getters);
      addSuperGetters(dto, getters);
    }
    return new ArrayList<>(getters.values());
  }

  /** Get the names of all the getters in the super DTO interface and upper ancestors. */
  protected Set<String> getSuperGetterNames(Class<?> dto) {
    final Map<String, Method> getters = new HashMap<>();
    Class<?> superDto = getSuperDtoInterface(dto);
    if (superDto != null) {
      addDtoGetters(superDto, getters);
      addSuperGetters(superDto, getters);
    }
    return getters.keySet();
  }

  /**
   * Adds all getters from parent <b>NOT DTO</b> interfaces for given {@code dto} interface. Does
   * not add method when it is already present in getters map.
   */
  private void addSuperGetters(Class<?> dto, Map<String, Method> getters) {
    for (Class<?> superInterface : dto.getInterfaces()) {
      if (!superInterface.isAnnotationPresent(DTO.class)) {
        for (Method method : superInterface.getDeclaredMethods()) {
          // when method is already present in map then child interface
          // overrides it, which means that it should not be put into getters
          if (isDtoGetter(method) && !getters.containsKey(method.getName())) {
            getters.put(method.getName(), method);
          }
        }
        addSuperGetters(superInterface, getters);
      }
    }
  }

  protected List<Method> getInheritedDtoGetters(Class<?> dto) {
    List<Method> getters = new ArrayList<>();
    if (enclosingTemplate.isDtoInterface(dto)) {
      Class<?> superInterface = getSuperDtoInterface(dto);
      while (superInterface != null) {
        addDtoGetters(superInterface, getters);
        superInterface = getSuperDtoInterface(superInterface);
      }

      addDtoGetters(dto, getters);
    }
    return getters;
  }

  private void addDtoGetters(Class<?> dto, Map<String, Method> getters) {
    for (Method method : dto.getDeclaredMethods()) {
      if (!method.isDefault() && isDtoGetter(method)) {
        getters.put(method.getName(), method);
      }
    }
  }

  private void addDtoGetters(Class<?> dto, List<Method> getters) {
    for (Method method : dto.getDeclaredMethods()) {
      if (!method.isDefault() && isDtoGetter(method)) {
        getters.add(method);
      }
    }
  }

  /** Check is specified method is DTO getter. */
  protected boolean isDtoGetter(Method method) {
    if (method.isAnnotationPresent(DelegateTo.class)) {
      return false;
    }
    String methodName = method.getName();
    if ((methodName.startsWith("get") || methodName.startsWith("is"))
        && method.getParameterTypes().length == 0) {
      if (methodName.length() > 2 && methodName.startsWith("is")) {
        return method.getReturnType() == Boolean.class || method.getReturnType() == boolean.class;
      }
      return methodName.length() > 3;
    }
    return false;
  }

  /** Tests whether or not a given generic type is allowed to be used as a generic. */
  protected static boolean isWhitelisted(Class<?> genericType) {
    return DtoTemplate.jreWhitelist.contains(genericType);
  }

  /** Tests whether or not a given return type is a number primitive or its wrapper type. */
  protected static boolean isNumber(Class<?> returnType) {
    if (null != returnType
        && (Number.class.isAssignableFrom(returnType)
            || int.class.equals(returnType)
            || long.class.equals(returnType)
            || short.class.equals(returnType)
            || float.class.equals(returnType)
            || double.class.equals(returnType)
            || byte.class.equals(returnType))) {
      return true;
    }
    return false;
  }

  /** Tests whether or not a given return type is a boolean primitive or its wrapper type. */
  protected static boolean isBoolean(Class<?> returnType) {
    return returnType.equals(Boolean.class) || returnType.equals(boolean.class);
  }

  protected static String getPrimitiveName(Class<?> returnType) {
    if (returnType.equals(Integer.class) || returnType.equals(int.class)) {
      return "int";
    } else if (returnType.equals(Long.class) || returnType.equals(long.class)) {
      return "long";
    } else if (returnType.equals(Short.class) || returnType.equals(short.class)) {
      return "short";
    } else if (returnType.equals(Float.class) || returnType.equals(float.class)) {
      return "float";
    } else if (returnType.equals(Double.class) || returnType.equals(double.class)) {
      return "double";
    } else if (returnType.equals(Byte.class) || returnType.equals(byte.class)) {
      return "byte";
    } else if (returnType.equals(Boolean.class) || returnType.equals(boolean.class)) {
      return "boolean";
    } else if (returnType.equals(Character.class) || returnType.equals(char.class)) {
      return "char";
    }
    throw new IllegalArgumentException("Unknown wrapper class type.");
  }

  /** Tests whether or not a given return type is a java.util.List. */
  public static boolean isList(Class<?> returnType) {
    return returnType.equals(List.class);
  }

  /** Tests whether or not a given return type is a java.util.Map. */
  public static boolean isMap(Class<?> returnType) {
    return returnType.equals(Map.class);
  }

  public static boolean isAny(Class<?> returnType) {
    return returnType.equals(Object.class);
  }

  /**
   * Expands the type and its first generic parameter (which can also have a first generic parameter
   * (...)).
   *
   * <p>For example, JsonArray&lt;JsonStringMap&lt;JsonArray&lt;SomeDto&gt;&gt;&gt; would produce
   * [JsonArray, JsonStringMap, JsonArray, SomeDto].
   */
  public static List<Type> expandType(Type curType) {
    List<Type> types = new LinkedList<>();
    do {
      types.add(curType);

      if (curType instanceof ParameterizedType) {
        Type[] genericParamTypes = ((ParameterizedType) curType).getActualTypeArguments();
        Type rawType = ((ParameterizedType) curType).getRawType();
        boolean map = rawType instanceof Class<?> && rawType == Map.class;
        if (!map && genericParamTypes.length != 1) {
          throw new IllegalStateException(
              "Multiple type parameters are not supported (neither are zero type parameters)");
        }
        Type genericParamType = map ? genericParamTypes[1] : genericParamTypes[0];
        if (genericParamType instanceof Class<?>) {
          Class<?> genericParamTypeClass = (Class<?>) genericParamType;
          if (isWhitelisted(genericParamTypeClass)) {
            assert genericParamTypeClass.equals(String.class)
                : "For JSON serialization there can be only strings or DTO types. ";
          }
        }
        curType = genericParamType;
      } else {
        if (curType instanceof Class) {
          Class<?> clazz = (Class<?>) curType;
          if (isList(clazz) || isMap(clazz)) {
            throw new DtoTemplate.MalformedDtoInterfaceException(
                "JsonArray and JsonStringMap must have a generic type specified.");
          }
        }
        curType = null;
      }
    } while (curType != null);
    return types;
  }

  public static Class<?> getRawClass(Type type) {
    return (Class<?>)
        ((type instanceof ParameterizedType) ? ((ParameterizedType) type).getRawType() : type);
  }

  /**
   * Returns public methods specified in DTO interface.
   *
   * <p>
   *
   * <p>For compact DTO (see {@link org.eclipse.che.dto.shared.CompactJsonDto}) methods are ordered
   * corresponding to {@link org.eclipse.che.dto.shared.SerializationIndex} annotation.
   *
   * <p>
   *
   * <p>Gaps in index sequence are filled with {@code null}s.
   */
  protected List<Method> getDtoMethods() {
    return dtoMethods;
  }

  private Method[] calcDtoMethods() {
    if (!compactJson) {
      return dtoInterface.getMethods();
    }

    Map<Integer, Method> methodsMap = new HashMap<>();
    int maxIndex = 0;
    for (Method method : dtoInterface.getMethods()) {
      SerializationIndex serializationIndex = method.getAnnotation(SerializationIndex.class);
      Preconditions.checkNotNull(
          serializationIndex,
          "Serialization index is not specified for %s in %s",
          method.getName(),
          dtoInterface.getSimpleName());

      // "53" is the number of bits in JS integer.
      // This restriction will allow to add simple bit-field
      // "serialization-skipping-list" in the future.
      int index = serializationIndex.value();
      Preconditions.checkState(
          index > 0 && index <= 53,
          "Serialization index out of range [1..53] for %s in %s",
          method.getName(),
          dtoInterface.getSimpleName());

      Preconditions.checkState(
          !methodsMap.containsKey(index),
          "Duplicate serialization index for %s in %s",
          method.getName(),
          dtoInterface.getSimpleName());

      maxIndex = Math.max(index, maxIndex);
      methodsMap.put(index, method);
    }

    Method[] result = new Method[maxIndex];
    for (int index = 0; index < maxIndex; index++) {
      result[index] = methodsMap.get(index + 1);
    }

    return result;
  }

  protected boolean isLastMethod(Method method) {
    Preconditions.checkNotNull(method);
    return method == dtoMethods.get(dtoMethods.size() - 1);
  }

  /** Create a textual representation of a string literal that evaluates to the given value. */
  protected String quoteStringLiteral(String value) {
    StringWriter sw = new StringWriter();
    try (JsonWriter writer = new JsonWriter(sw)) {
      writer.setLenient(true);
      writer.value(value);
      writer.flush();
    } catch (IOException ex) {
      throw new RuntimeException("Unexpected I/O failure: " + ex.getLocalizedMessage(), ex);
    }
    return sw.toString();
  }

  /** @return String representing the source definition for the DTO impl as an inner class. */
  abstract String serialize();
}
