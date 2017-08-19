/**
 * ***************************************************************************** Copyright (c) 2005,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.jface.text.contentassist;

/**
 * Extends {@link ICompletionProposal} with the following functions:
 *
 * <ul>
 *   <li>specify whether a proposal is automatically insertable
 * </ul>
 *
 * @since 3.1
 */
public interface ICompletionProposalExtension4 {

  /**
   * Returns <code>true</code> if the proposal may be automatically inserted, <code>false</code>
   * otherwise. Automatic insertion can happen if the proposal is the only one being proposed, in
   * which case the content assistant may decide to not prompt the user with a list of proposals,
   * but simply insert the single proposal. A proposal may veto this behavior by returning <code>
   * false</code> to a call to this method.
   *
   * @return <code>true</code> if the proposal may be inserted automatically, <code>false</code> if
   *     not
   */
  boolean isAutoInsertable();
}
