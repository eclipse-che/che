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
package org.eclipse.core.filesystem.provider;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;

/**
 * This class should be used by file system providers in their implementation of API methods that
 * return {@link IFileInfo} objects.
 *
 * @since org.eclipse.core.filesystem 1.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public class FileInfo implements IFileInfo {
  /** Internal attribute indicating if the file is a directory */
  private static final int ATTRIBUTE_DIRECTORY = 1 << 0;

  /** Internal attribute indicating if the file exists. */
  private static final int ATTRIBUTE_EXISTS = 1 << 16;

  /** Bit field of file attributes. Initialized to specify a writable resource. */
  private int attributes = EFS.ATTRIBUTE_OWNER_WRITE | EFS.ATTRIBUTE_OWNER_READ;

  private int errorCode = NONE;

  /** The last modified time. */
  private long lastModified = EFS.NONE;

  /** The file length. */
  private long length = EFS.NONE;

  /** The file name. */
  private String name = ""; // $NON-NLS-1$

  /** The target file name if this is a symbolic link */
  private String linkTarget = null;

  /** Creates a new file information object with default values. */
  public FileInfo() {
    super();
  }

  /**
   * Creates a new file information object. All values except the file name will have default
   * values.
   *
   * @param name The name of this file
   */
  public FileInfo(String name) {
    super();
    this.name = name;
  }

  /**
   * Convenience method to clear a masked region of the attributes bit field.
   *
   * @param mask The mask to be cleared
   */
  private void clear(int mask) {
    attributes &= ~mask;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#clone()
   */
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      // we know this object is cloneable
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Object o) {
    return name.compareTo(((FileInfo) o).name);
  }

  /* (non-Javadoc)
   * @see org.eclipse.core.filesystem.IFileInfo#exists()
   */
  public boolean exists() {
    return getAttribute(ATTRIBUTE_EXISTS);
  }

  /**
   * @since 1.4
   * @see IFileInfo#getError()
   */
  public int getError() {
    return errorCode;
  }

  public boolean getAttribute(int attribute) {
    if (attribute == EFS.ATTRIBUTE_READ_ONLY && isAttributeSuported(EFS.ATTRIBUTE_OWNER_WRITE))
      return (!isSet(EFS.ATTRIBUTE_OWNER_WRITE)) || isSet(EFS.ATTRIBUTE_IMMUTABLE);
    else if (attribute == EFS.ATTRIBUTE_EXECUTABLE
        && isAttributeSuported(EFS.ATTRIBUTE_OWNER_EXECUTE))
      return isSet(EFS.ATTRIBUTE_OWNER_EXECUTE);
    else return isSet(attribute);
  }

  /* (non-Javadoc)
   * @see org.eclipse.core.filesystem.IFileInfo#getStringAttribute(int)
   */
  public String getStringAttribute(int attribute) {
    if (attribute == EFS.ATTRIBUTE_LINK_TARGET) return this.linkTarget;
    return null;
  }

  /* (non-Javadoc)
   * @see org.eclipse.core.filesystem.IFileInfo#lastModified()
   */
  public long getLastModified() {
    return lastModified;
  }

  /* (non-Javadoc)
   * @see org.eclipse.core.filesystem.IFileInfo#length()
   */
  public long getLength() {
    return length;
  }

  /* (non-Javadoc)
   * @see org.eclipse.core.filesystem.IFileInfo#getName()
   */
  public String getName() {
    return name;
  }

  /* (non-Javadoc)
   * @see org.eclipse.core.filesystem.IFileInfo#isDirectory()
   */
  public boolean isDirectory() {
    return isSet(ATTRIBUTE_DIRECTORY);
  }

  private boolean isSet(long mask) {
    return (attributes & mask) != 0;
  }

  private void set(int mask) {
    attributes |= mask;
  }

  /* (non-Javadoc)
   * @see org.eclipse.core.filesystem.IFileInfo#setAttribute(int, boolean)
   */
  public void setAttribute(int attribute, boolean value) {
    if (attribute == EFS.ATTRIBUTE_READ_ONLY && isAttributeSuported(EFS.ATTRIBUTE_OWNER_WRITE)) {
      if (value) {
        clear(EFS.ATTRIBUTE_OWNER_WRITE | EFS.ATTRIBUTE_OTHER_WRITE | EFS.ATTRIBUTE_GROUP_WRITE);
        set(EFS.ATTRIBUTE_IMMUTABLE);
      } else {
        set(EFS.ATTRIBUTE_OWNER_WRITE | EFS.ATTRIBUTE_OWNER_READ);
        clear(EFS.ATTRIBUTE_IMMUTABLE);
      }
    } else if (attribute == EFS.ATTRIBUTE_EXECUTABLE
        && isAttributeSuported(EFS.ATTRIBUTE_OWNER_EXECUTE)) {
      if (value) set(EFS.ATTRIBUTE_OWNER_EXECUTE);
      else
        clear(
            EFS.ATTRIBUTE_OWNER_EXECUTE
                | EFS.ATTRIBUTE_GROUP_EXECUTE
                | EFS.ATTRIBUTE_OTHER_EXECUTE);
    } else {
      if (value) set(attribute);
      else clear(attribute);
    }
  }

  private static boolean isAttributeSuported(int value) {
    //		return (LocalFileNativesManager.getSupportedAttributes() & value) != 0;
    return true;
  }

  /**
   * Sets whether this is a file or directory.
   *
   * @param value <code>true</code> if this is a directory, and <code>false</code> if this is a
   *     file.
   */
  public void setDirectory(boolean value) {
    if (value) set(ATTRIBUTE_DIRECTORY);
    else clear(ATTRIBUTE_DIRECTORY);
  }

  /**
   * Sets whether this file or directory exists.
   *
   * @param value <code>true</code> if this file exists, and <code>false</code> otherwise.
   */
  public void setExists(boolean value) {
    if (value) set(ATTRIBUTE_EXISTS);
    else clear(ATTRIBUTE_EXISTS);
  }

  /**
   * Sets the error code indicating whether an I/O error was encountered when accessing the file.
   *
   * @param errorCode {@link IFileInfo#IO_ERROR} if this file has an I/O error, and {@link
   *     IFileInfo#NONE} otherwise.
   * @since 1.4
   */
  public void setError(int errorCode) {
    this.errorCode = errorCode;
  }

  /* (non-Javadoc)
   * @see org.eclipse.core.filesystem.IFileInfo#setLastModified(long)
   */
  public void setLastModified(long value) {
    lastModified = value;
  }

  /**
   * Sets the length of this file. A value of {@link EFS#NONE} indicates the file does not exist, is
   * a directory, or the length could not be computed.
   *
   * @param value the length of this file, or {@link EFS#NONE}
   */
  public void setLength(long value) {
    this.length = value;
  }

  /**
   * Sets the name of this file.
   *
   * @param name The file name
   */
  public void setName(String name) {
    if (name == null) throw new IllegalArgumentException();
    this.name = name;
  }

  /**
   * Sets or clears a String attribute, e.g. symbolic link target.
   *
   * @param attribute The kind of attribute to set. Currently only {@link EFS#ATTRIBUTE_LINK_TARGET}
   *     is supported.
   * @param value The String attribute, or <code>null</code> to clear the attribute
   * @since org.eclipse.core.filesystem 1.1
   */
  public void setStringAttribute(int attribute, String value) {
    if (attribute == EFS.ATTRIBUTE_LINK_TARGET) this.linkTarget = value;
  }

  /** For debugging purposes only. */
  public String toString() {
    return name;
  }
}
