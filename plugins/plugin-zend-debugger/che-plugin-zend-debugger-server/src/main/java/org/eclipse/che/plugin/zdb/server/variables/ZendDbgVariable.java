/*
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.zdb.server.variables;

import static org.eclipse.che.plugin.zdb.server.expressions.IDbgDataFacet.Facet.KIND_ARRAY_MEMBER;
import static org.eclipse.che.plugin.zdb.server.expressions.IDbgDataFacet.Facet.KIND_OBJECT_MEMBER;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.che.api.debug.shared.model.SimpleValue;
import org.eclipse.che.api.debug.shared.model.VariablePath;
import org.eclipse.che.api.debug.shared.model.impl.SimpleValueImpl;
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
  private boolean isComplete;

  public ZendDbgVariable(VariablePath variablePath, IDbgExpression zendDbgExpression) {
    this.variablePath = variablePath;
    this.zendDbgExpression = zendDbgExpression;
    this.name = createName(zendDbgExpression);
    this.variables = Collections.emptyList();
    this.isComplete =
        zendDbgExpression.getChildrenCount() == zendDbgExpression.getChildren().size();
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public SimpleValue getValue() {
    String value =
        zendDbgExpression.getDataType() == DataType.PHP_STRING
            ? '"' + zendDbgExpression.getValue() + '"'
            : zendDbgExpression.getValue();
    return new SimpleValueImpl(variables, value);
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
  public VariablePath getVariablePath() {
    return variablePath;
  }

  @Override
  public void setValue(String newValue) {
    if (zendDbgExpression.setValue(newValue)) {
      // Re-evaluate underlying expression
      isComplete = false;
      makeComplete();
    }
  }

  @Override
  public void makeComplete() {
    if (!isComplete) {
      // Evaluate wrapped expression to fetch all child variables
      zendDbgExpression.evaluate();
      variables = new ArrayList<>();
      int childId = 0;
      for (IDbgExpression child : zendDbgExpression.getChildren()) {
        List<String> childPath = new ArrayList<>(variablePath.getPath());
        childPath.add(String.valueOf(childId++));
        variables.add(new ZendDbgVariable(new VariablePathImpl(childPath), child));
      }
      isComplete = true;
    }
  }

  private String createName(IDbgExpression expression) {
    String name = expression.getExpression();
    if (expression.hasFacet(KIND_OBJECT_MEMBER)) {
      name = name.substring(name.lastIndexOf(":") + 1);
    } else if (expression.hasFacet(KIND_ARRAY_MEMBER)) {
      name = '[' + name + ']';
    }
    return name;
  }
}
