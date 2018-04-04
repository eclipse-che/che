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

import com.google.common.base.Objects;
import org.eclipse.che.api.debug.shared.model.MutableVariable;
import org.eclipse.che.api.debug.shared.model.SimpleValue;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.debug.shared.model.VariablePath;

/** @author Anatoliy Bazko */
public class MutableVariableImpl implements MutableVariable {
  private final String type;
  private final boolean isPrimitive;
  private final VariablePath variablePath;
  private String name;
  private SimpleValue value;

  public MutableVariableImpl(
      String type, String name, SimpleValue value, VariablePath variablePath, boolean isPrimitive) {

    this.name = name;

    this.value = value;
    this.type = type;
    this.isPrimitive = isPrimitive;
    this.variablePath = variablePath;
  }

  public MutableVariableImpl(Variable variable) {
    this(
        variable.getType(),
        variable.getName(),
        variable.getValue(),
        variable.getVariablePath(),
        variable.isPrimitive());
  }

  public MutableVariableImpl() {
    this(null, null, null, null, false);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
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
  public void setValue(SimpleValue value) {
    this.value = value;
  }

  @Override
  public VariablePath getVariablePath() {
    return variablePath;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof MutableVariableImpl)) return false;
    MutableVariableImpl that = (MutableVariableImpl) o;
    return Objects.equal(variablePath, that.variablePath);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(variablePath);
  }
}
