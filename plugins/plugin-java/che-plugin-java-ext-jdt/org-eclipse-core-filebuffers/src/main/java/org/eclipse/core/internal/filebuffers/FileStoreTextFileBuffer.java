/**
 * ***************************************************************************** Copyright (c) 2007,
 * 2013 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation James Blackburn
 * <jamesblackburn+eclipse@gmail.com> - [implementation] FileStoreTextFileBuffer eats IOException on
 * external file save - https://bugs.eclipse.org/333660
 * *****************************************************************************
 */
package org.eclipse.core.internal.filebuffers;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.SequenceInputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnmappableCharacterException;
import java.nio.charset.UnsupportedCharsetException;
import org.eclipse.core.filebuffers.IFileBufferStatusCodes;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.source.IAnnotationModel;

/** @since 3.3 (previously available as JavaTextFileBuffer since 3.3) */
public class FileStoreTextFileBuffer extends FileStoreFileBuffer implements ITextFileBuffer {

  private class DocumentListener implements IDocumentListener {

    /*
     * @see org.eclipse.jface.text.IDocumentListener#documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
     */
    public void documentAboutToBeChanged(DocumentEvent event) {}

    /*
     * @see org.eclipse.jface.text.IDocumentListener#documentChanged(org.eclipse.jface.text.DocumentEvent)
     */
    public void documentChanged(DocumentEvent event) {
      fCanBeSaved = true;
      removeFileBufferContentListeners();
      fManager.fireDirtyStateChanged(FileStoreTextFileBuffer.this, fCanBeSaved);
    }
  }

  /** Reader chunk size. */
  private static final int READER_CHUNK_SIZE = 2048;
  /** Buffer size. */
  private static final int BUFFER_SIZE = 8 * READER_CHUNK_SIZE;
  /** Constant for representing the error status. This is considered a value object. */
  private static final IStatus STATUS_ERROR =
      new Status(
          IStatus.ERROR,
          FileBuffersPlugin.PLUGIN_ID,
          IStatus.OK,
          FileBuffersMessages.FileBuffer_status_error,
          null);
  /** Constant denoting UTF-8 encoding. */
  private static final String CHARSET_UTF_8 = "UTF-8"; // $NON-NLS-1$

  /**
   * Constant denoting an empty set of properties
   *
   * @since 3.1
   */
  private static final QualifiedName[] NO_PROPERTIES = new QualifiedName[0];

  /** The element's document */
  protected IDocument fDocument;
  /**
   * The encoding used to create the document from the storage or <code>null</code> for workbench
   * encoding.
   */
  protected String fEncoding;
  /** Internal document listener */
  protected IDocumentListener fDocumentListener = new DocumentListener();
  /** The encoding which has explicitly been set on the file. */
  private String fExplicitEncoding;
  /** Tells whether the file on disk has a BOM. */
  private boolean fHasBOM;
  /** The annotation model of this file buffer */
  private IAnnotationModel fAnnotationModel;
  /**
   * Lock for lazy creation of annotation model.
   *
   * @since 3.2
   */
  private final Object fAnnotationModelCreationLock = new Object();
  /**
   * Tells whether the cache is up to date.
   *
   * @since 3.2
   */
  private boolean fIsCacheUpdated = false;

  public FileStoreTextFileBuffer(TextFileBufferManager manager) {
    super(manager);
  }

  /*
   * @see org.eclipse.core.filebuffers.ITextFileBuffer#getDocument()
   */
  public IDocument getDocument() {
    return fDocument;
  }

  /*
   * @see org.eclipse.core.filebuffers.ITextFileBuffer#getAnnotationModel()
   */
  public IAnnotationModel getAnnotationModel() {
    //		synchronized (fAnnotationModelCreationLock) {
    //			if (fAnnotationModel == null && !isDisconnected()) {
    //				fAnnotationModel = fManager.createAnnotationModel(getLocationOrName(),
    // LocationKind.LOCATION);
    //				if (fAnnotationModel != null)
    //					fAnnotationModel.connect(fDocument);
    //			}
    //		}
    //		return fAnnotationModel;
    throw new UnsupportedOperationException("getAnnotationModel");
  }

  /*
   * @see org.eclipse.core.filebuffers.ITextFileBuffer#getEncoding()
   */
  public String getEncoding() {
    if (!fIsCacheUpdated) cacheEncodingState();
    return fEncoding;
  }

