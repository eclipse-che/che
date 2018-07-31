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
package org.eclipse.che.commons.lang.reflect;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * Provides runtime information about parameterized type.
 *
 * @author andrew00x
 */
public final class ParameterizedTypeImpl implements ParameterizedType {

  private final Type[] typeArguments;
  private final Class<?> rawType;

  public ParameterizedTypeImpl(Class<?> rawType, Type... typeArguments) {
    this.rawType = rawType;
    this.typeArguments = new Type[typeArguments.length];
    System.arraycopy(typeArguments, 0, this.typeArguments, 0, this.typeArguments.length);
  }

  @Override
  public Type[] getActualTypeArguments() {
    return Arrays.copyOf(typeArguments, typeArguments.length);
  }

  @Override
  public Type getRawType() {
    return rawType;
  }

  @Override
  public Type getOwnerType() {
    return null;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(rawType.getName());
    builder.append('<');
    for (int i = 0, length = typeArguments.length; i < length; i++) {
      if (i > 0) {
        builder.append(", ");
      }
      builder.append(
          typeArguments[i] instanceof Class
              ? ((Class<?>) typeArguments[i]).getName()
              : typeArguments[i].toString());
    }
    builder.append('>');
    return builder.toString();
  }

  @Override
  public int hashCode() {
    int hashCode = 7;
    hashCode = 31 * hashCode + rawType.hashCode();
    hashCode = 31 * hashCode + Arrays.hashCode(typeArguments);
    return hashCode;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ParameterizedType)) {
      return false;
    }
    ParameterizedType other = (ParameterizedType) o;
    return rawType.equals(other.getRawType())
        && Arrays.equals(typeArguments, other.getActualTypeArguments());
  }
}
