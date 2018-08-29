/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2005 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.core.filebuffers;

import org.eclipse.core.runtime.IStatus;

/**
 * Implementers of {@link org.eclipse.core.filebuffers.IFileBuffer} may also implement <code>
 * IStateValidationSupport</code> in order to allow a {@link
 * org.eclipse.core.filebuffers.IFileBufferManager} to batch the stages of state validation when
 * calling {@link org.eclipse.core.filebuffers.IFileBufferManager#validateState(IFileBuffer[],
 * org.eclipse.core.runtime.IProgressMonitor, Object)}.
 *
 * @see org.eclipse.core.filebuffers.IFileBuffer
 * @since 3.1
 */
public interface IStateValidationSupport {

  /**
   * Tells this buffer that the validation state is about to be changed. File buffer listeners will
   * receive a {@link IFileBufferListener#stateChanging(IFileBuffer)} notification in response.
   */
  void validationStateAboutToBeChanged();

  /**
   * Tells this buffer that the validation state has been changed to the given value. After that
   * call, {@link IFileBuffer#isStateValidated()} will return the given value. Also {@link
   * IFileBuffer#getStatus()} will returns the provided status. File buffer listeners will receive a
   * {@link IFileBufferListener#stateValidationChanged(IFileBuffer, boolean)} notification.
   *
   * @param validationState <code>true</code> if validated, <code>false</code> otherwise
   * @param status the status of the executed validate state operation
   */
  void validationStateChanged(boolean validationState, IStatus status);

  /**
   * Tells this buffer that a initiated state validation failed. File buffer listeners will receive
   * a {@link IFileBufferListener#stateChangeFailed(IFileBuffer)} notification in response.
   */
  void validationStateChangeFailed();
}
