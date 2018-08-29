/**
 * ***************************************************************************** Copyright (c) 2005,
 * 2009 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation Martin Oberhuber (Wind River) -
 * [170317] add symbolic link support to API
 * *****************************************************************************
 */
package org.eclipse.core.filesystem;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A file store is responsible for storage and retrieval of a single file in some file system. The
 * actual protocols and media used for communicating with the file system are abstracted away by
 * this interface, apart from the store's ability to represent itself as a hierarchical {@link URI}.
 *
 * <p>File store instances are lightweight handle objects; a store knows how to access and store
 * file information, but does not retain a large memory footprint or operating system connections
 * such as sockets or file handles. The presence of a file store instance does not imply the
 * existence of a corresponding file in the file system represented by that store. A store that has
 * a corresponding file in its file system is said to <i>exist</i>.
 *
 * <p>As much as possible, implementations of this API maintain the characteristics of the
 * underlying file system represented by this store. For example, store instances will be
 * case-sensitive and case-preserving only when representing case-sensitive and case-preserving file
 * systems.
 *
 * @since org.eclipse.core.filesystem 1.0
 * @noimplement This interface is not intended to be implemented by clients. File store
 *     implementations must subclass {@link FileStore} rather than implementing this interface
 *     directly.
 */
public interface IFileStore extends IAdaptable {

  /**
   * Returns an {@link IFileInfo} instance for each file and directory contained within this store.
   *
   * @param options bit-wise or of option flag constants (currently only {@link EFS#NONE} is
   *     applicable).
   * @param monitor a progress monitor, or <code>null</code> if progress reporting and cancellation
   *     are not desired
   * @return An array of information about the children of this store, or an empty array if this
   *     store has no children.
   * @exception CoreException if this method fails. Reasons include:
   *     <ul>
   *       <li>This store does not exist.
   *     </ul>
   *
   * @see IFileTree#getChildInfos(IFileStore)
   */
  public IFileInfo[] childInfos(int options, IProgressMonitor monitor) throws CoreException;

  /**
   * Returns the names of the files and directories contained within this store.
   *
   * @param options bit-wise or of option flag constants (currently only {@link EFS#NONE} is
   *     applicable).
   * @param monitor a progress monitor, or <code>null</code> if progress reporting and cancellation
   *     are not desired
   * @return The names of the children of this store, or an empty array if this store has no
   *     children.
   * @exception CoreException if this method fails. Reasons include:
   *     <ul>
   *       <li>This store does not exist.
   *     </ul>
   */
  public String[] childNames(int options, IProgressMonitor monitor) throws CoreException;

  /**
   * Returns an {@link IFileStore} instance for each file and directory contained within this store.
   *
   * @param options bit-wise or of option flag constants (currently only {@link EFS#NONE} is
   *     applicable).
   * @param monitor a progress monitor, or <code>null</code> if progress reporting and cancellation
   *     are not desired
   * @return The children of this store, or an empty array if this store has no children.
   * @exception CoreException if this method fails. Reasons include:
   *     <ul>
   *       <li>This store does not exist.
   *     </ul>
   *
   * @see IFileTree#getChildStores(IFileStore)
   */
  public IFileStore[] childStores(int options, IProgressMonitor monitor) throws CoreException;

