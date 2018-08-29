/**
 * ***************************************************************************** Copyright (c) 2007,
 * 2013 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.internal.ui.JavaPlugin;

/**
 * The {@link SharedASTProvider} provides access to the {@link CompilationUnit AST root} used by the
 * current active Java editor.
 *
 * <p>For performance reasons, not more than one AST should be kept in memory at a time. Therefore,
 * clients must not keep any references to the shared AST or its nodes or bindings.
 *
 * <p>Clients can make the following assumptions about the AST:
 *
 * <dl>
 *   <li>the AST has a {@link ITypeRoot} as source: {@link CompilationUnit#getTypeRoot()} is not
 *       null.
 *   <li>the {@link AST#apiLevel() AST API level} is {@link AST#JLS8 API level 8} or higher
 *   <li>the AST has bindings resolved ({@link AST#hasResolvedBindings()})
 *   <li>{@link AST#hasStatementsRecovery() statement} and {@link AST#hasBindingsRecovery()
 *       bindings} recovery are enabled
 * </dl>
 *
 * It is possible that in the future a higher API level is used, or that future options will be
 * enabled.
 *
 * <p>The returned AST is shared. It is marked as {@link ASTNode#PROTECT} and must not be modified.
 * Clients are advised to use the non-modifying {@link ASTRewrite} to get update scripts.
 *
 * <p>This class is not intended to be subclassed or instantiated by clients.
 *
 * @since 3.4
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public final class SharedASTProvider {

  /** Wait flag class. */
  public static final class WAIT_FLAG {

    private String fName;

    private WAIT_FLAG(String name) {
      fName = name;
    }

    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return fName;
    }
  }

  /**
   * Wait flag indicating that a client requesting an AST wants to wait until an AST is ready.
   *
   * <p>An AST will be created by this AST provider if the shared AST is not for the given Java
   * element.
   */
  public static final WAIT_FLAG WAIT_YES = new WAIT_FLAG("wait yes"); // $NON-NLS-1$

  /**
   * Wait flag indicating that a client requesting an AST only wants to wait for the shared AST of
   * the active editor.
   *
   * <p>No AST will be created by the AST provider.
   */
  public static final WAIT_FLAG WAIT_ACTIVE_ONLY = new WAIT_FLAG("wait active only"); // $NON-NLS-1$

  /**
   * Wait flag indicating that a client requesting an AST only wants the already available shared
   * AST.
   *
   * <p>No AST will be created by the AST provider.
   */
  public static final WAIT_FLAG WAIT_NO = new WAIT_FLAG("don't wait"); // $NON-NLS-1$

  /**
   * Returns a compilation unit AST for the given Java element. If the element is the input of the
   * active Java editor, the AST is the shared AST.
   *
   * <p>Clients are not allowed to modify the AST and must not keep any references.
   *
   * @param element the {@link ITypeRoot}, must not be <code>null</code>
   * @param waitFlag {@link #WAIT_YES}, {@link #WAIT_NO} or {@link #WAIT_ACTIVE_ONLY}
   * @param progressMonitor the progress monitor or <code>null</code>
   * @return the AST or <code>null</code>.
   *     <dl>
   *       <li>If {@link #WAIT_NO} has been specified <code>null</code> is returned if the element
   *           is not input of the current Java editor or no AST is available
   *       <li>If {@link #WAIT_ACTIVE_ONLY} has been specified <code>null</code> is returned if the
   *           element is not input of the current Java editor
   *       <li>If {@link #WAIT_YES} has been specified either the shared AST is returned or a new
   *           AST is created.
   *       <li><code>null</code> will be returned if the operation gets canceled.
   *     </dl>
   */
  public static CompilationUnit getAST(
      ITypeRoot element, WAIT_FLAG waitFlag, IProgressMonitor progressMonitor) {
    return JavaPlugin.getDefault().getASTProvider().getAST(element, waitFlag, progressMonitor);
  }

  private SharedASTProvider() {
    // Prevent instantiation.
  }
}
