/**
 * ***************************************************************************** Copyright (c) 2005,
 * 2013 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation Tom Eicher
 * <eclipse@tom.eicher.name> - [content assist] prefix complete casted method proposals -
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=247547
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.text.java;

import org.eclipse.che.jface.text.contentassist.IContextInformation;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.internal.corext.util.CodeFormatterUtil;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.osgi.util.TextProcessor;

public class JavaMethodCompletionProposal extends LazyJavaCompletionProposal {
  /** Triggers for method proposals without parameters. Do not modify. */
  protected static final char[] METHOD_TRIGGERS = new char[] {';', ',', '.', '\t', '['};
  /** Triggers for method proposals. Do not modify. */
  protected static final char[] METHOD_WITH_ARGUMENTS_TRIGGERS = new char[] {';', '(', '-', ' '};
  /** Triggers for method name proposals (static imports). Do not modify. */
  protected static final char[] METHOD_NAME_TRIGGERS = new char[] {';'};

  private boolean fHasParameters;
  private boolean fHasParametersComputed = false;
  private FormatterPrefs fFormatterPrefs;

  public JavaMethodCompletionProposal(
      CompletionProposal proposal, JavaContentAssistInvocationContext context) {
    super(proposal, context);
  }

  @Override
  public void apply(IDocument document, char trigger, int offset) {
    if (trigger == ' ' || trigger == '(') trigger = '\0';
    super.apply(document, trigger, offset);
    if (needsLinkedMode()) {
      //			setUpLinkedMode(document, ')');
    } else if (!fProposal.isConstructor() && getReplacementString().endsWith(";")) { // $NON-NLS-1$
      //			setUpLinkedMode(document, ';');
    }
  }

  protected boolean needsLinkedMode() {
    return hasArgumentList() && hasParameters();
  }

  @Override
  public int getPrefixCompletionStart(IDocument document, int completionOffset) {
    if (fProposal.getKind() == CompletionProposal.METHOD_REF_WITH_CASTED_RECEIVER) {
      return fProposal.getTokenStart();
    } else if (fProposal.getKind() == CompletionProposal.CONSTRUCTOR_INVOCATION)
      return fProposal.getRequiredProposals()[0].getReplaceStart();
    return super.getPrefixCompletionStart(document, completionOffset);
  }

  @Override
  public CharSequence getPrefixCompletionText(IDocument document, int completionOffset) {
    if (hasArgumentList() || fProposal.getKind() == CompletionProposal.CONSTRUCTOR_INVOCATION) {
      String completion = String.valueOf(fProposal.getName());
      if (isCamelCaseMatching()) {
        String prefix = getPrefix(document, completionOffset);
        return getCamelCaseCompound(prefix, completion);
      }
      return completion;
    }
    return super.getPrefixCompletionText(document, completionOffset);
  }

  @Override
  protected IContextInformation computeContextInformation() {
    // no context information for METHOD_NAME_REF proposals (e.g. for static imports)
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=94654
    if ((fProposal.getKind() == CompletionProposal.METHOD_REF
            || fProposal.getKind() == CompletionProposal.CONSTRUCTOR_INVOCATION)
        && hasParameters()
        && (getReplacementString().endsWith(RPAREN)
            || getReplacementString().endsWith(SEMICOLON)
            || getReplacementString().length() == 0)) {
      ProposalContextInformation contextInformation = new ProposalContextInformation(fProposal);
      if (fContextInformationPosition != 0 && fProposal.getCompletion().length == 0)
        contextInformation.setContextInformationPosition(fContextInformationPosition);
      return contextInformation;
    }
    return super.computeContextInformation();
  }

  @Override
  protected char[] computeTriggerCharacters() {
    if (fProposal.getKind() == CompletionProposal.METHOD_NAME_REFERENCE)
      return METHOD_NAME_TRIGGERS;
    if (hasParameters()) return METHOD_WITH_ARGUMENTS_TRIGGERS;
    return METHOD_TRIGGERS;
  }

  /**
   * Returns <code>true</code> if the method being inserted has at least one parameter. Note that
   * this does not say anything about whether the argument list should be inserted. This depends on
   * the position in the document and the kind of proposal; see {@link #hasArgumentList() }.
   *
   * @return <code>true</code> if the method has any parameters, <code>false</code> if it has no
   *     parameters
   */
  protected final boolean hasParameters() {
    if (!fHasParametersComputed) {
      fHasParametersComputed = true;
      fHasParameters = computeHasParameters();
    }
    return fHasParameters;
  }

  /**
   * Returns whether we automatically complete the method with a semicolon.
   *
   * @return <code>true</code> if the return type of the method is void, <code>false</code>
   *     otherwise
   * @since 3.9
   */
  protected final boolean canAutomaticallyAppendSemicolon() {
    return !fProposal.isConstructor()
        && CharOperation.equals(
            new char[] {Signature.C_VOID}, Signature.getReturnType(fProposal.getSignature()));
  }

  private boolean computeHasParameters() throws IllegalArgumentException {
    return Signature.getParameterCount(fProposal.getSignature()) > 0;
  }

  /**
   * Returns <code>true</code> if the argument list should be inserted by the proposal, <code>false
   * </code> if not.
   *
   * @return <code>true</code> when the proposal is not in javadoc nor within an import and
   *     comprises the parameter list
   */
  protected boolean hasArgumentList() {
    if (CompletionProposal.METHOD_NAME_REFERENCE == fProposal.getKind()) return false;
    //		IPreferenceStore preferenceStore= JavaPlugin.getDefault().getPreferenceStore();
    boolean
        noOverwrite = /*preferenceStore.getBoolean(PreferenceConstants.CODEASSIST_INSERT_COMPLETION)*/
            true ^ isToggleEating();
    char[] completion = fProposal.getCompletion();
    return !isInJavadoc()
        && completion.length > 0
        && (noOverwrite || completion[completion.length - 1] == ')');
  }

  /**
   * Returns the method formatter preferences.
   *
   * @return the formatter settings
   */
  @Override
  protected final FormatterPrefs getFormatterPrefs() {
    if (fFormatterPrefs == null)
      fFormatterPrefs = new FormatterPrefs(fInvocationContext.getProject());
    return fFormatterPrefs;
  }

  /*
   * @see org.eclipse.jdt.internal.ui.text.java.LazyJavaCompletionProposal#computeReplacementString()
   */
  @Override
  protected String computeReplacementString() {
    if (!hasArgumentList()) return super.computeReplacementString();

    // we're inserting a method plus the argument list - respect formatter preferences
    StringBuffer buffer = new StringBuffer();
    appendMethodNameReplacement(buffer);

    FormatterPrefs prefs = getFormatterPrefs();

    if (hasParameters()) {
      setCursorPosition(buffer.length());

      if (prefs.afterOpeningParen) buffer.append(SPACE);

      // don't add the trailing space, but let the user type it in himself - typing the closing
      // paren will exit
      //			if (prefs.beforeClosingParen)
      //				buffer.append(SPACE);
    } else {
      if (prefs.inEmptyList) buffer.append(SPACE);
    }

    buffer.append(RPAREN);

    if (canAutomaticallyAppendSemicolon()) buffer.append(SEMICOLON);

    return buffer.toString();
  }

  /**
   * Appends everything up to the method name including the opening parenthesis.
   *
   * <p>In case of {@link org.eclipse.jdt.core.CompletionProposal#METHOD_REF_WITH_CASTED_RECEIVER}
   * it add cast.
   *
   * @param buffer the string buffer
   * @since 3.4
   */
  protected void appendMethodNameReplacement(StringBuffer buffer) {
    if (fProposal.getKind() == CompletionProposal.METHOD_REF_WITH_CASTED_RECEIVER) {
      String coreCompletion = String.valueOf(fProposal.getCompletion());
      String lineDelimiter = TextUtilities.getDefaultLineDelimiter(getTextViewer().getDocument());
      String replacement =
          CodeFormatterUtil.format(
              CodeFormatter.K_EXPRESSION,
              coreCompletion,
              0,
              lineDelimiter,
              fInvocationContext.getProject());
      buffer.append(replacement.substring(0, replacement.lastIndexOf('.') + 1));
    }

    if (fProposal.getKind() != CompletionProposal.CONSTRUCTOR_INVOCATION)
      buffer.append(fProposal.getName());

    FormatterPrefs prefs = getFormatterPrefs();
    if (prefs.beforeOpeningParen) buffer.append(SPACE);
    buffer.append(LPAREN);
  }

  @Override
  protected ProposalInfo computeProposalInfo() {
    IJavaProject project = fInvocationContext.getProject();
    if (project != null) return new MethodProposalInfo(project, fProposal);
    return super.computeProposalInfo();
  }

  /*
   * @see org.eclipse.jdt.internal.ui.text.java.LazyJavaCompletionProposal#computeSortString()
   */
  @Override
  protected String computeSortString() {
    /*
     * Lexicographical sort order:
     * 1) by relevance (done by the proposal sorter)
     * 2) by method name
     * 3) by parameter count
     * 4) by parameter type names
     */
    char[] name = fProposal.getName();
    char[] parameterList =
        Signature.toCharArray(fProposal.getSignature(), null, null, false, false);
    int parameterCount =
        Signature.getParameterCount(fProposal.getSignature())
            % 10; // we don't care about insane methods with >9 parameters
    StringBuffer buf = new StringBuffer(name.length + 2 + parameterList.length);

    buf.append(name);
    buf.append('\0'); // separator
    buf.append(parameterCount);
    buf.append(parameterList);
    return buf.toString();
  }

  /*
   * @see org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal#isOffsetValid(int)
   * @since 3.5
   */
  @Override
  protected boolean isOffsetValid(int offset) {
    if (fProposal.getKind() != CompletionProposal.CONSTRUCTOR_INVOCATION)
      return super.isOffsetValid(offset);

    return fProposal.getRequiredProposals()[0].getReplaceStart() <= offset;
  }

  /*
   * @see org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal#isValidPrefix(java.lang.String)
   */
  @Override
  protected boolean isValidPrefix(String prefix) {
    if (super.isValidPrefix(prefix)) return true;

    String word = TextProcessor.deprocess(getDisplayString());
    if (fProposal.getKind() == CompletionProposal.CONSTRUCTOR_INVOCATION) {
      int start =
          word.indexOf(JavaElementLabels.CONCAT_STRING) + JavaElementLabels.CONCAT_STRING.length();
      word = word.substring(start);
      return isPrefix(prefix, word) || isPrefix(prefix, new String(fProposal.getName()));
    }

    if (isInJavadoc()) {
      int idx = word.indexOf("{@link "); // $NON-NLS-1$
      if (idx == 0) {
        word = word.substring(7);
      } else {
        idx = word.indexOf("{@value "); // $NON-NLS-1$
        if (idx == 0) {
          word = word.substring(8);
        }
      }
    }
    return isPrefix(prefix, word);
  }

  /*
   * @see org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal#isPrefix(java.lang.String, java.lang.String)
   * @since 3.4
   */
  @Override
  protected boolean isPrefix(String prefix, String string) {
    if (fProposal.getKind() == CompletionProposal.METHOD_REF_WITH_CASTED_RECEIVER && prefix != null)
      prefix = prefix.substring(fProposal.getReceiverEnd() - fProposal.getReceiverStart() + 1);
    return super.isPrefix(prefix, string);
  }

  /*
   * @see org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal#getPrefix(org.eclipse.jface.text.IDocument, int)
   * @since 3.5
   */
  @Override
  protected String getPrefix(IDocument document, int offset) {
    if (fProposal.getKind() != CompletionProposal.CONSTRUCTOR_INVOCATION)
      return super.getPrefix(document, offset);

    int replacementOffset = fProposal.getRequiredProposals()[0].getReplaceStart();

    try {
      int length = offset - replacementOffset;
      if (length > 0) return document.get(replacementOffset, length);
    } catch (BadLocationException x) {
    }
    return ""; // $NON-NLS-1$
  }

  /*
   * @see org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal#createRequiredTypeCompletionProposal(org.eclipse.jdt.core.CompletionProposal, org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext)
   * @since 3.7
   */
  @Override
  protected LazyJavaCompletionProposal createRequiredTypeCompletionProposal(
      CompletionProposal completionProposal, JavaContentAssistInvocationContext invocationContext) {
    LazyJavaCompletionProposal requiredProposal =
        super.createRequiredTypeCompletionProposal(completionProposal, invocationContext);
    if (fProposal.getKind() == CompletionProposal.CONSTRUCTOR_INVOCATION
        && requiredProposal instanceof LazyGenericTypeProposal)
      ((LazyGenericTypeProposal) requiredProposal)
          .canUseDiamond(fProposal.canUseDiamond(fInvocationContext.getCoreContext()));
    return requiredProposal;
  }
}
