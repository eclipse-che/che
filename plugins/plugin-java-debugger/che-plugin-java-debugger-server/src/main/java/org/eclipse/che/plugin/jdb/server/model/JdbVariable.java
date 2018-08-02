/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.jdb.server.model;

import com.sun.jdi.LocalVariable;
import com.sun.jdi.StackFrame;
import com.sun.jdi.Value;
import org.eclipse.che.api.debug.shared.model.SimpleValue;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.debug.shared.model.VariablePath;
import org.eclipse.che.api.debug.shared.model.impl.VariablePathImpl;

/**
 * {@link org.eclipse.che.api.debug.shared.model.Variable} implementation for Java Debugger.
 *
 * @author Anatolii Bazko
 */
public class JdbVariable implements Variable {
  private final LocalVariable jdiVariable;
  private final SimpleValue value;

  public JdbVariable(StackFrame jdiStackFrame, LocalVariable jdiVariable) {
    Value jdiValue = jdiStackFrame.getValue(jdiVariable);

    this.jdiVariable = jdiVariable;
    this.value = jdiValue == null ? new JdbNullValue() : new JdbValue(jdiValue, getVariablePath());
  }

  public JdbVariable(SimpleValue value, LocalVariable jdiVariable) {
    this.jdiVariable = jdiVariable;
    this.value = value;
  }

  @Override
  public String getName() {
    return jdiVariable.name();
  }

  @Override
  public boolean isPrimitive() {
    return JdbType.isPrimitive(jdiVariable.signature());
  }

  @Override
  public SimpleValue getValue() {
    return value;
  }

  @Override
  public String getType() {
    return jdiVariable.typeName();
  }

  @Override
  public VariablePath getVariablePath() {
    return new VariablePathImpl(getName());
  }
}
