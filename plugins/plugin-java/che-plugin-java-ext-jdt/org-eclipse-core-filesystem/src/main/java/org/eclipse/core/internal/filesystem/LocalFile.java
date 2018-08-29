/**
 * ***************************************************************************** Copyright (c) 2005,
 * 2014 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation Martin Oberhuber (Wind River) -
 * [294429] Avoid substring baggage in FileInfo Martin Lippert (VMware) - [394607] Poor performance
 * when using findFilesForLocationURI Sergey Prigogin (Google) - Fix for bug 435519
 * *****************************************************************************
 */
package org.eclipse.core.internal.filesystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.filesystem.provider.FileInfo;
import org.eclipse.core.filesystem.provider.FileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

/**
 * File system implementation based on storage of files in the local operating system's file system.
 */
public class LocalFile extends FileStore {
  /** The java.io.File that this store represents. */
  protected final File file;
  /** The absolute file system path of the file represented by this store. */
  protected final String filePath;

  /** cached value for the toURI method */
  private URI uri;

  private static int attributes(File aFile) {
    if (!aFile.exists() || aFile.canWrite()) return EFS.NONE;
    return EFS.ATTRIBUTE_READ_ONLY;
  }

  /**
   * Creates a new local file.
   *
   * @param file The file this local file represents
   */
  public LocalFile(File file) {
    this.file = file;
    this.filePath = file.getAbsolutePath();
  }

  /**
   * This method is called after a failure to modify a file or directory. Check to see if the parent
   * is read-only and if so then throw an exception with a more specific message and error code.
   *
   * @param target The file that we failed to modify
   * @param exception The low level exception that occurred, or <code>null</code>
   * @throws CoreException A more specific exception if the parent is read-only
   */
  private void checkReadOnlyParent(File target, Throwable exception) throws CoreException {
    File parent = target.getParentFile();
    if (parent != null && (attributes(parent) & EFS.ATTRIBUTE_READ_ONLY) != 0) {
      String message = NLS.bind(Messages.readOnlyParent, target.getAbsolutePath());
      Policy.error(EFS.ERROR_PARENT_READ_ONLY, message, exception);
    }
  }

  /**
   * This method is called after a failure to modify a directory. Check to see if the target is not
   * writable (e.g. device doesn't not exist) and if so then throw an exception with a more specific
   * message and error code.
   *
   * @param target The directory that we failed to modify
   * @param exception The low level exception that occurred, or <code>null</code>
   * @throws CoreException A more specific exception if the target is not writable
   */
  private void checkTargetIsNotWritable(File target, Throwable exception) throws CoreException {
    if (!target.canWrite()) {
      String message = NLS.bind(Messages.couldNotWrite, target.getAbsolutePath());
      Policy.error(EFS.ERROR_WRITE, message);
    }
  }

  public String[] childNames(int options, IProgressMonitor monitor) {
    String[] names = file.list();
    return (names == null ? EMPTY_STRING_ARRAY : names);
  }

  public void copy(IFileStore destFile, int options, IProgressMonitor monitor)
      throws CoreException {
    if (destFile instanceof LocalFile) {
      File source = file;
      File destination = ((LocalFile) destFile).file;
      // handle case variants on a case-insensitive OS, or copying between
      // two equivalent files in an environment that supports symbolic links.
      // in these nothing needs to be copied (and doing so would likely lose data)
      try {
        if (source.getCanonicalFile().equals(destination.getCanonicalFile())) {
          // nothing to do
          return;
        }
      } catch (IOException e) {
        String message = NLS.bind(Messages.couldNotRead, source.getAbsolutePath());
        Policy.error(EFS.ERROR_READ, message, e);
      }
    }
    // fall through to super implementation
    super.copy(destFile, options, monitor);
  }