  /*
   * @see org.eclipse.core.filebuffers.ITextFileBuffer#setEncoding(java.lang.String)
   */
  public void setEncoding(String encoding) {
    fExplicitEncoding = encoding;
    if (encoding == null || encoding.equals(fEncoding)) fIsCacheUpdated = false;
    else {
      fEncoding = encoding;
      fHasBOM = false;
    }
  }

  /*
   * @see org.eclipse.core.filebuffers.ITextFileBuffer#getStatus()
   */
  public IStatus getStatus() {
    if (!isDisconnected()) {
      if (fStatus != null) return fStatus;
      return (fDocument == null ? STATUS_ERROR : Status.OK_STATUS);
    }
    return STATUS_ERROR;
  }

  private InputStream getFileContents(IFileStore fileStore) throws CoreException {
    if (!fFileStore.fetchInfo().exists()) return null;

    return fileStore.openInputStream(EFS.NONE, null);
  }

  private void setFileContents(InputStream stream, IProgressMonitor monitor) throws CoreException {
    OutputStream out = fFileStore.openOutputStream(EFS.NONE, null);
    try {
      byte[] buffer = new byte[8192];
      while (true) {
        int bytesRead = -1;
        bytesRead = stream.read(buffer);
        if (bytesRead == -1) {
          out.close();
          break;
        }
        out.write(buffer, 0, bytesRead);
        if (monitor != null) monitor.worked(1);
      }
    } catch (IOException ex) {
      String message = (ex.getMessage() != null ? ex.getMessage() : ""); // $NON-NLS-1$
      IStatus s = new Status(IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID, IStatus.OK, message, ex);
      throw new CoreException(s);
    } finally {
      try {
        stream.close();
      } catch (IOException e) {
      } finally {
        try {
          out.close();
        } catch (IOException e) {
        }
      }
    }
  }

  /*
   * @see org.eclipse.core.filebuffers.IFileBuffer#revert(org.eclipse.core.runtime.IProgressMonitor)
   */
  public void revert(IProgressMonitor monitor) throws CoreException {
    if (isDisconnected()) return;

    IDocument original = null;
    fStatus = null;

    try {
      original = fManager.createEmptyDocument(getLocationOrName(), LocationKind.LOCATION);
      cacheEncodingState();
      setDocumentContent(original, fFileStore, fEncoding, fHasBOM, monitor);
    } catch (CoreException x) {
      fStatus = x.getStatus();
    }

    if (original == null) return;

    String originalContents = original.get();
    boolean replaceContents = !originalContents.equals(fDocument.get());

    if (!replaceContents && !fCanBeSaved) return;

    fManager.fireStateChanging(this);
    try {

      if (replaceContents) {
        fManager.fireBufferContentAboutToBeReplaced(this);
        fDocument.set(original.get());
      }

      boolean fireDirtyStateChanged = fCanBeSaved;
      if (fCanBeSaved) {
        fCanBeSaved = false;
        addFileBufferContentListeners();
      }

      if (replaceContents) fManager.fireBufferContentReplaced(this);

      IFileInfo info = fFileStore.fetchInfo();
      if (info.exists()) fSynchronizationStamp = fFileStore.fetchInfo().getLastModified();

      //			if (fAnnotationModel instanceof IPersistableAnnotationModel) {
      //				IPersistableAnnotationModel persistableModel =
      // (IPersistableAnnotationModel)fAnnotationModel;
      //				try {
      //					persistableModel.revert(fDocument);
      //				} catch (CoreException x) {
      //					fStatus = x.getStatus();
      //				}
      //			}

      if (fireDirtyStateChanged) fManager.fireDirtyStateChanged(this, fCanBeSaved);

    } catch (RuntimeException x) {
      fManager.fireStateChangeFailed(this);
      throw x;
    }
  }

