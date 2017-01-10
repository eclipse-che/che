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

import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.TypeConverter;

/**
 * Converts {@link Binder#bindConstant() constant bindings} to {@link java.nio.Path} values.
 * 
 * @author Tareq Sharafy
 *
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
