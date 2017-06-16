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

import com.sun.jdi.LocalVariable;
import com.sun.jdi.StackFrame;
import com.sun.jdi.Value;

import org.eclipse.che.api.debug.shared.model.SimpleValue;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.debug.shared.model.VariablePath;
import org.eclipse.che.api.debug.shared.model.impl.VariablePathImpl;

/** @author andrew00x */
public class JdbVariable implements Variable {
    private final LocalVariable variable;
    private final SimpleValue   jdbValue;

    public JdbVariable(StackFrame stackFrame, LocalVariable variable) {
        Value value = stackFrame.getValue(variable);
        this.variable = variable;
        this.jdbValue = value == null ? new JdbNullValue() : new JdbValue(value);
    }

    @Override
    public String getName() {
        return variable.name();
    }

    @Override
    public boolean isPrimitive() {
        return JdbType.isPrimitive(variable.signature());
    }

    @Override
    public SimpleValue getValue() {
        return jdbValue;
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
