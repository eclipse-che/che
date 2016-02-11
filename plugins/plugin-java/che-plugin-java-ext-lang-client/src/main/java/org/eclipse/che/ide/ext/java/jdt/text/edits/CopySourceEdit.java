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
 * A copy source edit denotes the source of a copy operation. Copy source edits are only valid inside an edit tree if they have a
 * corresponding target edit. Furthermore the corresponding target edit can't be a direct or indirect child of the source edit.
 * Violating one of two requirements will result in a <code>
 * MalformedTreeException</code> when executing the edit tree.
 * <p/>
 * A copy source edit can manage an optional source modifier. A source modifier can provide a set of replace edits which will to
 * applied to the source before it gets inserted at the target position.
 */
public final class CopySourceEdit extends TextEdit {

    private CopyTargetEdit fTarget;

    private SourceModifier fModifier;

    private String fSourceContent;

    private TextEdit fSourceRoot;

    private static class PartialCopier extends TextEditVisitor {
        TextEdit fResult;

        List<TextEdit> fParents = new ArrayList<TextEdit>();

        TextEdit fCurrentParent;

        public static TextEdit perform(TextEdit source) {
            PartialCopier copier = new PartialCopier();
            source.accept(copier);
            return copier.fResult;
        }

        private void manageCopy(TextEdit copy) {
            if (fResult == null)
                fResult = copy;
            if (fCurrentParent != null) {
                fCurrentParent.addChild(copy);
            }
            fParents.add(fCurrentParent);
            fCurrentParent = copy;
        }

        public void postVisit(TextEdit edit) {
            fCurrentParent = fParents.remove(fParents.size() - 1);
        }

        public boolean visitNode(TextEdit edit) {
            manageCopy(edit.doCopy());
            return true;
        }

        public boolean visit(CopySourceEdit edit) {
            manageCopy(new RangeMarker(edit.getOffset(), edit.getLength()));
            return true;
        }

        public boolean visit(CopyTargetEdit edit) {
            manageCopy(new InsertEdit(edit.getOffset(), edit.getSourceEdit().getContent()));
            return true;
        }

        public boolean visit(MoveSourceEdit edit) {
            manageCopy(new DeleteEdit(edit.getOffset(), edit.getLength()));
            return true;
        }

        public boolean visit(MoveTargetEdit edit) {
            manageCopy(new InsertEdit(edit.getOffset(), edit.getSourceEdit().getContent()));
            return true;
        }
    }

    /**
     * Constructs a new copy source edit.
     *
     * @param offset
     *         the edit's offset
     * @param length
     *         the edit's length
     */
    public CopySourceEdit(int offset, int length) {
        super(offset, length);
    }

    /**
     * Constructs a new copy source edit.
     *
     * @param offset
     *         the edit's offset
     * @param length
     *         the edit's length
     * @param target
     *         the edit's target
     */
    public CopySourceEdit(int offset, int length, CopyTargetEdit target) {
        this(offset, length);
        setTargetEdit(target);
    }

    /* Copy Constructor */
    private CopySourceEdit(CopySourceEdit other) {
        super(other);
        if (other.fModifier != null)
            fModifier = other.fModifier.copy();
    }

    /**
     * Returns the associated target edit or <code>null</code> if no target edit is associated yet.
     *
     * @return the target edit or <code>null</code>
     */
    public CopyTargetEdit getTargetEdit() {
        return fTarget;
    }

    /**
     * Sets the target edit.
     *
     * @param edit
     *         the new target edit.
     * @throws MalformedTreeException
     *         is thrown if the target edit is a direct or indirect child of the source edit
     */
    public void setTargetEdit(CopyTargetEdit edit) throws MalformedTreeException {
        if (fTarget != edit) {
            fTarget = edit;
            fTarget.setSourceEdit(this);
        }
    }

    /**
     * Returns the current source modifier or <code>null</code> if no source modifier is set.
     *
     * @return the source modifier
     */
    public SourceModifier getSourceModifier() {
        return fModifier;
    }

    /**
     * Sets the optional source modifier.
     *
     * @param modifier
     *         the source modifier or <code>null</code> if no source modification is need.
     */
    public void setSourceModifier(SourceModifier modifier) {
        fModifier = modifier;
    }

    /* @see TextEdit#doCopy */
    protected TextEdit doCopy() {
        return new CopySourceEdit(this);
    }

    /* @see TextEdit#accept0 */
    protected void accept0(TextEditVisitor visitor) {
        boolean visitChildren = visitor.visit(this);
        if (visitChildren) {
            acceptChildren(visitor);
        }
    }

    // ---- API for CopyTargetEdit ------------------------------------------------

