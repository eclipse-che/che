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

import static org.eclipse.che.plugin.zdb.server.expressions.IDbgDataFacet.Facet.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.che.api.debug.shared.model.VariablePath;
import org.eclipse.che.api.debug.shared.model.impl.VariablePathImpl;
import org.eclipse.che.plugin.zdb.server.expressions.IDbgDataType.DataType;
import org.eclipse.che.plugin.zdb.server.expressions.IDbgExpression;

/**
 * PHP Zend debugger specific variable.
 *
 * @author Bartlomiej Laczkowski
 */
public class ZendDbgVariable implements IDbgVariable {
    private final IDbgExpression zendDbgExpression;
    private final VariablePath variablePath;
    private final String name;
    private List<IDbgVariable> variables;

    public ZendDbgVariable(VariablePath variablePath, IDbgExpression zendDbgExpression) {
        this.variablePath = variablePath;
        this.zendDbgExpression = zendDbgExpression;
        this.name = createName(zendDbgExpression);
    }

    @Override
    public boolean isExistInformation() {
        return true;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue() {
        if (zendDbgExpression.getDataType() == DataType.PHP_STRING)
            return '"' + zendDbgExpression.getValue() + '"';
        return zendDbgExpression.getValue();
    }

    @Override
    public String getType() {
        return zendDbgExpression.getDataType().getText();
    }

    @Override
    public boolean isPrimitive() {
        switch (zendDbgExpression.getDataType()) {
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
    public List<IDbgVariable> getVariables() {
        if (variables == null) {
            variables = new ArrayList<>();
            for (IDbgExpression child : zendDbgExpression.getChildren()) {
                List<String> childPath = new ArrayList<>(variablePath.getPath());
                childPath.add(createName(child));
                variables.add(new ZendDbgVariable(new VariablePathImpl(childPath), child));
            }
        }
        return variables;
    }

    @Override
    public VariablePath getVariablePath() {
        return variablePath;
    }

    @Override
    public void setValue(String newValue) {
        if (zendDbgExpression.setValue(newValue)) {
            // New value was set successfully, reset child variables
            variables = null;
        }
    }

    /**
     * Creates human-readable name for Zend dbg variable.
     *
     * @param expression
     * @return name for Zend dbg variable
     */
    public static String createName(IDbgExpression expression) {
        String name = expression.getExpression();
        if (expression.hasFacet(KIND_OBJECT_MEMBER)) {
            name = name.substring(name.lastIndexOf(":") + 1);
        } else if (expression.hasFacet(KIND_ARRAY_MEMBER)) {
            name = '[' + name + ']';
        }
        return name;
    }

}
