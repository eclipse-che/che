/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2010 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.core.filebuffers;

import org.eclipse.core.runtime.IPath;

/**
 * Interface for listeners to file buffer changes.
 *
 * @since 3.0
 */
public interface IFileBufferListener {

  /**
   * Informs the listener about the creation of the given buffer.
   *
   * @param buffer the created file buffer
   */
  void bufferCreated(IFileBuffer buffer);

  /**
   * Informs the listener that the given buffer has been disposed. All state information has already
   * been disposed and accessing it is forbidden. However, accessing the file buffer's content is
   * still allowed during the notification.
   *
   * @param buffer the disposed file buffer
   */
  void bufferDisposed(IFileBuffer buffer);

  /**
   * Informs the listener about an upcoming replace of the contents of the given buffer.
   *
   * @param buffer the affected file buffer
   */
  void bufferContentAboutToBeReplaced(IFileBuffer buffer);

  /**
   * Informs the listener that the buffer of the given buffer has been replaced.
   *
   * @param buffer the affected file buffer
   */
  void bufferContentReplaced(IFileBuffer buffer);

  /**
   * Informs the listener about the start of a state changing operation on the given buffer.
   *
   * @param buffer the affected file buffer
   */
  void stateChanging(IFileBuffer buffer);

  /**
   * Informs the listener that the dirty state of the given buffer changed to the specified value
   *
   * @param buffer the affected file buffer
   * @param isDirty <code>true</code> if the buffer is dirty, <code>false</code> otherwise
   */
  void dirtyStateChanged(IFileBuffer buffer, boolean isDirty);

  /**
   * Informs the listener that the state validation changed to the specified value.
   *
   * @param buffer the affected file buffer
   * @param isStateValidated <code>true</code> if the buffer state is validated, <code>false</code>
   *     otherwise
   */
  void stateValidationChanged(IFileBuffer buffer, boolean isStateValidated);

  /**
   * Informs the listener that the file underlying the given file buffer has been moved to the given
   * location.
   *
   * <p>This event is currently only sent if the file buffer is backed by an <code>IFile</code>.
   *
   * @param buffer the affected file buffer
   * @param path the new location (not just the container)
   */
  void underlyingFileMoved(IFileBuffer buffer, IPath path);

  /**
   * Informs the listener that the file underlying the given file buffer has been deleted.
   *
   * @param buffer the affected file buffer
   */
  void underlyingFileDeleted(IFileBuffer buffer);

  /**
   * Informs the listener that a state changing operation on the given file buffer failed.
   *
   * @param buffer the affected file buffer
   */
  void stateChangeFailed(IFileBuffer buffer);
}
