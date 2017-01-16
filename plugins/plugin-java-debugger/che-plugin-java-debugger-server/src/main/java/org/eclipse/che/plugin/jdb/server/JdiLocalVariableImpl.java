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

import com.sun.jdi.LocalVariable;
import com.sun.jdi.StackFrame;
import com.sun.jdi.Value;

import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;

/** @author andrew00x */
public class JdiLocalVariableImpl implements JdiLocalVariable {
    private final LocalVariable variable;
    private final StackFrame    stackFrame;

    public JdiLocalVariableImpl(StackFrame stackFrame, LocalVariable variable) {
        this.stackFrame = stackFrame;
        this.variable = variable;
    }

    @Override
    public String getName() {
        return variable.name();
    }

    @Override
    public boolean isArray() throws DebuggerException {
        return JdiType.isArray(variable.signature());
    }

    @Override
    public boolean isPrimitive() throws DebuggerException {
        return JdiType.isPrimitive(variable.signature());
    }

    @Override
    public JdiValue getValue() {
        Value value = stackFrame.getValue(variable);
        if (value == null) {
            return new JdiNullValue();
        }
        return new JdiValueImpl(value);
    }

    @Override
    public String getTypeName() {
        return variable.typeName();
    }
}
