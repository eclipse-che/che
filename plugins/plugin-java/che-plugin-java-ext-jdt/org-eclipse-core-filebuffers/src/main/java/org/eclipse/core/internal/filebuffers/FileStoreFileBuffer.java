/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.core.internal.filebuffers;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.text.IDocumentExtension4;

/** @since 3.3 (previously available as JavaFileBuffer since 3.0) */
public abstract class FileStoreFileBuffer extends AbstractFileBuffer {

  /** The location */
  protected IPath fLocation;
  /** How often the element has been connected */
  protected int fReferenceCount;
  /** Can the element be saved */
  protected boolean fCanBeSaved = false;
  /** The status of this element */
  protected IStatus fStatus;
  /** The time stamp at which this buffer synchronized with the underlying file. */
  protected long fSynchronizationStamp = IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP;
  /** How often the synchronization context has been requested */
  protected int fSynchronizationContextCount;

  public FileStoreFileBuffer(TextFileBufferManager manager) {
    super(manager);
  }

  protected abstract void addFileBufferContentListeners();

  protected abstract void removeFileBufferContentListeners();

  protected abstract void initializeFileBufferContent(IProgressMonitor monitor)
      throws CoreException;

  protected abstract void commitFileBufferContent(IProgressMonitor monitor, boolean overwrite)
      throws CoreException;

  public void create(IFileStore fileStore, IProgressMonitor monitor) throws CoreException {
    IFileInfo info = fileStore.fetchInfo();
    fFileStore = fileStore;
    if (fLocation == null) fLocation = URIUtil.toPath(fileStore.toURI());

    initializeFileBufferContent(monitor);
    if (info.exists()) fSynchronizationStamp = info.getLastModified();

    addFileBufferContentListeners();
  }

  public void create(IPath location, IProgressMonitor monitor) throws CoreException {
    fLocation = location;
    create(EFS.getStore(URIUtil.toURI(getLocation())), monitor);
  }

  public void connect() {
    ++fReferenceCount;
    if (fReferenceCount == 1) connected();
  }

  /**
   * Called when this file buffer has been connected. This is the case when there is exactly one
   * connection.
   *
   * <p>Clients may extend this method.
   */
  protected void connected() {}

  public void disconnect() throws CoreException {
    --fReferenceCount;
    if (fReferenceCount <= 0) disconnected();
  }

  /**
   * Called when this file buffer has been disconnected. This is the case when the number of
   * connections drops below <code>1</code>.
   *
   * <p>Clients may extend this method.
   */
  protected void disconnected() {}

  /*
   * @see org.eclipse.core.internal.filebuffers.AbstractFileBuffer#isDisconnected()
   * @since 3.1
   */
  protected boolean isDisconnected() {
    return fReferenceCount <= 0;
  }

  /*
   * @see org.eclipse.core.filebuffers.IFileBuffer#getLocation()
   */
  public IPath getLocation() {
    return fLocation;
  }

  /*
   * @see org.eclipse.core.filebuffers.IFileBuffer#commit(org.eclipse.core.runtime.IProgressMonitor, boolean)
   */
  public void commit(IProgressMonitor monitor, boolean overwrite) throws CoreException {
    if (!isDisconnected() && fCanBeSaved) {

      fManager.fireStateChanging(this);

      try {
        commitFileBufferContent(monitor, overwrite);
      } catch (CoreException x) {
        fManager.fireStateChangeFailed(this);
        throw x;
      } catch (RuntimeException x) {
        fManager.fireStateChangeFailed(this);
        throw x;
      }

      fCanBeSaved = false;
      addFileBufferContentListeners();
      fManager.fireDirtyStateChanged(this, fCanBeSaved);
    }
  }

  /*
   * @see org.eclipse.core.filebuffers.IFileBuffer#computeCommitRule()
   */
  public ISchedulingRule computeCommitRule() {
    return null;
  }

  /*
   * @see org.eclipse.core.filebuffers.IFileBuffer#isDirty()
   */
  public boolean isDirty() {
    return fCanBeSaved;
  }

  /*
   * @see org.eclipse.core.filebuffers.IFileBuffer#setDirty(boolean)
   */
  public void setDirty(boolean isDirty) {
    fCanBeSaved = isDirty;
  }

  /*
   * @see org.eclipse.core.filebuffers.IFileBuffer#isShared()
   */
  public boolean isShared() {
    return fReferenceCount > 1;
  }

  /*
   * @see org.eclipse.core.filebuffers.IFileBuffer#computeValidateStateRule()
   */
  public ISchedulingRule computeValidateStateRule() {
    return null;
  }

  /*
   * @see org.eclipse.core.filebuffers.IFileBuffer#validateState(org.eclipse.core.runtime.IProgressMonitor, java.lang.Object)
   */
  public void validateState(IProgressMonitor monitor, Object computationContext)
      throws CoreException {
    // nop
  }

  /*
   * @see org.eclipse.core.filebuffers.IFileBuffer#isStateValidated()
   */
  public boolean isStateValidated() {
    return true;
  }

  /*
   * @see org.eclipse.core.filebuffers.IFileBuffer#resetStateValidation()
   */
  public void resetStateValidation() {
    // nop
  }

  /*
   * @see org.eclipse.core.filebuffers.IFileBuffer#isSynchronized()
   */
  public boolean isSynchronized() {
    return fSynchronizationStamp == getModificationStamp();
  }

  /*
   * @see org.eclipse.core.filebuffers.IFileBuffer#requestSynchronizationContext()
   */
  public void requestSynchronizationContext() {
    ++fSynchronizationContextCount;
  }

  /*
   * @see org.eclipse.core.filebuffers.IFileBuffer#releaseSynchronizationContext()
   */
  public void releaseSynchronizationContext() {
    --fSynchronizationContextCount;
  }

  /*
   * @see org.eclipse.core.filebuffers.IFileBuffer#isSynchronizationContextRequested()
   */
  public boolean isSynchronizationContextRequested() {
    return fSynchronizationContextCount > 0;
  }

  /*
   * @see org.eclipse.core.filebuffers.IFileBuffer#isCommitable()
   */
  public boolean isCommitable() {
    IFileInfo info = fFileStore.fetchInfo();
    return info.exists() && !info.getAttribute(EFS.ATTRIBUTE_READ_ONLY);
  }

  /*
   * @see org.eclipse.core.filebuffers.IStateValidationSupport#validationStateChanged(boolean, org.eclipse.core.runtime.IStatus)
   */
  public void validationStateChanged(boolean validationState, IStatus status) {
    // nop
  }
}