  /*
   * @see org.eclipse.core.filebuffers.IFileBuffer#getContentType()
   * @since 3.1
   */
  public IContentType getContentType() throws CoreException {
    InputStream stream = null;
    try {
      if (isDirty()) {
        Reader reader = new DocumentReader(getDocument());
        try {
          IContentDescription desc =
              Platform.getContentTypeManager()
                  .getDescriptionFor(reader, fFileStore.getName(), NO_PROPERTIES);
          if (desc != null && desc.getContentType() != null) return desc.getContentType();
        } finally {
          try {
            reader.close();
          } catch (IOException ex) {
          }
        }
      }
      stream = fFileStore.openInputStream(EFS.NONE, null);
      IContentDescription desc =
          Platform.getContentTypeManager()
              .getDescriptionFor(stream, fFileStore.getName(), NO_PROPERTIES);
      if (desc != null && desc.getContentType() != null) return desc.getContentType();
      return null;
    } catch (IOException x) {
      throw new CoreException(
          new Status(
              IStatus.ERROR,
              FileBuffersPlugin.PLUGIN_ID,
              IStatus.OK,
              NLSUtility.format(
                  FileBuffersMessages.FileBuffer_error_queryContentDescription,
                  fFileStore.toString()),
              x));
    } finally {
      try {
        if (stream != null) stream.close();
      } catch (IOException x) {
      }
    }
  }

  /*
   * @see org.eclipse.core.internal.filebuffers.FileBuffer#addFileBufferContentListeners()
   */
  protected void addFileBufferContentListeners() {
    if (fDocument != null) fDocument.addDocumentListener(fDocumentListener);
  }

  /*
   * @see org.eclipse.core.internal.filebuffers.FileBuffer#removeFileBufferContentListeners()
   */
  protected void removeFileBufferContentListeners() {
    if (fDocument != null) fDocument.removeDocumentListener(fDocumentListener);
  }

  /*
   * @see org.eclipse.core.internal.filebuffers.FileBuffer#initializeFileBufferContent(org.eclipse.core.runtime.IProgressMonitor)
   */
  protected void initializeFileBufferContent(IProgressMonitor monitor) throws CoreException {
    try {
      fDocument = fManager.createEmptyDocument(getLocationOrName(), LocationKind.LOCATION);
      //			cacheEncodingState();
      setDocumentContent(fDocument, fFileStore, fEncoding, fHasBOM, monitor);
    } catch (CoreException x) {
      fDocument = fManager.createEmptyDocument(getLocationOrName(), LocationKind.LOCATION);
      fStatus = x.getStatus();
    }
  }

  /*
   * @see org.eclipse.core.internal.filebuffers.ResourceFileBuffer#connected()
   */
  protected void connected() {
    super.connected();
    if (fAnnotationModel != null) fAnnotationModel.connect(fDocument);
  }

  /*
   * @see org.eclipse.core.internal.filebuffers.ResourceFileBuffer#disconnected()
   */
  protected void disconnected() {
    if (fAnnotationModel != null) fAnnotationModel.disconnect(fDocument);
    super.disconnected();
  }

  protected void cacheEncodingState() {
    fEncoding = fExplicitEncoding;
    fHasBOM = false;
    fIsCacheUpdated = true;

    InputStream stream = null;
    try {
      stream = getFileContents(fFileStore);
      if (stream == null) return;

      QualifiedName[] options =
          new QualifiedName[] {IContentDescription.CHARSET, IContentDescription.BYTE_ORDER_MARK};
      IContentDescription description =
          null; // Platform.getContentTypeManager().getDescriptionFor(stream, fFileStore.getName(),
      // options);
      if (description != null) {
        fHasBOM = description.getProperty(IContentDescription.BYTE_ORDER_MARK) != null;
        if (fEncoding == null) fEncoding = description.getCharset();
      }
    } catch (CoreException e) {
      // do nothing
    } /*catch (IOException e) {
      	// do nothing
      }*/ finally {
      try {
        if (stream != null) stream.close();
      } catch (IOException ex) {
        FileBuffersPlugin.getDefault()
            .log(
                new Status(
                    IStatus.ERROR,
                    FileBuffersPlugin.PLUGIN_ID,
                    IStatus.OK,
                    FileBuffersMessages.JavaTextFileBuffer_error_closeStream,
                    ex));
      }
    }

    // Use global default
    if (fEncoding == null) fEncoding = fManager.getDefaultEncoding();
  }

