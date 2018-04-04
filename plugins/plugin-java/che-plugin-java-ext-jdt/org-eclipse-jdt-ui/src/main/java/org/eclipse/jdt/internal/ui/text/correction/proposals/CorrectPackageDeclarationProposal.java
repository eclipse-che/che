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
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.text.correction.CorrectionMessages;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.correction.CUCorrectionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

public class CorrectPackageDeclarationProposal extends CUCorrectionProposal {

  private IProblemLocation fLocation;

  public CorrectPackageDeclarationProposal(
      ICompilationUnit cu, IProblemLocation location, int relevance) {
    super(
        CorrectionMessages.CorrectPackageDeclarationProposal_name,
        cu,
        relevance,
        JavaPluginImages.get(JavaPluginImages.IMG_OBJS_PACKDECL));
    fLocation = location;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.ui.text.correction.CUCorrectionProposal#addEdits(org.eclipse.jdt.internal.corext.textmanipulation
   * .TextBuffer)
   */
  @Override
  protected void addEdits(IDocument doc, TextEdit root) throws CoreException {
    super.addEdits(doc, root);

    ICompilationUnit cu = getCompilationUnit();

    IPackageFragment parentPack = (IPackageFragment) cu.getParent();
    IPackageDeclaration[] decls = cu.getPackageDeclarations();

    if (parentPack.isDefaultPackage() && decls.length > 0) {
      for (int i = 0; i < decls.length; i++) {
        ISourceRange range = decls[i].getSourceRange();
        root.addChild(new DeleteEdit(range.getOffset(), range.getLength()));
      }
      return;
    }
    if (!parentPack.isDefaultPackage() && decls.length == 0) {
      String lineDelim = StubUtility.getLineDelimiterUsed(cu);
      String str =
          "package " + parentPack.getElementName() + ';' + lineDelim + lineDelim; // $NON-NLS-1$
      root.addChild(new InsertEdit(0, str));
      return;
    }

    root.addChild(
        new ReplaceEdit(fLocation.getOffset(), fLocation.getLength(), parentPack.getElementName()));
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.ui.text.correction.proposals.ChangeCorrectionProposal#getName()
   */
  @Override
  public String getName() {
    ICompilationUnit cu = getCompilationUnit();
    IPackageFragment parentPack = (IPackageFragment) cu.getParent();
    try {
      IPackageDeclaration[] decls = cu.getPackageDeclarations();
      if (parentPack.isDefaultPackage() && decls.length > 0) {
        return Messages.format(
            CorrectionMessages.CorrectPackageDeclarationProposal_remove_description,
            BasicElementLabels.getJavaElementName(decls[0].getElementName()));
      }
      if (!parentPack.isDefaultPackage() && decls.length == 0) {
        return (Messages.format(
            CorrectionMessages.CorrectPackageDeclarationProposal_add_description,
            JavaElementLabels.getElementLabel(parentPack, JavaElementLabels.ALL_DEFAULT)));
      }
    } catch (JavaModelException e) {
      JavaPlugin.log(e);
    }
    return (Messages.format(
        CorrectionMessages.CorrectPackageDeclarationProposal_change_description,
        JavaElementLabels.getElementLabel(parentPack, JavaElementLabels.ALL_DEFAULT)));
  }
}
