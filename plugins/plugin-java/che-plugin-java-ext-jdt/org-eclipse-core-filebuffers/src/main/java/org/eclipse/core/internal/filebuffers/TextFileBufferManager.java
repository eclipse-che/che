/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2012 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation Andrew Ferguson (Symbian) -
 * [api] enable document setup participants to customize behavior based on resource being opened -
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=208881
 * *****************************************************************************
 */
package org.eclipse.core.internal.filebuffers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.IFileBufferListener;
import org.eclipse.core.filebuffers.IFileBufferStatusCodes;
import org.eclipse.core.filebuffers.ISynchronizationContext;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;

/** @since 3.0 */
public class TextFileBufferManager implements ITextFileBufferManager {

  private abstract static class SafeNotifier implements ISafeRunnable {
    public void handleException(Throwable ex) {
      // NOTE: Logging is done by SafeRunner
    }
  }

  //	protected static final IContentType TEXT_CONTENT_TYPE=
  // Platform.getContentTypeManager().getContentType(IContentTypeManager.CT_TEXT);

  private Map fFilesBuffers = new HashMap();
  private Map fFileStoreFileBuffers = new HashMap();
  private List fFileBufferListeners = new ArrayList();
  protected ExtensionsRegistry fRegistry;
  private ISynchronizationContext fSynchronizationContext;

  public TextFileBufferManager() {
    fRegistry = new ExtensionsRegistry();
  }

  /**
   * {@inheritDoc}
   *
   * @deprecated As of 3.3, replaced by {@link #connect(IPath, LocationKind, IProgressMonitor)}
   */
  public void connect(IPath location, IProgressMonitor monitor) throws CoreException {
    connect(location, LocationKind.NORMALIZE, monitor);
  }

  /*
   * @see org.eclipse.core.filebuffers.IFileBufferManager#connect(org.eclipse.core.runtime.IPath, org.eclipse.core.filebuffers.IFileBufferManager.LocationKind, org.eclipse.core.runtime.IProgressMonitor)
   * @since 3.3
   */
  public void connect(IPath location, LocationKind locationKind, IProgressMonitor monitor)
      throws CoreException {
    Assert.isNotNull(location);
    if (locationKind == LocationKind.NORMALIZE) location = normalizeLocation(location);

    AbstractFileBuffer fileBuffer = null;
    synchronized (fFilesBuffers) {
      fileBuffer = internalGetFileBuffer(location);
      if (fileBuffer != null) {
        fileBuffer.connect();
        return;
      }
    }

    fileBuffer = createFileBuffer(location, locationKind);
    if (fileBuffer == null)
      throw new CoreException(
          new Status(
              IStatus.ERROR,
              FileBuffersPlugin.PLUGIN_ID,
              IFileBufferStatusCodes.CREATION_FAILED,
              FileBuffersMessages.FileBufferManager_error_canNotCreateFilebuffer,
              null));

    fileBuffer.create(location, monitor);

    synchronized (fFilesBuffers) {
      AbstractFileBuffer oldFileBuffer = internalGetFileBuffer(location);
      if (oldFileBuffer != null) {
        fileBuffer.disconnect();
        fileBuffer.dispose();
        oldFileBuffer.connect();
        return;
      }
      fileBuffer.connect();
      fFilesBuffers.put(location, fileBuffer);
    }

    // Do notification outside synchronized block
    fireBufferCreated(fileBuffer);
  }

  /*
  //	 * @see org.eclipse.core.filebuffers.IFileBufferManager#connectFileStore(org.eclipse.core.filesystem.IFileStore, org.eclipse.core.runtime.IProgressMonitor)
  //	 * @since 3.3
  //	 */
  //	public void connectFileStore(IFileStore fileStore, IProgressMonitor monitor) throws
  // CoreException {
  //		Assert.isLegal(fileStore != null);
  //
  //		FileStoreFileBuffer fileBuffer= null;
  //		synchronized (fFileStoreFileBuffers) {
  //			fileBuffer= internalGetFileBuffer(fileStore);
  //			if (fileBuffer != null)  {
  //				fileBuffer.connect();
  //				return;
  //			}
  //		}
  //
  //		fileBuffer= createFileBuffer(fileStore);
  //		if (fileBuffer == null)
  //			throw new CoreException(new Status(IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID,
  // IFileBufferStatusCodes.CREATION_FAILED,
  // FileBuffersMessages.FileBufferManager_error_canNotCreateFilebuffer, null));
  //
  //		fileBuffer.create(fileStore, monitor);
  //
  //		synchronized (fFileStoreFileBuffers) {
  //			AbstractFileBuffer oldFileBuffer= internalGetFileBuffer(fileStore);
  //			if (oldFileBuffer != null) {
  //				fileBuffer.disconnect();
  //				fileBuffer.dispose();
  //				oldFileBuffer.connect();
  //				return;
  //			}
  //			fileBuffer.connect();
  //			fFileStoreFileBuffers.put(fileStore, fileBuffer);
  //		}
  //
  //		// Do notification outside synchronized block
  //		fireBufferCreated(fileBuffer);
  //	}

