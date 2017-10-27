/**
 * ***************************************************************************** Copyright (c)
 * 2012-2015 Red Hat, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Red Hat, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.ide.ext.java.jdt.text.edits;

import org.eclipse.che.ide.api.editor.text.BadLocationException;
import org.eclipse.che.ide.ext.java.jdt.text.Document;

/** Text edit to replace a range in a document with a different string. */
public final class ReplaceEdit extends TextEdit {

  private String fText;

  /**
   * Constructs a new replace edit.
   *
   * @param offset the offset of the range to replace
   * @param length the length of the range to replace
   * @param text the new text
   */
  public ReplaceEdit(int offset, int length, String text) {
    super(offset, length);
    // Assert.isNotNull(text);
    fText = text;
  }

  /*
   * Copy constructor
   * @param other the edit to copy from
   */
  private ReplaceEdit(ReplaceEdit other) {
    super(other);
    fText = other.fText;
  }

  /**
   * Returns the new text replacing the text denoted by the edit.
   *
   * @return the edit's text.
   */
  public String getText() {
    return fText;
  }

  /* @see TextEdit#doCopy */
  protected TextEdit doCopy() {
    return new ReplaceEdit(this);
  }

  /* @see TextEdit#accept0 */
  protected void accept0(TextEditVisitor visitor) {
    boolean visitChildren = visitor.visit(this);
    if (visitChildren) {
      acceptChildren(visitor);
    }
  }

  /* @see TextEdit#performDocumentUpdating */
  int performDocumentUpdating(Document document) throws BadLocationException {
    document.replace(getOffset(), getLength(), fText);
    fDelta = fText.length() - getLength();
    return fDelta;
  }

  /* @see TextEdit#deleteChildren */
  boolean deleteChildren() {
    return true;
  }

  /*
   * @see org.eclipse.text.edits.TextEdit#internalToString(java.lang.StringBuffer, int)
   * @since 3.3
   */
  void internalToString(StringBuffer buffer, int indent) {
    super.internalToString(buffer, indent);
    buffer.append(" <<").append(fText); // $NON-NLS-1$
  }
}
