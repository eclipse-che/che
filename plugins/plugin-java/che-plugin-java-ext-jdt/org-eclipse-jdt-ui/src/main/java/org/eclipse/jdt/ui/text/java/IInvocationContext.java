/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2013 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.ui.text.java;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CheASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * Context information for quick fix and quick assist processors.
 *
 * <p>Note: this interface is not intended to be implemented.
 *
 * @since 3.0
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IInvocationContext {

  /** @return the current compilation unit */
  ICompilationUnit getCompilationUnit();

  /** @return the offset of the current selection */
  int getSelectionOffset();

  /** @return the length of the current selection */
  int getSelectionLength();

  /**
   * Returns an AST of the compilation unit, possibly only a partial AST focused on the selection
   * offset (see {@link CheASTParser#setFocalPosition(int)}). The returned AST is shared and
   * therefore protected and cannot be modified. The client must check the AST API level and do
   * nothing if they are given an AST they can't handle.
   *
   * @see org.eclipse.jdt.core.dom.AST#apiLevel()
   * @return the root of the AST corresponding to the current compilation unit
   */
  CompilationUnit getASTRoot();

  /**
   * If the AST contains nodes whose range is equal to the selection, returns the innermost of those
   * nodes. Otherwise, returns the first node in a preorder traversal of the AST, where the complete
   * node range is covered by the selection.
   *
   * @return the covered node, or <code>null</code> if the selection is empty or too short to cover
   *     an entire node
   */
  ASTNode getCoveredNode();

  /**
   * Returns the innermost node that fully contains the selection. A node also contains the
   * zero-length selection on either end.
   *
   * <p>If more than one node covers the selection, the returned node is the last covering node
   * found in a preorder traversal of the AST. This implies that for a zero-length selection between
   * two adjacent sibling nodes, the node on the right is returned.
   *
   * @return the covering node
   */
  ASTNode getCoveringNode();
}