  /**
   * {@inheritDoc}
   *
   * @deprecated As of 3.3, replaced by {@link #disconnect(IPath, LocationKind, IProgressMonitor)}
   */
  public void disconnect(IPath location, IProgressMonitor monitor) throws CoreException {
    disconnect(location, LocationKind.NORMALIZE, monitor);
  }

  /*
   * @since 3.3
   */
  protected IPath normalizeLocation(IPath location) {
    return location;
  }

  /*
   * @see org.eclipse.core.filebuffers.IFileBufferManager#disconnect(org.eclipse.core.runtime.IPath, org.eclipse.core.filebuffers.IFileBufferManager.LocationKind, org.eclipse.core.runtime.IProgressMonitor)
   * @since 3.3
   */
  public void disconnect(IPath location, LocationKind locationKind, IProgressMonitor monitor)
      throws CoreException {
    Assert.isNotNull(location);
    if (locationKind == LocationKind.NORMALIZE) location = normalizeLocation(location);

    AbstractFileBuffer fileBuffer;
    synchronized (fFilesBuffers) {
      fileBuffer = internalGetFileBuffer(location);
      if (fileBuffer == null) return;

      fileBuffer.disconnect();
      if (!fileBuffer.isDisconnected()) return;

      fFilesBuffers.remove(location);
    }

    // Do notification outside synchronized block
    fireBufferDisposed(fileBuffer);
    fileBuffer.dispose();
  }

  //	/*
  //	 * @see
  // org.eclipse.core.filebuffers.IFileBufferManager#disconnectFileStore(org.eclipse.core.filesystem.IFileStore, org.eclipse.core.runtime.IProgressMonitor)
  //	 * @since 3.3
  //	 */
  //	public void disconnectFileStore(IFileStore fileStore, IProgressMonitor monitor) throws
  // CoreException {
  //		Assert.isLegal(fileStore != null);
  //
  //		AbstractFileBuffer fileBuffer;
  //		synchronized (fFileStoreFileBuffers) {
  //			fileBuffer= internalGetFileBuffer(fileStore);
  //			if (fileBuffer == null)
  //				return;
  //
  //			fileBuffer.disconnect();
  //			if (!fileBuffer.isDisconnected())
  //				return;
  //
  //			fFileStoreFileBuffers.remove(fileStore);
  //		}
  //
  //		// Do notification outside synchronized block
  //		fireBufferDisposed(fileBuffer);
  //		fileBuffer.dispose();
  //	}

