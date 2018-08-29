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
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints.types.TType;

/** A ReturnTypeVariable is a ConstraintVariable which stands for the return type of a method. */
public final class ReturnTypeVariable2 extends ConstraintVariable2
    implements ISourceConstraintVariable {

  private final String fKey;
  private ICompilationUnit fCompilationUnit;

  public ReturnTypeVariable2(TType type, IMethodBinding binding) {
    super(type);
    fKey = binding.getKey();
  }

  public String getKey() {
    return fKey;
  }

  /*
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return getKey().hashCode();
  }

  /*
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object other) {
    if (this == other) return true;
    if (other.getClass() != ReturnTypeVariable2.class) return false;

    ReturnTypeVariable2 other2 = (ReturnTypeVariable2) other;
    return getKey().equals(other2.getKey());
  }

  public void setCompilationUnit(ICompilationUnit unit) {
    fCompilationUnit = unit;
  }

  public ICompilationUnit getCompilationUnit() {
    return fCompilationUnit;
  }
}
