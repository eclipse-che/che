/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.javaeditor;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.BufferChangedEvent;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IBuffer.ITextEditCapability;
import org.eclipse.jdt.core.IBufferChangedListener;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.RewriteSessionEditProcessor;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.UndoEdit;

/**
 * Adapts <code>IDocument</code> to <code>IBuffer</code>. Uses the same algorithm as the text widget
 * to determine the buffer's line delimiter. All text inserted into the buffer is converted to this
 * line delimiter. This class is <code>public</code> for test purposes only.
 */
public class DocumentAdapter implements IBuffer, IDocumentListener, ITextEditCapability {

  /** Internal implementation of a NULL instanceof IBuffer. */
  private static class NullBuffer implements IBuffer {
    public void addBufferChangedListener(IBufferChangedListener listener) {}

    public void append(char[] text) {}

    public void append(String text) {}

    public void close() {}

    public char getChar(int position) {
      return 0;
    }

    public char[] getCharacters() {
      return null;
    }

    public String getContents() {
      return null;
    }

    public int getLength() {
      return 0;
    }

    public IOpenable getOwner() {
      return null;
    }

    public String getText(int offset, int length) {
      return null;
    }

    public IResource getUnderlyingResource() {
      return null;
    }

    public boolean hasUnsavedChanges() {
      return false;
    }

    public boolean isClosed() {
      return false;
    }

    public boolean isReadOnly() {
      return true;
    }

    public void removeBufferChangedListener(IBufferChangedListener listener) {}

    public void replace(int position, int length, char[] text) {}

    public void replace(int position, int length, String text) {}

    public void save(IProgressMonitor progress, boolean force) throws JavaModelException {}

    public void setContents(char[] contents) {}

    public void setContents(String contents) {}
  }

  /** NULL implementing <code>IBuffer</code> */
  public static final IBuffer NULL = new NullBuffer();

  /**
   * Run the given runnable in the UI thread.
   *
   * @param runnable the runnable
   * @since 3.3
   */
  private static final void run(Runnable runnable) {
    //		Display currentDisplay = Display.getCurrent();
    //		if (currentDisplay != null)
    runnable.run();
    //		else
    //			Display.getDefault().syncExec(runnable);
  }

  /** Executes a document set content call in the UI thread. */
  protected class DocumentSetCommand implements Runnable {

    private String fContents;

    public void run() {
      if (!isClosed()) fDocument.set(fContents);
    }

    public void set(String contents) {
      fContents = contents;
      DocumentAdapter.run(this);
    }
  }

  /** Executes a document replace call in the UI thread. */
  protected class DocumentReplaceCommand implements Runnable {

    private int fOffset;
    private int fLength;
    private String fText;

    public void run() {
      try {
        if (!isClosed()) fDocument.replace(fOffset, fLength, fText);
      } catch (BadLocationException x) {
        // ignore
      }
    }

    public void replace(int offset, int length, String text) {
      fOffset = offset;
      fLength = length;
      fText = text;
      DocumentAdapter.run(this);
    }
  }

  /**
   * Executes a document replace call in the UI thread.
   *
   * @since 3.4
   */
  protected class ApplyTextEditCommand implements Runnable {

    private TextEdit fEdit;
    private UndoEdit fUndoEdit;

    public void run() {
      try {
        if (!isClosed()) {
          fUndoEdit =
              new RewriteSessionEditProcessor(
                      fDocument, fEdit, TextEdit.UPDATE_REGIONS | TextEdit.CREATE_UNDO)
                  .performEdits();
        }
      } catch (BadLocationException x) {
        // ignore
      }
    }

    public UndoEdit applyTextEdit(TextEdit edit) {
      fEdit = edit;
      fUndoEdit = null;
      DocumentAdapter.run(this);
      return fUndoEdit;
    }
  }

  private static final boolean DEBUG_LINE_DELIMITERS = true;

  private IOpenable fOwner;
  private IFile fFile;
  //	private ITextFileBuffer fTextFileBuffer;
  private IDocument fDocument;