  //	/**
  //	 * {@inheritDoc}
  //	 *
  //	 * @deprecated As of 3.2, replaced by {@link #isTextFileLocation(IPath, boolean)}
  //	 */
  //	public boolean isTextFileLocation(IPath location) {
  //		return isTextFileLocation(location, false);
  //	}
  //
  //	/**
  //	 * Returns whether a file store at the given location is or can be considered a
  //	 * text file. If the file store exists, the concrete content type of the file store is
  //	 * checked. If the concrete content type for the existing file store can not be
  //	 * determined, this method returns <code>!strict</code>. If the file store does
  //	 * not exist, it is checked whether a text content type is associated with
  //	 * the given location. If no content type is associated with the location,
  //	 * this method returns <code>!strict</code>.
  //	 * <p>
  //	 * The provided location is either a full path of a workspace resource or an
  //	 * absolute path in the local file system. The file buffer manager does not
  //	 * resolve the location of workspace resources in the case of linked
  //	 * resources.
  //	 * </p>
  //	 *
  //	 * @param fileStore	file store to check
  //	 * @param strict	<code>true</code> if a file with unknown content type
  //	 * 					is not treated as text file, <code>false</code> otherwise
  //	 * @return <code>true</code> if the location is a text file location
  //	 * @since 3.3
  //	 */
  //	protected boolean isTextFileLocation(IFileStore fileStore, boolean strict) {
  //		if (fileStore == null)
  //			return false;
  //
  //		IContentTypeManager manager= Platform.getContentTypeManager();
  //		IFileInfo fileInfo= fileStore.fetchInfo();
  //		if (fileInfo.exists()) {
  //			InputStream is= null;
  //			try {
  //				is= fileStore.openInputStream(EFS.NONE, null);
  //				IContentDescription description= manager.getDescriptionFor(is, fileStore.getName(),
  // IContentDescription.ALL);
  //				if (description != null) {
  //					IContentType type= description.getContentType();
  //					if (type != null)
  //						return type.isKindOf(TEXT_CONTENT_TYPE);
  //				}
  //			} catch (CoreException ex) {
  //				// ignore: API specification tells return true if content type can't be determined
  //			} catch (IOException ex) {
  //				// ignore: API specification tells return true if content type can't be determined
  //			} finally {
  //				if (is != null ) {
  //					try {
  //						is.close();
  //					} catch (IOException e) {
  //						// ignore: API specification tells to return true if content type can't be determined
  //					}
  //				}
  //			}
  //
  //			return !strict;
  //
  //		}
  //
  //		IContentType[] contentTypes= manager.findContentTypesFor(fileStore.getName());
  //		if (contentTypes != null && contentTypes.length > 0) {
  //			for (int i= 0; i < contentTypes.length; i++)
  //				if (contentTypes[i].isKindOf(TEXT_CONTENT_TYPE))
  //					return true;
  //			return false;
  //		}
  //		return !strict;
  //	}

  /*
   * @see org.eclipse.core.filebuffers.ITextFileBufferManager#isTextFileLocation(org.eclipse.core.runtime.IPath, boolean)
   * @since 3.2
   */
  public boolean isTextFileLocation(IPath location, boolean strict) {
    //		Assert.isNotNull(location);
    //		location= normalizeLocation(location);
    //		try {
    //			return isTextFileLocation(lo, strict);
    //		} catch (CoreException ex) {
    //			return false;
    //		}
    return true;
  }

  /**
   * {@inheritDoc}
   *
   * @deprecated As of 3.3, replaced by {@link #getFileBuffer(IPath, LocationKind)}
   */
  public IFileBuffer getFileBuffer(IPath location) {
    return getFileBuffer(location, LocationKind.NORMALIZE);
  }

  /*
   * @see org.eclipse.core.filebuffers.IFileBufferManager#getFileBuffer(org.eclipse.core.runtime.IPath, org.eclipse.core.filebuffers.IFileBufferManager.LocationKind)
   * @since 3.3
   */
  public IFileBuffer getFileBuffer(IPath location, LocationKind locationKind) {
    if (locationKind == LocationKind.NORMALIZE) location = normalizeLocation(location);
    return internalGetFileBuffer(location);
  }

  //	/*
  //	 * @see
  // org.eclipse.core.filebuffers.IFileBufferManager#getFileStoreFileBuffer(org.eclipse.core.filesystem.IFileStore)
  //	 * @since 3.3
  //	 */
  //	public IFileBuffer getFileStoreFileBuffer(IFileStore fileStore) {
  //		Assert.isLegal(fileStore != null);
  //		return internalGetFileBuffer(fileStore);
  //	}

  private AbstractFileBuffer internalGetFileBuffer(IPath location) {
    synchronized (fFilesBuffers) {
      return (AbstractFileBuffer) fFilesBuffers.get(location);
    }
  }

  //	private FileStoreFileBuffer internalGetFileBuffer(IFileStore fileStore) {
  //		synchronized (fFileStoreFileBuffers) {
  //			return (FileStoreFileBuffer)fFileStoreFileBuffers.get(fileStore);
  //		}
  //	}

  /**
   * {@inheritDoc}
   *
   * @deprecated As of 3.3, replaced by {@link #getTextFileBuffer(IPath, LocationKind)}
   */
  public ITextFileBuffer getTextFileBuffer(IPath location) {
    return getTextFileBuffer(location, LocationKind.NORMALIZE);
  }

