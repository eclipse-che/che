/**
 * ***************************************************************************** Copyright (c)
 * 2012-2015 Red Hat, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Red Hat, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.core.dom;

import java.util.Map;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;

/** @author Evgen Vidolob */
public class CheAST {
  /**
   * Internal method.
   *
   * <p>This method converts the given internal compiler AST for the given source string into a
   * compilation unit. This method is not intended to be called by clients.
   *
   * @param level the API level; one of the <code>JLS*</code> level constants
   * @param compilationUnitDeclaration an internal AST node for a compilation unit declaration
   * @param options compiler options
   * @param workingCopy the working copy that the AST is created from
   * @param monitor the progress monitor used to report progress and request cancellation, or <code>
   *     null</code> if none
   * @param isResolved whether the given compilation unit declaration is resolved
   * @return the compilation unit node
   * @noreference This method is not intended to be referenced by clients.
   * @since 3.4
   */
  public static CompilationUnit convertCompilationUnit(
      int level,
      org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration compilationUnitDeclaration,
      Map options,
      boolean isResolved,
      org.eclipse.jdt.internal.core.CompilationUnit workingCopy,
      int reconcileFlags,
      IProgressMonitor monitor) {

    ASTConverter converter = new ASTConverter(options, isResolved, monitor);
    AST ast = AST.newAST(level);
    int savedDefaultNodeFlag = ast.getDefaultNodeFlag();
    ast.setDefaultNodeFlag(ASTNode.ORIGINAL);
    BindingResolver resolver = null;
    if (isResolved) {
      resolver =
          new DefaultBindingResolver(
              compilationUnitDeclaration.scope,
              workingCopy.owner,
              new DefaultBindingResolver.BindingTables(),
              false,
              true);
      ((DefaultBindingResolver) resolver).isRecoveringBindings =
          (reconcileFlags & ICompilationUnit.ENABLE_BINDINGS_RECOVERY) != 0;
      ast.setFlag(AST.RESOLVED_BINDINGS);
    } else {
      resolver = new BindingResolver();
    }
    ast.setFlag(reconcileFlags);
    ast.setBindingResolver(resolver);
    converter.setAST(ast);

    CompilationUnit unit = converter.convert(compilationUnitDeclaration, workingCopy.getContents());
    unit.setLineEndTable(compilationUnitDeclaration.compilationResult.getLineSeparatorPositions());
    unit.setTypeRoot(workingCopy.originalFromClone());
    ast.setDefaultNodeFlag(savedDefaultNodeFlag);
    return unit;
  }
}
