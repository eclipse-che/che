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

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.osgi.util.TextProcessor;
import org.eclipse.swt.graphics.Image;

public class JavaCompletionProposal extends AbstractJavaCompletionProposal {

  /**
   * Creates a new completion proposal. All fields are initialized based on the provided
   * information.
   *
   * @param replacementString the actual string to be inserted into the document
   * @param replacementOffset the offset of the text to be replaced
   * @param replacementLength the length of the text to be replaced
   * @param image the image to display for this proposal
   * @param displayString the string to be displayed for the proposal If set to <code>null</code>,
   *     the replacement string will be taken as display string.
   * @param relevance the relevance
   */
  public JavaCompletionProposal(
      String replacementString,
      int replacementOffset,
      int replacementLength,
      Image image,
      String displayString,
      int relevance) {
    this(
        replacementString,
        replacementOffset,
        replacementLength,
        image,
        new StyledString(displayString),
        relevance,
        false);
  }

  /**
   * Creates a new completion proposal. All fields are initialized based on the provided
   * information.
   *
   * @param replacementString the actual string to be inserted into the document
   * @param replacementOffset the offset of the text to be replaced
   * @param replacementLength the length of the text to be replaced
   * @param image the image to display for this proposal
   * @param displayString the string to be displayed for the proposal If set to <code>null</code>,
   *     the replacement string will be taken as display string.
   * @param relevance the relevance
   */
  public JavaCompletionProposal(
      String replacementString,
      int replacementOffset,
      int replacementLength,
      Image image,
      StyledString displayString,
      int relevance) {
    this(
        replacementString,
        replacementOffset,
        replacementLength,
        image,
        displayString,
        relevance,
        false);
  }

  /**
   * Creates a new completion proposal. All fields are initialized based on the provided
   * information.
   *
   * @param replacementString the actual string to be inserted into the document
   * @param replacementOffset the offset of the text to be replaced
   * @param replacementLength the length of the text to be replaced
   * @param image the image to display for this proposal
   * @param displayString the string to be displayed for the proposal If set to <code>null</code>,
   *     the replacement string will be taken as display string.
   * @param relevance the relevance
   * @param inJavadoc <code>true</code> for a javadoc proposal
   */
  public JavaCompletionProposal(
      String replacementString,
      int replacementOffset,
      int replacementLength,
      Image image,
      StyledString displayString,
      int relevance,
      boolean inJavadoc) {
    this(
        replacementString,
        replacementOffset,
        replacementLength,
        image,
        displayString,
        relevance,
        inJavadoc,
        null);
  }

  /**
   * Creates a new completion proposal. All fields are initialized based on the provided
   * information.
   *
   * @param replacementString the actual string to be inserted into the document
   * @param replacementOffset the offset of the text to be replaced
   * @param replacementLength the length of the text to be replaced
   * @param image the image to display for this proposal
   * @param displayString the string to be displayed for the proposal If set to <code>null</code>,
   *     the replacement string will be taken as display string.
   * @param relevance the relevance
   * @param inJavadoc <code>true</code> for a javadoc proposal
   * @param invocationContext the invocation context of this completion proposal or <code>null
   *     </code> not available
   */
  public JavaCompletionProposal(
      String replacementString,
      int replacementOffset,
      int replacementLength,
      Image image,
      StyledString displayString,
      int relevance,
      boolean inJavadoc,
      JavaContentAssistInvocationContext invocationContext) {
    super(invocationContext);
    Assert.isNotNull(replacementString);
    Assert.isTrue(replacementOffset >= 0);
    Assert.isTrue(replacementLength >= 0);

    setReplacementString(replacementString);
    setReplacementOffset(replacementOffset);
    setReplacementLength(replacementLength);
    setImage(image);
    setStyledDisplayString(
        displayString == null ? new StyledString(replacementString) : displayString);
    setRelevance(relevance);
    setCursorPosition(replacementString.length());
    setInJavadoc(inJavadoc);
    setSortString(displayString == null ? replacementString : displayString.getString());
  }

  /*
   * @see org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal#isValidPrefix(java.lang.String)
   */
  @Override
  protected boolean isValidPrefix(String prefix) {
    String word = TextProcessor.deprocess(getDisplayString());
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
    } else if (word.indexOf("this.") != -1) { // $NON-NLS-1$
      word = word.substring(word.indexOf("this.") + 5); // $NON-NLS-1$
    }
    return isPrefix(prefix, word);
  }

  /*
   * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension3#getReplacementText()
   */
  @Override
  public CharSequence getPrefixCompletionText(IDocument document, int completionOffset) {
    String string = getReplacementString();
    int pos = string.indexOf('(');
    if (pos > 0) return string.subSequence(0, pos);
    else if (string.startsWith("this.")) // $NON-NLS-1$
    return string.substring(5);
    else return string;
  }
}
