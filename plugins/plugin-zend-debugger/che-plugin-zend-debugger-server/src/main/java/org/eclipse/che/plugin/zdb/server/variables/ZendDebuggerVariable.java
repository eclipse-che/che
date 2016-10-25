/*******************************************************************************
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.zdb.server.variables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.debug.shared.model.VariablePath;
import org.eclipse.che.api.debug.shared.model.impl.VariablePathImpl;
import org.eclipse.che.plugin.zdb.server.variables.IDbgDataType.DataType;

/**
 * PHP debug variable.
 *
 * @author Bartlomiej Laczkowski
 */
public class ZendDebuggerVariable implements Variable {
    private final IDbgVariable zendDbgVariable;
    private final VariablePath variablePath;
    private List<Variable> variables;

    public ZendDebuggerVariable(VariablePath variablePath, IDbgVariable zendDbgVariable) {
        this.variablePath = variablePath;
        this.zendDbgVariable = zendDbgVariable;
    }

    @Override
    public boolean isExistInformation() {
        return true;
    }

    @Override
    public String getName() {
        return zendDbgVariable.getName();
    }

    @Override
    public String getValue() {
        if (zendDbgVariable.getDataType() == DataType.PHP_STRING)
            return '"' + zendDbgVariable.getValue() + '"';
        return zendDbgVariable.getValue();
    }

    @Override
    public String getType() {
        return zendDbgVariable.getDataType().getText();
    }

    @Override
    public boolean isPrimitive() {
        switch (zendDbgVariable.getDataType()) {
        case PHP_BOOL:
        case PHP_FLOAT:
        case PHP_INT:
        case PHP_STRING:
        case PHP_NULL:
        case PHP_UNINITIALIZED:
            return true;
        default:
            return false;
        }
    }

    @Override
    public List<Variable> getVariables() {
        if (variables == null) {
            if (zendDbgVariable.getChildrenCount() > 0) {
                // Resolve expression to fetch child variables
                zendDbgVariable.resolve();
                variables = new ArrayList<>();
                for (IDbgVariable child : zendDbgVariable.getChildren()) {
                    List<String> childPath = new ArrayList<>(variablePath.getPath());
                    childPath.add(child.getName());
                    variables.add(new ZendDebuggerVariable(new VariablePathImpl(childPath), child));
                }
            } else {
                variables = Collections.emptyList();
            }
        }
        return variables;
    }

    @Override
    public VariablePath getVariablePath() {
        return variablePath;
    }

}
