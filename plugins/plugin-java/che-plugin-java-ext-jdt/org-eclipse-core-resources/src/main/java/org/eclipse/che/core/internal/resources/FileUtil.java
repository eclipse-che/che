/**
 * ***************************************************************************** Copyright (c) 2005,
 * 2012 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation Martin Oberhuber (Wind River) -
 * [44107] Add symbolic links to ResourceAttributes API James Blackburn (Broadcom Corp.) - ongoing
 * development *****************************************************************************
 */
package org.eclipse.che.core.internal.resources;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.internal.resources.ResourceException;
import org.eclipse.core.internal.utils.Messages;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.osgi.util.NLS;

/** Static utility methods for manipulating Files and URIs. */
public class FileUtil {
  /**
   * Singleton buffer created to prevent buffer creations in the transferStreams method. Used as an
   * optimization, based on the assumption that multiple writes won't happen in a given instance of
   * FileStore.
   */
  private static final byte[] buffer = new byte[8192];

  //	/**
  //	 * Converts a ResourceAttributes object into an IFileInfo object.
  //	 * @param attributes The resource attributes
  //	 * @return The file info
  //	 */
  //	public static IFileInfo attributesToFileInfo(ResourceAttributes attributes) {
  //		IFileInfo fileInfo = EFS.createFileInfo();
  //		fileInfo.setAttribute(EFS.ATTRIBUTE_READ_ONLY, attributes.isReadOnly());
  //		fileInfo.setAttribute(EFS.ATTRIBUTE_EXECUTABLE, attributes.isExecutable());
  //		fileInfo.setAttribute(EFS.ATTRIBUTE_ARCHIVE, attributes.isArchive());
  //		fileInfo.setAttribute(EFS.ATTRIBUTE_HIDDEN, attributes.isHidden());
  //		fileInfo.setAttribute(EFS.ATTRIBUTE_SYMLINK, attributes.isSymbolicLink());
  //		fileInfo.setAttribute(EFS.ATTRIBUTE_GROUP_READ, attributes.isSet(EFS.ATTRIBUTE_GROUP_READ));
  //		fileInfo.setAttribute(EFS.ATTRIBUTE_GROUP_WRITE, attributes.isSet(EFS.ATTRIBUTE_GROUP_WRITE));
  //		fileInfo.setAttribute(EFS.ATTRIBUTE_GROUP_EXECUTE,
  // attributes.isSet(EFS.ATTRIBUTE_GROUP_EXECUTE));
  //		fileInfo.setAttribute(EFS.ATTRIBUTE_OTHER_READ, attributes.isSet(EFS.ATTRIBUTE_OTHER_READ));
  //		fileInfo.setAttribute(EFS.ATTRIBUTE_OTHER_WRITE, attributes.isSet(EFS.ATTRIBUTE_OTHER_WRITE));
  //		fileInfo.setAttribute(EFS.ATTRIBUTE_OTHER_EXECUTE,
  // attributes.isSet(EFS.ATTRIBUTE_OTHER_EXECUTE));
  //		return fileInfo;
  //	}

  /** Converts an IPath into its canonical form for the local file system. */
  public static IPath canonicalPath(IPath path) {
    if (path == null) return null;
    try {
      final String pathString = path.toOSString();
      final String canonicalPath = new java.io.File(pathString).getCanonicalPath();
      // only create a new path if necessary
      if (canonicalPath.equals(pathString)) return path;
      return new Path(canonicalPath);
    } catch (IOException e) {
      return path;
    }
  }

  /** Converts a URI into its canonical form. */
  public static URI canonicalURI(URI uri) {
    if (uri == null) return null;
    if (EFS.SCHEME_FILE.equals(uri.getScheme())) {
      // only create a new URI if it is different
      final IPath inputPath = URIUtil.toPath(uri);
      final IPath canonicalPath = canonicalPath(inputPath);
      if (inputPath == canonicalPath) return uri;
      return URIUtil.toURI(canonicalPath);
    }
    return uri;
  }

  /**
   * Returns true if the given file system locations overlap. If "bothDirections" is true, this
   * means they are the same, or one is a proper prefix of the other. If "bothDirections" is false,
   * this method only returns true if the locations are the same, or the first location is a prefix
   * of the second. Returns false if the locations do not overlap Does the right thing with respect
   * to case insensitive platforms.
   */
  private static boolean computeOverlap(IPath location1, IPath location2, boolean bothDirections) {
    IPath one = location1;
    IPath two = location2;
    // If we are on a case-insensitive file system then convert to all lower case.
    if (!Workspace.caseSensitive) {
      one = new Path(location1.toOSString().toLowerCase());
      two = new Path(location2.toOSString().toLowerCase());
    }
    return one.isPrefixOf(two) || (bothDirections && two.isPrefixOf(one));
  }

