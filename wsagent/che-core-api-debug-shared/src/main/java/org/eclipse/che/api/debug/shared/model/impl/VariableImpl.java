/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.debug.shared.model.impl;

import org.eclipse.che.api.debug.shared.model.SimpleValue;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.debug.shared.model.VariablePath;

/** @author Anatoliy Bazko */
public class VariableImpl implements Variable {
  private final String name;
  private final SimpleValue value;
  private final String type;
  private final boolean isPrimitive;

  private final VariablePath variablePath;

  public VariableImpl(
      String type, String name, SimpleValue value, boolean isPrimitive, VariablePath variablePath) {

    this.name = name;

    this.value = value;
    this.type = type;
    this.isPrimitive = isPrimitive;

    this.variablePath = variablePath;
  }

  public VariableImpl(SimpleValue value, VariablePath variablePath) {
    this(null, null, value, false, variablePath);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public SimpleValue getValue() {
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
  public VariablePath getVariablePath() {
    return variablePath;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof VariableImpl)) return false;

    VariableImpl variable = (VariableImpl) o;

    if (isPrimitive != variable.isPrimitive) return false;
    if (name != null ? !name.equals(variable.name) : variable.name != null) return false;
    if (value != null ? !value.equals(variable.value) : variable.value != null) return false;
    if (type != null ? !type.equals(variable.type) : variable.type != null) return false;
    return !(variablePath != null
        ? !variablePath.equals(variable.variablePath)
        : variable.variablePath != null);
  }

  @Override
  public int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + (value != null ? value.hashCode() : 0);
    result = 31 * result + (type != null ? type.hashCode() : 0);
    result = 31 * result + (isPrimitive ? 1 : 0);
    result = 31 * result + (variablePath != null ? variablePath.hashCode() : 0);
    return result;
  }
}
