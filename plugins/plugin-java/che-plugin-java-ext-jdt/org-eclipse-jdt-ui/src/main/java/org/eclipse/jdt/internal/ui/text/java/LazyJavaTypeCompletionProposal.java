/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2012 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.text.java;

import org.eclipse.che.jdt.util.JavaModelUtil;
import org.eclipse.che.jface.text.contentassist.IContextInformation;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.internal.corext.codemanipulation.ContextSensitiveImportRewriteContext;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.util.QualifiedTypeNameHistory;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.SharedASTProvider;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;

/**
 * If passed compilation unit is not null, the replacement string will be seen as a qualified type
 * name.
 */
public class LazyJavaTypeCompletionProposal extends LazyJavaCompletionProposal {
  /** Triggers for types. Do not modify. */
  protected static final char[] TYPE_TRIGGERS = new char[] {'.', '\t', '[', '(', ' '};
  /** Triggers for types in javadoc. Do not modify. */
  protected static final char[] JDOC_TYPE_TRIGGERS = new char[] {'#', '}', ' ', '.'};

  /** The compilation unit, or <code>null</code> if none is available. */
  protected final ICompilationUnit fCompilationUnit;

  private String fQualifiedName;
  private String fSimpleName;
  private ImportRewrite fImportRewrite;
  private ContextSensitiveImportRewriteContext fImportContext;

  public LazyJavaTypeCompletionProposal(
      CompletionProposal proposal, JavaContentAssistInvocationContext context) {
    super(proposal, context);
    fCompilationUnit = context.getCompilationUnit();
    fQualifiedName = null;
  }

  void setImportRewrite(ImportRewrite importRewrite) {
    fImportRewrite = importRewrite;
  }

  public final String getQualifiedTypeName() {
    if (fQualifiedName == null)
      fQualifiedName =
          String.valueOf(Signature.toCharArray(Signature.getTypeErasure(fProposal.getSignature())));
    return fQualifiedName;
  }

  protected final String getSimpleTypeName() {
    if (fSimpleName == null) fSimpleName = Signature.getSimpleName(getQualifiedTypeName());
    return fSimpleName;
  }

  /*
   * @see org.eclipse.jdt.internal.ui.text.java.LazyJavaCompletionProposal#computeReplacementString()
   */
  @Override
  protected String computeReplacementString() {
    String replacement = super.computeReplacementString();

    /* No import rewriting ever from within the import section. */
    if (isImportCompletion()) return replacement;

    /* Always use the simple name for non-formal javadoc references to types. */
    // TODO fix
    if (fProposal.getKind() == CompletionProposal.TYPE_REF
        && fInvocationContext.getCoreContext().isInJavadocText()) return getSimpleTypeName();

    String qualifiedTypeName = getQualifiedTypeName();

    // Type in package info must be fully qualified.
    if (fCompilationUnit != null && JavaModelUtil.isPackageInfo(fCompilationUnit))
      return qualifiedTypeName;

    if (qualifiedTypeName.indexOf('.') == -1 && replacement.length() > 0)
      // default package - no imports needed
      return qualifiedTypeName;

    /*
     * If the user types in the qualification, don't force import rewriting on him - insert the
     * qualified name.
     */
    IDocument document = fInvocationContext.getDocument();
    if (document != null) {
      String prefix = getPrefix(document, getReplacementOffset() + getReplacementLength());
      int dotIndex = prefix.lastIndexOf('.');
      // match up to the last dot in order to make higher level matching still work (camel case...)
      if (dotIndex != -1
          && qualifiedTypeName
              .toLowerCase()
              .startsWith(prefix.substring(0, dotIndex + 1).toLowerCase()))
        return qualifiedTypeName;
    }

    /*
     * The replacement does not contain a qualification (e.g. an inner type qualified by its
     * parent) - use the replacement directly.
     */
    if (replacement.indexOf('.') == -1) {
      if (isInJavadoc())
        return getSimpleTypeName(); // don't use the braces added for javadoc link proposals
      return replacement;
    }

    /* Add imports if the preference is on. */
    if (fImportRewrite == null) fImportRewrite = createImportRewrite();
    if (fImportRewrite != null) {
      return fImportRewrite.addImport(qualifiedTypeName, fImportContext);
    }

    // fall back for the case we don't have an import rewrite (see allowAddingImports)

    /* No imports for implicit imports. */
    if (fCompilationUnit != null
        && JavaModelUtil.isImplicitImport(
            Signature.getQualifier(qualifiedTypeName), fCompilationUnit)) {
      return Signature.getSimpleName(qualifiedTypeName);
    }

    /* Default: use the fully qualified type name. */
    return qualifiedTypeName;
  }

