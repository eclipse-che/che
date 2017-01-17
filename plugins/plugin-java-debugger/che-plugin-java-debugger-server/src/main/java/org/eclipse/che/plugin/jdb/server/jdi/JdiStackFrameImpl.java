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

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Field;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.NativeMethodException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author andrew00x
 * @author Anatolii Bazko
 */
public class JdiStackFrameImpl implements JdiStackFrame {
    private final StackFrame        stackFrame;
    private       List<JdiField>    fields;
    private       List<JdiVariable> variables;

    public JdiStackFrameImpl(StackFrame stackFrame) {
        this.stackFrame = stackFrame;
    }

    @Override
    public List<JdiField> getFields() {
        if (fields == null) {
            try {
                fields = new LinkedList<>();

                ObjectReference object = stackFrame.thisObject();
                if (object == null) {
                    ReferenceType type = stackFrame.location().declaringType();
                    for (Field f : stackFrame.location().declaringType().allFields()) {
                        fields.add(new JdiFieldImpl(f, type));
                    }
                } else {
                    for (Field f : object.referenceType().allFields()) {
                        fields.add(new JdiFieldImpl(f, object));
                    }
                }

                fields.sort(new JdiFieldComparator());
            } catch (InvalidStackFrameException e) {
                fields = Collections.emptyList();
            }
        }
        return fields;
    }

    @Override
    public JdiField getFieldByName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Field name may not be null. ");
        }
        for (JdiField f : getFields()) {
            if (name.equals(f.getName())) {
                return f;
            }
        }
        return null;
    }

    @Override
    public List<JdiVariable> getVariables() {
        if (variables == null) {
            try {
                variables = stackFrame.visibleVariables()
                                      .stream()
                                      .map(v -> new JdiVariableImpl(stackFrame, v))
                                      .collect(Collectors.toList());
            } catch (AbsentInformationException | InvalidStackFrameException | NativeMethodException e) {
                variables = Collections.emptyList();
            }
        }
        return variables;
    }

    @Override
    public JdiVariable getVariableByName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Field name may not be null. ");
        }

        for (JdiVariable var : getVariables()) {
            if (name.equals(var.getName())) {
                return var;
            }
        }

        return null;
    }
}
