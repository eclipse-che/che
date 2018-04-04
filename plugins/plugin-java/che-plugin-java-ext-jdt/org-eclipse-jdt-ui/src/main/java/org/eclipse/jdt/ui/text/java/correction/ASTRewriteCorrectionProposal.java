/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2012 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.ui.text.java.correction;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.JavaUIStatus;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Image;
import org.eclipse.text.edits.TextEdit;

/**
 * A proposal for quick fixes and quick assists that works on an AST rewrite. Either a rewrite is
 * directly passed in the constructor or the method {@link #getRewrite()} is overridden to provide
 * the AST rewrite that is evaluated on the document when the proposal is applied.
 *
 * @since 3.8
 */
public class ASTRewriteCorrectionProposal extends CUCorrectionProposal {

  private ASTRewrite fRewrite;
  private ImportRewrite fImportRewrite;

  /**
   * Constructs an AST rewrite correction proposal.
   *
   * @param name the display name of the proposal
   * @param cu the compilation unit that is modified
   * @param rewrite the AST rewrite that is invoked when the proposal is applied or <code>null
   *     </code> if {@link #getRewrite()} is overridden
   * @param relevance the relevance of this proposal
   * @param image the image that is displayed for this proposal or <code>null</code> if no image is
   *     desired
   */
  public ASTRewriteCorrectionProposal(
      String name, ICompilationUnit cu, ASTRewrite rewrite, int relevance, Image image) {
    super(name, cu, relevance, image);
    fRewrite = rewrite;
  }

  /**
   * Constructs an AST rewrite correction proposal. Uses the default image for this proposal.
   *
   * @param name the display name of the proposal
   * @param cu the compilation unit that is modified
   * @param rewrite the AST rewrite that is invoked when the proposal is applied or <code>null
   *     </code> if {@link #getRewrite()} is overridden
   * @param relevance The relevance of this proposal
   */
  public ASTRewriteCorrectionProposal(
      String name, ICompilationUnit cu, ASTRewrite rewrite, int relevance) {
    this(
        name, cu, rewrite, relevance, JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE));
  }

  /**
   * Returns the import rewrite used for this compilation unit. <code>
   *
   * @return the import rewrite or <code>null</code> if no import rewrite has been set
   * @nooverride This method is not intended to be re-implemented or extended by clients.
   */
  public ImportRewrite getImportRewrite() {
    return fImportRewrite;
  }

  /**
   * Sets the import rewrite used for this compilation unit.
   *
   * @param rewrite the import rewrite
   * @nooverride This method is not intended to be re-implemented or extended by clients.
   */
  public void setImportRewrite(ImportRewrite rewrite) {
    fImportRewrite = rewrite;
  }

  /**
   * Creates and sets the import rewrite used for this compilation unit.
   *
   * @param astRoot the AST for the current CU
   * @return the created import rewrite
   * @nooverride This method is not intended to be re-implemented or extended by clients.
   */
  public ImportRewrite createImportRewrite(CompilationUnit astRoot) {
    fImportRewrite = StubUtility.createImportRewrite(astRoot, true);
    return fImportRewrite;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.ui.text.correction.CUCorrectionProposal#addEdits(org.eclipse.jface.text.IDocument)
   */
  @Override
  protected void addEdits(IDocument document, TextEdit editRoot) throws CoreException {
    super.addEdits(document, editRoot);
    ASTRewrite rewrite = getRewrite();
    if (rewrite != null) {
      try {
        TextEdit edit = rewrite.rewriteAST();
        editRoot.addChild(edit);
      } catch (IllegalArgumentException e) {
        throw new CoreException(JavaUIStatus.createError(IStatus.ERROR, e));
      }
    }
    if (fImportRewrite != null) {
      editRoot.addChild(fImportRewrite.rewriteImports(new NullProgressMonitor()));
    }
  }

  /**
   * Returns the rewrite that has been passed in the constructor. Implementors can override this
   * method to create the rewrite lazily. This method will only be called once.
   *
   * @return the rewrite to be used
   * @throws CoreException when the rewrite could not be created
   */
  protected ASTRewrite getRewrite() throws CoreException {
    if (fRewrite == null) {
      IStatus status =
          JavaUIStatus.createError(IStatus.ERROR, "Rewrite not initialized", null); // $NON-NLS-1$
      throw new CoreException(status);
    }
    return fRewrite;
  }
}
