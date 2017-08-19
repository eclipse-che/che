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

import org.eclipse.jface.text.IDocument;

/**
 * Extends {@link ICompletionProposal} with the following functions:
 *
 * <ul>
 *   <li>handling of trigger characters other than ENTER
 *   <li>completion proposal validation for a given offset
 *   <li>context information can be freely positioned
 * </ul>
 *
 * @since 2.0
 */
public interface ICompletionProposalExtension {

  /**
   * Applies the proposed completion to the given document. The insertion has been triggered by
   * entering the given character at the given offset. This method assumes that {@link
   * #isValidFor(org.eclipse.jface.text.IDocument, int)} returns <code>true</code> if called for
   * <code>offset</code>.
   *
   * @param document the document into which to insert the proposed completion
   * @param trigger the trigger to apply the completion
   * @param offset the offset at which the trigger has been activated
   */
  void apply(IDocument document, char trigger, int offset);

  /**
   * Returns whether this completion proposal is valid for the given position in the given document.
   *
   * @param document the document for which the proposal is tested
   * @param offset the offset for which the proposal is tested
   * @return <code>true</code> iff valid
   */
  boolean isValidFor(IDocument document, int offset);

  /**
   * Returns the characters which trigger the application of this completion proposal.
   *
   * @return the completion characters for this completion proposal or <code>null</code> if no
   *     completion other than the new line character is possible
   */
  char[] getTriggerCharacters();

  /**
   * Returns the position to which the computed context information refers to or <code>-1</code> if
   * no context information can be provided by this completion proposal.
   *
   * @return the position to which the context information refers to or <code>-1</code> for no
   *     information
   */
  int getContextInformationPosition();
}
