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
import com.sun.jdi.Field;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.VMCannotBeModifiedException;
import com.sun.jdi.Value;

/** @author andrew00x */
public class InstanceValue implements ExpressionValue {
    private final ObjectReference instance;
    private final Field           field;
    private       Value           value;

    public InstanceValue(ObjectReference instance, Field field) {
        this.instance = instance;
        this.field = field;
    }

    @Override
    public Value getValue() {
        if (value == null) {
            try {
                value = instance.getValue(field);
            } catch (IllegalArgumentException e) {
                throw new ExpressionException(e.getMessage(), e);
            }
        }
        return value;
    }

    @Override
    public void setValue(Value value) {
        try {
            instance.setValue(field, value);
        } catch (InvalidTypeException | ClassNotLoadedException | VMCannotBeModifiedException | IllegalArgumentException e) {
            throw new ExpressionException(e.getMessage(), e);
        }
        this.value = value;
    }
}
