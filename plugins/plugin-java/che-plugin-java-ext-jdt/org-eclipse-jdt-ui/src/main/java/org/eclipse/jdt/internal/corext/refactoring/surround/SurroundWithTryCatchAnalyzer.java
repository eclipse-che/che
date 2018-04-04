/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.surround;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.dom.Selection;

public class SurroundWithTryCatchAnalyzer extends SurroundWithAnalyzer {
  private ITypeBinding[] fExceptions;

  public SurroundWithTryCatchAnalyzer(ICompilationUnit unit, Selection selection)
      throws CoreException {
    super(unit, selection);
  }

  public ITypeBinding[] getExceptions() {
    return fExceptions;
  }

  @Override
  public void endVisit(CompilationUnit node) {
    BodyDeclaration enclosingNode = null;
    if (!getStatus().hasFatalError() && hasSelectedNodes())
      enclosingNode =
          (BodyDeclaration) ASTNodes.getParent(getFirstSelectedNode(), BodyDeclaration.class);

    super.endVisit(node);
    if (enclosingNode != null && !getStatus().hasFatalError()) {
      fExceptions = ExceptionAnalyzer.perform(enclosingNode, getSelection());
      if (fExceptions == null || fExceptions.length == 0) {
        fExceptions =
            new ITypeBinding[] {
              node.getAST().resolveWellKnownType("java.lang.Exception")
            }; // $NON-NLS-1$
      }
    }
  }
}
