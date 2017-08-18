/**
 * ***************************************************************************** Copyright (c) 2008,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.ui.cleanup;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * The context that contains all information required by a clean up to create a fix.
 *
 * @since 3.5
 */
public class CleanUpContext {

  private final ICompilationUnit fUnit;

  private final CompilationUnit fAst;

  /**
   * Creates a new clean up context.
   *
   * @param unit the compilation unit
   * @param ast the AST, can be <code>null</code> if {@link CleanUpRequirements#requiresAST()}
   *     returns <code>false</code>. The AST is guaranteed to contain changes made by previous clean
   *     ups only if {@link CleanUpRequirements#requiresFreshAST()} returns <code>true</code>.
   */
  public CleanUpContext(ICompilationUnit unit, CompilationUnit ast) {
    Assert.isLegal(unit != null);
    fUnit = unit;
    fAst = ast;
  }

  /**
   * The compilation unit to clean up.
   *
   * @return the compilation unit to clean up
   */
  public ICompilationUnit getCompilationUnit() {
    return fUnit;
  }

  /**
   * An AST built from the compilation unit to fix.
   *
   * <p>Can be <code>null</code> if {@link CleanUpRequirements#requiresAST()} returns <code>false
   * </code>. The AST is guaranteed to contain changes made by previous clean ups only if {@link
   * CleanUpRequirements#requiresFreshAST()} returns <code>true</code>.
   *
   * <p>Clients should check the AST API level and do nothing if they are given an AST they can't
   * handle (see {@link org.eclipse.jdt.core.dom.AST#apiLevel()}).
   *
   * @return an AST or <code>null</code> if none required
   */
  public CompilationUnit getAST() {
    return fAst;
  }
}
