/**
 * ***************************************************************************** Copyright (c) 2005,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation James Blackburn (Broadcom
 * Corp.) - ongoing development
 * *****************************************************************************
 */
package org.eclipse.core.filesystem.provider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.internal.filesystem.Messages;
import org.eclipse.core.internal.filesystem.Policy;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;

/**
 * The abstract superclass of all {@link IFileStore} implementations. All file stores must subclass
 * this base class, implementing all abstract methods according to their specification in the {@link
 * IFileStore} API.
 *
 * <p>Clients may subclass this class.
 *
 * @since org.eclipse.core.filesystem 1.0
 */
public abstract class FileStore /* extends PlatformObject*/ implements IFileStore {
  /**
   * Singleton buffer created to avoid buffer creations in the transferStreams method. Used as an
   * optimization, based on the assumption that multiple writes won't happen in a given instance of
   * FileStore.
   */
  private static final byte[] buffer = new byte[8192];

  /**
   * A file info array of size zero that can be used as a return value for methods that return
   * IFileInfo[] to avoid creating garbage objects.
   */
  protected static final IFileInfo[] EMPTY_FILE_INFO_ARRAY = new IFileInfo[0];

  /**
   * A string array of size zero that can be used as a return value for methods that return String[]
   * to avoid creating garbage objects.
   */
  protected static final String[] EMPTY_STRING_ARRAY = new String[0];