  public void delete(int options, IProgressMonitor monitor) throws CoreException {
    if (monitor == null) monitor = new NullProgressMonitor();
    //		else
    //			monitor = new InfiniteProgress(monitor);
    try {
      monitor.beginTask(NLS.bind(Messages.deleting, this), 200);
      String message = Messages.deleteProblem;
      MultiStatus result = new MultiStatus(Policy.PI_FILE_SYSTEM, EFS.ERROR_DELETE, message, null);
      internalDelete(file, filePath, result, monitor);
      if (!result.isOK()) throw new CoreException(result);
    } finally {
      monitor.done();
    }
  }

  public boolean equals(Object obj) {
    if (!(obj instanceof LocalFile)) return false;
    // Mac oddity: file.equals returns false when case is different even when
    // file system is not case sensitive (Radar bug 3190672)
    LocalFile otherFile = (LocalFile) obj;
    //		if (LocalFileSystem.MACOSX)
    //			return filePath.toLowerCase().equals(otherFile.filePath.toLowerCase());
    return file.equals(otherFile.file);
  }

  public IFileInfo fetchInfo(int options, IProgressMonitor monitor) {
    //		if (LocalFileNativesManager.isUsingNatives()) {
    //			FileInfo info = LocalFileNativesManager.fetchFileInfo(filePath);
    //			//natives don't set the file name on all platforms
    //			if (info.getName().length() == 0) {
    //				String name = file.getName();
    //				//Bug 294429: make sure that substring baggage is removed
    //				info.setName(new String(name.toCharArray()));
    //			}
    //			return info;
    //		}
    // in-lined non-native implementation
    FileInfo info = new FileInfo(file.getName());
    final long lastModified = file.lastModified();
    if (lastModified <= 0) {
      // if the file doesn't exist, all other attributes should be default values
      info.setExists(false);
      return info;
    }
    info.setLastModified(lastModified);
    info.setExists(true);
    info.setLength(file.length());
    info.setDirectory(file.isDirectory());
    info.setAttribute(EFS.ATTRIBUTE_READ_ONLY, file.exists() && !file.canWrite());
    info.setAttribute(EFS.ATTRIBUTE_HIDDEN, file.isHidden());
    return info;
  }

  public IFileStore getChild(IPath path) {
    return new LocalFile(new File(file, path.toOSString()));
  }

  public IFileStore getFileStore(IPath path) {
    return new LocalFile(new Path(file.getPath()).append(path).toFile());
  }

  public IFileStore getChild(String name) {
    return new LocalFile(new File(file, name));
  }

  //	/*
  //	 * (non-Javadoc)
  //	 * @see org.eclipse.core.filesystem.IFileStore#getFileSystem()
  //	 */
  //	public IFileSystem getFileSystem() {
  //		return LocalFileSystem.getInstance();
  //	}

  public String getName() {
    return file.getName();
  }

  public IFileStore getParent() {
    File parent = file.getParentFile();
    return parent == null ? null : new LocalFile(parent);
  }

  public int hashCode() {
    //		if (LocalFileSystem.MACOSX)
    //			return filePath.toLowerCase().hashCode();
    return file.hashCode();
  }

  /**
   * Deletes the given file recursively, adding failure info to the provided status object. The
   * filePath is passed as a parameter to optimize java.io.File object creation.
   */
  private boolean internalDelete(
      File target, String pathToDelete, MultiStatus status, IProgressMonitor monitor) {
    // first try to delete - this should succeed for files and symbolic links to directories
    if (monitor.isCanceled()) {
      throw new OperationCanceledException();
    }
    if (target.delete() || !target.exists()) return true;
    if (target.isDirectory()) {
      monitor.subTask(NLS.bind(Messages.deleting, target));
      String[] list = target.list();
      if (list == null) list = EMPTY_STRING_ARRAY;
      int parentLength = pathToDelete.length();
      boolean failedRecursive = false;
      for (int i = 0, imax = list.length; i < imax; i++) {
        if (monitor.isCanceled()) {
          throw new OperationCanceledException();
        }
        // optimized creation of child path object
        StringBuffer childBuffer = new StringBuffer(parentLength + list[i].length() + 1);
        childBuffer.append(pathToDelete);
        childBuffer.append(File.separatorChar);
        childBuffer.append(list[i]);
        String childName = childBuffer.toString();
        // try best effort on all children so put logical OR at end
        failedRecursive =
            !internalDelete(new File(childName), childName, status, monitor) || failedRecursive;
        monitor.worked(1);
      }
      try {
        // don't try to delete the root if one of the children failed
        if (!failedRecursive && target.delete()) return true;
      } catch (Exception e) {
        // we caught a runtime exception so log it
        String message = NLS.bind(Messages.couldnotDelete, target.getAbsolutePath());
        status.add(new Status(IStatus.ERROR, Policy.PI_FILE_SYSTEM, EFS.ERROR_DELETE, message, e));
        return false;
      }
    }
    // if we got this far, we failed
    String message = null;
    if (fetchInfo().getAttribute(EFS.ATTRIBUTE_READ_ONLY))
      message = NLS.bind(Messages.couldnotDeleteReadOnly, target.getAbsolutePath());
    else message = NLS.bind(Messages.couldnotDelete, target.getAbsolutePath());
    status.add(new Status(IStatus.ERROR, Policy.PI_FILE_SYSTEM, EFS.ERROR_DELETE, message, null));
    return false;
  }

