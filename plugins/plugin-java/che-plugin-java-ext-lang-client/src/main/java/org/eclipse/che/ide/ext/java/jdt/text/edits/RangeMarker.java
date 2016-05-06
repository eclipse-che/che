/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.java.jdt.text.edits;

import org.eclipse.che.ide.api.editor.text.BadLocationException;
import org.eclipse.che.ide.ext.java.jdt.text.Document;

/** A range marker can be used to track positions when executing text edits. */
public final class RangeMarker extends TextEdit {

    /**
     * Creates a new range marker for the given offset and length.
     *
     * @param offset
     *         the marker's offset
     * @param length
     *         the marker's length
     */
    public RangeMarker(int offset, int length) {
        super(offset, length);
    }

    /* Copy constructor */
    private RangeMarker(RangeMarker other) {
        super(other);
    }

    /* @see TextEdit#copy */
    protected TextEdit doCopy() {
        return new RangeMarker(this);
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
        fDelta = 0;
        return fDelta;
    }

    /* @see TextEdit#deleteChildren */
    boolean deleteChildren() {
        return false;
    }
}
