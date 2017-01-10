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

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.TypeConverter;

/** @author andrew00x */
public class PairConverter extends AbstractModule implements TypeConverter {
    @Override
    public Object convert(String value, TypeLiteral<?> toType) {
        return fromString(value);
    }

    static Pair<String, String> fromString(String value) {
        final int p = value.indexOf('=');
        if (p < 0) {
            return Pair.of(value, null);
        }
        final int length = value.length();
        if (p == length) {
            return Pair.of(value.substring(0, p), "");
        }
        return Pair.of(value.substring(0, p), value.substring(p + 1, length));
    }

    @Override
    protected void configure() {
        convertToTypes(Matchers.only(new StringPairTypeLiteral()), this);
    }

    private static class StringPairTypeLiteral extends TypeLiteral<Pair<String, String>> {
    }
}
