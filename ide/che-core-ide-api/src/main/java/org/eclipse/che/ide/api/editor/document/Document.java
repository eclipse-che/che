/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.editor.document;

import com.google.web.bindery.event.shared.HandlerRegistration;
import org.eclipse.che.ide.api.editor.events.CursorActivityHandler;
import org.eclipse.che.ide.api.editor.text.LinearRange;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.editor.text.TextRange;
import org.eclipse.che.ide.api.resources.VirtualFile;

/**
 * An abstraction over the editor representation of the document.
 *
 * @author "Mickaël Leduque"
 */
public interface Document extends ReadOnlyDocument {

  /**
   * Changes the cursor position.
   *
   * @param position the new position
   */
  void setCursorPosition(TextPosition position);

  /**
   * Change the selected range.
   *
   * @param range the new selected range
   */
  void setSelectedRange(TextRange range);

  /**
   * Returns the line index at the given character offset. The valid offsets are 0 to char count
   * inclusive. The line index for char count is <code>line count - 1</code>. Returns <code>-1
   * </code> if the offset is out of range.
   *
   * @param offset a character offset.
   * @return line index or <code>-1</code> if out of range.
   */
  int getLineAtOffset(int offset);

  /**
   * Returns the start character offset for the given line.
   *
   * <p>The valid indices are 0 to line count exclusive. Returns <code>-1</code> if the index is out
   * of range.
   *
   * @param lineIndex index of the line.
   * @return the line start offset or <code>-1</code> if out of range.
   */
  int getLineStart(int lineIndex);

  /**
   * Change the selected range and optionally move the viewport to show the new selection.
   *
   * @param range the new selected range
   * @param show true iff the viewport is moved to show the selection
   */
  void setSelectedRange(TextRange range, boolean show);

  /**
   * Change the selected range.
   *
   * @param range the new selected range
   */
  void setSelectedRange(LinearRange range);

  /**
   * Change the selected range and optionally move the viewport to show the new selection.
   *
   * @param range the new selected range
   * @param show true iff the viewport is moved to show the selection
   */
  void setSelectedRange(LinearRange range, boolean show);

  /**
   * Returns the document handle.
   *
   * @return the document handle
   */
  DocumentHandle getDocumentHandle();

  /**
   * Adds a cursor handler.
   *
   * @param handler the added handler
   * @return a handle to remove the handler
   */
  HandlerRegistration addCursorHandler(CursorActivityHandler handler);

  /**
   * Replaces the text range with the given replacement contents.
   *
   * @param offset start of the range
   * @param length en of the range
   * @param text the replacement text
   */
  void replace(int offset, int length, String text);

  /**
   * Replaces the text range with the given replacement contents.
   *
   * @param startLine start line of the range
   * @param startChar start char of the range
   * @param endLine end line of the range
   * @param endChar end char of the range
   * @param text the replacement text
   */
  void replace(int startLine, int startChar, int endLine, int endChar, String text);

  void setFile(VirtualFile file);

  VirtualFile getFile();

  /**
   * Returns a {@link ReadOnlyDocument} that refers to the same document.
   *
   * @return a read-only document
   */
  ReadOnlyDocument getReadOnlyDocument();
}
