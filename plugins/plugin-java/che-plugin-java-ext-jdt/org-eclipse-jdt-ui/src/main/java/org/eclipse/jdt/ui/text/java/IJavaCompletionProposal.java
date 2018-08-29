/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2012 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.ui.text.java;

import org.eclipse.che.jface.text.contentassist.ICompletionProposal;

/**
 * A completion proposal with a relevance value. The relevance value is used to sort the completion
 * proposals. Proposals with higher relevance should be listed before proposals with lower
 * relevance.
 *
 * <p>This interface can be implemented by clients.
 *
 * @see ICompletionProposal
 * @since 2.1
 */
public interface IJavaCompletionProposal extends ICompletionProposal {

  /**
   * Returns the relevance of this completion proposal.
   *
   * <p>The relevance is used to determine if this proposal is more relevant than another proposal.
   *
   * @return the relevance of this completion proposal in the range of [0, 1000]
   */
  int getRelevance();
}
