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

import org.eclipse.core.internal.filebuffers.FileBuffersPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

/**
 * Facade for the file buffers plug-in. Provides access to the text file buffer manager and helper
 * methods for location handling. This facade is available independent from the activation status of
 * the file buffers plug-in.
 *
 * <p>This class must not be used by clients that do not want to require <code>
 * org.eclipse.core.resources</code>. Use <code>ITextFileBufferManager.DEFAULT</code> to get the
 * default text file buffer manager.
 *
 * @since 3.0
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public final class FileBuffers {

  /**
   * The workspace root.
   *
   * @since 3.3
   */
  private static final IWorkspaceRoot WORKSPACE_ROOT = ResourcesPlugin.getWorkspace().getRoot();

  /** Cannot be instantiated. */
  private FileBuffers() {}

  /**
   * File buffer plug-in ID (value <code>"org.eclipse.core.filebuffers"</code>).
   *
   * @since 3.3.
   */
  public static final String PLUGIN_ID =
      "org.eclipse.core.filebuffers"; // FileBuffersPlugin.PLUGIN_ID;

  /**
   * Returns the text file buffer manager. May return <code>null</code> if the file buffers plug-in
   * is not active. This is, for example, the case when the method is called on plug-in shutdown.
   *
   * <p>Use <code>ITextFileBufferManager.DEFAULT</code> to get the default text file buffer manager
   * if you do not want to depend on <code>org.eclipse.core.resources</code>.
   *
   * @return the text file buffer manager or <code>null</code>
   */
  public static ITextFileBufferManager getTextFileBufferManager() {
    FileBuffersPlugin plugin = FileBuffersPlugin.getDefault();
    return plugin != null ? plugin.getFileBufferManager() : null;
  }

  //	/**
  //	 * Creates and returns an <em>unshared</em> text file buffer manager.
  //	 *
  //	 * @return the text file buffer manager or <code>null</code>
  //	 * @since 3.4
  //	 */
  //	public static ITextFileBufferManager createTextFileBufferManager()  {
  //		Bundle resourcesBundle= Platform.getBundle("org.eclipse.core.resources"); //$NON-NLS-1$
  //		if (resourcesBundle != null)
  //			return new ResourceTextFileBufferManager();
  //		return new TextFileBufferManager();
  //	}

  /**
   * Returns the workspace file at the given location if such a file exists.
   *
   * @param location the location
   * @return the workspace file at the location or <code>null</code> if no such file exists or if
   *     the location is not a valid location
   */
  public static IFile getWorkspaceFileAtLocation(IPath location) {
    return getWorkspaceFileAtLocation(location, false);
  }

  /**
   * Returns the workspace file at the given location if such a file exists.
   *
   * @param location the location
   * @param isNormalized <code>true</code> if the given location is already normalized
   * @return the workspace file at the location or <code>null</code> if no such file exists or if
   *     the location is not a valid location
   * @since 3.3
   */
  public static IFile getWorkspaceFileAtLocation(IPath location, boolean isNormalized) {
    IPath normalized;
    if (isNormalized) normalized = location;
    else normalized = normalizeLocation(location);

    if (normalized.segmentCount() >= 2) {
      // @see IContainer#getFile for the required number of segments
      IFile file = WORKSPACE_ROOT.getFile(normalized);
      if (file != null && file.exists()) return file;
    }
    return null;
  }

  /**
   * Returns the normalized form of the given path or location.
   *
   * <p>The normalized form is defined as follows:
   *
   * <ul>
   *   <li><b>Existing Workspace Files:</b> For a path or location for which there {@link
   *       org.eclipse.core.resources.IContainer#exists(org.eclipse.core.runtime.IPath) exists} a
   *       workspace file, the normalized form is that file's workspace relative, absolute path as
   *       returned by {@link IFile#getFullPath()}.
   *   <li><b>Non-existing Workspace Files:</b> For a path to a non-existing workspace file, the
   *       normalized form is the {@link IPath#makeAbsolute() absolute} form of the path.
   *   <li><b>External Files:</b> For a location for which there exists no workspace file, the
   *       normalized form is the {@link IPath#makeAbsolute() absolute} form of the location.
   * </ul>
   *
   * @param pathOrLocation the path or location to be normalized
   * @return the normalized form of <code>pathOrLocation</code>
   */
  public static IPath normalizeLocation(IPath pathOrLocation) {
    // existing workspace resources - this is the 93% case
    if (WORKSPACE_ROOT.exists(pathOrLocation)) return pathOrLocation.makeAbsolute();

    IFile file = WORKSPACE_ROOT.getFileForLocation(pathOrLocation);
    // existing workspace resources referenced by their file system path
    // files that do not exist (including non-accessible files) do not pass
    if (file != null && file.exists()) return file.getFullPath();

    // non-existing resources and external files
    return pathOrLocation.makeAbsolute();
  }
  //
  //	/**
  //	 * Returns the file in the local file system for the given location.
  //	 * <p>
  //	 * The location is either a full path of a workspace resource or an
  //	 * absolute path in the local file system.
  //	 * </p>
  //	 *
  //	 * @param location the location
  //	 * @return the {@link IFileStore} in the local file system for the given location
  //	 * @since 3.2
  //	 */
  //	public static IFileStore getFileStoreAtLocation(IPath location) {
  //		if (location == null)
  //			return null;
  //
  //		IFile file= getWorkspaceFileAtLocation(location);
  //		try {
  //			if (file != null) {
  //				URI uri= file.getLocationURI();
  //				if (uri == null)
  //					return null;
  //				return EFS.getStore(uri);
  //			}
  //		} catch (CoreException e) {
  //			//fall through and assume it is a local file
  //		}
  //		return EFS.getLocalFileSystem().getStore(location);
  //	}

  //	/**
  //	 * Returns the file in the local file system for the given location.
  //	 * <p>
  //	 * The location is either a full path of a workspace resource or an
  //	 * absolute path in the local file system.
  //	 * </p>
  //	 *
  //	 * @param location the location
  //	 * @return the {@link File} in the local file system for the given location
  //	 * @deprecated As of 3.2, replaced by {@link #getFileStoreAtLocation(IPath)}
  //	 */
  //	public static File getSystemFileAtLocation(IPath location) {
  //		IFileStore store= getFileStoreAtLocation(location);
  //		if (store != null) {
  //			try {
  //				return store.toLocalFile(EFS.NONE, null);
  //			} catch (CoreException e) {
  //				return null;
  //			}
  //		}
  //		return null;
  //	}

}
