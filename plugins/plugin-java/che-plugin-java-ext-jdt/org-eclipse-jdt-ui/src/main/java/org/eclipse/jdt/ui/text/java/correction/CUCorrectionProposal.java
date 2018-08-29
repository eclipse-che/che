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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.refactoring.CompilationUnitChange;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.text.correction.proposals.EditAnnotator;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.swt.graphics.Image;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * A proposal for quick fixes and quick assists that work on a single compilation unit. Either a
 * {@link TextChange text change} is directly passed in the constructor or method {@link
 * #addEdits(IDocument, TextEdit)} is overridden to provide the text edits that are applied to the
 * document when the proposal is evaluated.
 *
 * <p>The proposal takes care of the preview of the changes as proposal information.
 *
 * @since 3.8
 */
public class CUCorrectionProposal extends ChangeCorrectionProposal {

  private ICompilationUnit fCompilationUnit;
  private boolean fSwitchedEditor;

  /**
   * Constructs a correction proposal working on a compilation unit with a given text change.
   *
   * @param name the name that is displayed in the proposal selection dialog
   * @param cu the compilation unit to which the change can be applied
   * @param change the change that is executed when the proposal is applied or <code>null</code> if
   *     implementors override {@link #addEdits(IDocument, TextEdit)} to provide the text edits or
   *     {@link #createTextChange()} to provide a text change
   * @param relevance the relevance of this proposal
   * @param image the image that is displayed for this proposal or <code>null</code> if no image is
   *     desired
   */
  public CUCorrectionProposal(
      String name, ICompilationUnit cu, TextChange change, int relevance, Image image) {
    super(name, change, relevance, image);
    if (cu == null) {
      throw new IllegalArgumentException("Compilation unit must not be null"); // $NON-NLS-1$
    }
    fCompilationUnit = cu;
  }

  /**
   * Constructs a correction proposal working on a compilation unit with a given text change. Uses
   * the default image for this proposal.
   *
   * @param name the name that is displayed in the proposal selection dialog
   * @param cu the compilation unit to which the change can be applied
   * @param change the change that is executed when the proposal is applied or <code>null</code> if
   *     implementors override {@link #addEdits(IDocument, TextEdit)} to provide the text edits or
   *     {@link #createTextChange()} to provide a text change.
   * @param relevance the relevance of this proposal
   */
  public CUCorrectionProposal(String name, ICompilationUnit cu, TextChange change, int relevance) {
    this(name, cu, change, relevance, JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE));
  }

  /**
   * Constructs a correction proposal working on a compilation unit.
   *
   * <p>Users have to override {@link #addEdits(IDocument, TextEdit)} to provide the text edits or
   * {@link #createTextChange()} to provide a text change.
   *
   * @param name the name that is displayed in the proposal selection dialog
   * @param cu the compilation unit on that the change works
   * @param relevance the relevance of this proposal
   * @param image the image that is displayed for this proposal or <code>null</code> if no image is
   *     desired
   */
  protected CUCorrectionProposal(String name, ICompilationUnit cu, int relevance, Image image) {
    this(name, cu, null, relevance, image);
  }

  /**
   * Called when the {@link CompilationUnitChange} is initialized. Subclasses can override to add
   * text edits to the root edit of the change. Implementors must not access the proposal, e.g. not
   * call {@link #getChange()}.
   *
   * <p>The default implementation does not add any edits
   *
   * @param document content of the underlying compilation unit. To be accessed read only.
   * @param editRoot The root edit to add all edits to
   * @throws CoreException can be thrown if adding the edits is failing.
   */
  protected void addEdits(IDocument document, TextEdit editRoot) throws CoreException {
    // empty default implementation
  }

  @Override
  public Object getAdditionalProposalInfo(IProgressMonitor monitor) {
    StringBuffer buf = new StringBuffer();
    try {
      TextChange change = getTextChange();
      change.setKeepPreviewEdits(true);
      IDocument previewDocument = change.getPreviewDocument(monitor);
      TextEdit rootEdit = change.getPreviewEdit(change.getEdit());

      EditAnnotator ea = new EditAnnotator(buf, previewDocument);
      rootEdit.accept(ea);
      ea.unchangedUntil(previewDocument.getLength()); // Final pre-existing region
    } catch (CoreException e) {
      JavaPlugin.log(e);
    }
    return buf.toString();
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.text.contentassist.ICompletionProposal#apply(org.eclipse.jface.text.IDocument)
   */
  @Override
  public void apply(IDocument document) {
    try {
      ICompilationUnit unit = getCompilationUnit();
      //			IEditorPart part= null;
      if (unit.getResource().exists()) {
        boolean canEdit = performValidateEdit(unit);
        if (!canEdit) {
          return;
        }
        //				part= EditorUtility.isOpenInEditor(unit);
        //				if (part == null) {
        //					part= JavaUI.openInEditor(unit);
        //					if (part != null) {
        //						fSwitchedEditor= true;
        //						document= JavaUI.getDocumentProvider().getDocument(part.getEditorInput());
        //					}
        //				}
        //				IWorkbenchPage page= JavaPlugin.getActivePage();
        //				if (page != null && part != null) {
        //					page.bringToTop(part);
        //				}
        //				if (part != null) {
        //					part.setFocus();
        //				}
      }
      performChange(/*part, */ document);
    } catch (CoreException e) {
      //			ExceptionHandler.handle(e, CorrectionMessages.CUCorrectionProposal_error_title,
      // CorrectionMessages.CUCorrectionProposal_error_message);
      JavaPlugin.log(e);
    }
  }

  private boolean performValidateEdit(ICompilationUnit unit) {
    //		IStatus status= Resources.makeCommittable(unit.getResource(),
    // JavaPlugin.getActiveWorkbenchShell());
    //		if (!status.isOK()) {
    //			String label= CorrectionMessages.CUCorrectionProposal_error_title;
    //			String message= CorrectionMessages.CUCorrectionProposal_error_message;
    //			ErrorDialog.openError(JavaPlugin.getActiveWorkbenchShell(), label, message, status);
    //			return false;
    //		}
    return true;
  }

  /**
   * Creates the text change for this proposal. This method is only called once and only when no
   * text change has been passed in {@link #CUCorrectionProposal(String, ICompilationUnit,
   * TextChange, int, Image)}.
   *
   * @return the created text change
   * @throws CoreException if the creation of the text change failed
   */
  protected TextChange createTextChange() throws CoreException {
    ICompilationUnit cu = getCompilationUnit();
    String name = getName();
    TextChange change;
    if (!cu.getResource().exists()) {
      String source;
      try {
        source = cu.getSource();
      } catch (JavaModelException e) {
        JavaPlugin.log(e);
        source = new String(); // empty
      }
      Document document = new Document(source);
      document.setInitialLineDelimiter(StubUtility.getLineDelimiterUsed(cu));
      change = new DocumentChange(name, document);
    } else {
      CompilationUnitChange cuChange = new CompilationUnitChange(name, cu);
      cuChange.setSaveMode(TextFileChange.LEAVE_DIRTY);
      change = cuChange;
    }
    TextEdit rootEdit = new MultiTextEdit();
    change.setEdit(rootEdit);

    // initialize text change
    IDocument document = change.getCurrentDocument(new NullProgressMonitor());
    addEdits(document, rootEdit);
    return change;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.ui.text.correction.ChangeCorrectionProposal#createChange()
   */
  @Override
  protected final Change createChange() throws CoreException {
    return createTextChange(); // make sure that only text changes are allowed here
  }

  /**
   * Returns the text change that is invoked when the change is applied.
   *
   * @return the text change that is invoked when the change is applied
   * @throws CoreException if accessing the change failed
   */
  public final TextChange getTextChange() throws CoreException {
    return (TextChange) getChange();
  }

  /**
   * The compilation unit on which the change works.
   *
   * @return the compilation unit on which the change works
   */
  public final ICompilationUnit getCompilationUnit() {
    return fCompilationUnit;
  }

  /**
   * Creates a preview of the content of the compilation unit after applying the change.
   *
   * @return the preview of the changed compilation unit
   * @throws CoreException if the creation of the change failed
   * @noreference This method is not intended to be referenced by clients.
   */
  public String getPreviewContent() throws CoreException {
    return getTextChange().getPreviewContent(new NullProgressMonitor());
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    try {
      return getPreviewContent();
    } catch (CoreException e) {
      // didn't work out
    }
    return super.toString();
  }

  /**
   * Returns whether the changed compilation unit was not previously open in an editor.
   *
   * @return <code>true</code> if the changed compilation unit was not previously open in an editor,
   *     <code>false</code> if the changed compilation unit was already open in an editor
   * @noreference This method is not intended to be referenced by clients.
   */
  protected boolean didOpenEditor() {
    return fSwitchedEditor;
  }
}
