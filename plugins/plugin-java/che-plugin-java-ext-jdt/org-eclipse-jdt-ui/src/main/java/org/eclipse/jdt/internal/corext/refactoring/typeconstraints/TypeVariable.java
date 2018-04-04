/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.typeconstraints;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;

public final class TypeVariable extends ConstraintVariable {

  private final String fSource;
  private final CompilationUnitRange fTypeRange;

  public TypeVariable(Type type) {
    super(type.resolveBinding());
    fSource = type.toString();
    ICompilationUnit cu = ASTCreator.getCu(type);
    Assert.isNotNull(cu);
    fTypeRange = new CompilationUnitRange(cu, ASTNodes.getElementType(type));
  }

  public TypeVariable(ITypeBinding binding, String source, CompilationUnitRange range) {
    super(binding);
    fSource = source;
    fTypeRange = range;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return fSource;
  }

  public CompilationUnitRange getCompilationUnitRange() {
    return fTypeRange;
  }
}