  /*
   * @see org.eclipse.core.filebuffers.ITextFileBufferManager#getTextFileBuffer(org.eclipse.core.runtime.IPath, org.eclipse.core.filebuffers.IFileBufferManager.LocationKind)
   * @since 3.3
   */
  public ITextFileBuffer getTextFileBuffer(IPath location, LocationKind locationKind) {
    return (ITextFileBuffer) getFileBuffer(location, locationKind);
  }
  //
  //	/*
  //	 * @see
  // org.eclipse.core.filebuffers.ITextFileBufferManager#getFileStoreTextFileBuffer(org.eclipse.core.filesystem.IFileStore)
  //	 * @since 3.3
  //	 */
  //	public ITextFileBuffer getFileStoreTextFileBuffer(IFileStore fileStore) {
  //		Assert.isLegal(fileStore != null);
  //		return (ITextFileBuffer)getFileStoreFileBuffer(fileStore);
  //	}

  /*
   * @see org.eclipse.core.filebuffers.ITextFileBufferManager#getTextFileBuffer(org.eclipse.jface.text.IDocument)
   * @since 3.3
   */
  public ITextFileBuffer getTextFileBuffer(IDocument document) {
    Assert.isLegal(document != null);
    Iterator iter;
    synchronized (fFilesBuffers) {
      iter = new ArrayList(fFilesBuffers.values()).iterator();
    }

    while (iter.hasNext()) {
      Object buffer = iter.next();
      if (buffer instanceof ITextFileBuffer) {
        ITextFileBuffer textFileBuffer = (ITextFileBuffer) buffer;
        if (textFileBuffer.getDocument() == document) {
          if (!((AbstractFileBuffer) textFileBuffer).isDisconnected()) return textFileBuffer;
          return null;
        }
      }
    }
    synchronized (fFileStoreFileBuffers) {
      iter = new ArrayList(fFileStoreFileBuffers.values()).iterator();
    }
    while (iter.hasNext()) {
      Object buffer = iter.next();
      if (buffer instanceof ITextFileBuffer) {
        ITextFileBuffer textFileBuffer = (ITextFileBuffer) buffer;
        if (textFileBuffer.getDocument() == document) {
          if (!((AbstractFileBuffer) textFileBuffer).isDisconnected()) return textFileBuffer;
        }
      }
    }
    return null;
  }

  /*
   * @see org.eclipse.core.filebuffers.IFileBufferManager#getFileBuffers()
   * @since 3.4
   */
  public IFileBuffer[] getFileBuffers() {
    synchronized (fFilesBuffers) {
      Collection values = fFilesBuffers.values();
      return (IFileBuffer[]) values.toArray(new IFileBuffer[values.size()]);
    }
  }

  /*
   * @see org.eclipse.core.filebuffers.IFileBufferManager#getFileStoreFileBuffers()
   * @since 3.4
   */
  public IFileBuffer[] getFileStoreFileBuffers() {
    synchronized (fFileStoreFileBuffers) {
      Collection values = fFileStoreFileBuffers.values();
      return (IFileBuffer[]) values.toArray(new IFileBuffer[values.size()]);
    }
  }

  /*
   * @see org.eclipse.core.buffer.text.IBufferedFileManager#getDefaultEncoding()
   */
  public String getDefaultEncoding() {
    return System.getProperty("file.encoding"); // $NON-NLS-1$;
  }

  /**
   * {@inheritDoc}
   *
   * @deprecated As of 3.3, replaced by {@link #createEmptyDocument(IPath, LocationKind)}
   */
  public IDocument createEmptyDocument(IPath location) {
    return createEmptyDocument(location, LocationKind.NORMALIZE);
  }

