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

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints.types.TType;

/**
 * A ParameterTypeVariable is a ConstraintVariable which stands for the type of a method parameter.
 */
public final class ParameterTypeVariable2 extends ConstraintVariable2
    implements ISourceConstraintVariable {

  private final int fParameterIndex;
  private final String fKey;
  private ICompilationUnit fCompilationUnit;

  public ParameterTypeVariable2(TType type, int index, IMethodBinding binding) {
    super(type);
    Assert.isNotNull(binding);
    Assert.isTrue(0 <= index);
    fParameterIndex = index;
    fKey = binding.getKey();
  }

  public void setCompilationUnit(ICompilationUnit cu) {
    fCompilationUnit = cu;
  }

  public ICompilationUnit getCompilationUnit() {
    return fCompilationUnit;
  }

  public int getParameterIndex() {
    return fParameterIndex;
  }

  public String getKey() {
    return fKey;
  }

  /*
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return getParameterIndex() ^ getKey().hashCode();
  }

  /*
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object other) {
    if (this == other) return true;
    if (other.getClass() != ParameterTypeVariable2.class) return false;

    ParameterTypeVariable2 other2 = (ParameterTypeVariable2) other;
    return getParameterIndex() == other2.getParameterIndex() && getKey().equals(other2.getKey());
  }

  @Override
  public String toString() {
    String toString = (String) getData(TO_STRING);
    return toString == null
        ? "[Parameter(" + fParameterIndex + "," + fKey + ")]"
        : toString; // $NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
  }
}
