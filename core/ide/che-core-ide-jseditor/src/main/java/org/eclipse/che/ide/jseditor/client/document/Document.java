/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.jseditor.client.document;

import com.google.web.bindery.event.shared.HandlerRegistration;

import org.eclipse.che.ide.api.project.tree.VirtualFile;
import org.eclipse.che.ide.jseditor.client.events.CursorActivityHandler;
import org.eclipse.che.ide.jseditor.client.text.LinearRange;
import org.eclipse.che.ide.jseditor.client.text.TextPosition;
import org.eclipse.che.ide.jseditor.client.text.TextRange;

/**
 * An abstraction over the editor representation of the document.
 *
 * @author "Mickaël Leduque"
 */
public interface Document extends ReadOnlyDocument {

    /**
     * Changes the cursor position.
     *
     * @param position
     *         the new position
     */
    void setCursorPosition(TextPosition position);

    /**
     * Change the selected range.
     * @param range the new selected range
     */
    void setSelectedRange(TextRange range);

    /**
     * Change the selected range and optionally move the viewport to show the new selection.
     * @param range the new selected range
     * @param show true iff the viewport is moved to show the selection
     */
    void setSelectedRange(TextRange range, boolean show);

    /**
     * Change the selected range.
     * @param range the new selected range
     */
    void setSelectedRange(LinearRange range);

    /**
     * Change the selected range and optionally move the viewport to show the new selection.
     * @param range the new selected range
     * @param show true iff the viewport is moved to show the selection
     */
    void setSelectedRange(LinearRange range, boolean show);

    /**
     * Returns the document handle.
     * @return the document handle
     */
    DocumentHandle getDocumentHandle();

    /**
     * Adds a cursor handler.
     *
     * @param handler
     *         the added handler
     * @return a handle to remove the handler
     */
    HandlerRegistration addCursorHandler(CursorActivityHandler handler);

    /**
     * Replaces the text range with the given replacement contents.
     * @param offset start of the range
     * @param length en of the range
     * @param text the replacement text
     */
    void replace(int offset, int length, String text);

    void setFile(VirtualFile file);

    VirtualFile getFile();

    /**
     * Returns a {@link ReadOnlyDocument} that refers to the same document.
     * @return a read-only document
     */
    ReadOnlyDocument getReadOnlyDocument();
}
