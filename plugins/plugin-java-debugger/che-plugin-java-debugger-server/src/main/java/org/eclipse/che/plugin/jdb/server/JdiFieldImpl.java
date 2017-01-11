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

import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;

import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;

/** @author andrew00x */
public class JdiFieldImpl implements JdiField, Comparable<JdiFieldImpl> {
    private final Field           field;
    private final ReferenceType   type;
    private final ObjectReference object;

    public JdiFieldImpl(Field field, ObjectReference object) {
        this.field = field;
        this.object = object;
        this.type = null;
    }

    public JdiFieldImpl(Field field, ReferenceType type) {
        this.field = field;
        this.type = type;
        this.object = null;
    }

    @Override
    public String getName() {
        return field.name();
    }

    @Override
    public boolean isStatic() {
        return field.isStatic();
    }

    @Override
    public boolean isTransient() {
        return field.isTransient();
    }

    @Override
    public boolean isVolatile() {
        return field.isVolatile();
    }

    @Override
    public boolean isFinal() {
        return field.isFinal();
    }

    @Override
    public boolean isArray() throws DebuggerException {
        return JdiType.isArray(field.signature());
    }

    @Override
    public boolean isPrimitive() throws DebuggerException {
        return JdiType.isPrimitive(field.signature());
    }

    @Override
    public JdiValue getValue() {
        Value value = object == null ? type.getValue(field) : object.getValue(field);
        if (value == null) {
            return new JdiNullValue();
        }
        return new JdiValueImpl(value);
    }

    @Override
    public String getTypeName() {
        return field.typeName();
    }

    @Override
    public int compareTo(JdiFieldImpl o) {
        final boolean thisStatic = isStatic();
        final boolean thatStatic = o.isStatic();
        if (thisStatic && !thatStatic) {
            return -1;
        }
        if (!thisStatic && thatStatic) {
            return 1;
        }
        final String thisName = getName();
        final String thatName = o.getName();
        return thisName.compareTo(thatName);
    }
}
