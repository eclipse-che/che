/**
 * ***************************************************************************** Copyright (c) 2006,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.jface.text.contentassist;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Extends {@link ICompletionProposal} with the following function:
 *
 * <ul>
 *   <li>Allow background computation of the additional info.
 * </ul>
 *
 * @since 3.2
 */
public interface ICompletionProposalExtension5 {
  /**
   * Returns additional information about the proposal. The additional information will be presented
   * to assist the user in deciding if the selected proposal is the desired choice.
   *
   * <p>This method may be called on a non-UI thread.
   *
   * <p>By default, the returned information is converted to a string and displayed as text; if
   * {@link ICompletionProposalExtension3#getInformationControlCreator()} is implemented, the
   * information will be passed to a custom information control for display.
   *
   * @param monitor a monitor to report progress and to watch for {@link
   *     org.eclipse.core.runtime.IProgressMonitor#isCanceled() cancelation}.
   * @return the additional information, <code>null</code> for no information
   */
  Object getAdditionalProposalInfo(IProgressMonitor monitor);
}
