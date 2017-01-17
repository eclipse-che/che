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
package org.eclipse.che.plugin.jdb.server.jdi;

import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;

import org.eclipse.che.api.debug.shared.model.VariablePath;
import org.eclipse.che.api.debug.shared.model.impl.VariablePathImpl;

import static java.util.Arrays.asList;

/** @author andrew00x */
public class JdiFieldImpl implements JdiField {
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
    public boolean isExistInformation() {
        return !isPrimitive();
    }

    @Override
    public boolean isIsStatic() {
        return field.isStatic();
    }

    @Override
    public boolean isIsTransient() {
        return field.isTransient();
    }

    @Override
    public boolean isIsVolatile() {
        return field.isVolatile();
    }

    @Override
    public boolean isIsFinal() {
        return field.isFinal();
    }

    @Override
    public boolean isPrimitive() {
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
    public String getType() {
        return field.typeName();
    }

    @Override
    public VariablePath getVariablePath() {
        return new VariablePathImpl(asList(isIsStatic() ? "static" : "this", getName()));
    }
}