  /**
   * Copies the file represented by this store to the provided destination store. Copying occurs
   * with best-effort semantics; if some files cannot be copied, exceptions are recorded but other
   * files will continue to be copied if possible.
   *
   * <p>The {@link EFS#OVERWRITE} option flag indicates how this method deals with files that
   * already exist at the copy destination. If the <code>OVERWRITE</code> flag is present, then
   * existing files at the destination are overwritten with the corresponding files from the source
   * of the copy operation. When this flag is not present, existing files at the destination are not
   * overwritten and an exception is thrown indicating what files could not be copied. No exception
   * is thrown for directories that already exist at the destination.
   *
   * <p>Copying a file into a directory of the same name or vice versa always throws a <code>
   * CoreException</code>, regardless of whether the <code>OVERWRITE</code> flag is specified or
   * not.
   *
   * <p>The {@link EFS#SHALLOW} option flag indicates how this method deals with copying of
   * directories. If the <code>SHALLOW</code> flag is present, then a directory will be copied but
   * the files and directories within it will not. When this flag is not present, all child
   * directories and files of a directory are copied recursively.
   *
   * <p>In case of a recursive directory copy exception throwing may be deferred. Part of the copy
   * task may be executed without rollback until the exception occurs. The order of copy operations
   * is not specified.
   *
   * @param destination The destination of the copy.
   * @param options bit-wise or of option flag constants ( {@link EFS#OVERWRITE} or {@link
   *     EFS#SHALLOW}).
   * @param monitor a progress monitor, or <code>null</code> if progress reporting and cancellation
   *     are not desired
   * @exception CoreException if this method fails. Reasons include:
   *     <ul>
   *       <li>This store does not exist.
   *       <li>The parent of the destination file store does not exist.
   *       <li>The <code>OVERWRITE</code> flag is not specified and a file of the same name already
   *           exists at the copy destination.
   *       <li>A file is being copied, but a directory of the same name already exists at the copy
   *           destination.
   *       <li>A directory is being copied, but a file of the same name already exists at the copy
   *           destination.
   *     </ul>
   */
  public void copy(IFileStore destination, int options, IProgressMonitor monitor)
      throws CoreException;

  /**
   * Deletes the files and directories represented by this store. Deletion of a file that does not
   * exist has no effect.
   *
   * <p>Deletion occurs with best-effort semantics; if some files cannot be deleted, exceptions are
   * recorded but other files will continue to be deleted if possible.
   *
   * <p>Deletion of a file with attribute {@link EFS#ATTRIBUTE_SYMLINK} will always delete the link,
   * rather than the target of the link.
   *
   * @param options bit-wise or of option flag constants (currently only {@link EFS#NONE} is
   *     applicable).
   * @param monitor a progress monitor, or <code>null</code> if progress reporting and cancellation
   *     are not desired
   * @exception CoreException if this method fails. Reasons include:
   *     <ul>
   *       <li>Files or directories could not be deleted.
   *     </ul>
   *
   * @see EFS#ATTRIBUTE_SYMLINK
   */
  public void delete(int options, IProgressMonitor monitor) throws CoreException;

  /**
   * Fetches and returns information about this file from the underlying file system. Returns a file
   * info representing a non-existent file if the underlying file system could not be contacted.
   *
   * <p>This is a convenience method, similar to: <code>fetchInfo(EFS.NONE, null)</code>. This
   * method is intended as a convenience when dealing with fast, highly available file systems such
   * as the local file system. Clients that require progress reporting and error handling, for
   * example when dealing with remote file systems, should use {@link #fetchInfo(int,
   * IProgressMonitor)} instead.
   *
   * @return A structure containing information about this file.
   * @see #fetchInfo(int, IProgressMonitor)
   */
  public IFileInfo fetchInfo();

  /**
   * Fetches and returns information about this file from the underlying file system.
   *
   * <p>This method succeeds regardless of whether a corresponding file currently exists in the
   * underlying file system. In the case of a non-existent file, the returned info will include the
   * file's name and will return <code>false</code> when IFileInfo#exists() is called, but all other
   * information will assume default values.
   *
   * @param options bit-wise or of option flag constants (currently only {@link EFS#NONE} is
   *     applicable).
   * @param monitor a progress monitor, or <code>null</code> if progress reporting and cancellation
   *     are not desired
   * @return A structure containing information about this file.
   * @exception CoreException if this method fails. Reasons include:
   *     <ul>
   *       <li>Problems occurred while contacting the file system.
   *     </ul>
   *
   * @see IFileTree#getFileInfo(IFileStore)
   */
  public IFileInfo fetchInfo(int options, IProgressMonitor monitor) throws CoreException;

