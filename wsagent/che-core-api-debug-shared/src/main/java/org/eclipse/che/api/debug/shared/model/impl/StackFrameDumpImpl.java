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

import org.eclipse.che.api.debug.shared.dto.StackFrameDumpDto;
import org.eclipse.che.api.debug.shared.model.Field;
import org.eclipse.che.api.debug.shared.model.StackFrameDump;
import org.eclipse.che.api.debug.shared.model.Variable;

import java.util.Collections;
import java.util.List;

/**
 * @author Anatoliy Bazko
 */
public class StackFrameDumpImpl implements StackFrameDump {
    private final List<? extends Field>    fields;
    private final List<? extends Variable> variables;

    public StackFrameDumpImpl(List<? extends Field> fields, List<? extends Variable> variables) {
        this.fields = fields;
        this.variables = variables;
    }

    public StackFrameDumpImpl(StackFrameDumpDto dto) {
        this(dto.getFields(), dto.getVariables());
    }

    @Override
    public List<Field> getFields() {
        return Collections.unmodifiableList(fields);
    }

    @Override
    public List<Variable> getVariables() {
        return Collections.unmodifiableList(variables);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StackFrameDumpImpl)) return false;

        StackFrameDumpImpl that = (StackFrameDumpImpl)o;

        if (fields != null ? !fields.equals(that.fields) : that.fields != null) return false;
        return !(variables != null ? !variables.equals(that.variables) : that.variables != null);

    }

    @Override
    public int hashCode() {
        int result = fields != null ? fields.hashCode() : 0;
        result = 31 * result + (variables != null ? variables.hashCode() : 0);
        return result;
    }
}
