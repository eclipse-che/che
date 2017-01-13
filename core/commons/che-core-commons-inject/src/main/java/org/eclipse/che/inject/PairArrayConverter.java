/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.inject;

import org.eclipse.che.commons.lang.Pair;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.TypeConverter;

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

    private static class StringPairTypeLiteral extends TypeLiteral<Pair<String, String>[]> {
    }
}
