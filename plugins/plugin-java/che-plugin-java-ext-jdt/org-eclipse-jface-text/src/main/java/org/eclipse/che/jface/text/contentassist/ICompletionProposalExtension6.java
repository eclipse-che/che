/**
 * ***************************************************************************** Copyright (c) 2008
 * IBM Corporation and others. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.jface.text.contentassist;

import org.eclipse.jface.viewers.StyledString;

/**
 * Extends {@link ICompletionProposal} with the following function:
 *
 * <ul>
 *   <li>Allow styled ranges in the display string.
 * </ul>
 *
 * @since 3.4
 */
public interface ICompletionProposalExtension6 {

  /**
   * Returns the styled string used to display this proposal in the list of completion proposals.
   * This can for example be used to draw mixed colored labels.
   *
   * <p><strong>Note:</strong> {@link ICompletionProposal#getDisplayString()} still needs to be
   * correctly implemented as this method might be ignored in case of uninstalled owner draw
   * support.
   *
   * @return the string builder used to display this proposal
   */
  StyledString getStyledDisplayString();
}
