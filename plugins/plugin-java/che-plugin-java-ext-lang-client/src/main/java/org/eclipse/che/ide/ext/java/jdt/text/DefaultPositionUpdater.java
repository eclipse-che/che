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
package org.eclipse.che.ide.ext.java.jdt.text;

import org.eclipse.che.ide.api.text.BadPositionCategoryException;
import org.eclipse.che.ide.api.text.Position;

/**
 * Default implementation of {@link PositionUpdater}.
 * <p>
 * A default position updater must be configured with the position category whose positions it will update. Other position
 * categories are not affected by this updater.
 * </p>
 * <p>
 * This implementation follows the specification below:
 * </p>
 * <ul>
 * <li>Inserting or deleting text before the position shifts the position accordingly.</li>
 * <li>Inserting text at the position offset shifts the position accordingly.</li>
 * <li>Inserting or deleting text strictly contained by the position shrinks or stretches the position.</li>
 * <li>Inserting or deleting text after a position does not affect the position.</li>
 * <li>Deleting text which strictly contains the position deletes the position. Note that the position is not deleted if its only
 * shrunken to length zero. To delete a position, the modification must delete from <i>strictly before</i> to <i>strictly
 * after</i> the position.</li>
 * <li>Replacing text contained by the position shrinks or expands the position (but does not shift it), such that the final
 * position contains the original position and the replacing text.</li>
 * <li>Replacing text overlapping the position in other ways is considered as a sequence of first deleting the replaced text and
 * afterwards inserting the new text. Thus, a position is shrunken and can then be shifted (if the replaced text overlaps the
 * offset of the position).</li>
 * </ul>
 * This class can be used as is or be adapted by subclasses. Fields are protected to allow subclasses direct access. Because of
 * the frequency with which position updaters are used this is a performance decision.
 */
public class DefaultPositionUpdater implements PositionUpdater {

    /** The position category the updater draws responsible for */
    private final String fCategory;

    /** Caches the currently investigated position */
    protected Position fPosition;

    /** Caches the original state of the investigated position */
    protected Position fOriginalPosition = new Position(0, 0);

    /** Caches the offset of the replaced text */
    protected int fOffset;

    /** Caches the length of the replaced text */
    protected int fLength;

    /** Caches the length of the newly inserted text */
    protected int fReplaceLength;

    /** Caches the document */
    protected Document fDocument;

    /**
     * Creates a new default position updater for the given category.
     *
     * @param category
     *         the category the updater is responsible for
     */
    public DefaultPositionUpdater(String category) {
        fCategory = category;
    }

    /**
     * Returns the category this updater is responsible for.
     *
     * @return the category this updater is responsible for
     */
    protected String getCategory() {
        return fCategory;
    }

    /**
     * Returns whether the current event describes a well formed replace by which the current position is directly affected.
     *
     * @return <code>true</code> the current position is directly affected
     * @since 3.0
     */
    protected boolean isAffectingReplace() {
        return fLength > 0 && fReplaceLength > 0 && fPosition.length < fOriginalPosition.length;
    }

    /** Adapts the currently investigated position to an insertion. */
    protected void adaptToInsert() {

        int myStart = fPosition.offset;
        int myEnd = fPosition.offset + fPosition.length - 1;
        myEnd = Math.max(myStart, myEnd);

        int yoursStart = fOffset;
        int yoursEnd = fOffset + fReplaceLength - 1;
        yoursEnd = Math.max(yoursStart, yoursEnd);

        if (myEnd < yoursStart)
            return;

        if (myStart < yoursStart)
            fPosition.length += fReplaceLength;
        else
            fPosition.offset += fReplaceLength;
    }

    /** Adapts the currently investigated position to a deletion. */
    protected void adaptToRemove() {

        int myStart = fPosition.offset;
        int myEnd = fPosition.offset + fPosition.length - 1;
        myEnd = Math.max(myStart, myEnd);

        int yoursStart = fOffset;
        int yoursEnd = fOffset + fLength - 1;
        yoursEnd = Math.max(yoursStart, yoursEnd);

        if (myEnd < yoursStart)
            return;

        if (myStart <= yoursStart) {

            if (yoursEnd <= myEnd)
                fPosition.length -= fLength;
            else
                fPosition.length -= (myEnd - yoursStart + 1);

        } else if (yoursStart < myStart) {

            if (yoursEnd < myStart)
                fPosition.offset -= fLength;
            else {
                fPosition.offset -= (myStart - yoursStart);
                fPosition.length -= (yoursEnd - myStart + 1);
            }

        }

        // validate position to allowed values
        if (fPosition.offset < 0)
            fPosition.offset = 0;

        if (fPosition.length < 0)
            fPosition.length = 0;
    }

    /**
     * Adapts the currently investigated position to the replace operation. First it checks whether the change replaces only a
     * non-zero range inside the range of the position (including the borders). If not, it performs first the deletion of the
     * previous text and afterwards the insertion of the new text.
     */
    protected void adaptToReplace() {

        if (fLength > 0 && fPosition.offset <= fOffset && fOffset + fLength <= fPosition.offset + fPosition.length) {

            fPosition.length += fReplaceLength - fLength;

        } else {

            if (fLength > 0)
                adaptToRemove();

            if (fReplaceLength > 0)
                adaptToInsert();
        }
    }

    /**
     * Determines whether the currently investigated position has been deleted by the replace operation specified in the current
     * event. If so, it deletes the position and removes it from the document's position category.
     *
     * @return <code>true</code> if position has not been deleted
     */
    protected boolean notDeleted() {

        if (fOffset < fPosition.offset && (fPosition.offset + fPosition.length < fOffset + fLength)) {

            fPosition.delete();

            try {
                fDocument.removePosition(fCategory, fPosition);
            } catch (BadPositionCategoryException x) {
            }

            return false;
        }

        return true;
    }

    /*
     * @see org.eclipse.jface.text.IPositionUpdater#update(org.eclipse.jface.text. DocumentEvent)
     */
    public void update(DocumentEvent event) {

        try {

            fOffset = event.getOffset();
            fLength = event.getLength();
            fReplaceLength = (event.getText() == null ? 0 : event.getText().length());
            fDocument = event.getDocument();

            Position[] category = fDocument.getPositions(fCategory);
            for (int i = 0; i < category.length; i++) {

                fPosition = category[i];
                fOriginalPosition.offset = fPosition.offset;
                fOriginalPosition.length = fPosition.length;

                if (notDeleted())
                    adaptToReplace();
            }

        } catch (BadPositionCategoryException x) {
            // do nothing
        } finally {
            fDocument = null;
        }
    }
}