  public boolean isParentOf(IFileStore other) {
    if (!(other instanceof LocalFile)) return false;
    String thisPath = filePath;
    String thatPath = ((LocalFile) other).filePath;
    int thisLength = thisPath.length();
    int thatLength = thatPath.length();
    // if equal then not a parent
    if (thisLength >= thatLength) return false;
    //		if (getFileSystem().isCaseSensitive()) {
    if (thatPath.indexOf(thisPath) != 0) return false;
    //		} else {
    //			if (thatPath.toLowerCase().indexOf(thisPath.toLowerCase()) != 0)
    //				return false;
    //		}
    // The common portion must end with a separator character for this to be a parent of that
    return thisPath.charAt(thisLength - 1) == File.separatorChar
        || thatPath.charAt(thisLength) == File.separatorChar;
  }

  public IFileStore mkdir(int options, IProgressMonitor monitor) throws CoreException {
    boolean shallow = (options & EFS.SHALLOW) != 0;
    // must be a directory
    if (shallow) file.mkdir();
    else file.mkdirs();
    if (!file.isDirectory()) {
      checkReadOnlyParent(file, null);
      checkTargetIsNotWritable(file, null);
      String message = NLS.bind(Messages.failedCreateWrongType, filePath);
      Policy.error(EFS.ERROR_WRONG_TYPE, message);
    }
    return this;
  }

  public void move(IFileStore destFile, int options, IProgressMonitor monitor)
      throws CoreException {
    if (!(destFile instanceof LocalFile)) {
      super.move(destFile, options, monitor);
      return;
    }
    File source = file;
    File destination = ((LocalFile) destFile).file;
    boolean overwrite = (options & EFS.OVERWRITE) != 0;
    monitor = Policy.monitorFor(monitor);
    try {
      monitor.beginTask(NLS.bind(Messages.moving, source.getAbsolutePath()), 10);
      // this flag captures case renaming on a case-insensitive OS, or moving
      // two equivalent files in an environment that supports symbolic links.
      // in these cases we NEVER want to delete anything
      boolean sourceEqualsDest = false;
      try {
        sourceEqualsDest = source.getCanonicalFile().equals(destination.getCanonicalFile());
      } catch (IOException e) {
        String message = NLS.bind(Messages.couldNotMove, source.getAbsolutePath());
        Policy.error(EFS.ERROR_WRITE, message, e);
      }
      if (!sourceEqualsDest && !overwrite && destination.exists()) {
        String message = NLS.bind(Messages.fileExists, destination.getAbsolutePath());
        Policy.error(EFS.ERROR_EXISTS, message);
      }
      if (source.renameTo(destination)) {
        // double-check to ensure we really did move
        // since java.io.File#renameTo sometimes lies
        if (!sourceEqualsDest && source.exists()) {
          // XXX: document when this occurs
          if (destination.exists()) {
            // couldn't delete the source so remove the destination and throw an error
            // XXX: if we fail deleting the destination, the destination (root) may still exist
            new LocalFile(destination).delete(EFS.NONE, null);
            String message = NLS.bind(Messages.couldnotDelete, source.getAbsolutePath());
            Policy.error(EFS.ERROR_DELETE, message);
          }
          // source exists but destination doesn't so try to copy below
        } else {
          // destination.exists() returns false for broken links, this has to be handled explicitly
          if (!destination.exists() && !destFile.fetchInfo().getAttribute(EFS.ATTRIBUTE_SYMLINK)) {
            // neither the source nor the destination exist. this is REALLY bad
            String message =
                NLS.bind(
                    Messages.failedMove, source.getAbsolutePath(), destination.getAbsolutePath());
            Policy.error(EFS.ERROR_WRITE, message);
          }
          // the move was successful
          monitor.worked(10);
          return;
        }
      }
      // for some reason renameTo didn't work
      if (sourceEqualsDest) {
        String message = NLS.bind(Messages.couldNotMove, source.getAbsolutePath());
        Policy.error(EFS.ERROR_WRITE, message, null);
      }
      // fall back to default implementation
      super.move(destFile, options, Policy.subMonitorFor(monitor, 10));
    } finally {
      monitor.done();
    }
  }

