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
package org.eclipse.che.plugin.jdb.server.model;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.Value;

import org.eclipse.che.api.debug.shared.model.SimpleValue;
import org.eclipse.che.api.debug.shared.model.Variable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/** @author andrew00x */
public class JdbValue implements SimpleValue {
    private final Value value;

    private List<Variable> variables;

    public JdbValue(Value value) {
        if (value == null) {
            throw new IllegalArgumentException("Underlying value can not be null. ");
        }
        this.value = value;
    }

    @Override
    public String getString() {
        return value.toString();
    }

    @Override
    public List<Variable> getVariables() {
        if (variables == null) {
            if (isPrimitive()) {
                variables = Collections.emptyList();
            } else {
                variables = new LinkedList<>();
                if (isArray()) {
                    ArrayReference array = (ArrayReference)value;
                    for (int i = 0; i < array.length(); i++) {
                        variables.add(new JdbArrayElement(array.getValue(i), i));
                    }
                } else {
                    ObjectReference object = (ObjectReference)value;
                    for (Field f : object.referenceType().allFields()) {
                        variables.add(new JdbField(f, object));
                    }
                    variables = variables.stream()
                                         .map(v -> (org.eclipse.che.plugin.jdb.server.model.JdbField)v)
                                         .sorted(new JdbFieldComparator())
                                         .collect(Collectors.toList());
                }
            }
        }
        return variables;
    }

    public boolean isArray() {
        return value instanceof ArrayReference;
    }

    public boolean isPrimitive() {
        return value instanceof PrimitiveValue;
    }
}
