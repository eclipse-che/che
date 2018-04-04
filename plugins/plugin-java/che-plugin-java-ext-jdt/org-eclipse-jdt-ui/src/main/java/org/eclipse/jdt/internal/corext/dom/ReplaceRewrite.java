/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2013 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.dom;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.text.edits.TextEditGroup;

/** Rewrite helper for situations where one node can be replaced by many new nodes. */
public class ReplaceRewrite {

  protected ASTRewrite fRewrite;
  protected ASTNode[] fToReplace;
  protected StructuralPropertyDescriptor fDescriptor;

  public static ReplaceRewrite create(ASTRewrite rewrite, ASTNode[] nodes) {
    return new ReplaceRewrite(rewrite, nodes);
  }

  protected ReplaceRewrite(ASTRewrite rewrite, ASTNode[] nodes) {
    Assert.isNotNull(rewrite);
    Assert.isNotNull(nodes);
    Assert.isTrue(nodes.length > 0);
    fRewrite = rewrite;
    fToReplace = nodes;
    fDescriptor = fToReplace[0].getLocationInParent();
    if (nodes.length > 1) {
      Assert.isTrue(fDescriptor instanceof ChildListPropertyDescriptor);
    }
  }

  public void replace(ASTNode[] replacements, TextEditGroup description) {
    if (fToReplace.length == 1) {
      if (replacements.length == 1) {
        handleOneOne(replacements, description);
      } else {
        handleOneMany(replacements, description);
      }
    } else {
      handleManyMany(replacements, description);
    }
  }

  protected void handleOneOne(ASTNode[] replacements, TextEditGroup description) {
    fRewrite.replace(fToReplace[0], replacements[0], description);
  }

  protected void handleOneMany(ASTNode[] replacements, TextEditGroup description) {
    handleManyMany(replacements, description);
  }

  protected void handleManyMany(ASTNode[] replacements, TextEditGroup description) {
    ListRewrite container =
        fRewrite.getListRewrite(
            fToReplace[0].getParent(), (ChildListPropertyDescriptor) fDescriptor);
    if (fToReplace.length == replacements.length) {
      for (int i = 0; i < fToReplace.length; i++) {
        container.replace(fToReplace[i], replacements[i], description);
      }
    } else if (fToReplace.length < replacements.length) {
      for (int i = 0; i < fToReplace.length; i++) {
        container.replace(fToReplace[i], replacements[i], description);
      }
      for (int i = fToReplace.length; i < replacements.length; i++) {
        container.insertAfter(replacements[i], replacements[i - 1], description);
      }
    } else if (fToReplace.length > replacements.length) {
      int delta = fToReplace.length - replacements.length;
      for (int i = 0; i < delta; i++) {
        container.remove(fToReplace[i], description);
      }
      for (int i = delta, r = 0; i < fToReplace.length; i++, r++) {
        container.replace(fToReplace[i], replacements[r], description);
      }
    }
  }
}
