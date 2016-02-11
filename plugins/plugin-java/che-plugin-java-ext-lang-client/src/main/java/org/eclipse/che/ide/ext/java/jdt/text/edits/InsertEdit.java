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
 * Text edit to insert a text at a given position in a document.
 * <p/>
 * An insert edit is equivalent to <code>ReplaceEdit(offset, 0, text)
 * </code>
 */
public final class InsertEdit extends TextEdit {

    private String fText;

    /**
     * Constructs a new insert edit.
     *
     * @param offset
     *         the insertion offset
     * @param text
     *         the text to insert
     */
    public InsertEdit(int offset, String text) {
        super(offset, 0);
        fText = text;
    }

    /* Copy constructor */
    private InsertEdit(InsertEdit other) {
        super(other);
        fText = other.fText;
    }

    /**
     * Returns the text to be inserted.
     *
     * @return the edit's text.
     */
    public String getText() {
        return fText;
    }

    /* @see TextEdit#doCopy */
    protected TextEdit doCopy() {
        return new InsertEdit(this);
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
        return false;
    }

    /*
     * @see org.eclipse.text.edits.TextEdit#internalToString(java.lang.StringBuffer, int)
     * @since 3.3
     */
    void internalToString(StringBuffer buffer, int indent) {
        super.internalToString(buffer, indent);
        buffer.append(" <<").append(fText); //$NON-NLS-1$
    }
}
