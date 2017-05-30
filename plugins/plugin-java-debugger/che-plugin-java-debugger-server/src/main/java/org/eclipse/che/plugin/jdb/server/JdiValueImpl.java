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
import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;

import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;

import java.util.Arrays;
import java.util.List;

/** @author andrew00x */
public class JdiValueImpl implements JdiValue {
    private final Value         value;
    private       JdiVariable[] variables;

    public JdiValueImpl(Value value) {
        if (value == null) {
            throw new IllegalArgumentException("Underlying value may not be null. ");
        }
        this.value = value;
    }

    @Override
    public String getAsString() {
        return value.toString();
    }

    @Override
    public JdiVariable[] getVariables() {
        if (variables == null) {
            if (isPrimitive()) {
                variables = new JdiVariable[0];
            } else {
                if (isArray()) {
                    ArrayReference array = (ArrayReference)value;
                    int length = array.length();
                    variables = new JdiVariable[length];
                    for (int i = 0; i < length; i++) {
                        variables[i] = new JdiArrayElementImpl(i, array.getValue(i));
                    }
                } else {
                    ObjectReference object = (ObjectReference)value;
                    ReferenceType type = object.referenceType();
                    List<Field> fields = type.allFields();
                    variables = new JdiVariable[fields.size()];
                    int i = 0;
                    for (Field f : fields) {
                        variables[i++] = new JdiFieldImpl(f, object);
                    }
                    // See JdiFieldImpl#compareTo(JdiFieldImpl).
                    Arrays.sort(variables);
                }
            }
        }
        return variables;
    }

    @Override
    public JdiVariable getVariableByName(String name) throws DebuggerException {
        if (name == null) {
            throw new IllegalArgumentException("Variable name may not be null. ");
        }
        for (JdiVariable variable : getVariables()) {
            if (name.equals(variable.getName())) {
                return variable;
            }
        }
        return null;
    }

    private boolean isArray() {
        return value instanceof ArrayReference;
    }

    private boolean isPrimitive() {
        return value instanceof PrimitiveValue;
    }
}
