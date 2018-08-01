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
package org.eclipse.che.api.debug.shared.model.impl;

import org.eclipse.che.api.debug.shared.model.Field;
import org.eclipse.che.api.debug.shared.model.SimpleValue;
import org.eclipse.che.api.debug.shared.model.VariablePath;

/** @author Anatoliy Bazko */
public class FieldImpl extends VariableImpl implements Field {
  private boolean isFinal;
  private boolean isStatic;
  private boolean isTransient;
  private boolean isVolatile;

  public FieldImpl(
      String name,
      SimpleValue value,
      String type,
      boolean isPrimitive,
      VariablePath variablePath,
      boolean isFinal,
      boolean isStatic,
      boolean isTransient,
      boolean isVolatile) {
    super(type, name, value, isPrimitive, variablePath);
    this.isFinal = isFinal;
    this.isStatic = isStatic;
    this.isTransient = isTransient;
    this.isVolatile = isVolatile;
  }

  @Override
  public boolean isIsFinal() {
    return isFinal;
  }

  @Override
  public boolean isIsStatic() {
    return isStatic;
  }

  @Override
  public boolean isIsTransient() {
    return isTransient;
  }

  @Override
  public boolean isIsVolatile() {
    return isVolatile;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof FieldImpl)) return false;
    if (!super.equals(o)) return false;

    FieldImpl field = (FieldImpl) o;

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
