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
package org.eclipse.che.inject;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.TypeConverter;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Converts {@link Binder#bindConstant() constant bindings} to {@link java.nio.Path} values.
 *
 * @author Tareq Sharafy
 */
public class PathConverter extends AbstractModule implements TypeConverter {

  @Override
  public Object convert(String value, TypeLiteral<?> toType) {
    return Paths.get(value);
  }

  @Override
  protected void configure() {
    convertToTypes(Matchers.only(TypeLiteral.get(Path.class)), this);
  }
}
