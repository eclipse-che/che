/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2009 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation Tom Eicher (Avaloq Evolution
 * AG) - block selection mode
 * *****************************************************************************
 */
package org.eclipse.che.jface.text;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

/**
 * Standard implementation of {@link ITextSelection}.
 *
 * <p>Takes advantage of the weak contract of correctness of its interface. If generated from a
 * selection provider, it only remembers its offset and length and computes the remaining
 * information on request.
 */
public class TextSelection implements ITextSelection {

  /**
   * Debug option for asserting valid offset and length.
   *
   * @since 3.5
   */
  private static final boolean ASSERT_INVLID_SELECTION_NULL = false;
  //
  //	"true".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.jface.text/assert/TextSelection/validConstructorArguments"));
  // $NON-NLS-1$ //$NON-NLS-2$

  /** Internal empty text selection */
  private static final ITextSelection NULL = new TextSelection();

  /**
   * Returns the shared instance of the empty text selection.
   *
   * @return the shared instance of an empty text selection
   */
  public static ITextSelection emptySelection() {
    return NULL;
  }

  /** Document which delivers the data of the selection, possibly <code>null</code>. */
  private final IDocument fDocument;
  /** Offset of the selection */
  private int fOffset;
  /** Length of the selection */
  private int fLength;

  /** Creates an empty text selection. */
  private TextSelection() {
    fOffset = -1;
    fLength = -1;
    fDocument = null;
  }

  /**
   * Creates a text selection for the given range. This selection object describes generically a
   * text range and is intended to be an argument for the <code>setSelection</code> method of
   * selection providers.
   *
   * @param offset the offset of the range, must not be negative
   * @param length the length of the range, must not be negative
   */
  public TextSelection(int offset, int length) {
    this(null, offset, length);
  }

  /**
   * Creates a text selection for the given range of the given document. This selection object is
   * created by selection providers in responds <code>getSelection</code>.
   *
   * @param document the document whose text range is selected in a viewer
   * @param offset the offset of the selected range, must not be negative
   * @param length the length of the selected range, must not be negative
   */
  public TextSelection(IDocument document, int offset, int length) {
    if (ASSERT_INVLID_SELECTION_NULL) {
      org.eclipse.jface.text.Assert.isLegal(offset >= 0);
      org.eclipse.jface.text.Assert.isLegal(length >= 0);
    }
    fDocument = document;
    fOffset = offset;
    fLength = length;
  }

  /**
   * Tells whether this text selection is the empty selection.
   *
   * <p>A selection of length 0 is not an empty text selection as it describes, e.g., the cursor
   * position in a viewer.
   *
   * @return <code>true</code> if this selection is empty
   * @see #emptySelection()
   */
  public boolean isEmpty() {
    return this == NULL || /* backwards compatibility: */ fOffset < 0 || fLength < 0;
  }

  /*
   * @see org.eclipse.jface.text.ITextSelection#getOffset()
   */
  public int getOffset() {
    return fOffset;
  }

  /*
   * @see org.eclipse.jface.text.ITextSelection#getLength()
   */
  public int getLength() {
    return fLength;
  }

  /*
   * @see org.eclipse.jface.text.ITextSelection#getStartLine()
   */
  public int getStartLine() {

    try {
      if (fDocument != null) return fDocument.getLineOfOffset(fOffset);
    } catch (BadLocationException x) {
    }

    return -1;
  }

  /*
   * @see org.eclipse.jface.text.ITextSelection#getEndLine()
   */
  public int getEndLine() {
    try {
      if (fDocument != null) {
        int endOffset = fOffset + fLength;
        if (fLength != 0) endOffset--;
        return fDocument.getLineOfOffset(endOffset);
      }
    } catch (BadLocationException x) {
    }

    return -1;
  }

  /*
   * @see org.eclipse.jface.text.ITextSelection#getText()
   */
  public String getText() {
    try {
      if (fDocument != null) return fDocument.get(fOffset, fLength);
    } catch (BadLocationException x) {
    }

    return null;
  }

  /*
   * @see java.lang.Object#equals(Object)
   */
  public boolean equals(Object obj) {
    if (obj == this) return true;

    if (obj == null || getClass() != obj.getClass()) return false;

    TextSelection s = (TextSelection) obj;
    boolean sameRange = (s.fOffset == fOffset && s.fLength == fLength);
    if (sameRange) {

      if (s.fDocument == null && fDocument == null) return true;
      if (s.fDocument == null || fDocument == null) return false;

      try {
        String sContent = s.fDocument.get(fOffset, fLength);
        String content = fDocument.get(fOffset, fLength);
        return sContent.equals(content);
      } catch (BadLocationException x) {
      }
    }

    return false;
  }

  /*
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    int low = fDocument != null ? fDocument.hashCode() : 0;
    return (fOffset << 24) | (fLength << 16) | low;
  }

  /**
   * Returns the document underlying the receiver, possibly <code>null</code>.
   *
   * @return the document underlying the receiver, possibly <code>null</code>
   * @since 3.5
   */
  protected IDocument getDocument() {
    return fDocument;
  }
}
