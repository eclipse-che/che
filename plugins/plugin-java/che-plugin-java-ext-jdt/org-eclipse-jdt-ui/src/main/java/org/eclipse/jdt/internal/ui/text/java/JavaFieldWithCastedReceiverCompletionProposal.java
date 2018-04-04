/**
 * ***************************************************************************** Copyright (c) 2007,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.text.java;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

/**
 * Java completion proposal for {@link
 * org.eclipse.jdt.core.CompletionProposal#FIELD_REF_WITH_CASTED_RECEIVER}.
 *
 * @since 3.4
 */
public class JavaFieldWithCastedReceiverCompletionProposal extends JavaCompletionProposal {

  private CompletionProposal fProposal;

  public JavaFieldWithCastedReceiverCompletionProposal(
      String completion,
      int start,
      int length,
      Image image,
      StyledString label,
      int relevance,
      boolean inJavadoc,
      JavaContentAssistInvocationContext invocationContext,
      CompletionProposal proposal) {
    super(completion, start, length, image, label, relevance, inJavadoc, invocationContext);
    Assert.isNotNull(proposal);
    fProposal = proposal;
  }

  /*
   * @see org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal#isPrefix(java.lang.String, java.lang.String)
   */
  @Override
  protected boolean isPrefix(String prefix, String string) {
    if (prefix != null)
      prefix = prefix.substring(fProposal.getReceiverEnd() - fProposal.getReceiverStart() + 1);
    return super.isPrefix(prefix, string);
  }
}
