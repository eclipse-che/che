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
    private final JdiValue      jdiValue;

    public JdiVariableImpl(StackFrame stackFrame, LocalVariable variable) {
        Value value = stackFrame.getValue(variable);
        this.variable = variable;
        this.jdiValue = value == null ? new JdiNullValue() : new JdiValueImpl(value);
    }

    public JdiVariableImpl(LocalVariable variable) {
        this.variable = variable;
        this.jdiValue = null;
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
        return jdiValue;
    }

    @Override
    public String getType() {
        return variable.typeName();
    }

    @Override
    public VariablePath getVariablePath() {
        return new VariablePathImpl(getName());
    }
}
