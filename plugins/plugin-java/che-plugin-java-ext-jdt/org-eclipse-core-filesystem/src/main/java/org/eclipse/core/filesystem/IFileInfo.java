/**
 * ***************************************************************************** Copyright (c) 2005,
 * 2012 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation Martin Oberhuber (Wind River) -
 * [170317] add symbolic link support to API
 * *****************************************************************************
 */
package org.eclipse.core.filesystem;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A file info is a simple structure holding information about a file or directory. The information
 * contained here is static; changes to this object will not cause corresponding changes to any file
 * on disk, and changes to files on disk are not reflected in this object. At best, an IFileInfo
 * represents a snapshot of the state of a file at a particular moment in time.
 *
 * @see IFileStore#fetchInfo(int, IProgressMonitor)
 * @see IFileStore#putInfo(IFileInfo, int, IProgressMonitor)
 * @since org.eclipse.core.filesystem 1.0
 * @noimplement This interface is not intended to be implemented by clients. File store
 *     implementations should use the concrete class {@link
 *     org.eclipse.core.filesystem.provider.FileStore}
 */
public interface IFileInfo extends Comparable, Cloneable {
  /**
   * The constant indicating that file information was retrieved successfully.
   *
   * @since 1.4
   */
  public static final int NONE = 0;
  /**
   * The constant indicating that an I/O error was encountered while retrieving file information.
   *
   * @since 1.4
   */
  public static final int IO_ERROR = 5; // The value is chosen to match EIO Linux errno value.

  /**
   * Returns whether this file or directory exists.
   *
   * @return <code>true</code> if this file exists, and <code>false</code> if the file does not
   *     exist or an I/O error was encountered.
   */
  public abstract boolean exists();

  /**
   * Checks whether an I/O error was encountered while accessing this file or directory.
   *
   * @return {@link #IO_ERROR} if an I/O error was encountered, or {@link #NONE} otherwise.
   * @since 1.4
   */
  public abstract int getError();

  /**
   * Returns the value of the specified attribute for this file. The attribute must be one of the
   * <code>EFS#ATTRIBUTE_*</code> constants. Returns <code>false</code> if this file does not exist,
   * could not be accessed, or the provided attribute does not apply to this file system.
   *
   * @param attribute The attribute to retrieve the value for
   * @return the value of the specified attribute for this file.
   * @see IFileSystem#attributes()
   */
  public abstract boolean getAttribute(int attribute);

  /**
   * Returns the value of the specified attribute for this file. The attribute must be one of the
   * <code>EFS#ATTRIBUTE_*</code> constants. Returns <code>null</code> if this file does not exist,
   * could not be accessed, or the provided attribute does not apply to this file system.
   *
   * @param attribute The kind of attribute to return. Currently only {@link
   *     EFS#ATTRIBUTE_LINK_TARGET} is supported.
   * @return the value of the extended String attribute for this file.
   * @see IFileSystem#attributes()
   * @since org.eclipse.core.filesystem 1.1
   */
  public abstract String getStringAttribute(int attribute);

  /**
   * Returns the last modified time for this file, or {@link EFS#NONE} if the file does not exist or
   * the last modified time could not be computed.
   *
   * <p>The time is represented as the number of Universal Time (UT) milliseconds since the epoch
   * (00:00:00 GMT, January 1, 1970).
   *
   * @return the last modified time for this file, or {@link EFS#NONE}
   */
  public abstract long getLastModified();

  /**
   * Returns the length of this file, or {@link EFS#NONE} if the file does not exist, or the length
   * could not be computed. For directories, the return value is unspecified.
   *
   * @return the length of this file, or {@link EFS#NONE}
   */
  public abstract long getLength();

  /**
   * Returns the name of this file.
   *
   * @return the name of this file.
   */
  public abstract String getName();

  /**
   * Returns whether this file is a directory, or <code>false</code> if this file does not exist.
   *
   * @return <code>true</code> if this file is a directory, and <code>false</code> otherwise.
   */
  public abstract boolean isDirectory();

  /**
   * Sets the value of the specified attribute for this file info. The attribute must be one of the
   * <code>EFS#ATTRIBUTE_*</code> constants. Note that not all attributes are applicable in a given
   * file system.
   *
   * <p>Users must call {@link IFileStore#putInfo(IFileInfo, int, IProgressMonitor)} before changes
   * made to this info take effect in an underlying file.
   *
   * @param attribute The attribute to set the value for
   * @param value the value of the specified attribute for this file.
   * @see IFileSystem#attributes()
   */
  public abstract void setAttribute(int attribute, boolean value);

  /**
   * Sets the last modified time for this file. A value of {@link EFS#NONE} indicates the file does
   * not exist or the last modified time could not be computed.
   *
   * <p>Users must call {@link IFileStore#putInfo(IFileInfo, int, IProgressMonitor)} before changes
   * made to this info take effect in an underlying file.
   *
   * @param time the last modified time for this file, or {@link EFS#NONE}
   */
  public abstract void setLastModified(long time);
}