  /*
   * @see org.eclipse.core.internal.filebuffers.FileBuffer#commitFileBufferContent(org.eclipse.core.runtime.IProgressMonitor, boolean)
   */
  protected void commitFileBufferContent(IProgressMonitor monitor, boolean overwrite)
      throws CoreException {
    //		if (!isSynchronized() && !overwrite)
    //			throw new CoreException(new Status(IStatus.WARNING, FileBuffersPlugin.PLUGIN_ID,
    // IResourceStatus.OUT_OF_SYNC_LOCAL, FileBuffersMessages.FileBuffer_error_outOfSync, null));

    String encoding = computeEncoding();

    Charset charset;
    try {
      charset = Charset.forName(encoding);
    } catch (UnsupportedCharsetException ex) {
      String message =
          NLSUtility.format(
              FileBuffersMessages.ResourceTextFileBuffer_error_unsupported_encoding_message_arg,
              encoding);
      IStatus s = new Status(IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID, IStatus.OK, message, ex);
      throw new CoreException(s);
    } catch (IllegalCharsetNameException ex) {
      String message =
          NLSUtility.format(
              FileBuffersMessages.ResourceTextFileBuffer_error_illegal_encoding_message_arg,
              encoding);
      IStatus s = new Status(IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID, IStatus.OK, message, ex);
      throw new CoreException(s);
    }

    CharsetEncoder encoder = charset.newEncoder();
    encoder.onMalformedInput(CodingErrorAction.REPLACE);
    encoder.onUnmappableCharacter(CodingErrorAction.REPORT);

    byte[] bytes;
    int bytesLength;

    try {
      ByteBuffer byteBuffer = encoder.encode(CharBuffer.wrap(fDocument.get()));
      bytesLength = byteBuffer.limit();
      if (byteBuffer.hasArray()) bytes = byteBuffer.array();
      else {
        bytes = new byte[bytesLength];
        byteBuffer.get(bytes);
      }
    } catch (CharacterCodingException ex) {
      Assert.isTrue(ex instanceof UnmappableCharacterException);
      String message =
          NLSUtility.format(
              FileBuffersMessages.ResourceTextFileBuffer_error_charset_mapping_failed_message_arg,
              encoding);
      IStatus s =
          new Status(
              IStatus.ERROR,
              FileBuffersPlugin.PLUGIN_ID,
              IFileBufferStatusCodes.CHARSET_MAPPING_FAILED,
              message,
              null);
      throw new CoreException(s);
    }

    IFileInfo fileInfo = fFileStore.fetchInfo();
    if (fileInfo != null && fileInfo.exists()) {

      if (!overwrite) checkSynchronizationState();

      InputStream stream = new ByteArrayInputStream(bytes, 0, bytesLength);

      /*
       * XXX:
       * This is a workaround for a corresponding bug in Java readers and writer,
       * see http://developer.java.sun.com/developer/bugParade/bugs/4508058.html
       */
      if (fHasBOM && CHARSET_UTF_8.equals(encoding))
        stream =
            new SequenceInputStream(
                new ByteArrayInputStream(IContentDescription.BOM_UTF_8), stream);

      // here the file synchronizer should actually be removed and afterwards added again. However,
      // we are already inside an operation, so the delta is sent AFTER we have added the listener
      setFileContents(stream, monitor);
      // set synchronization stamp to know whether the file synchronizer must become active
      fSynchronizationStamp = fFileStore.fetchInfo().getLastModified();

      //			if (fAnnotationModel instanceof IPersistableAnnotationModel) {
      //				IPersistableAnnotationModel persistableModel= (IPersistableAnnotationModel)
      // fAnnotationModel;
      //				persistableModel.commit(fDocument);
      //			}

    } else {
      fFileStore.getParent().mkdir(EFS.NONE, null);
      OutputStream out = fFileStore.openOutputStream(EFS.NONE, null);
      try {
        /*
         * XXX:
         * This is a workaround for a corresponding bug in Java readers and writer,
         * see http://developer.java.sun.com/developer/bugParade/bugs/4508058.html
         */
        if (fHasBOM && CHARSET_UTF_8.equals(encoding)) out.write(IContentDescription.BOM_UTF_8);

        out.write(bytes, 0, bytesLength);
        out.flush();
        out.close();
      } catch (IOException x) {
        IStatus s =
            new Status(
                IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID, IStatus.OK, x.getLocalizedMessage(), x);
        throw new CoreException(s);
      } finally {
        try {
          out.close();
        } catch (IOException x) {
        }
      }

      // set synchronization stamp to know whether the file synchronizer must become active
      fSynchronizationStamp = fFileStore.fetchInfo().getLastModified();
    }
  }

