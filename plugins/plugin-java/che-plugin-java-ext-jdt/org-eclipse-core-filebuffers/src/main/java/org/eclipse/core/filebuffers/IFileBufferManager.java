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

/**
 * A file buffer manager manages file buffers for files while the files are connected to the file
 * buffer manager. In order to connect a file to a file buffer manager call <code>connect</code>.
 * After that call has successfully completed the file buffer can be obtained by <code>getFileBuffer
 * </code>. The file buffer is created on the first connect and disposed on the last disconnect.
 * I.e. the file buffer manager keeps track of how often a file is connected and returns the same
 * file buffer to each client as long as the file is connected.
 *
 * <p>Clients are not supposed to implement that interface.
 *
 * @since 3.0
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IFileBufferManager {

  /**
   * Connects the file at the given location to this manager. After that call successfully completed
   * it is guaranteed that each call to <code>getFileBuffer</code> returns the same file buffer
   * until <code>disconnect</code> is called.
   *
   * <p>The provided location is either a full path of a workspace resource or an absolute path in
   * the local file system. The file buffer manager does not resolve the location of workspace
   * resources in the case of linked resources.
   *
   * @param location the location of the file to be connected
   * @param monitor the progress monitor, or <code>null</code> if progress reporting is not desired
   * @throws CoreException if the file could not successfully be connected
   * @deprecated As of 3.3, replaced by {@link #connect(IPath, LocationKind, IProgressMonitor)}
   */
  void connect(IPath location, IProgressMonitor monitor) throws CoreException;

  /**
   * Connects the file at the given location to this manager. After that call successfully completed
   * it is guaranteed that each call to <code>getFileBuffer</code> returns the same file buffer
   * until <code>disconnect</code> is called.
   *
   * <p>The type of the provided location is specified by the given <code>locationKind</code>.
   *
   * @param location the location of the file to be connected
   * @param locationKind the kind of the given location
   * @param monitor the progress monitor, or <code>null</code> if progress reporting is not desired
   * @throws CoreException if the file could not successfully be connected
   * @see LocationKind
   * @since 3.3
   */
  void connect(IPath location, LocationKind locationKind, IProgressMonitor monitor)
      throws CoreException;

  //	/**
  //	 * Connects the given file store to this manager. After that call
  //	 * successfully completed it is guaranteed that each call to <code>getFileBuffer</code>
  //	 * returns the same file buffer until <code>disconnect</code> is called.
  //	 * <p>
  //	 * <strong>Note:</strong> This API must not be used if the given file
  //	 * store maps to a resource contained in the workspace. A file buffer
  //	 * that has been connected using a path will not be found.
  //	 * </p>
  //	 * <p>
  //	 * We had to use a different name than <code>connect</code> for this method
  //	 * due to https://bugs.eclipse.org/bugs/show_bug.cgi?id=148844
  //	 * </p>
  //	 *
  //	 * @param fileStore the file store to be connected
  //	 * @param monitor the progress monitor, or <code>null</code> if progress reporting is not
  // desired
  //	 * @throws CoreException if the file could not successfully be connected
  //	 * @since 3.3
  //	 */
  //	void connectFileStore(IFileStore fileStore, IProgressMonitor monitor) throws CoreException;

  /**
   * Disconnects the file at the given location from this manager. After that call successfully
   * completed there is no guarantee that <code>getFileBuffer</code> will return a valid file
   * buffer.
   *
   * <p>The provided location is either a full path of a workspace resource or an absolute path in
   * the local file system. The file buffer manager does not resolve the location of workspace
   * resources in the case of linked resources.
   *
   * @param location the location of the file to be disconnected
   * @param monitor the progress monitor, or <code>null</code> if progress reporting is not desired
   * @throws CoreException if the file could not successfully be disconnected
   * @deprecated As of 3.3, replaced by {@link #disconnect(IPath, LocationKind, IProgressMonitor)}
   */
  void disconnect(IPath location, IProgressMonitor monitor) throws CoreException;

  /**
   * Disconnects the file at the given location from this manager. After that call successfully
   * completed there is no guarantee that <code>getFileBuffer</code> will return a valid file
   * buffer.
   *
   * <p>The type of the provided location is specified by the given <code>locationKind</code>.
   *
   * @param location the location of the file to be disconnected
   * @param locationKind the kind of the given location
   * @param monitor the progress monitor, or <code>null</code> if progress reporting is not desired
   * @throws CoreException if the file could not successfully be disconnected
   * @see LocationKind
   * @since 3.3
   */
  void disconnect(IPath location, LocationKind locationKind, IProgressMonitor monitor)
      throws CoreException;

  //	/**
  //	 * Disconnects the given file store from this manager. After that
  //	 * call successfully completed there is no guarantee that <code>getFileBuffer</code>
  //	 * will return a valid file buffer.
  //	 * <p>
  //	 * <strong>Note:</strong> This API must not be used if the given file
  //	 * store maps to a resource contained in the workspace. A file buffer
  //	 * that has been connected using a path will not be found.
  //	 * </p>
  //	 * <p>
  //	 * We had to use a different name than <code>disconnect</code> for this method
  //	 * due to https://bugs.eclipse.org/bugs/show_bug.cgi?id=148844
  //	 * </p>
  //	 *
  //	 * @param fileStore the file store to be disconnected
  //	 * @param monitor the progress monitor, or <code>null</code> if progress reporting is not
  // desired
  //	 * @throws CoreException if the file could not successfully be disconnected
  //	 * @since 3.3
  //	 */
  //	void disconnectFileStore(IFileStore fileStore, IProgressMonitor monitor) throws CoreException;

  /**
   * Returns the file buffer managed for the given location or <code>null</code> if there is no such
   * file buffer.
   *
   * <p>The provided location is either a full path of a workspace resource or an absolute path in
   * the local file system. The file buffer manager does not resolve the location of workspace
   * resources in the case of linked resources.
   *
   * @param location the location
   * @return the file buffer managed for that location or <code>null</code>
   * @deprecated As of 3.3, replaced by {@link #getFileBuffer(IPath, LocationKind)}
   */
  IFileBuffer getFileBuffer(IPath location);

  /**
   * Returns the file buffer managed for the given location or <code>null</code> if there is no such
   * file buffer.
   *
   * <p>The type of the provided location is specified by the given <code>locationKind</code>.
   *
   * @param location the location
   * @param locationKind the kind of the given location
   * @return the file buffer managed for that location or <code>null</code>
   * @see LocationKind
   * @since 3.3
   */
  IFileBuffer getFileBuffer(IPath location, LocationKind locationKind);

  //	/**
  //	 * Returns the file buffer managed for the given file store or
  //	 * <code>null</code> if there is no such file buffer.
  //	 * <p>
  //	 * <strong>Note:</strong> This API must not be used if the given file
  //	 * store maps to a resource contained in the workspace. A file buffer
  //	 * that has been connected using a path will not be found.
  //	 * </p>
  //	 * <p>
  //	 * We had to use a different name than <code>getFileBuffer</code> for this method
  //	 * due to https://bugs.eclipse.org/bugs/show_bug.cgi?id=148844
  //	 * </p>
  //	 *
  //	 * @param fileStore the file store
  //	 * @return the file buffer managed for that file store or <code>null</code>
  //	 * @since 3.3
  //	 */
  //	IFileBuffer getFileStoreFileBuffer(IFileStore fileStore);

  /**
   * Returns all managed file buffers that are currently connected.
   *
   * <p><strong>Note:</strong> It is the clients responsibility to handle disconnected file buffers
   * as buffers can be disconnected after calling this method.
   *
   * @return the file buffers managed by this file buffer manager
   * @see #getFileStoreFileBuffers()
   * @since 3.4
   */
  IFileBuffer[] getFileBuffers();

  /**
   * Returns all managed file store file buffers that are currently connected.
   *
   * <p><strong>Note:</strong> It is the clients responsibility to handle disconnected file buffers
   * as buffers can be disconnected after calling this method.
   *
   * @return the file buffers managed by this file buffer manager
   * @see #getFileBuffers()
   * @since 3.4
   */
  IFileBuffer[] getFileStoreFileBuffers();

  /**
   * Sets the synchronization context for this file buffer manager, i.e., for all file buffers this
   * manager manages.
   *
   * @param context the synchronization context managed by this file buffer manager
   */
  void setSynchronizationContext(ISynchronizationContext context);

  /**
   * Executes the given runnable in the synchronization context of this file buffer manager. If
   * there is no synchronization context connected with this manager, the runnable is directly
   * executed.
   *
   * @param runnable the runnable to be executed
   * @since 3.5
   */
  public void execute(Runnable runnable);

  /**
   * The caller requests that the synchronization context is used to synchronize the given location
   * with its file buffer. This call as no effect if there is no file buffer managed for the given
   * location.
   *
   * <p>The provided location is either a full path of a workspace resource or an absolute path in
   * the local file system. The file buffer manager does not resolve the location of workspace
   * resources in the case of linked resources.
   *
   * @param location the location
   * @deprecated As of 3.1, replaced by {@link
   *     org.eclipse.core.filebuffers.IFileBuffer#requestSynchronizationContext()}
   */
  void requestSynchronizationContext(IPath location);

  /**
   * The caller no longer requests the synchronization context for the file buffer managed for the
   * given location. This method has no effect if there is no file buffer managed for this location.
   *
   * <p>The provided location is either a full path of a workspace resource or an absolute path in
   * the local file system. The file buffer manager does not resolve the location of workspace
   * resources in the case of linked resources.
   *
   * @param location the location
   * @deprecated As of 3.1, replaced by {@link IFileBuffer#releaseSynchronizationContext()}
   */
  void releaseSynchronizationContext(IPath location);

  /**
   * Adds the given listener to the list of file buffer listeners. After that call the listener is
   * informed about changes related to this file buffer manager. If the listener is already
   * registered with the file buffer, this call has no effect.
   *
   * @param listener the listener to be added
   */
  void addFileBufferListener(IFileBufferListener listener);

  /**
   * Removes the given listener from the list of file buffer listeners. If the listener is not
   * registered with this file buffer, this call has no effect.
   *
   * @param listener the listener to be removed
   */
  void removeFileBufferListener(IFileBufferListener listener);

  /**
   * Validates the state of the given file buffers and tries to bring the buffer's underlying file
   * into a state in which it can be modified. File buffers which do not support state validation
   * are left untouched.
   *
   * <p>In case of a single file buffer, {@link IFileBuffer#validateState(IProgressMonitor, Object)}
   * should be used.
   *
   * @param fileBuffers the file buffers to validate
   * @param monitor the progress monitor, or <code>null</code> if progress reporting is not desired
   * @param computationContext the context in which the validation is performed, e.g., a SWT shell
   * @exception CoreException if the underlying file can not be accessed or its state cannot be
   *     changed
   * @since 3.1
   */
  void validateState(IFileBuffer[] fileBuffers, IProgressMonitor monitor, Object computationContext)
      throws CoreException;
}