  public InputStream openInputStream(int options, IProgressMonitor monitor) throws CoreException {
    monitor = Policy.monitorFor(monitor);
    try {
      monitor.beginTask("", 1); // $NON-NLS-1$
      return new FileInputStream(file);
    } catch (FileNotFoundException e) {
      String message;
      if (!file.exists()) message = NLS.bind(Messages.fileNotFound, filePath);
      else if (file.isDirectory()) message = NLS.bind(Messages.notAFile, filePath);
      else message = NLS.bind(Messages.couldNotRead, filePath);
      Policy.error(EFS.ERROR_READ, message, e);
      return null;
    } finally {
      monitor.done();
    }
  }

  public OutputStream openOutputStream(int options, IProgressMonitor monitor) throws CoreException {
    monitor = Policy.monitorFor(monitor);
    try {
      monitor.beginTask("", 1); // $NON-NLS-1$
      return new FileOutputStream(file, (options & EFS.APPEND) != 0);
    } catch (FileNotFoundException e) {
      checkReadOnlyParent(file, e);
      String message;
      String path = filePath;
      if (file.isDirectory()) message = NLS.bind(Messages.notAFile, path);
      else message = NLS.bind(Messages.couldNotWrite, path);
      Policy.error(EFS.ERROR_WRITE, message, e);
      return null;
    } finally {
      monitor.done();
    }
  }

  public void putInfo(IFileInfo info, int options, IProgressMonitor monitor) throws CoreException {
    boolean success = true;
    if ((options & EFS.SET_ATTRIBUTES) != 0) {
      //			if (LocalFileNativesManager.isUsingNatives())
      //				success &= LocalFileNativesManager.putFileInfo(filePath, info, options);
    }
    // native does not currently set last modified
    if ((options & EFS.SET_LAST_MODIFIED) != 0)
      success &= file.setLastModified(info.getLastModified());
    if (!success && !file.exists())
      Policy.error(EFS.ERROR_NOT_EXISTS, NLS.bind(Messages.fileNotFound, filePath));
  }

  /* (non-Javadoc)
   * @see org.eclipse.core.filesystem.provider.FileStore#toLocalFile(int, org.eclipse.core.runtime.IProgressMonitor)
   */
  public File toLocalFile(int options, IProgressMonitor monitor) throws CoreException {
    //		if (options == EFS.CACHE)
    //			return super.toLocalFile(options, monitor);
    return file;
  }

  /* (non-Javadoc)
   * @see org.eclipse.core.filesystem.IFileStore#toString()
   */
  public String toString() {
    return file.toString();
  }

  /* (non-Javadoc)
   * @see org.eclipse.core.filesystem.IFileStore#toURI()
   */
  public URI toURI() {
    if (this.uri == null) {
      this.uri = URIUtil.toURI(filePath);
    }
    return this.uri;
  }
}
