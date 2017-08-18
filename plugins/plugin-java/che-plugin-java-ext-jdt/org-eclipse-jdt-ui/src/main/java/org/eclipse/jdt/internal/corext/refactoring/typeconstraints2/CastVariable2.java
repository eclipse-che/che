/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
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

public final class CastVariable2 extends ConstraintVariable2 implements ITypeConstraintVariable {

  private final CompilationUnitRange fRange;
  private final ConstraintVariable2 fExpressionVariable;

  public CastVariable2(
      TType type, CompilationUnitRange range, ConstraintVariable2 expressionVariable) {
    super(type);
    Assert.isNotNull(expressionVariable);
    Assert.isNotNull(range);
    fRange = range;
    fExpressionVariable = expressionVariable;
  }

  public CompilationUnitRange getRange() {
    return fRange;
  }

  public ICompilationUnit getCompilationUnit() {
    return fRange.getCompilationUnit();
  }

  public void setCompilationUnit(ICompilationUnit unit) {
    throw new UnsupportedOperationException();
  }

  public ConstraintVariable2 getExpressionVariable() {
    return fExpressionVariable;
  }

  // hashCode() and equals(..) not necessary (unique per construction)
}
