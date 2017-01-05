/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.api.editor.document;

import org.eclipse.che.ide.api.editor.text.LinearRange;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.editor.text.TextRange;

public interface ReadOnlyDocument {

    /**
     * Returns a line/character position for the given offset position.
     *
     * @param index
     *         the position from the start in the document
     * @return the line/character position
     */
    TextPosition getPositionFromIndex(int index);

    /**
     * Get linear position in the editor from a line/character position.
     *
     * @param position
     *         the line/character position
     * @return the offset from the document start
     */
    int getIndexFromPosition(TextPosition position);

    /**
     * Returns the cursor position in the editor.
     *
     * @return the cursor position
     */
    TextPosition getCursorPosition();

    /**
     * Returns the cursor position as an offset from the start.
     * @return the cursor position
     */
    int getCursorOffset();

    /**
     * Returns the selection range as a {@link TextRange} (i.e. two line, char objects: start en end).
     * @return the selection range
     */
    TextRange getSelectedTextRange();

    /**
     * Returns the selection range as a {@link LinearRange} (ie.e a start offset and a length).
     * @return the selection range
     */
    LinearRange getSelectedLinearRange();

    /**
     * Returns the number of lines in the document.
     *
     * @return the number of lines
     */
    int getLineCount();

    /**
     * Returns the contents of the editor.
     *
     * @return the contents
     */
    String getContents();

    /**
     * Returns the text content in the given range.<br>
     * Lines are separated by \n
     * @param offset the start of the range
     * @param length the length of the range
     * @return the range content
     */
    String getContentRange(int offset, int length);

    /**
     * Returns the text content in the given range.<br>
     * Lines are separated by \n
     * @param range the range
     * @return the range content
     */
    String getContentRange(TextRange range);

    /**
     * Returns the line content (without delimiter).
     * @param line the line index
     * @return the content of the line
     */
    String getLineContent(int line);

    /**
     * Returns the {@link TextRange} that defines the line.
     * @param line line index
     * @return the text range
     */
    TextRange getTextRangeForLine(int line);

    /**
     * Returns the {@link LinearRange} (offset, length) that defines the line.
     * @param line line index
     * @return the offset range
     */
    LinearRange getLinearRangeForLine(int line);

    /**
     * Returns the document text size.
     * @return the document size
     */
    int getContentsCharCount();


}
