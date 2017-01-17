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

import com.sun.jdi.LocalVariable;
import com.sun.jdi.StackFrame;
import com.sun.jdi.Value;

import org.eclipse.che.api.debug.shared.model.VariablePath;
import org.eclipse.che.api.debug.shared.model.impl.VariablePathImpl;

/** @author andrew00x */
public class JdiVariableImpl implements JdiVariable {
    private final LocalVariable variable;
    private final StackFrame    stackFrame;

    @Override
    public VariablePath getVariablePath() {
        return new VariablePathImpl(getName());
    }

    public JdiVariableImpl(StackFrame stackFrame, LocalVariable variable) {
        this.stackFrame = stackFrame;
        this.variable = variable;
    }

    @Override
    public String getName() {
        return variable.name();
    }

    @Override
    public boolean isPrimitive() {
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
    public String getType() {
        return variable.typeName();
    }
}