  /*
   * @see org.eclipse.core.filebuffers.ITextFileBufferManager#createEmptyDocument(org.eclipse.core.runtime.IPath, org.eclipse.core.filebuffers.LocationKind)
   * @since 3.3
   */
  public IDocument createEmptyDocument(final IPath location, final LocationKind locationKind) {
    //		IDocument documentFromFactory= createDocumentFromFactory(location, locationKind);
    final IDocument document;
    //		if (documentFromFactory != null)
    //			document= documentFromFactory;
    //		else
    document = new SynchronizableDocument();

    if (location == null) return document;

    // Set the initial line delimiter
    if (document instanceof IDocumentExtension4) {
      String initalLineDelimiter = getLineDelimiterPreference(location, locationKind);
      if (initalLineDelimiter != null)
        ((IDocumentExtension4) document).setInitialLineDelimiter(initalLineDelimiter);
    }

    //		final IDocumentSetupParticipant[] participants=
    // fRegistry.getDocumentSetupParticipants(location, locationKind);
    //		if (participants != null) {
    //			for (int i= 0; i < participants.length; i++) {
    //				final IDocumentSetupParticipant participant= participants[i];
    //				ISafeRunnable runnable= new ISafeRunnable() {
    //					public void run() throws Exception {
    //						if (participant instanceof IDocumentSetupParticipantExtension)
    //							((IDocumentSetupParticipantExtension)participant).setup(document, location,
    // locationKind);
    //						else
    //							participant.setup(document);
    //
    //						if (document.getDocumentPartitioner() != null) {
    //							String message=
    // NLSUtility.format(FileBuffersMessages.TextFileBufferManager_warning_documentSetupInstallsDefaultPartitioner, participant.getClass());
    //							IStatus status= new Status(IStatus.WARNING, FileBuffersPlugin.PLUGIN_ID, IStatus.OK,
    // message, null);
    //							FileBuffersPlugin.getDefault().log(status);
    //						}
    //					}
    //					public void handleException(Throwable t) {
    //						IStatus status= new Status(IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID, IStatus.OK,
    // FileBuffersMessages.TextFileBufferManager_error_documentSetupFailed, t);
    //						FileBuffersPlugin.getDefault().log(status);
    //					}
    //				};
    //				SafeRunner.run(runnable);
    //			}
    //		}

    return document;
  }

  //	/**
  //	 * Helper to get rid of deprecation warnings.
  //	 *
  //	 * @param location the location of the file to be connected
  //	 * @param locationKind the kind of the given location
  //	 * @return the created empty document or <code>null</code> if none got created
  //	 * @since 3.5
  //	 * @deprecated As of 3.5
  //	 */
  //	private IDocument createDocumentFromFactory(final IPath location, final LocationKind
  // locationKind) {
  //		final IDocument[] runnableResult= new IDocument[1];
  //		if (location != null) {
  //			final org.eclipse.core.filebuffers.IDocumentFactory factory=
  // fRegistry.getDocumentFactory(location, locationKind);
  //			if (factory != null) {
  //				ISafeRunnable runnable= new ISafeRunnable() {
  //					public void run() throws Exception {
  //						runnableResult[0]= factory.createDocument();
  //					}
  //					public void handleException(Throwable t) {
  //						IStatus status= new Status(IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID, IStatus.OK,
  // FileBuffersMessages.TextFileBufferManager_error_documentFactoryFailed, t);
  //						FileBuffersPlugin.getDefault().log(status);
  //					}
  //				};
  //				SafeRunner.run(runnable);
  //			}
  //		}
  //		return runnableResult[0];
  //	}

  //	/**
  //	 * {@inheritDoc}
  //	 *
  //	 * @deprecated As of 3.3, replaced by {@link #createAnnotationModel(IPath, LocationKind)}
  //	 */
  //	public IAnnotationModel createAnnotationModel(IPath location) {
  //		return createAnnotationModel(location, LocationKind.NORMALIZE);
  //	}

  //	/*
  //	 * @see
  // org.eclipse.core.filebuffers.ITextFileBufferManager#createAnnotationModel(org.eclipse.core.runtime.IPath, org.eclipse.core.filebuffers.LocationKind)
  //	 * @since 3.3
  //	 */
  //	public IAnnotationModel createAnnotationModel(IPath location, LocationKind locationKind) {
  //		Assert.isNotNull(location);
  //		IAnnotationModelFactory factory= fRegistry.getAnnotationModelFactory(location, locationKind);
  //		if (factory != null)
  //			return factory.createAnnotationModel(location);
  //		return null;
  //	}
  //
  //	/*
  //	 * @see
  // org.eclipse.core.filebuffers.IFileBufferManager#addFileBufferListener(org.eclipse.core.filebuffers.IFileBufferListener)
  //	 */
  //	public void addFileBufferListener(IFileBufferListener listener) {
  //		Assert.isNotNull(listener);
  //		synchronized (fFileBufferListeners) {
  //			if (!fFileBufferListeners.contains(listener))
  //				fFileBufferListeners.add(listener);
  //		}
  //	}
  //
  //	/*
  //	 * @see
  // org.eclipse.core.filebuffers.IFileBufferManager#removeFileBufferListener(org.eclipse.core.filebuffers.IFileBufferListener)
  //	 */
  //	public void removeFileBufferListener(IFileBufferListener listener) {
  //		Assert.isNotNull(listener);
  //		synchronized (fFileBufferListeners) {
  //			fFileBufferListeners.remove(listener);
  //		}
  //	}
  //
  /*
   * @see org.eclipse.core.filebuffers.IFileBufferManager#setSynchronizationContext(org.eclipse.core.filebuffers.ISynchronizationContext)
   */
  public void setSynchronizationContext(ISynchronizationContext context) {
    fSynchronizationContext = context;
  }

