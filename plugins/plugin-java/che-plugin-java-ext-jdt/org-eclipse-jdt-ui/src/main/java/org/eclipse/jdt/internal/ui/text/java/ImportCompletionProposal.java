/**
 * ***************************************************************************** Copyright (c) 2007,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation Nathan Beyer (Cerner)
 * <nbeyer@cerner.com> - [content assist][5.0] when selected method from favorites is a member of a
 * type with a type variable an invalid static import is added -
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=202221
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.text.java;

import org.eclipse.che.jdt.util.JavaModelUtil;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.internal.corext.codemanipulation.ContextSensitiveImportRewriteContext;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.SharedASTProvider;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;

/**
 * Completion proposal for required imports.
 *
 * @since 3.3
 */
public class ImportCompletionProposal extends AbstractJavaCompletionProposal {

  private final ICompilationUnit fCompilationUnit;
  private final int fParentProposalKind;
  private ImportRewrite fImportRewrite;
  private ContextSensitiveImportRewriteContext fImportContext;
  private final CompletionProposal fProposal;
  private boolean fReplacementStringComputed;

  public ImportCompletionProposal(
      CompletionProposal proposal,
      JavaContentAssistInvocationContext context,
      int parentProposalKind) {
    super(context);
    fProposal = proposal;
    fParentProposalKind = parentProposalKind;
    fCompilationUnit = context.getCompilationUnit();
  }

  /*
   * @see org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal#getReplacementString()
   */
  @Override
  public final String getReplacementString() {
    if (!fReplacementStringComputed) setReplacementString(computeReplacementString());
    return super.getReplacementString();
  }

  /**
   * Computes the replacement string.
   *
   * @return the replacement string
   */
  private String computeReplacementString() {
    int proposalKind = fProposal.getKind();
    String qualifiedTypeName = null;
    char[] qualifiedType = null;
    if (proposalKind == CompletionProposal.TYPE_IMPORT) {
      qualifiedType = fProposal.getSignature();
      qualifiedTypeName = String.valueOf(Signature.toCharArray(qualifiedType));
    } else if (proposalKind == CompletionProposal.METHOD_IMPORT
        || proposalKind == CompletionProposal.FIELD_IMPORT) {
      qualifiedType = Signature.getTypeErasure(fProposal.getDeclarationSignature());
      qualifiedTypeName = String.valueOf(Signature.toCharArray(qualifiedType));
    } else {
      /*
       * In 3.3 we only support the above import proposals, see
       * CompletionProposal#getRequiredProposals()
       */
      Assert.isTrue(false);
    }

    /* Add imports if the preference is on. */
    fImportRewrite = createImportRewrite();
    if (fImportRewrite != null) {
      if (proposalKind == CompletionProposal.TYPE_IMPORT) {
        String simpleType = fImportRewrite.addImport(qualifiedTypeName, fImportContext);
        if (fParentProposalKind == CompletionProposal.METHOD_REF)
          return simpleType + "."; // $NON-NLS-1$
      } else {
        String res =
            fImportRewrite.addStaticImport(
                qualifiedTypeName,
                String.valueOf(fProposal.getName()),
                proposalKind == CompletionProposal.FIELD_IMPORT,
                fImportContext);
        int dot = res.lastIndexOf('.');
        if (dot != -1) {
          String typeName = fImportRewrite.addImport(res.substring(0, dot), fImportContext);
          return typeName + '.';
        }
      }
      return ""; // $NON-NLS-1$
    }

    // Case where we don't have an import rewrite (see allowAddingImports)

    if (fCompilationUnit != null
        && JavaModelUtil.isImplicitImport(
            Signature.getQualifier(qualifiedTypeName), fCompilationUnit)) {
      /* No imports for implicit imports. */

      if (fProposal.getKind() == CompletionProposal.TYPE_IMPORT
          && fParentProposalKind == CompletionProposal.FIELD_REF) return ""; // $NON-NLS-1$
      qualifiedTypeName = String.valueOf(Signature.getSignatureSimpleName(qualifiedType));
    }

    return qualifiedTypeName + "."; // $NON-NLS-1$
  }

  /*
   * @see org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal#apply(org.eclipse.jface.text.IDocument, char, int)
   */
  @Override
  public void apply(IDocument document, char trigger, int offset) {
    try {
      super.apply(document, trigger, offset);

      if (fImportRewrite != null && fImportRewrite.hasRecordedChanges()) {
        int oldLen = document.getLength();
        fImportRewrite
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

  /**
   * Creates and returns the import rewrite if imports should be added at all.
   *
   * @return the import rewrite or <code>null</code> if no imports can or should be added
   */
  private ImportRewrite createImportRewrite() {
    if (fCompilationUnit != null && shouldAddImports()) {
      try {
        CompilationUnit cu = getASTRoot(fCompilationUnit);
        if (cu == null) {
          ImportRewrite rewrite = StubUtility.createImportRewrite(fCompilationUnit, true);
          fImportContext = null;
          return rewrite;
        } else {
          ImportRewrite rewrite = StubUtility.createImportRewrite(cu, true);
          fImportContext =
              new ContextSensitiveImportRewriteContext(
                  cu, fInvocationContext.getInvocationOffset(), rewrite);
          return rewrite;
        }
      } catch (CoreException x) {
        JavaPlugin.log(x);
      }
    }
    return null;
  }

  private CompilationUnit getASTRoot(ICompilationUnit compilationUnit) {
    return SharedASTProvider.getAST(compilationUnit, SharedASTProvider.WAIT_NO, null);
  }

  /**
   * Returns <code>true</code> if imports should be added. The return value depends on the context
   * and preferences only and does not take into account the contents of the compilation unit or the
   * kind of proposal. Even if <code>true</code> is returned, there may be cases where no imports
   * are added for the proposal. For example:
   *
   * <ul>
   *   <li>when completing within the import section
   *   <li>when completing informal javadoc references (e.g. within <code>&lt;code&gt;</code> tags)
   *   <li>when completing a type that conflicts with an existing import
   *   <li>when completing an implicitly imported type (same package, <code>java.lang</code> types)
   * </ul>
   *
   * <p>The decision whether a qualified type or the simple type name should be inserted must take
   * into account these different scenarios.
   *
   * @return <code>true</code> if imports may be added, <code>false</code> if not
   */
  private boolean shouldAddImports() {
    if (isInJavadoc() && !isJavadocProcessingEnabled()) return false;

    //		IPreferenceStore preferenceStore= JavaPlugin.getDefault().getPreferenceStore();
    return true; // preferenceStore.getBoolean(PreferenceConstants.CODEASSIST_ADDIMPORT);
  }

  /**
   * Returns whether Javadoc processing is enabled.
   *
   * @return <code>true</code> if Javadoc processing is enabled, <code>false</code> otherwise
   */
  private boolean isJavadocProcessingEnabled() {
    IJavaProject project = fCompilationUnit.getJavaProject();
    boolean processJavadoc;
    if (project == null)
      processJavadoc =
          JavaCore.ENABLED.equals(JavaCore.getOption(JavaCore.COMPILER_DOC_COMMENT_SUPPORT));
    else
      processJavadoc =
          JavaCore.ENABLED.equals(project.getOption(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, true));
    return processJavadoc;
  }
}
