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

import com.sun.jdi.ArrayReference;
import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.Value;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/** @author andrew00x */
public class JdiValueImpl implements JdiValue {
    private final Value             value;
    private       List<JdiVariable> variables;

    public JdiValueImpl(Value value) {
        if (value == null) {
            throw new IllegalArgumentException("Underlying value may not be null. ");
        }
        this.value = value;
    }

    @Override
    public String getString() {
        return value.toString();
    }

    @Override
    public List<JdiVariable> getVariables() {
        if (variables == null) {
            if (isPrimitive()) {
                variables = Collections.emptyList();
            } else {
                variables = new LinkedList<>();
                if (isArray()) {
                    ArrayReference array = (ArrayReference)value;
                    for (int i = 0; i < array.length(); i++) {
                        variables.add(new JdiArrayElementImpl(i, array.getValue(i)));
                    }
                } else {
                    ObjectReference object = (ObjectReference)value;
                    for (Field f : object.referenceType().allFields()) {
                        variables.add(new JdiFieldImpl(f, object));
                    }
                    variables = variables.stream()
                                         .map(v -> (JdiField)v)
                                         .sorted(new JdiFieldComparator())
                                         .collect(Collectors.toList());
                }
            }
        }
        return variables;
    }

    @Override
    public JdiVariable getVariableByName(String name) {
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

    @Override
    public boolean isArray() {
        return value instanceof ArrayReference;
    }

    @Override
    public boolean isPrimitive() {
        return value instanceof PrimitiveValue;
    }
}
