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
package org.eclipse.che.ide.api.editor.partition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.che.ide.api.editor.text.BadLocationException;
import org.eclipse.che.ide.api.editor.text.BadPositionCategoryException;
import org.eclipse.che.ide.api.editor.text.Position;
import org.eclipse.che.ide.api.editor.text.TypedPosition;
import org.eclipse.che.ide.runtime.Assert;
import org.eclipse.che.ide.util.loging.Log;
/** Implementation for {@link DocumentPositionMap}. */
public class DocumentPositionMapImpl implements DocumentPositionMap {


    /** All positions managed by the document ordered by their start positions. */
    private final Map<String, List<TypedPosition>> positions = new HashMap<>();

    /** All positions managed by the document ordered by there end positions. */
    private final Map<String, List<TypedPosition>> endPositions = new HashMap<>();

    private int contentLength = 0;

    @Override
    public void addPositionCategory(final String category) {
        if (category == null) {
            return;
        }

        if (!containsPositionCategory(category)) {
            this.positions.put(category, new ArrayList<TypedPosition>());
            this.endPositions.put(category, new ArrayList<TypedPosition>());
        }
    }

    @Override
    public boolean containsPosition(String category, int offset, int length) {
        if (category == null) {
            return false;
        }

        final List<TypedPosition> list = this.positions.get(category);
        if (list == null) {
            return false;
        }

        final int size = list.size();
        if (size == 0) {
            return false;
        }

        int index = computeIndexInPositionList(list, offset, true);
        if (index < size) {
            Position p = list.get(index);
            while (p != null && p.offset == offset) {
                if (p.length == length) {
                    return true;
                }
                ++index;
                p = (index < size) ? (Position)list.get(index) : null;
            }
        }

        return false;
    }

    @Override
    public boolean containsPositionCategory(final String category) {
        if (category != null) {
            return this.positions.containsKey(category);
        }
        return false;
    }

    protected int computeIndexInPositionList(final List<TypedPosition> positions, final int offset,
                                             final boolean orderedByOffset) {
        if (positions.size() == 0) {
            return 0;
        }

        int left = 0;
        int right = positions.size() - 1;
        int mid = 0;
        Position p = null;

        while (left < right) {

            mid = (left + right) / 2;

            p = positions.get(mid);
            final int pOffset = getOffset(orderedByOffset, p);
            if (offset < pOffset) {
                if (left == mid) {
                    right = left;
                } else {
                    right = mid - 1;
                }
            } else if (offset > pOffset) {
                if (right == mid) {
                    left = right;
                } else {
                    left = mid + 1;
                }
            } else if (offset == pOffset) {
                left = right = mid;
            }

        }

        int pos = left;
        p = positions.get(pos);
        int pPosition = getOffset(orderedByOffset, p);
        if (offset > pPosition) {
            // append to the end
            pos++;
        } else {
            // entry will become the first of all entries with the same offset
            do {
                --pos;
                if (pos < 0) {
                    break;
                }
                p = positions.get(pos);
                pPosition = getOffset(orderedByOffset, p);
            } while (offset == pPosition);
            ++pos;
        }

        Assert.isTrue(0 <= pos && pos <= positions.size());

        return pos;
    }

    private int getOffset(boolean orderedByOffset, Position position) {
        if (orderedByOffset || position.getLength() == 0) {
            return position.getOffset();
        }
        return position.getOffset() + position.getLength() - 1;
    }

    @Override
    public int computeIndexInCategory(final String category, final int offset) throws BadLocationException,
                                                                              BadPositionCategoryException {

        if (0 > offset || offset > this.contentLength) {
            throw new BadLocationException();
        }

        final List<TypedPosition> c = this.positions.get(category);
        if (c == null) {
            throw new BadPositionCategoryException();
        }

        return computeIndexInPositionList(c, offset, true);
    }

    @Override
    public List<TypedPosition> getPositions(String category) throws BadPositionCategoryException {

        if (category == null) {
            throw new BadPositionCategoryException();
        }

        final List<TypedPosition> c = this.positions.get(category);
        if (c == null) {
            throw new BadPositionCategoryException();
        }

        return new ArrayList<>(c);
    }

    @Override
    public List<String> getPositionCategories() {
        return new ArrayList<>(this.positions.keySet());
    }

    @Override
    public void removePosition(String category, TypedPosition position) throws BadPositionCategoryException {

        if (position == null) {
            return;
        }

        if (category == null) {
            throw new BadPositionCategoryException();
        }

        final List<TypedPosition> c = this.positions.get(category);
        if (c == null) {
            throw new BadPositionCategoryException();
        }
        removeFromPositionsList(c, position, true);

        final List<TypedPosition> endPositions = this.endPositions.get(category);
        if (endPositions == null) {
            throw new BadPositionCategoryException();
        }
        removeFromPositionsList(endPositions, position, false);
    }

    @Override
    public void removePositionCategory(String category) throws BadPositionCategoryException {

        if (category == null) {
            return;
        }

        if (!containsPositionCategory(category)) {
            throw new BadPositionCategoryException();
        }

        this.positions.remove(category);
        this.endPositions.remove(category);
    }

