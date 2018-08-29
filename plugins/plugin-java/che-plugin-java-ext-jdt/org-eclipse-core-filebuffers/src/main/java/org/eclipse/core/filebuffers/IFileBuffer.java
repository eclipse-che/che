/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.core.filebuffers;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.text.IDocumentExtension4;

/**
 * A file buffer represents a file that can be edited by more than one client. Editing is session
 * oriented. This means that editing is a sequence of modification steps. The start of the sequence
 * and the end of the sequence are explicitly indicated. There are no time constraints connected
 * with the sequence of modification steps. A file buffer reifies editing sessions and allows them
 * to interleave.
 *
 * <p>It is not specified whether simultaneous editing sessions can be owned by different threads.
 *
 * <p>Clients are not supposed to implement that interface. Instances of this type are obtained from
 * a {@link org.eclipse.core.filebuffers.IFileBufferManager}.
 *
 * @since 3.0
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IFileBuffer {

  /**
   * Returns the location of this file buffer.
   *
   * <p>The location is either a full path of a workspace resource or an absolute path in the local
   * file system.
   *
   * <p><strong>Note:</strong> Since 3.3 this method can also return <code>null</code> if the file
   * is not on the local file system.
   *
   * @return the location of this file buffer or <code>null</code> if the file is not on the local
   *     file system
   */
  IPath getLocation();

  //	/**
  //	 * Returns the file store of this file buffer.
  //	 *
  //	 * @return the file store of this file buffer
  //	 * @since 3.3
  //	 */
  //	IFileStore getFileStore();

  /**
   * Returns whether this file buffer is shared by more than one client.
   *
   * @return <code>true</code> if this file buffer is shared by more than one client
   */
  boolean isShared();

  /**
   * Returns whether this file buffer is synchronized with the file system. This is when the file
   * buffer's underlying file is in synchronization with the file system and the file buffer has
   * been initialized after the underlying files has been modified the last time.
   *
   * @return <code>true</code> if the file buffer is synchronized with the file system
   */
  boolean isSynchronized();

  /**
   * Returns the modification stamp of the file underlying this file buffer.
   *
   * <p>{@link IDocumentExtension4#UNKNOWN_MODIFICATION_STAMP} is returned if the buffer cannot get
   * the modification stamp from the underlying file.
   *
   * <p><strong>Note:</strong> The value of the modification stamp returned for non-existing files
   * can differ depending on the underlying file system.
   *
   * @return the modification stamp of the file underlying this file buffer
   */
  long getModificationStamp();

  /**
   * Returns whether this file buffer is commitable. This is the case when the file buffer's state
   * has been successfully validated.
   *
   * @return <code>true</code> if the file buffer is commitable, <code>false</code> otherwise
   * @since 3.1
   */
  boolean isCommitable();

  /**
   * Computes the scheduling rule that is required for committing a changed buffer.
   *
   * @return the commit scheduling rule or <code>null</code>
   * @since 3.1
   */
  ISchedulingRule computeCommitRule();

  /**
   * Commits this file buffer by changing the contents of the underlying file to the contents of
   * this file buffer. After that call, <code>isDirty</code> returns <code>false</code> and <code>
   * isSynchronized</code> returns <code>true</code>.
   *
   * @param monitor the progress monitor, or <code>null</code> if progress reporting is not desired
   * @param overwrite indicates whether the underlying file should be overwritten if it is not
   *     synchronized with the file system
   * @throws CoreException if writing or accessing the underlying file fails
   */
  void commit(IProgressMonitor monitor, boolean overwrite) throws CoreException;

  /**
   * Reverts the contents of this file buffer to the content of its underlying file. After that call
   * successfully returned, <code>isDirty</code> returns <code>false</code> and <code>isSynchronized
   * </code> returns <code>true</code>.
   *
   * @param monitor the progress monitor, or <code>null</code> if progress reporting is not desired
   * @throws CoreException if reading or accessing the underlying file fails
   */
  void revert(IProgressMonitor monitor) throws CoreException;

  /**
   * Returns whether changes have been applied to this file buffer since initialization, or the most
   * recent <code>revert</code> or <code>commit</code> call.
   *
   * @return <code>true</code> if changes have been applied to this buffer
   */
  boolean isDirty();

  /**
   * Sets the dirty state of the file buffer to the given value. A direct subsequent call to <code>
   * isDirty</code> returns the previously set value.
   *
   * @param isDirty <code>true</code> if the buffer should be marked dirty, <code>false</code>
   *     otherwise
   * @since 3.1
   */
  void setDirty(boolean isDirty);

  /**
   * Computes the scheduling rule that is required for validating the state of the buffer.
   *
   * @return the validate state scheduling rule or <code>null</code>
   * @since 3.1
   */
  ISchedulingRule computeValidateStateRule();

  /**
   * Validates the state of this file buffer and tries to bring the buffer's underlying file into a
   * state in which it can be modified. If state validation is not supported this operation does
   * nothing.
   *
   * @param monitor the progress monitor, or <code>null</code> if progress reporting is not desired
   * @param computationContext the context in which the validation is performed, e.g., a SWT shell
   * @exception CoreException if the underlying file can not be accessed or its state cannot be
   *     changed
   */
  void validateState(IProgressMonitor monitor, Object computationContext) throws CoreException;

  /**
   * Returns whether the state of this file buffer has been validated. If state validation is not
   * supported this method always returns <code>true</code>.
   *
   * @return <code>true</code> if the state has been validated, <code>false</code> otherwise
   */
  boolean isStateValidated();

  /**
   * Resets state validation. If state validation is supported, <code>isStateValidated</code>
   * afterwards returns <code>false</code> until the state is revalidated.
   */
  void resetStateValidation();

  /**
   * Returns the status of this file buffer. This is the result of the last operation performed on
   * this file buffer or internally initiated by this file buffer.
   *
   * @return the status of this file buffer
   */
  IStatus getStatus();

  /**
   * The caller requests that the synchronization context is used to synchronize this file buffer
   * with its underlying file.
   *
   * @since 3.1
   */
  void requestSynchronizationContext();

  /**
   * The caller no longer requests the synchronization context for this file buffer.
   *
   * @since 3.1
   */
  void releaseSynchronizationContext();

  /**
   * Returns whether a synchronization context has been requested for this file buffer and not yet
   * released.
   *
   * @return <code>true</code> if a synchronization context is requested, <code>false</code>
   *     otherwise
   * @since 3.1
   */
  boolean isSynchronizationContextRequested();

  /**
   * Returns the content type of this file buffer or <code>null</code> if none could be determined.
   * If the file buffer is dirty, the returned content type is determined by the buffer's dirty
   * state.
   *
   * @return the content type or <code>null</code>
   * @throws CoreException if reading or accessing the underlying file fails
   * @since 3.1
   */
  IContentType getContentType() throws CoreException;
}
