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

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.text.edits.TextEditGroup;

/**
 * Rewrite helper for {@link Statement}s. Ensures that the replacement nodes are enclosed in a Block
 * if necessary.
 */
public class StatementRewrite extends ReplaceRewrite {

  public StatementRewrite(ASTRewrite rewrite, ASTNode[] nodes) {
    super(rewrite, nodes);
  }

  @Override
  protected void handleOneMany(ASTNode[] replacements, TextEditGroup description) {
    AST ast = fToReplace[0].getAST();
    // to replace == 1, but more than one replacement. Have to check if we
    // need to insert a block to not change structure
    if (ASTNodes.isControlStatementBody(fDescriptor)) {
      Block block = ast.newBlock();
      ListRewrite statements = fRewrite.getListRewrite(block, Block.STATEMENTS_PROPERTY);
      for (int i = 0; i < replacements.length; i++) {
        statements.insertLast(replacements[i], description);
      }
      fRewrite.replace(fToReplace[0], block, description);
    } else {
      ListRewrite container =
          fRewrite.getListRewrite(
              fToReplace[0].getParent(), (ChildListPropertyDescriptor) fDescriptor);
      container.replace(fToReplace[0], replacements[0], description);
      for (int i = 1; i < replacements.length; i++) {
        container.insertAfter(replacements[i], replacements[i - 1], description);
      }
    }
  }
}