  private boolean fIsClosed = true;

  private DocumentSetCommand fSetCmd = new DocumentSetCommand();
  private DocumentReplaceCommand fReplaceCmd = new DocumentReplaceCommand();
  private ApplyTextEditCommand fTextEditCmd = new ApplyTextEditCommand();

  private Set<String> fLegalLineDelimiters;

  private List<IBufferChangedListener> fBufferListeners = new ArrayList<IBufferChangedListener>(3);

  /** @since 3.2 */
  private IPath fPath;

  /** @since 3.3 */
  //	private LocationKind fLocationKind;

  /** @since 3.6 */
  //	private IFileStore fFileStore;

  /**
   * Constructs a new document adapter.
   *
   * @param owner the owner of this buffer
   * @param path the path of the file that backs the buffer
   * @since 3.2
   */
  public DocumentAdapter(IOpenable owner, IPath path) {
    Assert.isLegal(path != null);
    fOwner = owner;
    fPath = path;
    //		fLocationKind = LocationKind.NORMALIZE;

    initialize();
  }

  //	/**
  //	 * Constructs a new document adapter.
  //	 *
  //	 * @param owner the owner of this buffer
  //	 * @param fileStore the file store of the file that backs the buffer
  //	 * @param path the path of the file that backs the buffer
  //	 * @since 3.6
  //	 */
  //	public DocumentAdapter(IOpenable owner, IFileStore fileStore, IPath path) {
  //		Assert.isLegal(fileStore != null);
  //		Assert.isLegal(path != null);
  //		fOwner = owner;
  //		fFileStore = fileStore;
  //		fPath = path;
  //		fLocationKind = LocationKind.NORMALIZE;
  //
  //		initialize();
  //	}

  /**
   * Constructs a new document adapter.
   *
   * @param owner the owner of this buffer
   * @param file the <code>IFile</code> that backs the buffer
   */
  public DocumentAdapter(IOpenable owner, IFile file) {
    fOwner = owner;
    fFile = file;
    fPath = fFile.getFullPath();
    //		fLocationKind = LocationKind.IFILE;

    initialize();
  }

  /**
   * Constructs a new document adapter.
   *
   * @param owner the owner of this buffer
   * @param file the <code>IFile</code> that backs the buffer
   */
  public DocumentAdapter(IOpenable owner, IPath path, String content) {
    fOwner = owner;
    //		fFile = file;
    fPath = path;
    //		fLocationKind = LocationKind.IFILE;
    fDocument = new Document(content);
    initialize();
  }

  private void initialize() {
    //		ITextFileBufferManager manager = FileBuffers.getTextFileBufferManager();
    //		try {
    //			if (fFileStore != null) {
    //				manager.connectFileStore(fFileStore, new NullProgressMonitor());
    //				fTextFileBuffer = manager.getFileStoreTextFileBuffer(fFileStore);
    //			} else {
    //				manager.connect(fPath, fLocationKind, new NullProgressMonitor());
    //				fTextFileBuffer = manager.getTextFileBuffer(fPath, fLocationKind);
    //			}
    if (fDocument == null) {
      if (fFile != null) {
        try (InputStream inputStream = fFile.getContents()) {
          fDocument = new Document(IoUtil.readStream(inputStream));
        } catch (IOException | CoreException e) {
          JavaPlugin.log(e);
          fDocument = new Document("");
        }
      } else {
        fDocument = new Document("");
      }
    }
    //			fDocument = fTextFileBuffer.getDocument();
    //		} catch (CoreException x) {
    //			fDocument = manager.createEmptyDocument(fPath, fLocationKind);
    //			if (fDocument instanceof ISynchronizable)
    //				((ISynchronizable)fDocument).setLockObject(new Object());
    //		}
    fDocument.addDocumentListener(this);
    fIsClosed = false;
  }

  /**
   * Returns the adapted document.
   *
   * @return the adapted document
   */
  public IDocument getDocument() {
    return fDocument;
  }

