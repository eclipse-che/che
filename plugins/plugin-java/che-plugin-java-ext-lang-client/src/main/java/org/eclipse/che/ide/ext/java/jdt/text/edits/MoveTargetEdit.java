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

import java.util.ArrayList;
import java.util.List;

/**
 * A move target edit denotes the target of a move operation. Move target edits are only valid inside an edit tree if they have a
 * corresponding source edit. Furthermore a target edit can't can't be a direct or indirect child of its associated source edit.
 * Violating one of two requirements will result in a <code>
 * MalformedTreeException</code> when executing the edit tree.
 * <p/>
 * Move target edits can't be used as a parent for other edits. Trying to add an edit to a move target edit results in a <code>
 * MalformedTreeException</code> as well.
 */
public final class MoveTargetEdit extends TextEdit {

    private MoveSourceEdit fSource;

    /**
     * Constructs a new move target edit
     *
     * @param offset
     *         the edit's offset
     */
    public MoveTargetEdit(int offset) {
        super(offset, 0);
    }

    /**
     * Constructs an new move target edit
     *
     * @param offset
     *         the edit's offset
     * @param source
     *         the corresponding source edit
     */
    public MoveTargetEdit(int offset, MoveSourceEdit source) {
        this(offset);
        setSourceEdit(source);
    }

    /* Copy constructor */
    private MoveTargetEdit(MoveTargetEdit other) {
        super(other);
    }

    /**
     * Returns the associated source edit or <code>null</code> if no source edit is associated yet.
     *
     * @return the source edit or <code>null</code>
     */
    public MoveSourceEdit getSourceEdit() {
        return fSource;
    }

    /**
     * Sets the source edit.
     *
     * @param edit
     *         the source edit
     * @throws MalformedTreeException
     *         is thrown if the target edit is a direct or indirect child of the source edit
     */
    public void setSourceEdit(MoveSourceEdit edit) {
        if (fSource != edit) {
            fSource = edit;
            fSource.setTargetEdit(this);
            TextEdit parent = getParent();
            while (parent != null) {
                if (parent == fSource)
                    throw new MalformedTreeException(parent, this, "Source edit must not be the parent of the target."); //$NON-NLS-1$
                parent = parent.getParent();
            }
        }
    }

    /* @see TextEdit#doCopy */
    protected TextEdit doCopy() {
        return new MoveTargetEdit(this);
    }

    /* @see TextEdit#postProcessCopy */
    protected void postProcessCopy(TextEditCopier copier) {
        if (fSource != null) {
            MoveTargetEdit target = (MoveTargetEdit)copier.getCopy(this);
            MoveSourceEdit source = (MoveSourceEdit)copier.getCopy(fSource);
            if (target != null && source != null)
                target.setSourceEdit(source);
        }
    }

    /* @see TextEdit#accept0 */
    protected void accept0(TextEditVisitor visitor) {
        boolean visitChildren = visitor.visit(this);
        if (visitChildren) {
            acceptChildren(visitor);
        }
    }

    // ---- consistency check ----------------------------------------------------------

    /* @see TextEdit#traverseConsistencyCheck */
    int traverseConsistencyCheck(TextEditProcessor processor, Document document, List<List<TextEdit>> sourceEdits) {
        return super.traverseConsistencyCheck(processor, document, sourceEdits) + 1;
    }

    /* @see TextEdit#performConsistencyCheck */
    void performConsistencyCheck(TextEditProcessor processor, Document document) throws MalformedTreeException {
        if (fSource == null)
            throw new MalformedTreeException(getParent(), this, "No source edit provided."); //$NON-NLS-1$
        if (fSource.getTargetEdit() != this)
            throw new MalformedTreeException(getParent(), this, "Source edit has different target edit."); //$NON-NLS-1$
    }

    // ---- document updating ----------------------------------------------------------------

    /* @see TextEdit#performDocumentUpdating */
    int performDocumentUpdating(Document document) throws BadLocationException {
        String source = fSource.getContent();
        document.replace(getOffset(), getLength(), source);
        fDelta = source.length() - getLength();

        MultiTextEdit sourceRoot = fSource.getSourceRoot();
        if (sourceRoot != null) {
            sourceRoot.internalMoveTree(getOffset());
            TextEdit[] sourceChildren = sourceRoot.removeChildren();
            List<TextEdit> children = new ArrayList<TextEdit>(sourceChildren.length);
            for (int i = 0; i < sourceChildren.length; i++) {
                TextEdit child = sourceChildren[i];
                child.internalSetParent(this);
                children.add(child);
            }
            internalSetChildren(children);
        }
        fSource.clearContent();
        return fDelta;
    }

    // ---- region updating --------------------------------------------------------------

    /* @see org.eclipse.text.edits.TextEdit#traversePassThree */
    int traverseRegionUpdating(TextEditProcessor processor, Document document, int accumulatedDelta, boolean delete) {
        // the children got already updated / normalized while they got removed
        // from the source edit. So we only have to adjust the offset computed to
        // far.
        if (delete) {
            deleteTree();
        } else {
            internalMoveTree(accumulatedDelta);
        }
        return accumulatedDelta + fDelta;
    }

    boolean deleteChildren() {
        return false;
    }
}
