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
package org.eclipse.che.api.debug.shared.model.impl;

import org.eclipse.che.api.debug.shared.model.VariablePath;
import org.eclipse.che.api.debug.shared.model.Variable;

import java.util.Collections;
import java.util.List;

/**
 * @author Anatoliy Bazko
 */
public class VariableImpl implements Variable {
    private final String                   name;
    private final boolean                  isExistInformation;
    private final String                   value;
    private final String                   type;
    private final boolean                  isPrimitive;
    private final List<? extends Variable> variables;
    private final VariablePath             variablePath;

    public VariableImpl(String type,
                        String name,
                        String value,
                        boolean isPrimitive,
                        VariablePath variablePath,
                        List<? extends Variable> variables,
                        boolean isExistInformation) {
        this.name = name;
        this.isExistInformation = isExistInformation;
        this.value = value;
        this.type = type;
        this.isPrimitive = isPrimitive;
        this.variables = variables;
        this.variablePath = variablePath;
    }

    public VariableImpl(String value, VariablePath variablePath) {
        this(null, null, value, false, variablePath, Collections.<Variable>emptyList(), false);
    }

    @Override
    public String getName() {
        return name;
    }

    public boolean isExistInformation() {
        return isExistInformation;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public boolean isPrimitive() {
        return isPrimitive;
    }

    @Override
    public List<Variable> getVariables() {
        return Collections.unmodifiableList(variables);
    }

    @Override
    public VariablePath getVariablePath() {
        return variablePath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VariableImpl)) return false;

        VariableImpl variable = (VariableImpl)o;

        if (isExistInformation != variable.isExistInformation) return false;
        if (isPrimitive != variable.isPrimitive) return false;
        if (name != null ? !name.equals(variable.name) : variable.name != null) return false;
        if (value != null ? !value.equals(variable.value) : variable.value != null) return false;
        if (type != null ? !type.equals(variable.type) : variable.type != null) return false;
        if (variables != null ? !variables.equals(variable.variables) : variable.variables != null) return false;
        return !(variablePath != null ? !variablePath.equals(variable.variablePath) : variable.variablePath != null);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (isExistInformation ? 1 : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (isPrimitive ? 1 : 0);
        result = 31 * result + (variables != null ? variables.hashCode() : 0);
        result = 31 * result + (variablePath != null ? variablePath.hashCode() : 0);
        return result;
    }
}
