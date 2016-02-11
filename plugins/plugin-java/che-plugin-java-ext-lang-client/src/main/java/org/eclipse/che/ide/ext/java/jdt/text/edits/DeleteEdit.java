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

import org.eclipse.che.ide.api.text.BadLocationException;
import org.eclipse.che.ide.ext.java.jdt.text.Document;

/**
 * Text edit to delete a range in a document.
 * <p/>
 * A delete edit is equivalent to <code>ReplaceEdit(
 * offset, length, "")</code>.
 */
public final class DeleteEdit extends TextEdit {

    /**
     * Constructs a new delete edit.
     *
     * @param offset
     *         the offset of the range to replace
     * @param length
     *         the length of the range to replace
     */
    public DeleteEdit(int offset, int length) {
        super(offset, length);
    }

    /* Copy constructor */
    private DeleteEdit(DeleteEdit other) {
        super(other);
    }

    /* @see TextEdit#doCopy */
    protected TextEdit doCopy() {
        return new DeleteEdit(this);
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
        document.replace(getOffset(), getLength(), ""); //$NON-NLS-1$
        fDelta = -getLength();
        return fDelta;
    }

    /* @see TextEdit#deleteChildren */
    boolean deleteChildren() {
        return true;
    }
}
