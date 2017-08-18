/**
 * ***************************************************************************** Copyright (c)
 * 2014-2015 Red Hat, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Red Hat, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.core.filesystem;

import java.net.URI;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This is the main interface to a single file system. Each file system instance manages interaction
 * with all files in the backing store represented by a particular URI scheme.
 *
 * <p>File systems are registered using the "filesystems" extension point.
 *
 * @see EFS#getFileSystem(String)
 * @since org.eclipse.core.filesystem 1.0
 * @noimplement This interface is not intended to be implemented by clients. File system
 *     implementations must subclass {@link FileSystem} rather than implementing this interface
 *     directly.
 */
public interface IFileSystem extends IAdaptable {

  /**
   * Returns the file attributes supported by this file system. This value is a bit mask of the
   * <code>EFS.ATTRIBUTE_*</code> constants.
   *
   * @return the file attributes supported by this file system.
   */
  public int attributes();

  /**
   * Returns whether this file system supports deletion
   *
   * @return <code>true</code> if this file system allows deletion of files and directories, and
   *     <code>false</code> otherwise
   */
  public boolean canDelete();

  /**
   * Returns whether this file system supports modification.
   *
   * @return <code>true</code> if this file system allows modification of files and directories, and
   *     <code>false</code> otherwise
   */
  public boolean canWrite();

  //	/**
  //	 * Returns a file tree containing information about the complete sub-tree
  //	 * rooted at the given store.  Returns <code>null</code> if this file
  //	 * system does not support the creation of such file trees.
  //	 * <p>
  //	 * A file tree accurately represents the state of a portion of a file system
  //	 * at the time it is created, but it is never updated. Clients using a file
  //	 * tree must tolerate the fact that the actual file system contents may
  //	 * change after the tree is generated.
  //	 * </p>
  //	 *
  //	 * @param root The store to use as the root of the file tree
  //	 * @param monitor a progress monitor, or <code>null</code> if progress
  //	 *    reporting and cancellation are not desired
  //	 * @return an {@link IFileTree} containing the sub-tree of the given store,
  //	 * or <code>null</code>
  //	 * @exception CoreException if this method fails. Reasons include:
  //	 * <ul>
  //	 * <li>Problems occurred while contacting the file system.</li>
  //	 * </ul>
  //	 * @see IFileTree
  //	 */
  //	public IFileTree fetchFileTree(IFileStore root, IProgressMonitor monitor) throws CoreException;

  /**
   * Returns the file store in this file system corresponding to the given local file. Returns
   * <code>null</code> if this file system cannot provide an {@link IFileStore} corresponding to a
   * local file.
   *
   * @param file The file to be converted
   * @return The {@link IFileStore} corresponding to the given file, or <code>null</code>
   * @see IFileStore#toLocalFile(int, IProgressMonitor)
   */
  public IFileStore fromLocalFile(java.io.File file);

  /**
   * Returns the URI scheme of this file system.
   *
   * @return the URI scheme of this file system.
   */
  public String getScheme();

  /**
   * Returns a handle to a file store in this file system. This is a handle-only method; this method
   * succeeds regardless of whether a file exists at that path in this file system.
   *
   * <p>This is a convenience method for file systems that do not make use of the authority {@link
   * URI} component, such as a host or user information. The provided path argument is interpreted
   * as the path component of the file system's {@link URI}. For example, this method can be used to
   * safely navigate within the local file system.
   *
   * @param path A path to a file store within the scheme of this file system.
   * @return A handle to a file store in this file system
   * @see EFS#getLocalFileSystem()
   */
  public IFileStore getStore(IPath path);

  /**
   * Returns a handle to a file store in this file system. This is a handle-only method; this method
   * succeeds regardless of whether a file exists at that path in this file system. The provided URI
   * must have the appropriate scheme and component parts for the file system on which this method
   * is called.
   *
   * @param uri The URI of the file store to return.
   * @return A handle to a file store in this file system
   */
  public IFileStore getStore(URI uri);

  /**
   * Returns whether this file system is case sensitive. A case sensitive file system treats files
   * with names that differ only in case as different files. For example, "HELLO", "Hello", and
   * "hello" would be three different files or directories in a case sensitive file system.
   *
   * @return <code>true</code> if this file system is case sensitive, and <code>false</code>
   *     otherwise.
   */
  public boolean isCaseSensitive();
}
