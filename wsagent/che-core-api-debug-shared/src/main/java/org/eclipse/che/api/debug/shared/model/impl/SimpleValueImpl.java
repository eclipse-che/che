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

import org.eclipse.che.api.debug.shared.dto.SimpleValueDto;
import org.eclipse.che.api.debug.shared.model.SimpleValue;
import org.eclipse.che.api.debug.shared.model.Variable;

import java.util.Collections;
import java.util.List;

/**
 * @author Anatoliy Bazko
 */
public class SimpleValueImpl implements SimpleValue {
    private final List<? extends Variable> variables;
    private final String                   value;

    public SimpleValueImpl(List<? extends Variable> variables, String value) {
        this.variables = variables;
        this.value = value;
    }

    public SimpleValueImpl(SimpleValueDto dto) {
        this(dto.getVariables(), dto.getValue());
    }

    @Override
    public List<Variable> getVariables() {
        return Collections.unmodifiableList(variables);
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleValueImpl)) return false;

        SimpleValueImpl value1 = (SimpleValueImpl)o;

        if (variables != null ? !variables.equals(value1.variables) : value1.variables != null) return false;
        return !(value != null ? !value.equals(value1.value) : value1.value != null);

    }

    @Override
    public int hashCode() {
        int result = variables != null ? variables.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}
