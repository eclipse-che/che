/**
 * ***************************************************************************** Copyright (c) 2007,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.jface.text.quickassist;

import org.eclipse.che.jface.text.contentassist.ICompletionProposalExtension6;
import org.eclipse.core.commands.IHandler;

/**
 * Extends {@link IQuickAssistAssistant} with the following function:
 *
 * <ul>
 *   <li>allows to get a handler for the given command identifier
 *   <li>allows to enable support for colored labels in the proposal popup
 * </ul>
 *
 * @since 3.4
 */
public interface IQuickAssistAssistantExtension {

  /**
   * Returns the handler for the given command identifier.
   *
   * <p>The same handler instance will be returned when called a more than once with the same
   * command identifier.
   *
   * @param commandId the command identifier
   * @return the handler for the given command identifier
   * @throws IllegalArgumentException if the command is not supported by this content assistant
   * @throws IllegalStateException if called when this content assistant is uninstalled
   */
  IHandler getHandler(String commandId);

  /**
   * Enables the support for colored labels in the proposal popup.
   *
   * <p>Completion proposals can implement {@link ICompletionProposalExtension6} to provide colored
   * proposal labels.
   *
   * @param isEnabled if <code>true</code> the support for colored labels is enabled in the proposal
   *     popup
   * @since 3.4
   */
  void enableColoredLabels(boolean isEnabled);
}
