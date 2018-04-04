/**
 * ***************************************************************************** Copyright (c) 2016
 * TypeFox GmbH (http://www.typefox.io) and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * *****************************************************************************
 */
package org.eclipse.che.api.languageserver.generator;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

/**
 * This class is a backport from org.eclipse.lsp4j.jsonrpc.messages.Either in version 0.2.0-SNAPSHOT
 * remove when moving to 0.2.0 version
 *
 * @author Thomas Mäder
 */
public class EitherUtil {
  static Type getLeftDisjointType(Type type) {
    return getDisjointType(type, 0);
  }

  static Type getRightDisjointType(Type type) {
    return getDisjointType(type, 1);
  }

  static Type getFirstDisjointType(Type type) {
    return getDisjointType(type, 0);
  }

  static Type getSecondDisjointType(Type type) {
    return getDisjointType(type, 1);
  }

  static Type getThirdDisjointType(Type type) {
    return getDisjointType(type, 2);
  }

  private static Type getDisjointType(Type type, int index) {
    if (type instanceof ParameterizedType) {
      final ParameterizedType parameterizedType = (ParameterizedType) type;
      return parameterizedType.getActualTypeArguments()[index];
    }
    if (type instanceof Class) {
      final Class<?> cls = (Class<?>) type;
      return cls.getTypeParameters()[index];
    }
    return null;
  }

  /** Return all disjoint types. */
  static Collection<Type> getAllDisjoinTypes(Type type) {
    return collectDisjoinTypes(type, new ArrayList<>());
  }

  private static Collection<Type> collectDisjoinTypes(Type type, Collection<Type> types) {
    if (isEither(type)) {
      if (type instanceof ParameterizedType) {
        return collectDisjoinTypes((ParameterizedType) type, types);
      }
      if (type instanceof Class) {
        return collectDisjoinTypes((Class<?>) type, types);
      }
    }
    types.add(type);
    return types;
  }

  private static Collection<Type> collectDisjoinTypes(
      ParameterizedType type, Collection<Type> types) {
    for (Type typeArgument : type.getActualTypeArguments()) {
      collectDisjoinTypes(typeArgument, types);
    }
    return types;
  }

  private static Collection<Type> collectDisjoinTypes(Class<?> type, Collection<Type> types) {
    for (Type typeParameter : type.getTypeParameters()) {
      collectDisjoinTypes(typeParameter, types);
    }
    return types;
  }

  /** Test whether the given type is Either. */
  private static boolean isEither(Type type) {
    if (type instanceof ParameterizedType) {
      return isEither((ParameterizedType) type);
    }
    if (type instanceof Class) {
      return isEither((Class<?>) type);
    }
    return false;
  }

  /** Test whether the given type is Either. */
  private static boolean isEither(ParameterizedType type) {
    return isEither(type.getRawType());
  }

  /** Test whether the given class is Either. */
  private static boolean isEither(Class<?> cls) {
    return Either.class.isAssignableFrom(cls);
  }
}
