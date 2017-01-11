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

import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.debug.shared.model.VariablePath;

import java.util.List;

/**
 * @author Anatoliy Bazko
 */
public class FieldImpl extends VariableImpl {
    private boolean isFinal;
    private boolean isStatic;
    private boolean isTransient;
    private boolean isVolatile;

    public FieldImpl(String name,
                     boolean isExistInformation,
                     String value,
                     String type,
                     boolean isPrimitive,
                     List<Variable> variables,
                     VariablePath variablePath,
                     boolean isFinal,
                     boolean isStatic,
                     boolean isTransient,
                     boolean isVolatile) {
        super(type, name, value, isPrimitive, variablePath, variables, isExistInformation);
        this.isFinal = isFinal;
        this.isStatic = isStatic;
        this.isTransient = isTransient;
        this.isVolatile = isVolatile;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public boolean isTransient() {
        return isTransient;
    }

    public boolean isVolatile() {
        return isVolatile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FieldImpl)) return false;
        if (!super.equals(o)) return false;

        FieldImpl field = (FieldImpl)o;

        if (isFinal != field.isFinal) return false;
        if (isStatic != field.isStatic) return false;
        if (isTransient != field.isTransient) return false;
        return isVolatile == field.isVolatile;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (isFinal ? 1 : 0);
        result = 31 * result + (isStatic ? 1 : 0);
        result = 31 * result + (isTransient ? 1 : 0);
        result = 31 * result + (isVolatile ? 1 : 0);
        return result;
    }
}
