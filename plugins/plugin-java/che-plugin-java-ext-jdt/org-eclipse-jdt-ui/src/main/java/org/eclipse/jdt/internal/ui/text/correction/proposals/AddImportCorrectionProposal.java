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
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.internal.corext.util.QualifiedTypeNameHistory;
import org.eclipse.jdt.ui.text.java.correction.ASTRewriteCorrectionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Image;

public class AddImportCorrectionProposal extends ASTRewriteCorrectionProposal {

  private final String fTypeName;
  private final String fQualifierName;

  public AddImportCorrectionProposal(
      String name,
      ICompilationUnit cu,
      int relevance,
      Image image,
      String qualifierName,
      String typeName,
      SimpleName node) {
    super(name, cu, ASTRewrite.create(node.getAST()), relevance, image);
    fTypeName = typeName;
    fQualifierName = qualifierName;
  }

  public String getQualifiedTypeName() {
    return fQualifierName + '.' + fTypeName;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.ui.text.correction.ChangeCorrectionProposal#performChange(org.eclipse.ui.IEditorPart, org.eclipse.jface.text.IDocument)
   */
  @Override
  protected void performChange(
      /*IEditorPart activeEditor, */ IDocument document) throws CoreException {
    super.performChange(/*activeEditor,*/ document);
    rememberSelection();
  }

  private void rememberSelection() {
    QualifiedTypeNameHistory.remember(getQualifiedTypeName());
  }
}
