/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.jdb.server.model;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;

import org.eclipse.che.api.debug.shared.model.Field;
import org.eclipse.che.api.debug.shared.model.SimpleValue;
import org.eclipse.che.api.debug.shared.model.VariablePath;
import org.eclipse.che.api.debug.shared.model.impl.VariablePathImpl;

import static java.util.Arrays.asList;

/**
 * Java debugger implementation of {@link Field}
 *
 * @author andrew00x
 * @author Anatolii Bazko
 */
public class JdbField implements Field {
    private final com.sun.jdi.Field jdiField;

    private final ReferenceType   type;
    private final ObjectReference object;

    public JdbField(com.sun.jdi.Field jdiField, ObjectReference object) {
        this.jdiField = jdiField;
        this.object = object;
        this.type = null;
    }

    public JdbField(com.sun.jdi.Field jdiField, ReferenceType type) {
        this.jdiField = jdiField;
        this.type = type;
        this.object = null;
    }

    @Override
    public String getName() {
        return jdiField.name();
    }

    @Override
    public boolean isIsStatic() {
        return jdiField.isStatic();
    }

    @Override
    public boolean isIsTransient() {
        return jdiField.isTransient();
    }

    @Override
    public boolean isIsVolatile() {
        return jdiField.isVolatile();
    }

    @Override
    public boolean isIsFinal() {
        return jdiField.isFinal();
    }

    @Override
    public boolean isPrimitive() {
        return JdbType.isPrimitive(jdiField.signature());
    }

    @Override
    public SimpleValue getValue() {
        Value value = object == null ? type.getValue(jdiField) : object.getValue(jdiField);
        if (value == null) {
            return new JdbNullValue();
        }
        return new JdbValue(value);
    }

    @Override
    public String getType() {
        return jdiField.typeName();
    }

    @Override
    public VariablePath getVariablePath() {
        return new VariablePathImpl(asList(isIsStatic() ? "static" : "this", getName()));
    }
}