  /**
   * Returns a child of this store as specified by the provided path. The path is treated as
   * relative to this store. This is equivalent to
   *
   * <pre>
   *    IFileStore result = this;
   *    for (int i = 0; i < path.segmentCount(); i++) {
   *       result = result.getChild(path.segment(i));
   *    return result;
   * </pre>
   *
   * <p>This is a handle-only method; a child is provided regardless of whether this store or the
   * child store exists, or whether this store represents a directory or not.
   *
   * <p>The provided path must not contain segments that are self references (".") or parent
   * references ("..").
   *
   * @param path The path of the child store to return
   * @return A child file store.
   * @deprecated use {@link #getFileStore(IPath)} instead
   */
  public IFileStore getChild(IPath path);

  /**
   * Returns a handle to the member store identified by the given path. The path is treated as
   * relative to this store.
   *
   * <p>This is a handle-only method; a store is provided regardless of whether this store or the
   * member store exists, or whether this store represents a directory or not.
   *
   * @param path the path of the member store
   * @return the member store
   * @since org.eclipse.core.filesystem 1.2
   */
  public IFileStore getFileStore(IPath path);

  /**
   * Returns a child store with the provided name whose parent is this store. This is a handle-only
   * method; a child is provided regardless of whether this store or the child store exists, or
   * whether this store represents a directory or not.
   *
   * @param name The name of the child store to return
   * @return A child file store.
   */
  public IFileStore getChild(String name);

  //	/**
  //	 * Returns the file system this store belongs to.
  //	 *
  //	 * @return The file system this store belongs to.
  //	 */
  //	public IFileSystem getFileSystem();

  /**
   * Returns the name of this store. This is a handle-only method; the name is returned regardless
   * of whether this store exists.
   *
   * <p>Note that when dealing with case-insensitive file systems, this name may differ in case from
   * the name of the corresponding file in the file system. To obtain the exact name used in the
   * file system, use <code>fetchInfo().getName()</code>.
   *
   * @return The name of this store
   */
  public String getName();

  /**
   * Returns the parent of this store. This is a handle only method; the parent is returned
   * regardless of whether this store or the parent store exists. This method returns <code>null
   * </code> when this store represents the root directory of a file system.
   *
   * @return The parent store, or <code>null</code> if this store is the root of a file system.
   */
  public IFileStore getParent();

  /**
   * Returns whether this store is a parent of the provided store. This is equivalent to, but
   * typically more efficient than, the following: <code>
   * while (true) {
   * 	other = other.getParent();
   * 	if (other == null)
   * 		return false;
   * 	if (this.equals(other))
   * 		return true;
   * }
   * </code>
   *
   * <p>This is a handle only method; this test works regardless of whether this store or the
   * parameter store exists.
   *
   * @param other The store to test for parentage.
   * @return <code>true</code> if this store is a parent of the provided store, and <code>false
   *     </code> otherwise.
   */
  public boolean isParentOf(IFileStore other);

  /**
   * Creates a directory, and optionally its parent directories. If the directory already exists,
   * this method has no effect.
   *
   * <p>The {@link EFS#SHALLOW} option flag indicates how this method deals with creation when the
   * parent directory does not exist. If the <code>SHALLOW</code> flag is present, this method will
   * fail if the parent directory does not exist. When the flag is not present, all necessary parent
   * directories are also created.
   *
   * @param options bit-wise or of option flag constants ({@link EFS#SHALLOW}).
   * @param monitor a progress monitor, or <code>null</code> if progress reporting and cancellation
   *     are not desired
   * @return This directory
   * @exception CoreException if this method fails. Reasons include:
   *     <ul>
   *       <li>The directory could not be created
   *       <li>A file already exists with this name that is not a directory
   *       <li>The {@link EFS#SHALLOW} option flag was specified and the parent of this directory
   *           does not exist.
   *     </ul>
   */
  public IFileStore mkdir(int options, IProgressMonitor monitor) throws CoreException;