  /**
   * {@inheritDoc}
   *
   * @deprecated As of 3.1, replaced by {@link
   *     org.eclipse.core.filebuffers.IFileBuffer#requestSynchronizationContext()}
   */
  public void requestSynchronizationContext(IPath location) {
    Assert.isNotNull(location);
    location = normalizeLocation(location);

    AbstractFileBuffer fileBuffer = internalGetFileBuffer(location);
    if (fileBuffer != null) fileBuffer.requestSynchronizationContext();
  }

  /**
   * {@inheritDoc}
   *
   * @deprecated As of 3.1, replaced by {@link IFileBuffer#releaseSynchronizationContext()}
   */
  public void releaseSynchronizationContext(IPath location) {
    Assert.isNotNull(location);
    location = normalizeLocation(location);

    AbstractFileBuffer fileBuffer = internalGetFileBuffer(location);
    if (fileBuffer != null) fileBuffer.releaseSynchronizationContext();
  }

  @Override
  public void addFileBufferListener(IFileBufferListener listener) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeFileBufferListener(IFileBufferListener listener) {
    throw new UnsupportedOperationException();
  }

  /** {@inheritDoc} */
  public void execute(Runnable runnable) {
    if (fSynchronizationContext != null) fSynchronizationContext.run(runnable);
    else runnable.run();
  }

  private AbstractFileBuffer createFileBuffer(IPath location, LocationKind locationKind) {
    /*
     * XXX: the following code is commented out for performance
     * reasons and because we do not yet create a special binary
     * file buffer.
     */
    //		if (isTextFileLocation(location, false))
    //			return createTextFileBuffer(location);
    //		return createBinaryFileBuffer(location, locationKind);
    return createTextFileBuffer(location, locationKind);
  }

  /**
   * Creates a text file buffer for the given path.
   *
   * @param location the location of the file to be connected
   * @param locationKind the kind of the given location
   * @return the text file buffer
   * @since 3.3
   */
  protected AbstractFileBuffer createTextFileBuffer(IPath location, LocationKind locationKind) {
    //		Assert.isLegal(locationKind != LocationKind.IFILE);
    return new FileStoreTextFileBuffer(this);
  }

  //	private AbstractFileBuffer createBinaryFileBuffer(IPath location, LocationKind locationKind) {
  //		// XXX: should return a binary file buffer - using text file buffer for now
  //		return createTextFileBuffer(location, locationKind);
  //	}

  //	private FileStoreFileBuffer createFileBuffer(IFileStore location) {
  //		/*
  //		 * XXX: the following code is commented out for performance
  //		 * reasons and because we do not yet create a special binary
  //		 * file buffer.
  //		 */
  ////		if (isTextFileLocation(location, false))
  ////			return createTextFileBuffer(location);
  ////		return createBinaryFileBuffer(location);
  //		return createTextFileBuffer(location);
  //
  //	}

  //	/**
  //	 * Creates a text file buffer for the given file store.
  //	 *
  //	 * @param location the file store
  //	 * @return the text file buffer
  //	 * @since 3.3
  //	 */
  //	protected FileStoreFileBuffer createTextFileBuffer(IFileStore location) {
  //		return new FileStoreTextFileBuffer(this);
  //	}

  //	private FileStoreFileBuffer createBinaryFileBuffer(FileStore location) {
  //		// XXX: should return a binary file buffer - using text file buffer for now
  //		return createTextFileBuffer(location);
  //	}

  private Iterator getFileBufferListenerIterator() {
    synchronized (fFileBufferListeners) {
      return new ArrayList(fFileBufferListeners).iterator();
    }
  }

  protected void fireDirtyStateChanged(final IFileBuffer buffer, final boolean isDirty) {
    Iterator e = getFileBufferListenerIterator();
    while (e.hasNext()) {
      final IFileBufferListener l = (IFileBufferListener) e.next();
      SafeRunner.run(
          new SafeNotifier() {
            public void run() {
              l.dirtyStateChanged(buffer, isDirty);
            }
          });
    }
  }

