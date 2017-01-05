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
package org.eclipse.che.plugin.jdb.server.expression;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VMCannotBeModifiedException;
import com.sun.jdi.Value;

/** @author andrew00x */
public class StaticValue implements ExpressionValue {
    private final ReferenceType klass;
    private final Field         field;
    private       Value         value;

    public StaticValue(ReferenceType klass, Field field) {
        this.klass = klass;
        this.field = field;
    }

    @Override
    public Value getValue() {
        if (value == null) {
            try {
                value = klass.getValue(field);
            } catch (IllegalArgumentException e) {
                throw new ExpressionException(e.getMessage(), e);
            }
        }
        return value;
    }

    @Override
    public void setValue(Value value) {
        if (!(klass instanceof ClassType)) {
            throw new ExpressionException("Unable update field " + field.name());
        }
        try {
            ((ClassType)klass).setValue(field, value);
        } catch (InvalidTypeException | ClassNotLoadedException | VMCannotBeModifiedException | IllegalArgumentException e) {
            throw new ExpressionException(e.getMessage(), e);
        }
        this.value = value;
    }
}