  /*
   * @see IBuffer#addBufferChangedListener(IBufferChangedListener)
   */
  public void addBufferChangedListener(IBufferChangedListener listener) {
    Assert.isNotNull(listener);
    if (!fBufferListeners.contains(listener)) fBufferListeners.add(listener);
  }

  /*
   * @see IBuffer#removeBufferChangedListener(IBufferChangedListener)
   */
  public void removeBufferChangedListener(IBufferChangedListener listener) {
    Assert.isNotNull(listener);
    fBufferListeners.remove(listener);
  }

  /*
   * @see IBuffer#append(char[])
   */
  public void append(char[] text) {
    append(new String(text));
  }

  /*
   * @see IBuffer#append(String)
   */
  public void append(String text) {
    if (DEBUG_LINE_DELIMITERS) {
      validateLineDelimiters(text);
    }
    fReplaceCmd.replace(fDocument.getLength(), 0, text);
  }

  /*
   * @see IBuffer#close()
   */
  public void close() {

    if (isClosed()) return;

    IDocument d = fDocument;
    fDocument = new Document();
    fIsClosed = true;
    d.removeDocumentListener(this);
    //
    //		if (fTextFileBuffer != null) {
    //			ITextFileBufferManager manager = FileBuffers.getTextFileBufferManager();
    //			try {
    //				if (fFileStore != null)
    //					manager.disconnectFileStore(fFileStore, new NullProgressMonitor());
    //				else
    //					manager.disconnect(fPath, fLocationKind, new NullProgressMonitor());
    //			} catch (CoreException x) {
    //				// ignore
    //			}
    //			fTextFileBuffer = null;
    //		}

    fireBufferChanged(new BufferChangedEvent(this, 0, 0, null));
    fBufferListeners.clear();
  }

  /*
   * @see IBuffer#getChar(int)
   */
  public char getChar(int position) {
    try {
      return fDocument.getChar(position);
    } catch (BadLocationException x) {
      throw new ArrayIndexOutOfBoundsException();
    }
  }

  /*
   *  @see IBuffer#getCharacters()
   */
  public char[] getCharacters() {
    String content = getContents();
    return content == null ? null : content.toCharArray();
  }

  /*
   * @see IBuffer#getContents()
   */
  public String getContents() {
    return fDocument.get();
  }

  /*
   * @see IBuffer#getLength()
   */
  public int getLength() {
    return fDocument.getLength();
  }

  /*
   * @see IBuffer#getOwner()
   */
  public IOpenable getOwner() {
    return fOwner;
  }

  /*
   * @see IBuffer#getText(int, int)
   */
  public String getText(int offset, int length) {
    try {
      return fDocument.get(offset, length);
    } catch (BadLocationException x) {
      throw new ArrayIndexOutOfBoundsException();
    }
  }

  /*
   * @see IBuffer#getUnderlyingResource()
   */
  public IResource getUnderlyingResource() {
    return fFile;
  }

  /*
   * @see IBuffer#hasUnsavedChanges()
   */
  public boolean hasUnsavedChanges() {
    //		return fTextFileBuffer != null ? fTextFileBuffer.isDirty() : false;
    throw new UnsupportedOperationException();
  }

  /*
   * @see IBuffer#isClosed()
   */
  public boolean isClosed() {
    return fIsClosed;
  }

  /*
   * @see IBuffer#isReadOnly()
   */
  public boolean isReadOnly() {
    //		if (fTextFileBuffer != null)
    //			return !fTextFileBuffer.isCommitable();

    IResource resource = getUnderlyingResource();
    if (resource == null) return true;

    final ResourceAttributes attributes = resource.getResourceAttributes();
    return attributes == null ? false : attributes.isReadOnly();
  }

  /*
   * @see IBuffer#replace(int, int, char[])
   */
  public void replace(int position, int length, char[] text) {
    replace(position, length, new String(text));
  }