  /**
   * Moves the file represented by this store to the provided destination store. Moving occurs with
   * best-effort semantics; if some files cannot be moved, exceptions are recorded but other files
   * will continue to be moved if possible.
   *
   * <p>The {@link EFS#OVERWRITE} option flag indicates how this method deals with files that
   * already exist at the move destination. If the <code>OVERWRITE</code> flag is present, then
   * existing files at the destination are overwritten with the corresponding files from the source
   * of the move operation. When this flag is not present, existing files at the destination are not
   * overwritten and an exception is thrown indicating what files could not be moved.
   *
   * @param destination The destination of the move.
   * @param options bit-wise or of option flag constants ({@link EFS#OVERWRITE}).
   * @param monitor a progress monitor, or <code>null</code> if progress reporting and cancellation
   *     are not desired
   * @exception CoreException if this method fails. Reasons include:
   *     <ul>
   *       <li>This store does not exist.
   *       <li>The parent of the destination file store does not exist.
   *       <li>The {@link EFS#OVERWRITE} flag is not specified and a file of the same name already
   *           exists at the destination.
   *     </ul>
   */
  public void move(IFileStore destination, int options, IProgressMonitor monitor)
      throws CoreException;

  /**
   * Returns an open input stream on the contents of this file. The number of concurrently open
   * streams depends on the implementation and can be limited. The caller is responsible for closing
   * the provided stream when it is no longer needed.
   *
   * <p>The returned stream is not guaranteed to be buffered efficiently. When reading large blocks
   * of data from the stream, a <code>BufferedInputStream</code> wrapper should be used, or some
   * other form of content buffering.
   *
   * <p>It depends on the implementation how the limit of concurrently opened streams is handled.
   * <code>CoreException</code> may be thrown, when the limit is exceeded.
   *
   * @param options bit-wise or of option flag constants (currently only {@link EFS#NONE} is
   *     applicable).
   * @param monitor a progress monitor, or <code>null</code> if progress reporting and cancellation
   *     are not desired
   * @return An input stream on the contents of this file.
   * @exception CoreException if this method fails. Reasons include:
   *     <ul>
   *       <li>This store does not exist.
   *       <li>This store represents a directory.
   *       <li>The limit of concurrently opened streams has been exceeded.
   *     </ul>
   */
  public InputStream openInputStream(int options, IProgressMonitor monitor) throws CoreException;

  /**
   * Returns an open output stream on the contents of this file. The number of concurrently open
   * streams depends on implementation and can be limited. The caller is responsible for closing the
   * provided stream when it is no longer needed. This file need not exist in the underlying file
   * system at the time this method is called.
   *
   * <p>The returned stream is not guaranteed to be buffered efficiently. When writing large blocks
   * of data to the stream, a <code>BufferedOutputStream</code> wrapper should be used, or some
   * other form of content buffering.
   *
   * <p>It depends on the implementation how the limit of concurrently opened streams is handled.
   * <code>CoreException</code> may be thrown, when the limit is exceeded.
   *
   * <p>The {@link EFS#APPEND} update flag controls where output is written to the file. If this
   * flag is specified, content written to the stream will be appended to the end of the file. If
   * this flag is not specified, the contents of the existing file, if any, is truncated to zero and
   * the new output will be written from the start of the file.
   *
   * @param options bit-wise or of option flag constants ( {@link EFS#APPEND}).
   * @param monitor a progress monitor, or <code>null</code> if progress reporting and cancellation
   *     are not desired
   * @return An output stream on the contents of this file.
   * @exception CoreException if this method fails. Reasons include:
   *     <ul>
   *       <li>This store represents a directory.
   *       <li>The parent of this store does not exist.
   *       <li>The limit of concurrently opened streams has been exceeded.
   *     </ul>
   */
  public OutputStream openOutputStream(int options, IProgressMonitor monitor) throws CoreException;