  //	/**
  //	 * Returns true if the given file system locations overlap. If "bothDirections" is true,
  //	 * this means they are the same, or one is a proper prefix of the other.  If "bothDirections"
  //	 * is false, this method only returns true if the locations are the same, or the first location
  //	 * is a prefix of the second.  Returns false if the locations do not overlap
  //	 */
  //	private static boolean computeOverlap(URI location1, URI location2, boolean bothDirections) {
  //		if (location1.equals(location2))
  //			return true;
  //		String scheme1 = location1.getScheme();
  //		String scheme2 = location2.getScheme();
  //		if (scheme1 == null ? scheme2 != null : !scheme1.equals(scheme2))
  //			return false;
  //		if (EFS.SCHEME_FILE.equals(scheme1) && EFS.SCHEME_FILE.equals(scheme2))
  //			return computeOverlap(URIUtil.toPath(location1), URIUtil.toPath(location2), bothDirections);
  //		IFileSystem system = null;
  //		try {
  //			system = EFS.getFileSystem(scheme1);
  //		} catch (CoreException e) {
  //			//handled below
  //		}
  //		if (system == null) {
  //			//we are stuck with string comparison
  //			String string1 = location1.toString();
  //			String string2 = location2.toString();
  //			return string1.startsWith(string2) || (bothDirections && string2.startsWith(string1));
  //		}
  //		IFileStore store1 = system.getStore(location1);
  //		IFileStore store2 = system.getStore(location2);
  //		return store1.equals(store2) || store1.isParentOf(store2) || (bothDirections &&
  // store2.isParentOf(store1));
  //	}
  //
  //	/**
  //	 * Converts an IFileInfo object into a ResourceAttributes object.
  //	 * @param fileInfo The file info
  //	 * @return The resource attributes
  //	 */
  //	public static ResourceAttributes fileInfoToAttributes(IFileInfo fileInfo) {
  //		ResourceAttributes attributes = new ResourceAttributes();
  //		attributes.setReadOnly(fileInfo.getAttribute(EFS.ATTRIBUTE_READ_ONLY));
  //		attributes.setArchive(fileInfo.getAttribute(EFS.ATTRIBUTE_ARCHIVE));
  //		attributes.setExecutable(fileInfo.getAttribute(EFS.ATTRIBUTE_EXECUTABLE));
  //		attributes.setHidden(fileInfo.getAttribute(EFS.ATTRIBUTE_HIDDEN));
  //		attributes.setSymbolicLink(fileInfo.getAttribute(EFS.ATTRIBUTE_SYMLINK));
  //		attributes.set(EFS.ATTRIBUTE_GROUP_READ, fileInfo.getAttribute(EFS.ATTRIBUTE_GROUP_READ));
  //		attributes.set(EFS.ATTRIBUTE_GROUP_WRITE, fileInfo.getAttribute(EFS.ATTRIBUTE_GROUP_WRITE));
  //		attributes.set(EFS.ATTRIBUTE_GROUP_EXECUTE,
  // fileInfo.getAttribute(EFS.ATTRIBUTE_GROUP_EXECUTE));
  //		attributes.set(EFS.ATTRIBUTE_OTHER_READ, fileInfo.getAttribute(EFS.ATTRIBUTE_OTHER_READ));
  //		attributes.set(EFS.ATTRIBUTE_OTHER_WRITE, fileInfo.getAttribute(EFS.ATTRIBUTE_OTHER_WRITE));
  //		attributes.set(EFS.ATTRIBUTE_OTHER_EXECUTE,
  // fileInfo.getAttribute(EFS.ATTRIBUTE_OTHER_EXECUTE));
  //		return attributes;
  //	}
  //
  //	private static String getLineSeparatorFromPreferences(Preferences node) {
  //		try {
  //			// be careful looking up for our node so not to create any nodes as side effect
  //			if (node.nodeExists(Platform.PI_RUNTIME))
  //				return node.node(Platform.PI_RUNTIME).get(Platform.PREF_LINE_SEPARATOR, null);
  //		} catch (BackingStoreException e) {
  //			// ignore
  //		}
  //		return null;
  //	}
  //
  //	/**
  //	 * Returns line separator appropriate for the given file. The returned value
  //	 * will be the first available value from the list below:
  //	 * <ol>
  //	 *   <li> Line separator currently used in that file.
  //	 *   <li> Line separator defined in project preferences.
  //	 *   <li> Line separator defined in instance preferences.
  //	 *   <li> Line separator defined in default preferences.
  //	 *   <li> Operating system default line separator.
  //	 * </ol>
  //	 * @param file the file for which line separator should be returned
  //	 * @return line separator for the given file
  //	 */
  //	public static String getLineSeparator(IFile file) {
  //		if (file.exists()) {
  //			InputStream input = null;
  //			try {
  //				input = file.getContents();
  //				int c = input.read();
  //				while (c != -1 && c != '\r' && c != '\n')
  //					c = input.read();
  //				if (c == '\n')
  //					return "\n"; //$NON-NLS-1$
  //				if (c == '\r') {
  //					if (input.read() == '\n')
  //						return "\r\n"; //$NON-NLS-1$
  //					return "\r"; //$NON-NLS-1$
  //				}
  //			} catch (CoreException e) {
  //				// ignore
  //			} catch (IOException e) {
  //				// ignore
  //			} finally {
  //				safeClose(input);
  //			}
  //		}
  //		Preferences rootNode = Platform.getPreferencesService().getRootNode();
  //		String value = null;
  //		// if the file does not exist or has no content yet, try with project preferences
  //		value =
  // getLineSeparatorFromPreferences(rootNode.node(ProjectScope.SCOPE).node(file.getProject().getName()));
  //		if (value != null)
  //			return value;
  //		// try with instance preferences
  //		value = getLineSeparatorFromPreferences(rootNode.node(InstanceScope.SCOPE));
  //		if (value != null)
  //			return value;
  //		// try with default preferences
  //		value = getLineSeparatorFromPreferences(rootNode.node(DefaultScope.SCOPE));
  //		if (value != null)
  //			return value;
  //		// if there is no preference set, fall back to OS default value
  //		return System.getProperty("line.separator"); //$NON-NLS-1$
  //	}
  //
  //	/**
  //	 * Returns true if the given file system locations overlap, and false otherwise.
  //	 * Overlap means the locations are the same, or one is a proper prefix of the other.
  //	 */
  //	public static boolean isOverlapping(URI location1, URI location2) {
  //		return computeOverlap(location1, location2, true);
  //	}
  //
  //	/**
  //	 * Returns true if location1 is the same as, or a proper prefix of, location2.
  //	 * Returns false otherwise.
  //	 */
  //	public static boolean isPrefixOf(IPath location1, IPath location2) {
  //		return computeOverlap(location1, location2, false);
  //	}
  //
  //	/**
  //	 * Returns true if location1 is the same as, or a proper prefix of, location2.
  //	 * Returns false otherwise.
  //	 */
  //	public static boolean isPrefixOf(URI location1, URI location2) {
  //		return computeOverlap(location1, location2, false);
  //	}