  /*
   * @see IBuffer#replace(int, int, String)
   */
  public void replace(int position, int length, String text) {
    if (DEBUG_LINE_DELIMITERS) {
      validateLineDelimiters(text);
    }
    fReplaceCmd.replace(position, length, text);
  }

  /*
   * @see IBuffer#save(IProgressMonitor, boolean)
   */
  public void save(IProgressMonitor progress, boolean force) throws JavaModelException {
    //		try {
    //			if (fTextFileBuffer != null)
    //				fTextFileBuffer.commit(progress, force);
    //		} catch (CoreException e) {
    //			throw new JavaModelException(e);
    //		}
    throw new UnsupportedOperationException();
  }

  /*
   * @see IBuffer#setContents(char[])
   */
  public void setContents(char[] contents) {
    setContents(new String(contents));
  }

  /*
   * @see IBuffer#setContents(String)
   */
  public void setContents(String contents) {
    int oldLength = fDocument.getLength();

    if (contents == null) {

      if (oldLength != 0) fSetCmd.set(""); // $NON-NLS-1$

    } else {

      // set only if different
      if (DEBUG_LINE_DELIMITERS) {
        validateLineDelimiters(contents);
      }

      if (!contents.equals(fDocument.get())) fSetCmd.set(contents);
    }
  }

  private void validateLineDelimiters(String contents) {

    if (fLegalLineDelimiters == null) {
      // collect all line delimiters in the document
      HashSet<String> existingDelimiters = new HashSet<String>();

      for (int i = fDocument.getNumberOfLines() - 1; i >= 0; i--) {
        try {
          String curr = fDocument.getLineDelimiter(i);
          if (curr != null) {
            existingDelimiters.add(curr);
          }
        } catch (BadLocationException e) {
          JavaPlugin.log(e);
        }
      }
      if (existingDelimiters.isEmpty()) {
        return; // first insertion of a line delimiter: no test
      }
      fLegalLineDelimiters = existingDelimiters;
    }

    DefaultLineTracker tracker = new DefaultLineTracker();
    tracker.set(contents);

    int lines = tracker.getNumberOfLines();
    if (lines <= 1) return;

    for (int i = 0; i < lines; i++) {
      try {
        String curr = tracker.getLineDelimiter(i);
        if (curr != null && !fLegalLineDelimiters.contains(curr)) {
          StringBuffer buf =
              new StringBuffer(
                  "WARNING: javaeditor.DocumentAdapter added new line delimiter to code: "); // $NON-NLS-1$
          for (int k = 0; k < curr.length(); k++) {
            if (k > 0) buf.append(' ');
            buf.append((int) curr.charAt(k));
          }
          IStatus status =
              new Status(
                  IStatus.WARNING,
                  JavaPlugin.ID_PLUGIN,
                  IStatus.OK,
                  buf.toString(),
                  new Throwable());
          JavaPlugin.log(status);
        }
      } catch (BadLocationException e) {
        JavaPlugin.log(e);
      }
    }
  }

  /*
   * @see IDocumentListener#documentAboutToBeChanged(DocumentEvent)
   */
  public void documentAboutToBeChanged(DocumentEvent event) {
    // there is nothing to do here
  }

  /*
   * @see IDocumentListener#documentChanged(DocumentEvent)
   */
  public void documentChanged(DocumentEvent event) {
    fireBufferChanged(
        new BufferChangedEvent(this, event.getOffset(), event.getLength(), event.getText()));
  }

  private void fireBufferChanged(BufferChangedEvent event) {
    if (fBufferListeners != null && fBufferListeners.size() > 0) {
      Iterator<IBufferChangedListener> e =
          new ArrayList<IBufferChangedListener>(fBufferListeners).iterator();
      while (e.hasNext()) e.next().bufferChanged(event);
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p><strong>Note:</strong> This implementation applies the edits in a rewrite session.
   *
   * @since 3.4
   */
  public UndoEdit applyTextEdit(TextEdit edit, IProgressMonitor monitor) throws JavaModelException {
    return fTextEditCmd.applyTextEdit(edit);
  }
}
