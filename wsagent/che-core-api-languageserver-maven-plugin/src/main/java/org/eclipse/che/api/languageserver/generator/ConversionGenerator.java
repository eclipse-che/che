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

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.List;

/**
 * Base class for conversion generators. Subclasses are responsible for generating conversion code
 * for a single property of a dto.
 *
 * @author Thomas Mäder
 */
public abstract class ConversionGenerator {

  protected ConversionGenerator() {}

  protected Type containedType(Type paramType) {
    if (paramType == null || !List.class.isAssignableFrom(getRawClass(paramType))) {
      return null;
    }
    if (paramType instanceof ParameterizedType) {
      ParameterizedType genericType = (ParameterizedType) paramType;
      return getUpperBound(genericType.getActualTypeArguments()[0]);
    }
    if (paramType instanceof Class<?>) {
      Class<?> clazz = (Class<?>) paramType;
      Type c = containedType(clazz.getGenericSuperclass());
      if (c != null) {
        return c;
      }
      for (Type intf : clazz.getGenericInterfaces()) {
        c = containedType(intf);
        if (c != null) {
          return c;
        }
      }
    }
    return null;
  }

  private Type getUpperBound(Type type) {
    if (type instanceof WildcardType) {
      return ((WildcardType) type).getUpperBounds()[0];
    }
    return type;
  }

  protected Class<?> getRawClass(Type type) {
    if (type instanceof ParameterizedType) {
      return getRawClass(((ParameterizedType) type).getRawType());
    } else if (type instanceof WildcardType) {
      return getRawClass(((WildcardType) type).getUpperBounds()[0]);
    }
    return (Class<?>) type;
  }

  protected boolean isSimpleNumberType(Class<?> clazz) {
    return clazz == double.class
        || clazz == int.class
        || clazz == float.class
        || clazz == long.class;
  }

  protected String getterName(Class<?> receiverClass, Method m) {
    if (boolean.class == m.getParameterTypes()[0] || Boolean.class == m.getParameterTypes()[0]) {
      String root = m.getName().substring(3);
      try {
        return receiverClass.getMethod("is" + root, new Class<?>[] {}).getName();
      } catch (NoSuchMethodException e) {
        try {
          return receiverClass.getMethod("get" + root, new Class<?>[] {}).getName();
        } catch (NoSuchMethodException e1) {
          StringBuilder b = new StringBuilder(root);
          b.setCharAt(0, Character.toLowerCase(root.charAt(0)));
          return b.toString();
        }
      }
    }
    return m.getName().replaceFirst("set", "get");
  }

  protected String fieldName(Method setterMethod) {
    StringBuilder name = new StringBuilder(setterMethod.getName().substring(3));
    name.setCharAt(0, Character.toLowerCase(name.charAt(0)));
    return name.toString();
  }
}
