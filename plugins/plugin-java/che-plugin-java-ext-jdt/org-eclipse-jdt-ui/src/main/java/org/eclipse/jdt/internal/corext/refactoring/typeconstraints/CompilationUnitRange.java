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
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.internal.corext.SourceRangeFactory;

public final class CompilationUnitRange {

  private final ICompilationUnit fCompilationUnit;
  private final ISourceRange fSourceRange;

  public CompilationUnitRange(ICompilationUnit unit, ISourceRange range) {
    Assert.isNotNull(unit);
    Assert.isNotNull(range);
    fCompilationUnit = unit;
    fSourceRange = range;
  }

  public CompilationUnitRange(ICompilationUnit unit, ASTNode node) {
    this(unit, SourceRangeFactory.create(node));
  }

  public ICompilationUnit getCompilationUnit() {
    return fCompilationUnit;
  }

  public ISourceRange getSourceRange() {
    return fSourceRange;
  }

  // rootNode must be the ast root for fCompilationUnit
  public ASTNode getNode(CompilationUnit rootNode) {
    NodeFinder finder =
        new NodeFinder(rootNode, fSourceRange.getOffset(), fSourceRange.getLength());
    ASTNode result = finder.getCoveringNode();
    if (result != null) return result;
    return finder.getCoveredNode();
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "("
        + fSourceRange.toString()
        + " in "
        + fCompilationUnit.getElementName()
        + ")"; // $NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof CompilationUnitRange)) return false;
    CompilationUnitRange other = (CompilationUnitRange) obj;
    return fCompilationUnit.equals(other.fCompilationUnit)
        && fSourceRange.equals(other.fSourceRange);
  }

  @Override
  public int hashCode() {
    return (37 * fCompilationUnit.hashCode()) ^ fSourceRange.hashCode();
  }
}
