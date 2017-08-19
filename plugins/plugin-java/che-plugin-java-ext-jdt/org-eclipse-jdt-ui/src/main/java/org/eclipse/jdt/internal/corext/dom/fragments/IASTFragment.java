/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.dom.fragments;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.text.edits.TextEditGroup;

/**
 * An IASTFragment represents 'part' of an AST, but not necessarily a subtree of the AST. A fragment
 * may simply be an instance of some sort of pattern or formation in an AST. Such "fragments",
 * however, do correspond to a contiguous source code region, and, thus, posses a source start
 * position, and a source length. Every fragment maps to an ASTNode, although this mapping is not
 * necessarily straightforward, and more than one fragment may map to a given node.
 *
 * <p>Fragments support abstract operations, which support the notion of 'matching' fragments. One
 * operation determines whether a fragment 'matches' a given fragment. Another operation finds all
 * sub-fragments (fragments contained within a parent fragment, including the parent itself) which
 * 'match' another given fragment.
 */
public interface IASTFragment {

  /**
   * Determines whether <code> other </code>
   * 'matches' <code> this </code>.
   * This binary operation should be reflexive,
   * symmetric, and transitive.
   *
   * That two node match does not imply that their source ranges
   * are the same, or that they map (via getAssociatedNode()) to the
   * same node.
   * @param other the element to test with
   * @return return <code> true if the passed element matches the current element.
   */
  public boolean matches(IASTFragment other);

  /**
   * Returns (at least some approximation of) a maximal set of sub-fragments of this fragment which
   * match <code> toMatch </code>
   *
   * @param toMatch the fragment to match
   * @return set of sub fragments that match
   */
  public IASTFragment[] getSubFragmentsMatching(IASTFragment toMatch);

  /**
   * Every fragment maps to a node. Multiple fragments can map to the same node.
   *
   * @return ASTNode The node to which this fragment maps.
   */
  public ASTNode getAssociatedNode();

  /**
   * Every fragment has a source start position.
   *
   * @return int The source start position.
   */
  public int getStartPosition();

  /**
   * Every fragment has a source length.
   *
   * @return int The source length.
   */
  public int getLength();

  /**
   * Replaces this fragment with the given replacement node.
   *
   * @param rewrite an ASTRewrite
   * @param replacement replacement for this fragment
   * @param textEditGroup a description or <code>null</code>
   */
  public void replace(ASTRewrite rewrite, ASTNode replacement, TextEditGroup textEditGroup);
}
