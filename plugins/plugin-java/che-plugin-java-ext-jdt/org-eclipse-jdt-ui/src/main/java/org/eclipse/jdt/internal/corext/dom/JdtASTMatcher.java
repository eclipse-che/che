/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.dom;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.SimpleName;

public class JdtASTMatcher extends ASTMatcher {

  @Override
  public boolean match(SimpleName node, Object other) {
    boolean isomorphic = super.match(node, other);
    if (!isomorphic || !(other instanceof SimpleName)) return false;
    SimpleName name = (SimpleName) other;
    IBinding nodeBinding = node.resolveBinding();
    IBinding otherBinding = name.resolveBinding();
    if (nodeBinding == null) {
      if (otherBinding != null) {
        return false;
      }
    } else {
      if (nodeBinding != otherBinding) {
        return false;
      }
    }
    if (node.resolveTypeBinding() != name.resolveTypeBinding()) return false;
    return true;
  }

  public static boolean doNodesMatch(ASTNode one, ASTNode other) {
    Assert.isNotNull(one);
    Assert.isNotNull(other);

    return one.subtreeMatch(new JdtASTMatcher(), other);
  }
}
