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

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Field;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.NativeMethodException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;

import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;
import org.eclipse.che.plugin.jdb.server.exceptions.DebuggerAbsentInformationException;

import java.util.Arrays;
import java.util.List;

/** @author andrew00x */
public class JdiStackFrameImpl implements JdiStackFrame {
    private final StackFrame         stackFrame;
    private       JdiField[]         fields;
    private       JdiLocalVariable[] localVariables;

    public JdiStackFrameImpl(StackFrame stackFrame) {
        this.stackFrame = stackFrame;
    }

    @Override
    public JdiField[] getFields() throws DebuggerException {
        if (fields == null) {
            try {
                ObjectReference object = stackFrame.thisObject();
                if (object == null) {
                    ReferenceType type = stackFrame.location().declaringType();
                    List<Field> fs = stackFrame.location().declaringType().allFields();
                    fields = new JdiField[fs.size()];
                    int i = 0;
                    for (Field f : fs) {
                        fields[i++] = new JdiFieldImpl(f, type);
                    }
                } else {
                    List<Field> fs = object.referenceType().allFields();
                    fields = new JdiField[fs.size()];
                    int i = 0;
                    for (Field f : fs) {
                        fields[i++] = new JdiFieldImpl(f, object);
                    }
                }

                Arrays.sort(fields);
            } catch (InvalidStackFrameException e) {
                throw new DebuggerException(e.getMessage(), e);
            }
        }
        return fields;
    }

    @Override
    public JdiField getFieldByName(String name) throws DebuggerException {
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
    public JdiLocalVariable[] getLocalVariables() throws DebuggerException {
        if (localVariables == null) {
            try {
                List<LocalVariable> targetVariables = stackFrame.visibleVariables();
                localVariables = new JdiLocalVariable[targetVariables.size()];
                int i = 0;
                for (LocalVariable var : targetVariables) {
                    localVariables[i++] = new JdiLocalVariableImpl(stackFrame, var);
                }
            } catch (AbsentInformationException e) {
                throw new DebuggerAbsentInformationException(e.getMessage(), e);
            } catch (InvalidStackFrameException | NativeMethodException e) {
                throw new DebuggerException(e.getMessage(), e);
            }
        }
        return localVariables;
    }

    @Override
    public JdiLocalVariable getLocalVariableByName(String name) throws DebuggerException {
        if (name == null) {
            throw new IllegalArgumentException("Field name may not be null. ");
        }
        for (JdiLocalVariable var : getLocalVariables()) {
            if (name.equals(var.getName())) {
                return var;
            }
        }
        return null;
    }
}
