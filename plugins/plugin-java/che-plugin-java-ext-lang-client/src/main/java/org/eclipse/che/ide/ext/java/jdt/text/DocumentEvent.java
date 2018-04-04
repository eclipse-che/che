/**
 * ***************************************************************************** Copyright (c)
 * 2012-2015 Red Hat, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Red Hat, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.ide.ext.java.jdt.text;

import org.eclipse.che.ide.runtime.Assert;

/**
 * Specification of changes applied to documents. All changes are represented as replace commands,
 * i.e. specifying a document range whose text gets replaced with different text. In addition to
 * this information, the event also contains the changed document.
 */
public class DocumentEvent {

  /** The changed document */
  public Document fDocument;

  /** The document offset */
  public int fOffset;

  /** Length of the replaced document text */
  public int fLength;

  /** Text inserted into the document */
  public String fText = ""; // $NON-NLS-1$

  /**
   * The modification stamp of the document when firing this event.
   *
   * @since 3.1 and public since 3.3
   */
  public long fModificationStamp;

  /**
   * Creates a new document event.
   *
   * @param doc the changed document
   * @param offset the offset of the replaced text
   * @param length the length of the replaced text
   * @param text the substitution text
   */
  public DocumentEvent(Document doc, int offset, int length, String text) {

    Assert.isNotNull(doc);
    Assert.isTrue(offset >= 0);
    Assert.isTrue(length >= 0);
    //
    // if (ASSERT_TEXT_NOT_NULL)
    // Assert.isNotNull(text);

    fDocument = doc;
    fOffset = offset;
    fLength = length;
    fText = text;
    //
    // if (fDocument instanceof IDocumentExtension4)
    fModificationStamp = fDocument.getModificationStamp();
    // else
    // fModificationStamp= IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP;
  }

  /** Creates a new, not initialized document event. */
  public DocumentEvent() {}

  /**
   * Returns the changed document.
   *
   * @return the changed document
   */
  public Document getDocument() {
    return fDocument;
  }

  /**
   * Returns the offset of the change.
   *
   * @return the offset of the change
   */
  public int getOffset() {
    return fOffset;
  }

  /**
   * Returns the length of the replaced text.
   *
   * @return the length of the replaced text
   */
  public int getLength() {
    return fLength;
  }

  /**
   * Returns the text that has been inserted.
   *
   * @return the text that has been inserted
   */
  public String getText() {
    return fText;
  }

  /**
   * Returns the document's modification stamp at the time when this event was sent.
   *
   * @return the modification stamp or {@link IDocumentExtension4#UNKNOWN_MODIFICATION_STAMP}.
   * @see IDocumentExtension4#getModificationStamp()
   * @since 3.1
   */
  public long getModificationStamp() {
    return fModificationStamp;
  }

  /*
   * @see java.lang.Object#toString()
   * @since 3.4
   */
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("offset: "); // $NON-NLS-1$
    buffer.append(fOffset);
    buffer.append(", length: "); // $NON-NLS-1$
    buffer.append(fLength);
    buffer.append(", timestamp: "); // $NON-NLS-1$
    buffer.append(fModificationStamp);
    buffer.append("\ntext:>"); // $NON-NLS-1$
    buffer.append(fText);
    buffer.append("<\n"); // $NON-NLS-1$
    return buffer.toString();
  }
}