  /**
   * Closes a stream and ignores any resulting exception. This is useful when doing stream cleanup
   * in a finally block where secondary exceptions are not worth logging.
   *
   * <p><strong>WARNING:</strong> If the API contract requires notifying clients of I/O problems,
   * then you <strong>must</strong> explicitly close() output streams outside of safeClose(). Some
   * OutputStreams will defer an IOException from write() to close(). So while the writes may
   * 'succeed', ignoring the IOExcpetion will result in silent data loss.
   *
   * <p>This method should only be used as a fail-safe to ensure resources are not leaked. See also:
   * https://bugs.eclipse.org/bugs/show_bug.cgi?id=332543
   */
  public static void safeClose(Closeable stream) {
    try {
      if (stream != null) stream.close();
    } catch (IOException e) {
      // ignore
    }
  }

  /**
   * Converts a URI to an IPath. Returns null if the URI cannot be represented as an IPath.
   *
   * <p>Note this method differs from URIUtil in its handling of relative URIs as being relative to
   * path variables.
   */
  public static IPath toPath(URI uri) {
    if (uri == null) return null;
    final String scheme = uri.getScheme();
    // null scheme represents path variable
    if (scheme == null || EFS.SCHEME_FILE.equals(scheme))
      return new Path(uri.getSchemeSpecificPart());
    return null;
  }

  public static final void transferStreams(
      InputStream source, OutputStream destination, String path, IProgressMonitor monitor)
      throws CoreException {
    //		monitor = Policy.monitorFor(monitor);
    try {
      /*
       * Note: although synchronizing on the buffer is thread-safe,
       * it may result in slower performance in the future if we want
       * to allow concurrent writes.
       */
      synchronized (buffer) {
        while (true) {
          int bytesRead = -1;
          try {
            bytesRead = source.read(buffer);
          } catch (IOException e) {
            String msg = NLS.bind(Messages.localstore_failedReadDuringWrite, path);
            throw new ResourceException(IResourceStatus.FAILED_READ_LOCAL, new Path(path), msg, e);
          }
          try {
            if (bytesRead == -1) {
              // Bug 332543 - ensure we don't ignore failures on close()
              destination.close();
              break;
            }
            destination.write(buffer, 0, bytesRead);
          } catch (IOException e) {
            String msg = NLS.bind(Messages.localstore_couldNotWrite, path);
            throw new ResourceException(IResourceStatus.FAILED_WRITE_LOCAL, new Path(path), msg, e);
          }
          //					monitor.worked(1);
        }
      }
    } finally {
      safeClose(source);
      safeClose(destination);
    }
  }

  /** Not intended for instantiation. */
  private FileUtil() {
    super();
  }
}