  protected final boolean isImportCompletion() {
    char[] completion = fProposal.getCompletion();
    if (completion.length == 0) return false;

    char last = completion[completion.length - 1];
    /*
     * Proposals end in a semicolon when completing types in normal imports or when completing
     * static members, in a period when completing types in static imports.
     */
    return last == ';' || last == '.';
  }

  private ImportRewrite createImportRewrite() {
    if (fCompilationUnit != null
        && allowAddingImports()
        && !JavaModelUtil.isPackageInfo(fCompilationUnit)) {
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
    return SharedASTProvider.getAST(
        compilationUnit, SharedASTProvider.WAIT_NO, new NullProgressMonitor());
  }

  /*
   * @see org.eclipse.jdt.internal.ui.text.java.LazyJavaCompletionProposal#apply(org.eclipse.jface.text.IDocument, char, int)
   */
  @Override
  public void apply(IDocument document, char trigger, int offset) {
    try {
      boolean insertClosingParenthesis = trigger == '(' && autocloseBrackets();
      if (insertClosingParenthesis) {
        StringBuffer replacement = new StringBuffer(getReplacementString());
        updateReplacementWithParentheses(replacement);
        setReplacementString(replacement.toString());
        trigger = '\0';
      }

      super.apply(document, trigger, offset);

      if (fImportRewrite != null && fImportRewrite.hasRecordedChanges()) {
        int oldLen = document.getLength();
        fImportRewrite
            .rewriteImports(new NullProgressMonitor())
            .apply(document, TextEdit.UPDATE_REGIONS);
        setReplacementOffset(getReplacementOffset() + document.getLength() - oldLen);
      }

      //			if (insertClosingParenthesis)
      //				setUpLinkedMode(document, ')');

      rememberSelection();
    } catch (CoreException e) {
      JavaPlugin.log(e);
    } catch (BadLocationException e) {
      JavaPlugin.log(e);
    }
  }

  protected void updateReplacementWithParentheses(StringBuffer replacement) {
    FormatterPrefs prefs = getFormatterPrefs();

    if (prefs.beforeOpeningParen) replacement.append(SPACE);
    replacement.append(LPAREN);

    if (prefs.afterOpeningParen) replacement.append(SPACE);

    setCursorPosition(replacement.length());

    if (prefs.afterOpeningParen) replacement.append(SPACE);

    replacement.append(RPAREN);
  }

  /**
   * Remembers the selection in the content assist history.
   *
   * @throws org.eclipse.jdt.core.JavaModelException if anything goes wrong
   * @since 3.2
   */
  protected final void rememberSelection() throws JavaModelException {
    IType lhs = fInvocationContext.getExpectedType();
    IType rhs = (IType) getJavaElement();
    if (lhs != null && rhs != null)
      JavaPlugin.getDefault().getContentAssistHistory().remember(lhs, rhs);

    QualifiedTypeNameHistory.remember(getQualifiedTypeName());
  }

  /**
   * Returns <code>true</code> if imports may be added. The return value depends on the context and
   * preferences only and does not take into account the contents of the compilation unit or the
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
   * <p>Subclasses may extend.
   *
   * @return <code>true</code> if imports may be added, <code>false</code> if not
   */
  protected boolean allowAddingImports() {
    if (isInJavadoc()) {
      // TODO fix
      //			if (!fContext.isInJavadocFormalReference())
      //				return false;
      if (fProposal.getKind() == CompletionProposal.TYPE_REF
          && fInvocationContext.getCoreContext().isInJavadocText()) return false;

      if (!isJavadocProcessingEnabled()) return false;
    }

    //		IPreferenceStore preferenceStore= JavaPlugin.getDefault().getPreferenceStore();
    //		return preferenceStore.getBoolean(PreferenceConstants.CODEASSIST_ADDIMPORT);
    return true;
  }

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

  /*
   * @see org.eclipse.jdt.internal.ui.text.java.LazyJavaCompletionProposal#isValidPrefix(java.lang.String)
   */
  @Override
  protected boolean isValidPrefix(String prefix) {
    return isPrefix(prefix, getSimpleTypeName()) || isPrefix(prefix, getQualifiedTypeName());
  }

  /*
   * @see org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal#getCompletionText()
   */
  @Override
  public CharSequence getPrefixCompletionText(IDocument document, int completionOffset) {
    String prefix = getPrefix(document, completionOffset);

    String completion;
    // return the qualified name if the prefix is already qualified
    if (prefix.indexOf('.') != -1) completion = getQualifiedTypeName();
    else completion = getSimpleTypeName();

    if (isCamelCaseMatching()) return getCamelCaseCompound(prefix, completion);

    return completion;
  }

  /*
   * @see org.eclipse.jdt.internal.ui.text.java.LazyJavaCompletionProposal#computeTriggerCharacters()
   */
  @Override
  protected char[] computeTriggerCharacters() {
    return isInJavadoc() ? JDOC_TYPE_TRIGGERS : TYPE_TRIGGERS;
  }

  /*
   * @see org.eclipse.jdt.internal.ui.text.java.LazyJavaCompletionProposal#computeProposalInfo()
   */
  @Override
  protected ProposalInfo computeProposalInfo() {
    IJavaProject project;
    if (fCompilationUnit != null) project = fCompilationUnit.getJavaProject();
    else project = fInvocationContext.getProject();
    if (project != null) return new TypeProposalInfo(project, fProposal);

    return super.computeProposalInfo();
  }

  /*
   * @see org.eclipse.jdt.internal.ui.text.java.LazyJavaCompletionProposal#computeSortString()
   */
  @Override
  protected String computeSortString() {
    // try fast sort string to avoid display string creation
    return getSimpleTypeName() + Character.MIN_VALUE + getQualifiedTypeName();
  }

  /*
   * @see org.eclipse.jdt.internal.ui.text.java.LazyJavaCompletionProposal#computeRelevance()
   */
  @Override
  protected int computeRelevance() {
    /*
     * There are two histories: the RHS history remembers types used for the current expected
     * type (left hand side), while the type history remembers recently used types in general).
     *
     * The presence of an RHS ranking is a much more precise sign for relevance as it proves the
     * subtype relationship between the proposed type and the expected type.
     *
     * The "recently used" factor (of either the RHS or general history) is less important, it should
     * not override other relevance factors such as if the type is already imported etc.
     */
    float rhsHistoryRank = fInvocationContext.getHistoryRelevance(getQualifiedTypeName());
    float typeHistoryRank =
        QualifiedTypeNameHistory.getDefault().getNormalizedPosition(getQualifiedTypeName());

    int recencyBoost = Math.round((rhsHistoryRank + typeHistoryRank) * 5);
    int rhsBoost = rhsHistoryRank > 0.0f ? 50 : 0;
    int baseRelevance = super.computeRelevance();

    return baseRelevance + rhsBoost + recencyBoost;
  }

  /*
   * @see org.eclipse.jdt.internal.ui.text.java.LazyJavaCompletionProposal#computeContextInformation()
   * @since 3.3
   */
  @Override
  protected IContextInformation computeContextInformation() {
    char[] signature = fProposal.getSignature();
    char[][] typeParameters = Signature.getTypeArguments(signature);
    if (typeParameters.length == 0) return super.computeContextInformation();

    ProposalContextInformation contextInformation = new ProposalContextInformation(fProposal);
    if (fContextInformationPosition != 0 && fProposal.getCompletion().length == 0)
      contextInformation.setContextInformationPosition(fContextInformationPosition);
    return contextInformation;
  }
}
