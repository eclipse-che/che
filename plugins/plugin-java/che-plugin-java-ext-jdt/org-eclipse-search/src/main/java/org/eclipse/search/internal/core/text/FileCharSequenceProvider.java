/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2010 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.search.internal.core.text;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.content.IContentDescription;

/** */
public class FileCharSequenceProvider {

  private static int NUMBER_OF_BUFFERS = 3;
  public static int BUFFER_SIZE = 2 << 18; // public for testing

  private FileCharSequence fReused = null;

  public CharSequence newCharSequence(IFile file) throws CoreException, IOException {
    if (fReused == null) {
      return new FileCharSequence(file);
    }
    FileCharSequence curr = fReused;
    fReused = null;
    curr.reset(file);
    return curr;
  }

  public void releaseCharSequence(CharSequence seq) throws IOException {
    if (seq instanceof FileCharSequence) {
      FileCharSequence curr = (FileCharSequence) seq;
      try {
        curr.close();
      } finally {
        if (fReused == null) {
          fReused = curr;
        }
      }
    }
  }

  public static class FileCharSequenceException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /* package */ FileCharSequenceException(IOException e) {
      super(e);
    }

    /* package */ FileCharSequenceException(CoreException e) {
      super(e);
    }

    public void throwWrappedException() throws CoreException, IOException {
      Throwable wrapped = getCause();
      if (wrapped instanceof CoreException) {
        throw (CoreException) wrapped;
      } else if (wrapped instanceof IOException) {
        throw (IOException) wrapped;
      }
      // not possible
    }
  }

  private static final class CharSubSequence implements CharSequence {

    private final int fSequenceOffset;
    private final int fSequenceLength;
    private final FileCharSequence fParent;

    public CharSubSequence(FileCharSequence parent, int offset, int length) {
      fParent = parent;
      fSequenceOffset = offset;
      fSequenceLength = length;
    }

    /* (non-Javadoc)
     * @see java.lang.CharSequence#length()
     */
    public int length() {
      return fSequenceLength;
    }

    /* (non-Javadoc)
     * @see java.lang.CharSequence#charAt(int)
     */
    public char charAt(int index) {
      if (index < 0) {
        throw new IndexOutOfBoundsException("index must be larger than 0"); // $NON-NLS-1$
      }
      if (index >= fSequenceLength) {
        throw new IndexOutOfBoundsException("index must be smaller than length"); // $NON-NLS-1$
      }
      return fParent.charAt(fSequenceOffset + index);
    }

    /* (non-Javadoc)
     * @see java.lang.CharSequence#subSequence(int, int)
     */
    public CharSequence subSequence(int start, int end) {
      if (end < start) {
        throw new IndexOutOfBoundsException("end cannot be smaller than start"); // $NON-NLS-1$
      }
      if (start < 0) {
        throw new IndexOutOfBoundsException("start must be larger than 0"); // $NON-NLS-1$
      }
      if (end > fSequenceLength) {
        throw new IndexOutOfBoundsException(
            "end must be smaller or equal than length"); // $NON-NLS-1$
      }
      return fParent.subSequence(fSequenceOffset + start, fSequenceOffset + end);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
      try {
        return fParent.getSubstring(fSequenceOffset, fSequenceLength);
      } catch (IOException e) {
        throw new FileCharSequenceException(e);
      } catch (CoreException e) {
        throw new FileCharSequenceException(e);
      }
    }
  }

  private static final class Buffer {
    private final char[] fBuf;
    private int fOffset;
    private int fLength;

    private Buffer fNext;
    private Buffer fPrevious;

    public Buffer() {
      fBuf = new char[BUFFER_SIZE];
      reset();
      fNext = this;
      fPrevious = this;
    }

    public boolean contains(int pos) {
      int offset = fOffset;
      return offset <= pos && pos < offset + fLength;
    }

    /**
     * Fills the buffer by reading from the given reader.
     *
     * @param reader the reader to read from
     * @param pos the offset of the reader in the file
     * @return returns true if the end of the file has been reached
     * @throws IOException if reading from the buffer fails
     */
    public boolean fill(Reader reader, int pos) throws IOException {
      int res = reader.read(fBuf);
      if (res == -1) {
        fOffset = pos;
        fLength = 0;
        return true;
      }

      int charsRead = res;
      while (charsRead < BUFFER_SIZE) {
        res = reader.read(fBuf, charsRead, BUFFER_SIZE - charsRead);
        if (res == -1) {
          fOffset = pos;
          fLength = charsRead;
          return true;
        }
        charsRead += res;
      }
      fOffset = pos;
      fLength = BUFFER_SIZE;
      return false;
    }

    public char get(int pos) {
      return fBuf[pos - fOffset];
    }

    public StringBuffer append(StringBuffer buf, int start, int length) {
      return buf.append(fBuf, start - fOffset, length);
    }

    public StringBuffer appendAll(StringBuffer buf) {
      return buf.append(fBuf, 0, fLength);
    }

    public int getEndOffset() {
      return fOffset + fLength;
    }

    public void removeFromChain() {
      fPrevious.fNext = fNext;
      fNext.fPrevious = fPrevious;

      fNext = this;
      fPrevious = this;
    }

    public void insertBefore(Buffer other) {
      fNext = other;
      fPrevious = other.fPrevious;
      fPrevious.fNext = this;
      other.fPrevious = this;
    }

    public Buffer getNext() {
      return fNext;
    }

    public Buffer getPrevious() {
      return fPrevious;
    }

    public void reset() {
      fOffset = -1;
      fLength = 0;
    }
  }

  private final class FileCharSequence implements CharSequence {

    private static final String CHARSET_UTF_8 = "UTF-8"; // $NON-NLS-1$

    private Reader fReader;
    private int fReaderPos;

    private Integer fLength;

    private Buffer fMostCurrentBuffer; // access to the buffer chain
    private int fNumberOfBuffers;

    private IFile fFile;

    public FileCharSequence(IFile file) throws CoreException, IOException {
      fNumberOfBuffers = 0;
      reset(file);
    }

    public void reset(IFile file) throws CoreException, IOException {
      fFile = file;
      fLength = null; // only calculated on demand

      Buffer curr = fMostCurrentBuffer;
      if (curr != null) {
        do {
          curr.reset();
          curr = curr.getNext();
        } while (curr != fMostCurrentBuffer);
      }
      initializeReader();
    }

    private void initializeReader() throws CoreException, IOException {
      if (fReader != null) {
        fReader.close();
      }
      String charset = fFile.getCharset();
      fReader = new InputStreamReader(getInputStream(charset), charset);
      fReaderPos = 0;
    }

    private InputStream getInputStream(String charset) throws CoreException, IOException {
      boolean ok = false;
      InputStream contents = fFile.getContents();
      try {
        if (CHARSET_UTF_8.equals(charset)) {
          /*
           * This is a workaround for a corresponding bug in Java readers and writer,
           * see http://developer.java.sun.com/developer/bugParade/bugs/4508058.html
           * we remove the BOM before passing the stream to the reader
           */
          IContentDescription description = fFile.getContentDescription();
          if ((description != null)
              && (description.getProperty(IContentDescription.BYTE_ORDER_MARK) != null)) {
            int bomLength = IContentDescription.BOM_UTF_8.length;
            byte[] bomStore = new byte[bomLength];
            int bytesRead = 0;
            do {
              int bytes = contents.read(bomStore, bytesRead, bomLength - bytesRead);
              if (bytes == -1) throw new IOException();
              bytesRead += bytes;
            } while (bytesRead < bomLength);

            if (!Arrays.equals(bomStore, IContentDescription.BOM_UTF_8)) {
              // discard file reader, we were wrong, no BOM -> new stream
              contents.close();
              contents = fFile.getContents();
            }
          }
        }
        ok = true;
      } finally {
        if (!ok && contents != null)
          try {
            contents.close();
          } catch (IOException ex) {
            // ignore
          }
      }
      return contents;
    }

    private void clearReader() throws IOException {
      if (fReader != null) {
        fReader.close();
      }
      fReader = null;
      fReaderPos = Integer.MAX_VALUE;
    }

    /* (non-Javadoc)
     * @see java.lang.CharSequence#length()
     */
    public int length() {
      if (fLength == null) {
        try {
          getBuffer(Integer.MAX_VALUE);
        } catch (IOException e) {
          throw new FileCharSequenceException(e);
        } catch (CoreException e) {
          throw new FileCharSequenceException(e);
        }
      }
      return fLength.intValue();
    }

    private Buffer getBuffer(int pos) throws IOException, CoreException {
      Buffer curr = fMostCurrentBuffer;
      if (curr != null) {
        do {
          if (curr.contains(pos)) {
            return curr;
          }
          curr = curr.getNext();
        } while (curr != fMostCurrentBuffer);
      }

      Buffer buf = findBufferToUse();
      fillBuffer(buf, pos);
      if (buf.contains(pos)) {
        return buf;
      }
      return null;
    }

    private Buffer findBufferToUse() {
      if (fNumberOfBuffers < NUMBER_OF_BUFFERS) {
        fNumberOfBuffers++;
        Buffer newBuffer = new Buffer();
        if (fMostCurrentBuffer == null) {
          fMostCurrentBuffer = newBuffer;
          return newBuffer;
        }
        newBuffer.insertBefore(fMostCurrentBuffer); // insert before first
        return newBuffer;
      }
      return fMostCurrentBuffer.getPrevious();
    }

    private boolean fillBuffer(Buffer buffer, int pos) throws CoreException, IOException {
      if (fReaderPos > pos) {
        initializeReader();
      }

      do {
        boolean endReached = buffer.fill(fReader, fReaderPos);
        fReaderPos = buffer.getEndOffset();
        if (endReached) {
          fLength = new Integer(fReaderPos); // at least we know the size of the file now
          fReaderPos = Integer.MAX_VALUE; // will have to reset next time
          return true;
        }
      } while (fReaderPos <= pos);

      return true;
    }

    /* (non-Javadoc)
     * @see java.lang.CharSequence#charAt(int)
     */
    public char charAt(final int index) {
      final Buffer current = fMostCurrentBuffer;
      if (current != null && current.contains(index)) {
        return current.get(index);
      }

      if (index < 0) {
        throw new IndexOutOfBoundsException("index must be larger than 0"); // $NON-NLS-1$
      }
      if (fLength != null && index >= fLength.intValue()) {
        throw new IndexOutOfBoundsException("index must be smaller than length"); // $NON-NLS-1$
      }

      try {
        final Buffer buffer = getBuffer(index);
        if (buffer == null) {
          throw new IndexOutOfBoundsException("index must be smaller than length"); // $NON-NLS-1$
        }
        if (buffer != fMostCurrentBuffer) {
          // move to first
          if (buffer.getNext() != fMostCurrentBuffer) { // already before the current?
            buffer.removeFromChain();
            buffer.insertBefore(fMostCurrentBuffer);
          }
          fMostCurrentBuffer = buffer;
        }
        return buffer.get(index);
      } catch (IOException e) {
        throw new FileCharSequenceException(e);
      } catch (CoreException e) {
        throw new FileCharSequenceException(e);
      }
    }

    public String getSubstring(int start, int length) throws IOException, CoreException {
      int pos = start;
      int endPos = start + length;

      if (fLength != null && endPos > fLength.intValue()) {
        throw new IndexOutOfBoundsException("end must be smaller than length"); // $NON-NLS-1$
      }

      StringBuffer res = new StringBuffer(length);

      Buffer buffer = getBuffer(pos);
      while (pos < endPos && buffer != null) {
        int bufEnd = buffer.getEndOffset();
        if (bufEnd >= endPos) {
          return buffer.append(res, pos, endPos - pos).toString();
        }
        buffer.append(res, pos, bufEnd - pos);
        pos = bufEnd;
        buffer = getBuffer(pos);
      }
      return res.toString();
    }

    /* (non-Javadoc)
     * @see java.lang.CharSequence#subSequence(int, int)
     */
    public CharSequence subSequence(int start, int end) {
      if (end < start) {
        throw new IndexOutOfBoundsException("end cannot be smaller than start"); // $NON-NLS-1$
      }
      if (start < 0) {
        throw new IndexOutOfBoundsException("start must be larger than 0"); // $NON-NLS-1$
      }
      if (fLength != null && end > fLength.intValue()) {
        throw new IndexOutOfBoundsException("end must be smaller than length"); // $NON-NLS-1$
      }
      return new CharSubSequence(this, start, end - start);
    }

    public void close() throws IOException {
      clearReader();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
      int len = fLength != null ? fLength.intValue() : 4000;
      StringBuffer res = new StringBuffer(len);
      try {
        Buffer buffer = getBuffer(0);
        while (buffer != null) {
          buffer.appendAll(res);
          buffer = getBuffer(res.length());
        }
        return res.toString();
      } catch (IOException e) {
        throw new FileCharSequenceException(e);
      } catch (CoreException e) {
        throw new FileCharSequenceException(e);
      }
    }
  }
}