    private void removeFromPositionsList(List<TypedPosition> positions, TypedPosition position, boolean orderedByOffset) {
        final int size = positions.size();

        // Assume position is somewhere near it was before
        final int index =
                          computeIndexInPositionList(positions, orderedByOffset ? position.offset : position.offset + position.length
                                                                                                    - 1, orderedByOffset);
        if (index < size && positions.get(index) == position) {
            positions.remove(index);
            return;
        }

        int back = index - 1;
        int forth = index + 1;
        while (back >= 0 || forth < size) {
            if (back >= 0) {
                if (position == positions.get(back)) {
                    positions.remove(back);
                    return;
                }
                back--;
            }

            if (forth < size) {
                if (position == positions.get(forth)) {
                    positions.remove(forth);
                    return;
                }
                forth++;
            }
        }
    }

    /*
     * @see org.eclipse.jface.text.IDocument#addPosition(java.lang.String, org.eclipse.jface.text.Position)
     */
    @Override
    public void addPosition(String category, TypedPosition position) throws BadLocationException,
                                                                    BadPositionCategoryException {

        if ((0 > position.offset) || (0 > position.length) || (position.offset + position.length > this.contentLength)) {
            throw new BadLocationException();
        }

        if (category == null) {
            throw new BadPositionCategoryException();
        }

        final List<TypedPosition> list = this.positions.get(category);
        if (list == null) {
            throw new BadPositionCategoryException();
        }
        list.add(computeIndexInPositionList(list, position.offset, true), position);

        final List<TypedPosition> endPositions = this.endPositions.get(category);
        if (endPositions == null) {
            throw new BadPositionCategoryException();
        }
        endPositions.add(computeIndexInPositionList(endPositions,
                                                    position.offset + position.length - 1,
                                                    false),
                         position);
    }

    @Override
    public void addPosition(final TypedPosition position) throws BadLocationException {
        try {
            addPosition(Categories.DEFAULT_CATEGORY, position);
        } catch (final BadPositionCategoryException e) {
            Log.warn(DocumentPositionMapImpl.class, "Should not happen: DEFAULT_CATEGORY is not a valid category!");
        }
    }

    @Override
    public List<TypedPosition> getPositions(int offset, int length, boolean canStartBefore,
                                            boolean canEndAfter) throws BadPositionCategoryException {
        return getPositions(Categories.DEFAULT_CATEGORY, offset, length, canStartBefore, canEndAfter);
    }

    @Override
    public List<TypedPosition> getPositions(String category, int offset, int length, boolean canStartBefore,
                                            boolean canEndAfter) throws BadPositionCategoryException {
        if (canStartBefore && canEndAfter || (!canStartBefore && !canEndAfter)) {
            List<TypedPosition> documentPositions;
            if (canStartBefore && canEndAfter) {
                if (offset < this.contentLength / 2) {
                    documentPositions = getStartingPositions(category, 0, offset + length);
                } else {
                    documentPositions = getEndingPositions(category, offset, this.contentLength - offset + 1);
                }
            } else {
                documentPositions = getStartingPositions(category, offset, length);
            }

            final List<TypedPosition> list = new ArrayList<TypedPosition>(documentPositions.size());

            final Position region = new Position(offset, length);

            for (final TypedPosition position: documentPositions) {
                if (isWithinRegion(region, position, canStartBefore, canEndAfter)) {
                    list.add(position);
                }
            }

            return list;
        } else if (canStartBefore) {
            final List<TypedPosition> list = getEndingPositions(category, offset, length);
            return list;
        } else {
            Assert.isLegal(canEndAfter && !canStartBefore);

            final List<TypedPosition> list = getStartingPositions(category, offset, length);
            return list;
        }
    }

    private List<TypedPosition> getEndingPositions(String category, int offset, int length)
                                                                                           throws BadPositionCategoryException {
        final List<TypedPosition> positions = endPositions.get(category);
        if (positions == null) {
            throw new BadPositionCategoryException();
        }

        final int indexStart = computeIndexInPositionList(positions, offset, false);
        final int indexEnd = computeIndexInPositionList(positions, offset + length, false);

        return positions.subList(indexStart, indexEnd);
    }

    private List<TypedPosition> getStartingPositions(String category, int offset, int length)
                                                                                             throws BadPositionCategoryException {
        final List<TypedPosition> categoryPositions = positions.get(category);
        if (categoryPositions == null) {
            throw new BadPositionCategoryException();
        }

        final int indexStart = computeIndexInPositionList(categoryPositions, offset, true);
        final int indexEnd = computeIndexInPositionList(categoryPositions, offset + length, true);

        return categoryPositions.subList(indexStart, indexEnd);
    }

    private boolean isWithinRegion(Position region, Position position, boolean canStartBefore, boolean canEndAfter) {
        if (canStartBefore && canEndAfter) {
            return region.overlapsWith(position.getOffset(), position.getLength());
        } else if (canStartBefore) {
            return region.includes(position.getOffset() + position.getLength() - 1);
        } else if (canEndAfter) {
            return region.includes(position.getOffset());
        } else {
            final int start = position.getOffset();
            return region.includes(start) && region.includes(start + position.getLength() - 1);
        }
    }

    @Override
    public void setContentLength(final int newLength) {
        this.contentLength = newLength;
    }

    @Override
    public void resetPositions() {
        for (final List<TypedPosition> list : positions.values()) {
            list.clear();
        }
        for (final List<TypedPosition> list : endPositions.values()) {
            list.clear();
        }
    }
}
