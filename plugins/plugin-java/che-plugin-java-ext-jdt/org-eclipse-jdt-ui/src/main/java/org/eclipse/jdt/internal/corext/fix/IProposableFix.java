/**
 * ***************************************************************************** Copyright (c) 2007,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.fix;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.ui.cleanup.ICleanUpFix;

/**
 * A <code>ICleanUpFix</code> which can be used in a correction proposal environment. A proposal
 * will be shown to the user and if chosen the fix is executed.
 *
 * @since 3.4
 */
public interface IProposableFix extends ICleanUpFix {

  /**
   * Returns the string to be displayed in the list of completion proposals.
   *
   * @return the string to be displayed
   */
  public String getDisplayString();

  /**
   * Returns optional additional information about the proposal. The additional information will be
   * presented to assist the user in deciding if the selected proposal is the desired choice.
   *
   * <p>Returns <b>null</b> if the default proposal info should be used.
   *
   * @return the additional information or <code>null</code>
   */
  public String getAdditionalProposalInfo();

  /**
   * A status informing about issues with this fix or <b>null</b> if no issues.
   *
   * @return status to inform the user
   */
  public IStatus getStatus();
}
