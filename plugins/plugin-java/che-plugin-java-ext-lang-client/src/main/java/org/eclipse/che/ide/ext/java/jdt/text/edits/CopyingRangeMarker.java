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

/**
 * A <code>CopyingRangeMarker</code> can be used to track positions when executing text edits.
 * Additionally a copying range marker stores a local copy of the text it captures when it gets
 * executed.
 */
public final class CopyingRangeMarker extends TextEdit {

  private String fText;

  /**
   * Creates a new <tt>CopyRangeMarker</tt> for the given offset and length.
   *
   * @param offset the marker's offset
   * @param length the marker's length
   */
  public CopyingRangeMarker(int offset, int length) {
    super(offset, length);
  }

  /* Copy constructor */
  private CopyingRangeMarker(CopyingRangeMarker other) {
    super(other);
    fText = other.fText;
  }

  /* @see TextEdit#doCopy */
  protected TextEdit doCopy() {
    return new CopyingRangeMarker(this);
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
    fText = document.get(getOffset(), getLength());
    fDelta = 0;
    return fDelta;
  }

  /* @see TextEdit#deleteChildren */
  boolean deleteChildren() {
    return false;
  }
}
