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
package org.eclipse.che.inject;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.TypeConverter;
import org.eclipse.che.commons.lang.Pair;

/** @author andrew00x */
public class PairArrayConverter extends AbstractModule implements TypeConverter {
  @Override
  public Object convert(String value, TypeLiteral<?> toType) {
    final String[] pairs = Iterables.toArray(Splitter.on(",").split(value), String.class);
    @SuppressWarnings("unchecked")
    final Pair<String, String>[] result = new Pair[pairs.length];
    for (int i = 0; i < pairs.length; i++) {
      result[i] = PairConverter.fromString(pairs[i]);
    }
    return result;
  }

  @Override
  protected void configure() {
    convertToTypes(Matchers.only(new StringPairTypeLiteral()), this);
  }

  private static class StringPairTypeLiteral extends TypeLiteral<Pair<String, String>[]> {}
}
