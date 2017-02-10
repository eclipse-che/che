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
import org.eclipse.che.ide.api.editor.text.Region;
import org.eclipse.che.ide.ext.java.jdt.text.Document;

import java.util.List;

/**
 * A multi-text edit can be used to aggregate several edits into one edit. The edit itself doesn't modify a document.
 * <p/>
 * Clients are allowed to implement subclasses of a multi-text edit.Subclasses must implement <code>doCopy()</code> to ensure the
 * a copy of the right type is created. Not implementing <code>doCopy()</code> in subclasses will result in an assertion failure
 * during copying.
 */
public class MultiTextEdit extends TextEdit {

    private boolean fDefined;

    /**
     * Creates a new <code>MultiTextEdit</code>. The range of the edit is determined by the range of its children.
     * <p/>
     * Adding this edit to a parent edit sets its range to the range covered by its children. If the edit doesn't have any children
     * its offset is set to the parent's offset and its length is set to 0.
     */
    public MultiTextEdit() {
        super(0, Integer.MAX_VALUE);
        fDefined = false;
    }

    /**
     * Creates a new </code>MultiTextEdit</code> for the given range. Adding a child to this edit which isn't covered by the given
     * range will result in an exception.
     *
     * @param offset
     *         the edit's offset
     * @param length
     *         the edit's length.
     * @see TextEdit#addChild(TextEdit)
     * @see TextEdit#addChildren(TextEdit[])
     */
    public MultiTextEdit(int offset, int length) {
        super(offset, length);
        fDefined = true;
    }

    /* Copy constructor. */
    protected MultiTextEdit(MultiTextEdit other) {
        super(other);
    }

    /**
     * Checks the edit's integrity.
     * <p>
     * Note that this method <b>should only be called</b> by the edit framework and not by normal clients.
     * </p>
     * <p>
     * This default implementation does nothing. Subclasses may override if needed.
     * </p>
     *
     * @throws MalformedTreeException
     *         if the edit isn't in a valid state and can therefore not be executed
     */
    protected void checkIntegrity() throws MalformedTreeException {
        // does nothing
    }

    /** {@inheritDoc} */
    final boolean isDefined() {
        if (fDefined)
            return true;
        return hasChildren();
    }

    /** {@inheritDoc} */
    public final int getOffset() {
        if (fDefined)
            return super.getOffset();

        List<TextEdit> children = internalGetChildren();
        if (children == null || children.size() == 0)
            return 0;
        // the children are already sorted
        return ((TextEdit)children.get(0)).getOffset();
    }

    /** {@inheritDoc} */
    public final int getLength() {
        if (fDefined)
            return super.getLength();

        List<TextEdit> children = internalGetChildren();
        if (children == null || children.size() == 0)
            return 0;
        // the children are already sorted
        TextEdit first = (TextEdit)children.get(0);
        TextEdit last = (TextEdit)children.get(children.size() - 1);
        return last.getOffset() - first.getOffset() + last.getLength();
    }

    /** {@inheritDoc} */
    public final boolean covers(TextEdit other) {
        if (fDefined)
            return super.covers(other);
        // an undefined multiple text edit covers everything
        return true;
    }

    /* @see org.eclipse.text.edits.TextEdit#canZeroLengthCover() */
    protected boolean canZeroLengthCover() {
        return true;
    }

    /* @see TextEdit#copy */
    protected TextEdit doCopy() {
        //Assert.isTrue(MultiTextEdit.class == getClass(), "Subclasses must reimplement copy0"); //$NON-NLS-1$
        return new MultiTextEdit(this);
    }

    /* @see TextEdit#accept0 */
    protected void accept0(TextEditVisitor visitor) {
        boolean visitChildren = visitor.visit(this);
        if (visitChildren) {
            acceptChildren(visitor);
        }
    }

    /*
     * @see org.eclipse.text.edits.TextEdit#adjustOffset(int)
     * @since 3.1
     */
    void adjustOffset(int delta) {
        if (fDefined)
            super.adjustOffset(delta);
    }

    /*
     * @see org.eclipse.text.edits.TextEdit#adjustLength(int)
     * @since 3.1
     */
    void adjustLength(int delta) {
        if (fDefined)
            super.adjustLength(delta);
    }

    /* @see TextEdit#performConsistencyCheck */
    void performConsistencyCheck(TextEditProcessor processor, Document document) throws MalformedTreeException {
        checkIntegrity();
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

    void aboutToBeAdded(TextEdit parent) {
        defineRegion(parent.getOffset());
    }

    void defineRegion(int parentOffset) {
        if (fDefined)
            return;
        if (hasChildren()) {
            Region region = getCoverage(getChildren());
            internalSetOffset(region.getOffset());
            internalSetLength(region.getLength());
        } else {
            internalSetOffset(parentOffset);
            internalSetLength(0);
        }
        fDefined = true;
    }

    /*
     * @see org.eclipse.text.edits.TextEdit#internalToString(java.lang.StringBuffer, int)
     * @since 3.3
     */
    void internalToString(StringBuffer buffer, int indent) {
        super.internalToString(buffer, indent);
        if (!fDefined)
            buffer.append(" [undefined]"); //$NON-NLS-1$
    }
}
