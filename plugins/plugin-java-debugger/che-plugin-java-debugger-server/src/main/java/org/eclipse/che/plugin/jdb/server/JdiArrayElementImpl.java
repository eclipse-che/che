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
package org.eclipse.che.plugin.jdb.server;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.Value;

/** @author andrew00x */
public class JdiArrayElementImpl implements JdiArrayElement {
    private final int    index;
    private final Value  value;
    private final String name;

    public JdiArrayElementImpl(int index, Value value) {
        this.index = index;
        this.value = value;
        this.name = "[" + index + "]";
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isArray() {
        return value instanceof ArrayReference;
    }

    @Override
    public boolean isPrimitive() {
        return value instanceof PrimitiveValue;
    }

    @Override
    public JdiValue getValue() {
        if (value == null) {
            return new JdiNullValue();
        }
        return new JdiValueImpl(value);
    }

    @Override
    public String getTypeName() {
        if (value == null) {
            return "null";
        }
        return value.type().name();
    }
}
