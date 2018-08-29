/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2012 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.text.correction.proposals;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.internal.corext.dom.LinkedNodeFinder;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.SharedASTProvider;
import org.eclipse.jdt.ui.text.java.correction.CUCorrectionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

public class RenameNodeCorrectionProposal extends CUCorrectionProposal {

  private String fNewName;
  private int fOffset;
  private int fLength;

  public RenameNodeCorrectionProposal(
      String name, ICompilationUnit cu, int offset, int length, String newName, int relevance) {
    super(name, cu, relevance, JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE));
    fOffset = offset;
    fLength = length;
    fNewName = newName;
  }

  /*(non-Javadoc)
   * @see org.eclipse.jdt.internal.ui.text.correction.CUCorrectionProposal#addEdits(org.eclipse.jface.text.IDocument)
   */
  @Override
  protected void addEdits(IDocument doc, TextEdit root) throws CoreException {
    super.addEdits(doc, root);

    // build a full AST
    CompilationUnit unit =
        SharedASTProvider.getAST(getCompilationUnit(), SharedASTProvider.WAIT_YES, null);

    ASTNode name = NodeFinder.perform(unit, fOffset, fLength);
    if (name instanceof SimpleName) {

      SimpleName[] names = LinkedNodeFinder.findByProblems(unit, (SimpleName) name);
      if (names != null) {
        for (int i = 0; i < names.length; i++) {
          SimpleName curr = names[i];
          root.addChild(new ReplaceEdit(curr.getStartPosition(), curr.getLength(), fNewName));
        }
        return;
      }
    }
    root.addChild(new ReplaceEdit(fOffset, fLength, fNewName));
  }
}