  /**
   * Transfers the contents of an input stream to an output stream, using a large buffer.
   *
   * @param source The input stream to transfer
   * @param destination The destination stream of the transfer
   * @param path A path representing the data being transferred for use in error messages.
   * @param monitor A progress monitor. The monitor is assumed to have already done beginWork with
   *     one unit of work allocated per buffer load of contents to be transferred.
   * @throws CoreException
   */
  private static final void transferStreams(
      InputStream source, OutputStream destination, String path, IProgressMonitor monitor)
      throws CoreException {
    monitor = Policy.monitorFor(monitor);
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
            String msg = NLS.bind(Messages.failedReadDuringWrite, path);
            Policy.error(EFS.ERROR_READ, msg, e);
          }
          try {
            if (bytesRead == -1) {
              destination.close();
              break;
            }
            destination.write(buffer, 0, bytesRead);
          } catch (IOException e) {
            String msg = NLS.bind(Messages.couldNotWrite, path);
            Policy.error(EFS.ERROR_WRITE, msg, e);
          }
          monitor.worked(1);
        }
      }
    } finally {
      Policy.safeClose(source);
      Policy.safeClose(destination);
    }
  }

  /**
   * The default implementation of {@link IFileStore#childInfos(int, IProgressMonitor)}. Subclasses
   * should override this method where a more efficient implementation is possible. This default
   * implementation calls {@link #fetchInfo()} on each child, which will result in a file system
   * call for each child.
   */
  public IFileInfo[] childInfos(int options, IProgressMonitor monitor) throws CoreException {
    IFileStore[] childStores = childStores(options, monitor);
    IFileInfo[] childInfos = new IFileInfo[childStores.length];
    for (int i = 0; i < childStores.length; i++) {
      childInfos[i] = childStores[i].fetchInfo();
    }
    return childInfos;
  }

  /* (non-Javadoc)
   * @see org.eclipse.core.filesystem.IFileStore#childNames(int, org.eclipse.core.runtime.IProgressMonitor)
   */
  public abstract String[] childNames(int options, IProgressMonitor monitor) throws CoreException;

  /**
   * The default implementation of {@link IFileStore#childStores(int, IProgressMonitor)}. Subclasses
   * may override.
   */
  public IFileStore[] childStores(int options, IProgressMonitor monitor) throws CoreException {
    String[] children = childNames(options, monitor);
    IFileStore[] wrapped = new IFileStore[children.length];
    for (int i = 0; i < wrapped.length; i++) wrapped[i] = getChild(children[i]);
    return wrapped;
  }

  /**
   * The default implementation of {@link IFileStore#copy(IFileStore, int, IProgressMonitor)}. This
   * implementation performs a copy by using other primitive methods. Subclasses may override this
   * method.
   */
  public void copy(IFileStore destination, int options, IProgressMonitor monitor)
      throws CoreException {
    monitor = Policy.monitorFor(monitor);
    Policy.checkCanceled(monitor);
    final IFileInfo sourceInfo = fetchInfo(EFS.NONE, null);
    if (sourceInfo.isDirectory()) copyDirectory(sourceInfo, destination, options, monitor);
    else copyFile(sourceInfo, destination, options, monitor);
  }

  /**
   * Recursively copies a directory as specified by {@link IFileStore#copy(IFileStore, int,
   * IProgressMonitor)}.
   *
   * @param sourceInfo The current file information for the source of the move
   * @param destination The destination of the copy.
   * @param options bit-wise or of option flag constants ( {@link EFS#OVERWRITE} or {@link
   *     EFS#SHALLOW}).
   * @param monitor a progress monitor, or <code>null</code> if progress reporting and cancellation
   *     are not desired
   * @exception CoreException if this method fails. Reasons include:
   *     <ul>
   *       <li>This store does not exist.
   *       <li>A file of the same name already exists at the copy destination.
   *     </ul>
   */
  protected void copyDirectory(
      IFileInfo sourceInfo, IFileStore destination, int options, IProgressMonitor monitor)
      throws CoreException {
    try {
      IFileStore[] children = null;
      int opWork = 1;
      if ((options & EFS.SHALLOW) == 0) {
        children = childStores(EFS.NONE, null);
        opWork += children.length;
      }
      monitor.beginTask("", opWork); // $NON-NLS-1$
      monitor.subTask(NLS.bind(Messages.copying, toString()));
      // create directory
      destination.mkdir(EFS.NONE, Policy.subMonitorFor(monitor, 1));
      // copy attributes
      transferAttributes(sourceInfo, destination);

      if (children == null) return;
      // copy children
      for (int i = 0; i < children.length; i++)
        children[i].copy(
            destination.getChild(children[i].getName()), options, Policy.subMonitorFor(monitor, 1));
    } finally {
      monitor.done();
    }
  }

  /**
   * Copies a file as specified by {@link IFileStore#copy(IFileStore, int, IProgressMonitor)}.
   *
   * @param sourceInfo The current file information for the source of the move
   * @param destination The destination of the copy.
   * @param options bit-wise or of option flag constants ( {@link EFS#OVERWRITE} or {@link
   *     EFS#SHALLOW}).
   * @param monitor a progress monitor, or <code>null</code> if progress reporting and cancellation
   *     are not desired
   * @exception CoreException if this method fails. Reasons include:
   *     <ul>
   *       <li>This store does not exist.
   *       <li>The <code>OVERWRITE</code> flag is not specified and a file of the same name already
   *           exists at the copy destination.
   *       <li>A directory of the same name already exists at the copy destination.
   *     </ul>
   */
  protected void copyFile(
      IFileInfo sourceInfo, IFileStore destination, int options, IProgressMonitor monitor)
      throws CoreException {
    try {
      if ((options & EFS.OVERWRITE) == 0 && destination.fetchInfo().exists())
        Policy.error(EFS.ERROR_EXISTS, NLS.bind(Messages.fileExists, destination));
      long length = sourceInfo.getLength();
      int totalWork;
      if (length == -1) totalWork = IProgressMonitor.UNKNOWN;
      else totalWork = 1 + (int) (length / buffer.length);
      String sourcePath = toString();
      monitor.beginTask(NLS.bind(Messages.copying, sourcePath), totalWork);
      InputStream in = null;
      OutputStream out = null;
      try {
        in = openInputStream(EFS.NONE, Policy.subMonitorFor(monitor, 0));
        out = destination.openOutputStream(EFS.NONE, Policy.subMonitorFor(monitor, 0));
        transferStreams(in, out, sourcePath, monitor);
        transferAttributes(sourceInfo, destination);
      } catch (CoreException e) {
        Policy.safeClose(in);
        Policy.safeClose(out);
        // if we failed to write, try to cleanup the half written file
        if (!destination.fetchInfo(0, null).exists()) destination.delete(EFS.NONE, null);
        throw e;
      }
    } finally {
      monitor.done();
    }
  }

  /**
   * The default implementation of {@link IFileStore#delete(int, IProgressMonitor)}. This
   * implementation always throws an exception indicating that deletion is not supported by this
   * file system. This method should be overridden for all file systems on which deletion is
   * supported.
   *
   * @param options bit-wise or of option flag constants
   * @param monitor a progress monitor, or <code>null</code> if progress reporting and cancellation
   *     are not desired
   */
  public void delete(int options, IProgressMonitor monitor) throws CoreException {
    Policy.error(EFS.ERROR_DELETE, NLS.bind(Messages.noImplDelete, toString()));
  }

  /**
   * This implementation of {@link Object#equals(Object)} defines equality based on the file store's
   * URI. Subclasses should override this method to return <code>true</code> if and only if the two
   * file stores represent the same resource in the backing file system. Issues to watch out for
   * include whether the file system is case-sensitive, and whether trailing slashes are considered
   * significant. Subclasses that override this method should also override {@link #hashCode()}.
   *
   * @param obj The object to compare with the receiver for equality
   * @return <code>true</code> if this object is equal to the provided object, and <code>false
   *     </code> otherwise.
   * @since org.eclipse.core.filesystem 1.1
   */
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof FileStore)) return false;
    return toURI().equals(((FileStore) obj).toURI());
  }

  /**
   * The default implementation of {@link IFileStore#fetchInfo()}. This implementation forwards to
   * {@link IFileStore#fetchInfo(int, IProgressMonitor)}. Subclasses may override this method.
   */
  public IFileInfo fetchInfo() {
    try {
      return fetchInfo(EFS.NONE, null);
    } catch (CoreException e) {
      // there was an error contacting the file system, so treat it as non-existent file
      FileInfo result = new FileInfo(getName());
      result.setExists(false);
      return result;
    }
  }

  /* (non-Javadoc)
   * @see org.eclipse.core.filesystem.IFileStore#fetchInfo(int, org.eclipse.core.runtime.IProgressMonitor)
   */
  public abstract IFileInfo fetchInfo(int options, IProgressMonitor monitor) throws CoreException;

  /**
   * The default implementation of {@link IFileStore#getChild(IPath)}. Subclasses may override.
   *
   * @deprecated
   */
  public IFileStore getChild(IPath path) {
    IFileStore result = this;
    for (int i = 0, imax = path.segmentCount(); i < imax; i++)
      result = result.getChild(path.segment(i));
    return result;
  }

  /**
   * The default implementation of {@link IFileStore#getFileStore(IPath)} Subclasses may override.
   *
   * @since org.eclipse.core.filesystem 1.2
   */
  public IFileStore getFileStore(IPath path) {
    IFileStore result = this;
    String segment = null;
    for (int i = 0, imax = path.segmentCount(); i < imax; i++) {
      segment = path.segment(i);
      if (segment.equals(".")) // $NON-NLS-1$
      continue;
      else if (segment.equals("..") && result.getParent() != null) // $NON-NLS-1$
      result = result.getParent();
      else result = result.getChild(segment);
    }
    return result;
  }

  /* (non-Javadoc)
   * @see org.eclipse.core.filesystem.IFileStore#getChild(java.lang.String)
   */
  public abstract IFileStore getChild(String name);

  //	/**
  //	 * The default implementation of {@link IFileStore#getFileSystem()}.
  //	 * Subclasses may override.
  //	 */
  //	public IFileSystem getFileSystem() {
  //		try {
  //			return EFS.getFileSystem(toURI().getScheme());
  //		} catch (CoreException e) {
  //			//this will only happen if toURI() has been incorrectly implemented
  //			throw new RuntimeException(e);
  //		}
  //	}

  /* (non-Javadoc)
   * @see org.eclipse.core.filesystem.IFileStore#getName()
   */
  public abstract String getName();

  /* (non-Javadoc)
   * @see org.eclipse.core.filesystem.IFileStore#getParent()
   */
  public abstract IFileStore getParent();

  /**
   * This implementation of {@link Object#hashCode()} uses a definition of equality based on
   * equality of the file store's URI. Subclasses that override {@link #equals(Object)} should also
   * override this method to ensure the contract of {@link Object#hashCode()} is honored.
   *
   * @return A hash code value for this file store
   * @since org.eclipse.core.filesystem 1.1
   */
  public int hashCode() {
    return toURI().hashCode();
  }

  /**
   * The default implementation of {@link IFileStore#isParentOf(IFileStore)}. This implementation
   * performs parent calculation using other primitive methods. Subclasses may override this method.
   *
   * @param other The store to test for parentage.
   * @return <code>true</code> if this store is a parent of the provided store, and <code>false
   *     </code> otherwise.
   */
  public boolean isParentOf(IFileStore other) {
    while (true) {
      other = other.getParent();
      if (other == null) return false;
      if (this.equals(other)) return true;
    }
  }

  /**
   * The default implementation of {@link IFileStore#mkdir(int, IProgressMonitor)}. This
   * implementation always throws an exception indicating that this file system is read only. This
   * method should be overridden for all writable file systems.
   *
   * @param options bit-wise or of option flag constants
   * @param monitor a progress monitor, or <code>null</code> if progress reporting and cancellation
   *     are not desired
   */
  public IFileStore mkdir(int options, IProgressMonitor monitor) throws CoreException {
    Policy.error(EFS.ERROR_WRITE, NLS.bind(Messages.noImplWrite, toString()));
    return null; // can't get here
  }

  /**
   * The default implementation of {@link IFileStore#move(IFileStore, int, IProgressMonitor)}. This
   * implementation performs a move by using other primitive methods. Subclasses may override this
   * method.
   */
  public void move(IFileStore destination, int options, IProgressMonitor monitor)
      throws CoreException {
    monitor = Policy.monitorFor(monitor);
    try {
      monitor.beginTask(NLS.bind(Messages.moving, destination.toString()), 100);
      copy(destination, options & EFS.OVERWRITE, Policy.subMonitorFor(monitor, 70));
      delete(EFS.NONE, Policy.subMonitorFor(monitor, 30));
    } catch (CoreException e) {
      // throw new error to indicate failure occurred during a move
      String message = NLS.bind(Messages.couldNotMove, toString());
      Policy.error(EFS.ERROR_WRITE, message, e);
    } finally {
      monitor.done();
    }
  }

  /* (non-Javadoc)
   * @see org.eclipse.core.filesystem.IFileStore#openInputStream(int, IProgressMonitor)
   */
  public abstract InputStream openInputStream(int options, IProgressMonitor monitor)
      throws CoreException;

  /**
   * The default implementation of {@link IFileStore#openOutputStream(int, IProgressMonitor)}. This
   * implementation always throws an exception indicating that this file system is read only. This
   * method should be overridden for all writable file systems.
   *
   * <p>Implementations of this method are responsible for ensuring that the exact sequence of bytes
   * written to the output stream are returned on a subsequent call to {@link #openInputStream(int,
   * IProgressMonitor)}, unless there have been intervening modifications to the file in the file
   * system. For example, the implementation of this method must not perform conversion of line
   * terminator characters on text data in the stream.
   *
   * @param options bit-wise or of option flag constants
   * @param monitor a progress monitor, or <code>null</code> if progress reporting and cancellation
   *     are not desired
   */
  public OutputStream openOutputStream(int options, IProgressMonitor monitor) throws CoreException {
    Policy.error(EFS.ERROR_WRITE, NLS.bind(Messages.noImplWrite, toString()));
    return null; // can't get here
  }

  /**
   * The default implementation of {@link IFileStore#putInfo(IFileInfo, int, IProgressMonitor)}.
   * This implementation always throws an exception indicating that this file system is read only.
   * This method should be overridden for all writable file systems.
   *
   * @param options bit-wise or of option flag constants
   * @param monitor a progress monitor, or <code>null</code> if progress reporting and cancellation
   *     are not desired
   */
  public void putInfo(IFileInfo info, int options, IProgressMonitor monitor) throws CoreException {
    Policy.error(EFS.ERROR_WRITE, NLS.bind(Messages.noImplWrite, toString()));
  }

  /**
   * The default implementation of {@link IFileStore#toLocalFile(int, IProgressMonitor)}. When the
   * {@link EFS#CACHE} option is specified, this method returns a cached copy of this store in the
   * local file system, or <code>null</code> if this store does not exist.
   */
  public File toLocalFile(int options, IProgressMonitor monitor) throws CoreException {
    //		monitor = Policy.monitorFor(monitor);
    //		//caching is the only recognized option
    //		if (options != EFS.CACHE)
    //			return null;
    //		return FileCache.getCache().cache(this, monitor);
    throw new UnsupportedOperationException();
  }

  @Override
  public Object getAdapter(Class aClass) {
    throw new UnsupportedOperationException("getAdapter");
  }

  /**
   * Default implementation of {@link IFileStore#toString()}. This default implementation returns a
   * string equal to the one returned by #toURI().toString(). Subclasses may override to provide a
   * more specific string representation of this store.
   *
   * @return A string representation of this store.
   */
  public String toString() {
    return toURI().toString();
  }

  /* (non-Javadoc)
   * @see org.eclipse.core.filesystem.IFileStore#toURI()
   */
  public abstract URI toURI();

  private void transferAttributes(IFileInfo sourceInfo, IFileStore destination)
      throws CoreException {
    int options = EFS.SET_ATTRIBUTES | EFS.SET_LAST_MODIFIED;
    destination.putInfo(sourceInfo, options, null);
  }
}
