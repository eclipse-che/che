/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.typeconstraints2;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints.types.TType;

/**
 * A VariableVariable is a ConstraintVariable which stands for the type of a variable, namely a
 * field or a local variable Use {@link ParameterTypeVariable2} for method parameters).
 */
public final class VariableVariable2 extends ConstraintVariable2
    implements ISourceConstraintVariable {

  private final String fKey;
  private ICompilationUnit fCompilationUnit;

  public VariableVariable2(TType type, IVariableBinding binding) {
    super(type);
    fKey = binding.getKey();
  }

  public void setCompilationUnit(ICompilationUnit unit) {
    fCompilationUnit = unit;
  }

  public ICompilationUnit getCompilationUnit() {
    return fCompilationUnit;
  }

  public String getKey() {
    return fKey;
  }

  /*
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return fKey.hashCode();
  }

  /*
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object other) {
    if (this == other) return true;
    if (other.getClass() != VariableVariable2.class) return false;

    return fKey.equals(((VariableVariable2) other).getKey());
  }
}