    String getContent() {
        // The source content can be null if the edit wasn't executed
        // due to an exclusion list of the text edit processor. Return
        // the empty string which can be moved without any harm.
        if (fSourceContent == null)
            return ""; //$NON-NLS-1$
        return fSourceContent;
    }

    void clearContent() {
        fSourceContent = null;
    }

    /* @see TextEdit#postProcessCopy */
    protected void postProcessCopy(TextEditCopier copier) {
        if (fTarget != null) {
            CopySourceEdit source = (CopySourceEdit)copier.getCopy(this);
            CopyTargetEdit target = (CopyTargetEdit)copier.getCopy(fTarget);
            if (source != null && target != null)
                source.setTargetEdit(target);
        }
    }

    // ---- consistency check ----------------------------------------------------

    int traverseConsistencyCheck(TextEditProcessor processor, Document document, List<List<TextEdit>> sourceEdits) {
        int result = super.traverseConsistencyCheck(processor, document, sourceEdits);
        // Since source computation takes place in a recursive fashion (see
        // performSourceComputation) we only do something if we don't have a
        // computed source already.
        if (fSourceContent == null) {
            if (sourceEdits.size() <= result) {
                List<TextEdit> list = new ArrayList<TextEdit>();
                list.add(this);
                for (int i = sourceEdits.size(); i < result; i++)
                    sourceEdits.add(null);
                sourceEdits.add(list);
            } else {
                List<TextEdit> list = sourceEdits.get(result);
                if (list == null) {
                    list = new ArrayList<TextEdit>();
                    sourceEdits.add(result, list);
                }
                list.add(this);
            }
        }
        return result;
    }

    void performConsistencyCheck(TextEditProcessor processor, Document document) throws MalformedTreeException {
        if (fTarget == null)
            throw new MalformedTreeException(getParent(), this, "No target edit provided."); //$NON-NLS-1$
        if (fTarget.getSourceEdit() != this)
            throw new MalformedTreeException(getParent(), this, "Target edit has different source edit."); //$NON-NLS-1$
      /*
       * causes ASTRewrite to fail if (getRoot() != fTarget.getRoot()) throw new MalformedTreeException(getParent(), this,
       * TextEditMessages.getString("CopySourceEdit.different_tree")); //$NON-NLS-1$
       */
    }

    // ---- source computation -------------------------------------------------------

    void traverseSourceComputation(TextEditProcessor processor, Document document) {
        // always perform source computation independent of processor.considerEdit
        // The target might need the source and the source is computed in a
        // temporary buffer.
        performSourceComputation(processor, document);
    }

    void performSourceComputation(TextEditProcessor processor, Document document) {
        try {
            MultiTextEdit root = new MultiTextEdit(getOffset(), getLength());
            root.internalSetChildren(internalGetChildren());
            fSourceContent = document.get(getOffset(), getLength());
            fSourceRoot = PartialCopier.perform(root);
            fSourceRoot.internalMoveTree(-getOffset());
            if (fSourceRoot.hasChildren()) {
                EditDocument subDocument = new EditDocument(fSourceContent);
                TextEditProcessor subProcessor =
                        TextEditProcessor.createSourceComputationProcessor(subDocument, fSourceRoot, TextEdit.NONE);
                subProcessor.performEdits();
                if (needsTransformation())
                    applyTransformation(subDocument);
                fSourceContent = subDocument.get();
                fSourceRoot = null;
            } else {
                if (needsTransformation()) {
                    EditDocument subDocument = new EditDocument(fSourceContent);
                    applyTransformation(subDocument);
                    fSourceContent = subDocument.get();
                }
            }
        } catch (BadLocationException cannotHappen) {
            // Assert.isTrue(false);
            // skip
        }
    }

    private boolean needsTransformation() {
        return fModifier != null;
    }

    private void applyTransformation(Document document) throws MalformedTreeException {
        TextEdit newEdit = new MultiTextEdit(0, document.getLength());
        ReplaceEdit[] replaces = fModifier.getModifications(document.get());
        for (int i = 0; i < replaces.length; i++) {
            newEdit.addChild(replaces[i]);
        }
        try {
            newEdit.apply(document, TextEdit.NONE);
        } catch (BadLocationException cannotHappen) {
            // skip
        }
    }

    // ---- document updating ----------------------------------------------------------------

    int performDocumentUpdating(Document document) throws BadLocationException {
        fDelta = 0;
        return fDelta;
    }

    // ---- region updating ----------------------------------------------------------------

    /* @see TextEdit#deleteChildren */
    boolean deleteChildren() {
        return false;
    }
}
