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
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints.CompilationUnitRange;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints.types.TType;

/** A TypeVariable is a ConstraintVariable which stands for a single type reference (in source). */
public final class TypeVariable2 extends ConstraintVariable2 implements ITypeConstraintVariable {

  private final CompilationUnitRange fRange;

  public TypeVariable2(TType type, CompilationUnitRange range) {
    super(type);
    Assert.isNotNull(range);
    fRange = range;
  }

  public CompilationUnitRange getRange() {
    return fRange;
  }

  /*
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return getRange().hashCode() ^ getType().hashCode();
  }

  /*
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object other) {
    // TODO: unique per construction?  //return this == other;
    if (this == other) return true;
    if (other.getClass() != TypeVariable2.class) return false;

    TypeVariable2 otherTypeVariable = (TypeVariable2) other;
    return getRange().equals(otherTypeVariable.getRange())
        && getType() == otherTypeVariable.getType();
  }

  public void setCompilationUnit(ICompilationUnit unit) {
    throw new UnsupportedOperationException();
  }

  public ICompilationUnit getCompilationUnit() {
    return fRange.getCompilationUnit();
  }

  @Override
  public String toString() {
    return super.toString()
        + " ["
        + fRange.getSourceRange().getOffset()
        + '+'
        + fRange.getSourceRange().getLength()
        + ']'; // $NON-NLS-1$
  }
}
