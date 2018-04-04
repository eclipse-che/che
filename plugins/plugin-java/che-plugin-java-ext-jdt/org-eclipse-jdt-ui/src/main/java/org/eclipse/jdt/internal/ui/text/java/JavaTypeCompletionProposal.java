/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.text.java;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.text.edits.TextEdit;

/**
 * If passed compilation unit is not null, the replacement string will be seen as a qualified type
 * name.
 */
public class JavaTypeCompletionProposal extends JavaCompletionProposal {

  protected final ICompilationUnit fCompilationUnit;

  /** The unqualified type name. */
  private final String fUnqualifiedTypeName;
  /** The fully qualified type name. */
  private final String fFullyQualifiedTypeName;

  public JavaTypeCompletionProposal(
      String replacementString,
      ICompilationUnit cu,
      int replacementOffset,
      int replacementLength,
      Image image,
      StyledString displayString,
      int relevance) {
    this(
        replacementString,
        cu,
        replacementOffset,
        replacementLength,
        image,
        displayString,
        relevance,
        null);
  }

  public JavaTypeCompletionProposal(
      String replacementString,
      ICompilationUnit cu,
      int replacementOffset,
      int replacementLength,
      Image image,
      StyledString displayString,
      int relevance,
      String fullyQualifiedTypeName) {
    this(
        replacementString,
        cu,
        replacementOffset,
        replacementLength,
        image,
        displayString,
        relevance,
        fullyQualifiedTypeName,
        null);
  }

  public JavaTypeCompletionProposal(
      String replacementString,
      ICompilationUnit cu,
      int replacementOffset,
      int replacementLength,
      Image image,
      StyledString displayString,
      int relevance,
      String fullyQualifiedTypeName,
      JavaContentAssistInvocationContext invocationContext) {
    super(
        replacementString,
        replacementOffset,
        replacementLength,
        image,
        displayString,
        relevance,
        false,
        invocationContext);
    fCompilationUnit = cu;
    fFullyQualifiedTypeName = fullyQualifiedTypeName;
    fUnqualifiedTypeName =
        fullyQualifiedTypeName != null ? Signature.getSimpleName(fullyQualifiedTypeName) : null;
  }

  /**
   * Updates the replacement string.
   *
   * @param document the document
   * @param trigger the trigger
   * @param offset the offset
   * @param impRewrite the import rewrite
   * @return <code>true</code> if the cursor position should be updated, <code>false</code>
   *     otherwise
   * @throws org.eclipse.jface.text.BadLocationException if accessing the document fails
   * @throws org.eclipse.core.runtime.CoreException if something else fails
   */
  protected boolean updateReplacementString(
      IDocument document, char trigger, int offset, ImportRewrite impRewrite)
      throws CoreException, BadLocationException {

    // avoid adding imports when inside imports container
    if (impRewrite != null && fFullyQualifiedTypeName != null) {
      String replacementString = getReplacementString();
      String qualifiedType = fFullyQualifiedTypeName;
      if (qualifiedType.indexOf('.') != -1
          && replacementString.startsWith(qualifiedType)
          && !replacementString.endsWith(String.valueOf(';'))) {
        IType[] types = impRewrite.getCompilationUnit().getTypes();
        if (types.length > 0 && types[0].getSourceRange().getOffset() <= offset) {
          // ignore positions above type.
          setReplacementString(impRewrite.addImport(getReplacementString()));
          return true;
        }
      }
    }
    return false;
  }

  /* (non-Javadoc)
   * @see ICompletionProposalExtension#apply(IDocument, char, int)
   */
  @Override
  public void apply(IDocument document, char trigger, int offset) {
    try {
      ImportRewrite impRewrite = null;

      if (fCompilationUnit != null && allowAddingImports()) {
        impRewrite = StubUtility.createImportRewrite(fCompilationUnit, true);
      }

      boolean updateCursorPosition = updateReplacementString(document, trigger, offset, impRewrite);

      if (updateCursorPosition) setCursorPosition(getReplacementString().length());

      super.apply(document, trigger, offset);

      if (impRewrite != null) {
        int oldLen = document.getLength();
        impRewrite
            .rewriteImports(new NullProgressMonitor())
            .apply(document, TextEdit.UPDATE_REGIONS);
        setReplacementOffset(getReplacementOffset() + document.getLength() - oldLen);
      }
    } catch (CoreException e) {
      JavaPlugin.log(e);
    } catch (BadLocationException e) {
      JavaPlugin.log(e);
    }
  }

  private boolean allowAddingImports() {
    //		IPreferenceStore preferenceStore= JavaPlugin.getDefault().getPreferenceStore();
    //		return preferenceStore.getBoolean(PreferenceConstants.CODEASSIST_ADDIMPORT);
    // todo
    return true;
  }

  /*
   * @see org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal#isValidPrefix(java.lang.String)
   */
  @Override
  protected boolean isValidPrefix(String prefix) {
    return super.isValidPrefix(prefix)
        || isPrefix(prefix, fUnqualifiedTypeName)
        || isPrefix(prefix, fFullyQualifiedTypeName);
  }

  /*
   * @see org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal#getCompletionText()
   */
  @Override
  public CharSequence getPrefixCompletionText(IDocument document, int completionOffset) {
    return fUnqualifiedTypeName;
  }
}
