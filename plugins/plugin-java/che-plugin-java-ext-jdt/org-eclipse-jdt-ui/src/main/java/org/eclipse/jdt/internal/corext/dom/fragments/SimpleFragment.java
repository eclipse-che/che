/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.dom.fragments;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.internal.corext.dom.JdtASTMatcher;
import org.eclipse.text.edits.TextEditGroup;

class SimpleFragment extends ASTFragment {
  private final ASTNode fNode;

  SimpleFragment(ASTNode node) {
    Assert.isNotNull(node);
    fNode = node;
  }

  @Override
  public IASTFragment[] getMatchingFragmentsWithNode(ASTNode node) {
    if (!JdtASTMatcher.doNodesMatch(getAssociatedNode(), node)) return new IASTFragment[0];

    IASTFragment match = ASTFragmentFactory.createFragmentForFullSubtree(node);
    Assert.isTrue(match.matches(this) || this.matches(match));
    return new IASTFragment[] {match};
  }

  public boolean matches(IASTFragment other) {
    return other.getClass().equals(getClass())
        && JdtASTMatcher.doNodesMatch(other.getAssociatedNode(), getAssociatedNode());
  }

  public IASTFragment[] getSubFragmentsMatching(IASTFragment toMatch) {
    return ASTMatchingFragmentFinder.findMatchingFragments(
        getAssociatedNode(), (ASTFragment) toMatch);
  }

  public int getStartPosition() {
    return fNode.getStartPosition();
  }

  public int getLength() {
    return fNode.getLength();
  }

  public ASTNode getAssociatedNode() {
    return fNode;
  }

  public void replace(ASTRewrite rewrite, ASTNode replacement, TextEditGroup textEditGroup) {
    if (replacement instanceof Name && fNode.getParent() instanceof ParenthesizedExpression) {
      // replace including the parenthesized expression around it
      rewrite.replace(fNode.getParent(), replacement, textEditGroup);
    } else {
      rewrite.replace(fNode, replacement, textEditGroup);
    }
  }

  @Override
  public int hashCode() {
    return fNode.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    SimpleFragment other = (SimpleFragment) obj;
    return fNode.equals(other.fNode);
  }
}