  /**
   * Writes information about this file to the underlying file system. Only certain parts of the
   * file information structure can be written using this method, as specified by the option flags.
   * Other changed information in the provided info will be ignored. This method has no effect when
   * no option flags are provided. The following example sets the last modified time for a file
   * store, leaving other values unchanged:
   *
   * <pre>
   *    IFileInfo info = EFS#createFileInfo();
   *    info.setLastModified(System.currentTimeMillis());
   *    store.putInfo(info, EFS.SET_LAST_MODIFIED, monitor);
   * </pre>
   *
   * <p>The {@link EFS#SET_ATTRIBUTES} update flag controls whether the file's attributes are
   * changed. When this flag is specified, the <code>EFS#ATTRIBUTE_*</code> values, with the
   * exception of <code>EFS#ATTRIBUTE_DIRECTORY</code>, <code>EFS#ATTRIBUTE_SYMLINK</code> and
   * <code>EFS#ATTRIBUTE_LINK_TARGET</code>, are set for this file. When this flag is not specified,
   * changed attributes on the provided file info are ignored.
   *
   * <p>Since Eclipse 3.6, implementations shall also make a best effort to consult UNIX umask in
   * order to set the same attributes for other access groups. This setting of attributes for others
   * may change the file system state even if an attribute appears to be set for the current user
   * already.
   *
   * <p>Clearing an attribute causes clearing it for all access groups. This means setting and
   * clearing an attribute might not restore previous file system state as these operations are not
   * symmetrical.
   *
   * <p>The {@link EFS#SET_LAST_MODIFIED} update flag controls whether the file's last modified time
   * is changed. When this flag is specified, the last modified time for the file in the underlying
   * file system is updated to the value in the provided info object. Due to the different
   * granularities of file systems, the time that is set might not exact match the provided time.
   *
   * @param info The file information instance containing the values to set.
   * @param options bit-wise or of option flag constants ( {@link EFS#SET_ATTRIBUTES} or {@link
   *     EFS#SET_LAST_MODIFIED}).
   * @param monitor a progress monitor, or <code>null</code> if progress reporting and cancellation
   *     are not desired
   * @exception CoreException if this method fails. Reasons include:
   *     <ul>
   *       <li>This store does not exist.
   *     </ul>
   *
   * @see EFS#createFileInfo()
   */
  public void putInfo(IFileInfo info, int options, IProgressMonitor monitor) throws CoreException;

  /**
   * Returns a file in the local file system with the same state as this file.
   *
   * <p>The {@link EFS#CACHE} option flag indicates whether this method should return the actual
   * underlying file or a cached local copy. When the {@link EFS#CACHE} flag is specified, this
   * method will return a cached local file with the same state and contents as this file. When the
   * {@link EFS#CACHE} flag is not specified, this method will return the actual underlying local
   * file, or <code>null</code> if this store is not a local file.
   *
   * <p>In the case of a cached file, the returned file is intended to be used for read operations
   * only. No guarantee is made about synchronization between the returned file and this store. If
   * the cached file is modified in any way, those changes may not be reflected in this store, but
   * may affect other callers who are using the local representation of this store.
   *
   * <p>While the implementation of this method may use caching to return the same result for
   * multiple calls to this method, it is guaranteed that the returned file will reflect the state
   * of this file store at the time of this call. As such, this method will always contact the
   * backing file system of this store, either to validate cache consistency or to fetch new
   * contents.
   *
   * <p>The caller is not responsible for deleting this file when they are done with using it. If
   * the returned file is a cached copy, it will be deleted automatically at the end of this session
   * (Eclipse shutdown or virtual machine exit).
   *
   * @param options bit-wise or of option flag constants ( only {@link EFS#CACHE} applies).
   * @param monitor a progress monitor, or <code>null</code> if progress reporting and cancellation
   *     are not desired
   * @return A local file with the same state as this file or <code>null</code> if {@link EFS#CACHE}
   *     is not specified and this is not a local file.
   * @exception CoreException if this method fails. Reasons include:
   *     <ul>
   *       <li>A corresponding file could not be created in the local file system.
   *     </ul>
   *
   * @see IFileSystem#fromLocalFile(java.io.File)
   */
  public java.io.File toLocalFile(int options, IProgressMonitor monitor) throws CoreException;

  /**
   * Returns a string representation of this store. The string will be translated if applicable, and
   * suitable for displaying in error messages to an end-user. The actual format of the string is
   * unspecified.
   *
   * @return A string representation of this store.
   */
  public String toString();

  /**
   * Returns a URI instance corresponding to this store. The resulting URI, when passed to {@link
   * EFS#getStore(URI)}, will return a store equal to this instance.
   *
   * @return A URI corresponding to this store.
   * @see EFS#getStore(URI)
   */
  public URI toURI();
}