  protected void fireBufferContentAboutToBeReplaced(final IFileBuffer buffer) {
    Iterator e = getFileBufferListenerIterator();
    while (e.hasNext()) {
      final IFileBufferListener l = (IFileBufferListener) e.next();
      SafeRunner.run(
          new SafeNotifier() {
            public void run() {
              l.bufferContentAboutToBeReplaced(buffer);
            }
          });
    }
  }

  protected void fireBufferContentReplaced(final IFileBuffer buffer) {
    Iterator e = getFileBufferListenerIterator();
    while (e.hasNext()) {
      final IFileBufferListener l = (IFileBufferListener) e.next();
      SafeRunner.run(
          new SafeNotifier() {
            public void run() {
              l.bufferContentReplaced(buffer);
            }
          });
    }
  }

  protected void fireUnderlyingFileMoved(final IFileBuffer buffer, final IPath target) {
    Iterator e = getFileBufferListenerIterator();
    while (e.hasNext()) {
      final IFileBufferListener l = (IFileBufferListener) e.next();
      SafeRunner.run(
          new SafeNotifier() {
            public void run() {
              l.underlyingFileMoved(buffer, target);
            }
          });
    }
  }

  protected void fireUnderlyingFileDeleted(final IFileBuffer buffer) {
    Iterator e = getFileBufferListenerIterator();
    while (e.hasNext()) {
      final IFileBufferListener l = (IFileBufferListener) e.next();
      SafeRunner.run(
          new SafeNotifier() {
            public void run() {
              l.underlyingFileDeleted(buffer);
            }
          });
    }
  }

  protected void fireStateValidationChanged(
      final IFileBuffer buffer, final boolean isStateValidated) {
    Iterator e = getFileBufferListenerIterator();
    while (e.hasNext()) {
      final IFileBufferListener l = (IFileBufferListener) e.next();
      SafeRunner.run(
          new SafeNotifier() {
            public void run() {
              l.stateValidationChanged(buffer, isStateValidated);
            }
          });
    }
  }

  protected void fireStateChanging(final IFileBuffer buffer) {
    Iterator e = getFileBufferListenerIterator();
    while (e.hasNext()) {
      final IFileBufferListener l = (IFileBufferListener) e.next();
      SafeRunner.run(
          new SafeNotifier() {
            public void run() {
              l.stateChanging(buffer);
            }
          });
    }
  }

  protected void fireStateChangeFailed(final IFileBuffer buffer) {
    Iterator e = getFileBufferListenerIterator();
    while (e.hasNext()) {
      final IFileBufferListener l = (IFileBufferListener) e.next();
      SafeRunner.run(
          new SafeNotifier() {
            public void run() {
              l.stateChangeFailed(buffer);
            }
          });
    }
  }

  protected void fireBufferCreated(final IFileBuffer buffer) {
    Iterator e = getFileBufferListenerIterator();
    while (e.hasNext()) {
      final IFileBufferListener l = (IFileBufferListener) e.next();
      SafeRunner.run(
          new SafeNotifier() {
            public void run() {
              l.bufferCreated(buffer);
            }
          });
    }
  }

  protected void fireBufferDisposed(final IFileBuffer buffer) {
    Iterator e = getFileBufferListenerIterator();
    while (e.hasNext()) {
      final IFileBufferListener l = (IFileBufferListener) e.next();
      SafeRunner.run(
          new SafeNotifier() {
            public void run() {
              l.bufferDisposed(buffer);
            }
          });
    }
  }

  /*
   * @see org.eclipse.core.filebuffers.IFileBufferManager#validateState(org.eclipse.core.filebuffers.IFileBuffer[], org.eclipse.core.runtime.IProgressMonitor, java.lang.Object)
   * @since 3.1
   */
  public void validateState(
      final IFileBuffer[] fileBuffers, IProgressMonitor monitor, final Object computationContext)
      throws CoreException {}

  /**
   * Returns the line delimiter to be used by the given location.
   *
   * @param location the location of the file to be connected
   * @param locationKind the kind of the given location
   * @return the line delimiter
   * @since 3.3
   */
  protected String getLineDelimiterPreference(IPath location, LocationKind locationKind) {
    return System.getProperty("line.separator"); // $NON-NLS-1$
  }
}
