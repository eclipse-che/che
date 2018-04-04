/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2005 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.jface.text.contentassist;

import org.eclipse.che.jface.text.ITextViewer;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;

/**
 * Extends {@link ICompletionProposal} with the following functions:
 *
 * <ul>
 *   <li>handling of trigger characters with modifiers
 *   <li>visual indication for selection of a proposal
 * </ul>
 *
 * @since 2.1
 */
public interface ICompletionProposalExtension2 {

  /**
   * Applies the proposed completion to the given document. The insertion has been triggered by
   * entering the given character with a modifier at the given offset. This method assumes that
   * {@link #validate(org.eclipse.jface.text.IDocument, int, org.eclipse.jface.text.DocumentEvent)}
   * returns <code>true</code> if called for <code>offset</code>.
   *
   * @param viewer the text viewer into which to insert the proposed completion
   * @param trigger the trigger to apply the completion
   * @param stateMask the state mask of the modifiers
   * @param offset the offset at which the trigger has been activated
   */
  void apply(ITextViewer viewer, char trigger, int stateMask, int offset);

  /**
   * Called when the proposal is selected.
   *
   * @param viewer the text viewer.
   * @param smartToggle the smart toggle key was pressed
   */
  void selected(ITextViewer viewer, boolean smartToggle);

  /**
   * Called when the proposal is unselected.
   *
   * @param viewer the text viewer.
   */
  void unselected(ITextViewer viewer);

  /**
   * Requests the proposal to be validated with respect to the document event. If the proposal
   * cannot be validated, the methods returns <code>false</code>. If the document event was <code>
   * null</code>, only the caret offset was changed, but not the document.
   *
   * <p>This method replaces {@link
   * ICompletionProposalExtension#isValidFor(org.eclipse.jface.text.IDocument, int)}
   *
   * @param document the document
   * @param offset the caret offset
   * @param event the document event, may be <code>null</code>
   * @return boolean
   */
  boolean validate(IDocument document, int offset, DocumentEvent event);
}
