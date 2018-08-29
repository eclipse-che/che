/**
 * ***************************************************************************** Copyright (c) 2005,
 * 2013 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation Martin Oberhuber (Wind River) -
 * [170317] add symbolic link support to API
 * *****************************************************************************
 */
package org.eclipse.core.filesystem;

import java.io.File;
import java.net.URI;
import org.eclipse.core.internal.filesystem.LocalFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This class is the main entry point for clients of the Eclipse file system API. This class has
 * factory methods for obtaining instances of file systems and file stores, and provides constants
 * for option values and error codes.
 *
 * @since org.eclipse.core.filesystem 1.0
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class EFS {

  /**
   * The unique identifier constant (value "<code>org.eclipse.core.filesystem</code>") of the Core
   * file system plug-in.
   */
  public static final String PI_FILE_SYSTEM = "org.eclipse.core.filesystem"; // $NON-NLS-1$

  /**
   * The simple identifier constant (value "<code>filesystems</code>") of the extension point of the
   * Core file system plug-in where plug-ins declare file system implementations.
   */
  public static final String PT_FILE_SYSTEMS = "filesystems"; // $NON-NLS-1$

  /**
   * A constant known to be zero (0), used in operations which take bit flags to indicate that "no
   * bits are set". This value is also used as a default value in cases where a file system
   * attribute cannot be computed.
   *
   * @see IFileInfo#getLength()
   * @see IFileInfo#getLastModified()
   */
  public static final int NONE = 0;

  /**
   * Option flag constant (value 1 &lt;&lt;0) indicating a file opened for appending data to the
   * end.
   *
   * @see IFileStore#openOutputStream(int, IProgressMonitor)
   */
  public static final int APPEND = 1 << 0;

  /**
   * Option flag constant (value 1 &lt;&lt;1) indicating that existing files may be overwritten.
   *
   * @see IFileStore#copy(IFileStore, int, IProgressMonitor)
   * @see IFileStore#move(IFileStore, int, IProgressMonitor)
   */
  public static final int OVERWRITE = 1 << 1;

  /**
   * Option flag constant (value 1 &lt;&lt;2) indicating that an operation acts on a single file or
   * directory, and not its parents or children.
   *
   * @see IFileStore#copy(IFileStore, int, IProgressMonitor)
   * @see IFileStore#mkdir(int, IProgressMonitor)
   */
  public static final int SHALLOW = 1 << 2;

  /**
   * Option flag constant (value 1 &lt;&lt;10) indicating that a file's attributes should be
   * updated.
   *
   * @see IFileStore#putInfo(IFileInfo, int, IProgressMonitor)
   */
  public static final int SET_ATTRIBUTES = 1 << 10;

  /**
   * Option flag constant (value 1 &lt;&lt;11) indicating that a file's last modified time should be
   * updated.
   *
   * @see IFileStore#putInfo(IFileInfo, int, IProgressMonitor)
   */
  public static final int SET_LAST_MODIFIED = 1 << 11;

  /**
   * Option flag constant (value 1 &lt;&lt;12) indicating that a cached representation of a file
   * should be returned.
   *
   * @see IFileStore#toLocalFile(int, IProgressMonitor)
   */
  public static final int CACHE = 1 << 12;

  /**
   * Attribute constant (value 1 &lt;&lt;1) indicating that a file is read only.
   *
   * @see IFileStore#fetchInfo()
   * @see IFileStore#putInfo(IFileInfo, int, IProgressMonitor)
   * @see IFileInfo#getAttribute(int)
   * @see IFileInfo#setAttribute(int, boolean)
   */
  public static final int ATTRIBUTE_READ_ONLY = 1 << 1;

  /**
   * Attribute constant (value 1 &lt;&lt;21) indicating that a file is marked with immutable flag.
   *
   * @see IFileStore#fetchInfo()
   * @see IFileStore#putInfo(IFileInfo, int, IProgressMonitor)
   * @see IFileInfo#getAttribute(int)
   * @see IFileInfo#setAttribute(int, boolean)
   * @since org.eclipse.core.filesystem 1.3
   */
  public static final int ATTRIBUTE_IMMUTABLE = 1 << 21;

  /**
   * Attribute constant (value 1 &lt;&lt;22) indicating that a file's owner has a read permission.
   *
   * @see IFileStore#fetchInfo()
   * @see IFileStore#putInfo(IFileInfo, int, IProgressMonitor)
   * @see IFileInfo#getAttribute(int)
   * @see IFileInfo#setAttribute(int, boolean)
   * @since org.eclipse.core.filesystem 1.3
   */
  public static final int ATTRIBUTE_OWNER_READ = 1 << 22;

  /**
   * Attribute constant (value 1 &lt;&lt;23) indicating that file's owner has a write permission.
   *
   * @see IFileStore#fetchInfo()
   * @see IFileStore#putInfo(IFileInfo, int, IProgressMonitor)
   * @see IFileInfo#getAttribute(int)
   * @see IFileInfo#setAttribute(int, boolean)
   * @since org.eclipse.core.filesystem 1.3
   */
  public static final int ATTRIBUTE_OWNER_WRITE = 1 << 23;

  /**
   * Attribute constant (value 1 &lt;&lt;24) indicating that file's owner has an execute permission.
   *
   * @see IFileStore#fetchInfo()
   * @see IFileStore#putInfo(IFileInfo, int, IProgressMonitor)
   * @see IFileInfo#getAttribute(int)
   * @see IFileInfo#setAttribute(int, boolean)
   * @since org.eclipse.core.filesystem 1.3
   */
  public static final int ATTRIBUTE_OWNER_EXECUTE = 1 << 24;

  /**
   * Attribute constant (value 1 &lt;&lt;25) indicating that users in file's group have a read
   * permission.
   *
   * @see IFileStore#fetchInfo()
   * @see IFileStore#putInfo(IFileInfo, int, IProgressMonitor)
   * @see IFileInfo#getAttribute(int)
   * @see IFileInfo#setAttribute(int, boolean)
   * @since org.eclipse.core.filesystem 1.3
   */
  public static final int ATTRIBUTE_GROUP_READ = 1 << 25;

  /**
   * Attribute constant (value 1 &lt;&lt;26) indicating that users in file's group have a write
   * permission.
   *
   * @see IFileStore#fetchInfo()
   * @see IFileStore#putInfo(IFileInfo, int, IProgressMonitor)
   * @see IFileInfo#getAttribute(int)
   * @see IFileInfo#setAttribute(int, boolean)
   * @since org.eclipse.core.filesystem 1.3
   */
  public static final int ATTRIBUTE_GROUP_WRITE = 1 << 26;

  /**
   * Attribute constant (value 1 &lt;&lt;27) indicating that users in file's group have an execute
   * permission.
   *
   * @see IFileStore#fetchInfo()
   * @see IFileStore#putInfo(IFileInfo, int, IProgressMonitor)
   * @see IFileInfo#getAttribute(int)
   * @see IFileInfo#setAttribute(int, boolean)
   * @since org.eclipse.core.filesystem 1.3
   */
  public static final int ATTRIBUTE_GROUP_EXECUTE = 1 << 27;

  /**
   * Attribute constant (value 1 &lt;&lt;28) indicating that other users have a read permission.
   *
   * @see IFileStore#fetchInfo()
   * @see IFileStore#putInfo(IFileInfo, int, IProgressMonitor)
   * @see IFileInfo#getAttribute(int)
   * @see IFileInfo#setAttribute(int, boolean)
   * @since org.eclipse.core.filesystem 1.3
   */
  public static final int ATTRIBUTE_OTHER_READ = 1 << 28;

  /**
   * Attribute constant (value 1 &lt;&lt;29) indicating that other users have a write permission.
   *
   * @see IFileStore#fetchInfo()
   * @see IFileStore#putInfo(IFileInfo, int, IProgressMonitor)
   * @see IFileInfo#getAttribute(int)
   * @see IFileInfo#setAttribute(int, boolean)
   * @since org.eclipse.core.filesystem 1.3
   */
  public static final int ATTRIBUTE_OTHER_WRITE = 1 << 29;

  /**
   * Attribute constant (value 1 &lt;&lt;30) indicating that other users have an execute permission.
   *
   * @see IFileStore#fetchInfo()
   * @see IFileStore#putInfo(IFileInfo, int, IProgressMonitor)
   * @see IFileInfo#getAttribute(int)
   * @see IFileInfo#setAttribute(int, boolean)
   * @since org.eclipse.core.filesystem 1.3
   */
  public static final int ATTRIBUTE_OTHER_EXECUTE = 1 << 30;

  /**
   * Attribute constant (value 1 &lt;&lt;2) indicating that a file is a executable.
   *
   * @see IFileStore#fetchInfo()
   * @see IFileStore#putInfo(IFileInfo, int, IProgressMonitor)
   * @see IFileInfo#getAttribute(int)
   * @see IFileInfo#setAttribute(int, boolean)
   */
  public static final int ATTRIBUTE_EXECUTABLE = 1 << 2;

  /**
   * Attribute constant (value 1 &lt;&lt;3) indicating that a file is an archive.
   *
   * @see IFileStore#fetchInfo()
   * @see IFileStore#putInfo(IFileInfo, int, IProgressMonitor)
   * @see IFileInfo#getAttribute(int)
   * @see IFileInfo#setAttribute(int, boolean)
   */
  public static final int ATTRIBUTE_ARCHIVE = 1 << 3;

  /**
   * Attribute constant (value 1 &lt;&lt;4) indicating that a file is hidden.
   *
   * @see IFileStore#fetchInfo()
   * @see IFileStore#putInfo(IFileInfo, int, IProgressMonitor)
   * @see IFileInfo#getAttribute(int)
   * @see IFileInfo#setAttribute(int, boolean)
   */
  public static final int ATTRIBUTE_HIDDEN = 1 << 4;

  /**
   * Attribute constant (value 1 &lt;&lt;5) indicating that a file is a symbolic link.
   *
   * <p>If this attribute is <code>true</code> for a given {@link IFileInfo} instance, a String
   * value may be associated with the attribute holding the symbolic link target. This link target
   * can be retrieved with {@link IFileInfo#getStringAttribute(int)} with attribute type {@link
   * #ATTRIBUTE_LINK_TARGET}.
   *
   * <p>Symbolic links are handled transparently, as implemented by the underlying operating system.
   * This means, that all other attributes of a {@link IFileInfo} apply to the link target instead
   * of the link. Reading or writing a file, or changing attributes applies to the link target and
   * not the link itself. In case a symbolic link points to another symbolic link, the chain of
   * links is transparently followed and operations apply to the actual file or directory being
   * referenced by the chain of symbolic links.
   *
   * <p>Broken symbolic links (which do not reference any valid file or directory) are being
   * returned by {@link IFileStore#childInfos(int, IProgressMonitor)}, but {@link
   * IFileInfo#exists()} returns <code>false</code> for these. Operations like reading or writing on
   * broken symbolic links throw a "file not found" exception.
   *
   * @see IFileStore#fetchInfo()
   * @see IFileStore#putInfo(IFileInfo, int, IProgressMonitor)
   * @see IFileInfo#getAttribute(int)
   * @see IFileInfo#setAttribute(int, boolean)
   * @since org.eclipse.core.filesystem 1.1
   */
  public static final int ATTRIBUTE_SYMLINK = 1 << 5;

  /**
   * Attribute constant (value 1 &lt;&lt;6) for a string attribute indicating the target file name
   * of a symbolic link.
   *
   * <p>Note that setting the link target attribute does not cause a symbolic link to be created, or
   * changed to link to a different file. Rather, this attribute is set by file system
   * implementations based on the current state of a link.
   *
   * @see IFileInfo#getStringAttribute(int)
   * @see FileInfo#setStringAttribute(int, String)
   * @see #ATTRIBUTE_SYMLINK
   * @since org.eclipse.core.filesystem 1.1
   */
  public static final int ATTRIBUTE_LINK_TARGET = 1 << 6;

  /**
   * Scheme constant (value "file") indicating the local file system scheme.
   *
   * @see EFS#getLocalFileSystem()
   */
  public static final String SCHEME_FILE = "file"; // $NON-NLS-1$

  /**
   * Scheme constant (value "null") indicating the null file system scheme.
   *
   * @see EFS#getNullFileSystem()
   */
  public static final String SCHEME_NULL = "null"; // $NON-NLS-1$

  /*
   * Status code definitions
   */
  // Errors [266-298]
  /**
   * Status code constant (value 268) indicating a store unexpectedly exists on the file system.
   * Severity: error. Category: file system.
   */
  public static final int ERROR_EXISTS = 268;

  /**
   * Status code constant (value 269) indicating a store unexpectedly does not exist on the file
   * system. Severity: error. Category: file system.
   */
  public static final int ERROR_NOT_EXISTS = 269;

  /**
   * Status code constant (value 270) indicating the file system location for a store could not be
   * computed. Severity: error. Category: file system.
   */
  public static final int ERROR_NO_LOCATION = 270;

  /**
   * Status code constant (value 271) indicating an error occurred while reading from the file
   * system. Severity: error. Category: file system.
   */
  public static final int ERROR_READ = 271;

  /**
   * Status code constant (value 272) indicating an error occurred while writing to the file system.
   * Severity: error. Category: file system.
   */
  public static final int ERROR_WRITE = 272;

  /**
   * Status code constant (value 273) indicating an error occurred while deleting from the file
   * system. Severity: error. Category: file system.
   */
  public static final int ERROR_DELETE = 273;

  /**
   * Status code constant (value 275) indicating this file system is not case sensitive and a file
   * that differs only in case unexpectedly exists on the file system. Severity: error. Category:
   * file system.
   */
  public static final int ERROR_CASE_VARIANT_EXISTS = 275;

  /**
   * Status code constant (value 276) indicating a file exists in the file system but is not of the
   * expected type (file instead of directory, or vice-versa). Severity: error. Category: file
   * system.
   */
  public static final int ERROR_WRONG_TYPE = 276;

  /**
   * Status code constant (value 277) indicating that the parent file in the file system is marked
   * as read-only. Severity: error. Category: file system.
   */
  public static final int ERROR_PARENT_READ_ONLY = 277;

  /**
   * Status code constant (value 279) indicating that the file in the file system is marked as
   * read-only. Severity: error. Category: file system.
   */
  public static final int ERROR_READ_ONLY = 279;

  /**
   * Status code constant (value 280) indicating that the file system failed to authenticate the
   * request. This can be caused by missing or incorrect authentication information being supplied.
   * Severity: error. Category: file system.
   *
   * @since 1.4
   */
  public static final int ERROR_AUTH_FAILED = 280;

  /**
   * Status code constant (value 566) indicating an internal error has occurred. Severity: error.
   * Category: internal.
   */
  public static final int ERROR_INTERNAL = 566;

  private static String WS_PATH;

  //	/**
  //	 * Creates an empty file information object.  The resulting information
  //	 * will represent a non-existent file with no name and no attributes set.
  //	 *
  //	 * @see IFileStore#putInfo(IFileInfo, int, IProgressMonitor)
  //	 * @return an empty file information object.
  //	 */
  //	public static IFileInfo createFileInfo() {
  //		return new FileInfo();
  //	}

  //	/**
  //	 * Returns a file system corresponding to the given scheme.
  //	 *
  //	 * @param scheme The file system URI scheme
  //	 * @return The corresponding file system for the given scheme
  //	 * @exception CoreException if this method fails. Reasons include:
  //	 * <ul>
  //	 * <li>There is no registered file system for the given URI scheme.</li>
  //	 * <li>There was a failure initializing the file system.</li>
  //	 * </ul>
  //	 */
  //	public static IFileSystem getFileSystem(String scheme) throws CoreException {
  //		return InternalFileSystemCore.getInstance().getFileSystem(scheme);
  //	}
  //
  //	/**
  //	 * Returns the local file system.
  //	 *
  //	 * @return The local file system
  //	 */
  //	public static IFileSystem getLocalFileSystem() {
  //		return InternalFileSystemCore.getInstance().getLocalFileSystem();
  //	}
  //
  //	/**
  //	 * Returns the null file system.  The null file system can be used
  //	 * to represent a non-existent or unresolved file system. An example
  //	 * of a null file system is a file system whose location is relative to an undefined
  //	 * variable, or a system whose scheme is unknown.
  //	 * <p>
  //	 * Basic handle-based queries can be performed on the null file system, but all
  //	 * operations that actually require file system access will fail.
  //	 *
  //	 * @return The null file system
  //	 */
  //	public static IFileSystem getNullFileSystem() {
  //		return InternalFileSystemCore.getInstance().getNullFileSystem();
  //	}

  /**
   * Returns the file store corresponding to the provided URI.
   *
   * @param uri The URI of the file store to return
   * @return The file store
   * @exception CoreException if this method fails. Reasons include:
   *     <ul>
   *       <li>There is no registered file system for the given URI scheme.
   *       <li>The URI syntax was not in the appropriate form for that scheme.
   *       <li>There was a failure initializing the file system.
   *     </ul>
   */
  public static IFileStore getStore(URI uri) throws CoreException {
    //		return InternalFileSystemCore.getInstance().getStore(uri);
    //		throw new UnsupportedOperationException("getStore");
    String pathname = uri.getSchemeSpecificPart();
    return new LocalFile(new File(WS_PATH + pathname));
  }

  /** This class is not intended to be instantiated. */
  private EFS() {
    super();
  }

  public static IFileSystem getLocalFileSystem() {
    throw new UnsupportedOperationException("getLocalFileSystem is not supported");
  }

  public static void setWsPath(String wsPath) {
    EFS.WS_PATH = wsPath;
  }
}