  private String computeEncoding() {
    // Make sure cache is up to date
    if (!fIsCacheUpdated) cacheEncodingState();

    // User-defined encoding has first priority
    if (fExplicitEncoding != null) return fExplicitEncoding;

    //		// Probe content
    //		Reader reader= new DocumentReader(fDocument);
    //		try {
    //			QualifiedName[] options= new QualifiedName[] { IContentDescription.CHARSET,
    // IContentDescription.BYTE_ORDER_MARK };
    //			IContentDescription description= Platform.getContentTypeManager().getDescriptionFor(reader,
    // fFileStore.getName(), options);
    //			if (description != null) {
    //				String encoding= description.getCharset();
    //				if (encoding != null)
    //					return encoding;
    //			}
    //		} catch (IOException ex) {
    //			// Try next strategy
    //		} finally {
    //			try {
    //				reader.close();
    //			} catch (IOException x) {
    //			}
    //		}

    // Use file's encoding if the file has a BOM
    if (fHasBOM) return fEncoding;

    // Use global default
    return fManager.getDefaultEncoding();
  }

  /**
   * Initializes the given document with the given file's content using the given encoding.
   *
   * @param document the document to be initialized
   * @param file the file which delivers the document content
   * @param encoding the character encoding for reading the given stream
   * @param hasBOM tell whether the given file has a BOM
   * @param monitor the progress monitor
   * @exception CoreException if the given stream can not be read
   */
  private void setDocumentContent(
      IDocument document,
      IFileStore file,
      String encoding,
      boolean hasBOM,
      IProgressMonitor monitor)
      throws CoreException {
    InputStream contentStream = getFileContents(file);
    if (contentStream == null) return;

    Reader in = null;
    try {

      if (encoding == null) encoding = fManager.getDefaultEncoding();

      /*
       * XXX:
       * This is a workaround for a corresponding bug in Java readers and writer,
       * see http://developer.java.sun.com/developer/bugParade/bugs/4508058.html
       */
      if (hasBOM && CHARSET_UTF_8.equals(encoding)) {
        int n = 0;
        do {
          int bytes = contentStream.read(new byte[IContentDescription.BOM_UTF_8.length]);
          if (bytes == -1) throw new IOException();
          n += bytes;
        } while (n < IContentDescription.BOM_UTF_8.length);
      }

      in = new BufferedReader(new InputStreamReader(contentStream, encoding), BUFFER_SIZE);
      StringBuffer buffer = new StringBuffer(BUFFER_SIZE);
      char[] readBuffer = new char[READER_CHUNK_SIZE];
      int n = in.read(readBuffer);
      while (n > 0) {
        buffer.append(readBuffer, 0, n);
        n = in.read(readBuffer);
      }

      document.set(buffer.toString());

    } catch (IOException x) {
      String msg = x.getMessage() == null ? "" : x.getMessage(); // $NON-NLS-1$
      IStatus s = new Status(IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID, IStatus.OK, msg, x);
      throw new CoreException(s);
    } finally {
      try {
        if (in != null) in.close();
        else contentStream.close();
      } catch (IOException x) {
      }
    }
  }

  /**
   * Checks whether the given file is synchronized with the local file system. If the file has been
   * changed, a <code>CoreException</code> is thrown.
   *
   * @exception CoreException if file has been changed on the file system
   */
  private void checkSynchronizationState() throws CoreException {
    if (!isSynchronized()) {
      Status status =
          new Status(
              IStatus.ERROR,
              FileBuffersPlugin.PLUGIN_ID,
              274 /* IResourceStatus.OUT_OF_SYNC_LOCAL */,
              FileBuffersMessages.FileBuffer_error_outOfSync,
              null);
      throw new CoreException(status);
    }
  }

  /**
   * Returns the location if it is <code>null</code> or the name as <code>IPath</code> otherwise.
   *
   * @return a non-null <code>IPath</code>
   * @since 3.3.1
   */
  private IPath getLocationOrName() {
    IPath path = getLocation();
    if (path == null) path = new Path(fFileStore.getName());
    return path;
  }
}
