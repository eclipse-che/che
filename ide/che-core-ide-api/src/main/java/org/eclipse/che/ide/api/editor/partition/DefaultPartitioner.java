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
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.che.ide.api.editor.events.DocumentChangeEvent;
import org.eclipse.che.ide.api.editor.text.BadLocationException;
import org.eclipse.che.ide.api.editor.text.BadPositionCategoryException;
import org.eclipse.che.ide.api.editor.text.Position;
import org.eclipse.che.ide.api.editor.text.TypedPosition;
import org.eclipse.che.ide.api.editor.text.TypedRegion;
import org.eclipse.che.ide.api.editor.text.TypedRegionImpl;
import org.eclipse.che.ide.api.editor.text.rules.Token;
import org.eclipse.che.ide.api.editor.document.DocumentHandle;
import org.eclipse.che.ide.util.loging.Log;

/**
 * Default implementation of the {@link DocumentPartitioner}.
 */
public class DefaultPartitioner implements DocumentPartitioner {

    /** The identifier of the default partitioning. */
    public final static String DEFAULT_PARTITIONING = "__dftl_partitioning";

    private final PartitionScanner scanner;
    private final DocumentPositionMap documentPositionMap;

    private DocumentHandle documentHandle;

    /** The legal content types of this partitioner */
    private final List<String> legalContentTypes;
    /** The position category this partitioner uses to store the document's partitioning information. */
    private final String positionCategory;


    public DefaultPartitioner(final PartitionScanner scanner,
                              final List<String> legalContentTypes,
                              final DocumentPositionMap documentPositionMap) {
        this.legalContentTypes = new ArrayList<>(legalContentTypes);
        this.scanner = scanner;
        this.documentPositionMap = documentPositionMap;
        this.positionCategory = DocumentPositionMap.Categories.DEFAULT_CATEGORY;
    }

    @Override
    public void initialize() {
        this.documentPositionMap.addPositionCategory(this.positionCategory);
        this.documentPositionMap.setContentLength(this.documentHandle.getDocument().getContentsCharCount());
    }

    private void updatePositions() {
        // set before the scan as the scan uses the content length
        this.documentPositionMap.setContentLength(getContentLength());
        this.documentPositionMap.resetPositions();

        Position current = null;
        try {
            Token token = scanner.nextToken();
            while (!token.isEOF()) {

                final String contentType = getTokenContentType(token);

                if (isSupportedContentType(contentType)) {
                    final TypedPosition position = new TypedPosition(scanner.getTokenOffset(), scanner.getTokenLength(), contentType);
                    current = position;
                    this.documentPositionMap.addPosition(this.positionCategory, position);
                }

                token = scanner.nextToken();
            }
        } catch (final BadLocationException x) {
            Log.error(DefaultPartitioner.class, "Invalid position: " + String.valueOf(current) + " (max:" + getContentLength() + ").", x);
        } catch (final BadPositionCategoryException x) {
            Log.error(DefaultPartitioner.class, "Invalid position category: " + this.positionCategory, x);
        }
    }

    @Override
    public void onDocumentChange(final DocumentChangeEvent event) {
        this.scanner.setScannedString(event.getDocument().getDocument().getContents());
        updatePositions();
    }

    @Override
    public List<String> getLegalContentTypes() {
        return new ArrayList<>(this.legalContentTypes);
    }

    @Override
    public String getContentType(final int offset) {
        final TypedPosition position = findClosestPosition(offset);
        if (position != null && position.includes(offset)) {
            return position.getType();
        }

        return DEFAULT_CONTENT_TYPE;
    }

    @Override
    public final List<TypedRegion> computePartitioning(final int offset, final int length) {
        return computePartitioning(offset, length, false);
    }

    private List<TypedRegion> computePartitioning(final int offset,
                                                  final int length,
                                                  final boolean includeZeroLengthPartitions) {
        final List<TypedRegion> result = new ArrayList<>();

        final int contentLength = getContentLength();
        try {

            final int endOffset = offset + length;

            final List<TypedPosition> category = getPositions();

            TypedPosition previous = null;
            TypedPosition current = null;
            int start, end, gapOffset;
            final Position gap = new Position(0);

            final int startIndex = getFirstIndexEndingAfterOffset(category, offset);
            final int endIndex = getFirstIndexStartingAfterOffset(category, endOffset);
            for (int i = startIndex; i < endIndex; i++) {

                current = category.get(i);

                gapOffset = (previous != null) ? previous.getOffset() + previous.getLength() : 0;
                gap.setOffset(gapOffset);
                gap.setLength(current.getOffset() - gapOffset);
                if ((includeZeroLengthPartitions && overlapsOrTouches(gap, offset, length))
                    || (gap.getLength() > 0 && gap.overlapsWith(offset, length))) {
                    start = Math.max(offset, gapOffset);
                    end = Math.min(endOffset, gap.getOffset() + gap.getLength());
                    result.add(new TypedRegionImpl(start, end - start, DEFAULT_CONTENT_TYPE));
                }

                if (current.overlapsWith(offset, length)) {
                    start = Math.max(offset, current.getOffset());
                    end = Math.min(endOffset, current.getOffset() + current.getLength());
                    result.add(new TypedRegionImpl(start, end - start, current.getType()));
                }

                previous = current;
            }

            if (previous != null) {
                gapOffset = previous.getOffset() + previous.getLength();
                gap.setOffset(gapOffset);
                gap.setLength(contentLength - gapOffset);
                if ((includeZeroLengthPartitions && overlapsOrTouches(gap, offset, length)) ||
                    (gap.getLength() > 0 && gap.overlapsWith(offset, length))) {
                    start = Math.max(offset, gapOffset);
                    end = Math.min(endOffset, contentLength);
                    result.add(new TypedRegionImpl(start, end - start, DEFAULT_CONTENT_TYPE));
                }
            }

            if (result.isEmpty()) {
                result.add(new TypedRegionImpl(offset, length, DEFAULT_CONTENT_TYPE));
            }

        } catch (final BadPositionCategoryException ex) {
            Logger.getLogger(DefaultPartitioner.class.getName()).fine("Bad position in computePartitioning.");
        } catch (final RuntimeException ex) {
            Logger.getLogger(DefaultPartitioner.class.getName()).warning("computePartitioning failed.");
            throw ex;
        }

        return result;
    }

    @Override
    public TypedRegion getPartition(final int offset) {
        final int contentLength = getContentLength();

        List<TypedPosition> category = null;
        try {
            category = getPositions();
        } catch (final BadPositionCategoryException e) {
            Log.warn(DefaultPartitioner.class, "Invalid position cateory... with default category! ", e);
            return defaultRegion();
        }

        if (category == null || category.size() == 0) {
            return defaultRegion();
        }

        Integer index = null;
        try {
            index = this.documentPositionMap.computeIndexInCategory(positionCategory, offset);
        } catch (final BadLocationException e) {
            Log.warn(DefaultPartitioner.class, "Invalid location " + offset + " (max=" + contentLength + ").");
            return defaultRegion();
        } catch (final BadPositionCategoryException e) {
            Log.warn(DefaultPartitioner.class, "Invalid position cateory... with default category " + positionCategory + "!", e);
            return defaultRegion();
        }
        if (index == null) {
            return defaultRegion();
        }

        if (index < category.size()) {

            final TypedPosition next = category.get(index);

            if (offset == next.offset) {
                return new TypedRegionImpl(next.getOffset(),
                                           next.getLength(),
                                           next.getType());
            }

            if (index == 0) {
                return new TypedRegionImpl(0, next.offset, DEFAULT_CONTENT_TYPE);
            }

            final TypedPosition previous = category.get(index - 1);
            if (previous.includes(offset)) {
                return new TypedRegionImpl(previous.getOffset(),
                                           previous.getLength(),
                                           previous.getType());
            }

            final int endOffset = previous.getOffset() + previous.getLength();
            return new TypedRegionImpl(endOffset,
                                       next.getOffset() - endOffset,
                                       DEFAULT_CONTENT_TYPE);
        }

        final TypedPosition previous = category.get(category.size() - 1);
        if (previous.includes(offset)) {
            return new TypedRegionImpl(previous.getOffset(),
                                       previous.getLength(),
                                       previous.getType());
        }

        final int endOffset = previous.getOffset() + previous.getLength();
        return new TypedRegionImpl(endOffset,
                                   contentLength - endOffset,
                                   DEFAULT_CONTENT_TYPE);

    }

    private TypedRegionImpl defaultRegion() {
        return new TypedRegionImpl(0, getContentLength(), DEFAULT_CONTENT_TYPE);
    }

    /**
     * Returns <code>true</code> if the given ranges overlap with or touch each other.
     *
     * @param gap the first range
     * @param offset the offset of the second range
     * @param length the length of the second range
     * @return <code>true</code> if the given ranges overlap with or touch each other
     */
    private static boolean overlapsOrTouches(Position gap, int offset, int length) {
        return gap.getOffset() <= offset + length && offset <= gap.getOffset() + gap.getLength();
    }

    /**
     * Returns the index of the first position which ends after the given offset.
     *
     * @param positions the positions in linear order
     * @param offset the offset
     * @return the index of the first position which ends after the offset
     */
    private static int getFirstIndexEndingAfterOffset(final List<TypedPosition> positions, final int offset) {
        int i = -1;
        int j = positions.size();
        while (j - i > 1) {
            final int k = (i + j) >> 1;
            final Position p = positions.get(k);
            if (p.getOffset() + p.getLength() > offset) {
                j = k;
            } else {
                i = k;
            }
        }
        return j;
    }

    /**
     * Returns the index of the first position which starts at or after the given offset.
     *
     * @param positions the positions in linear order
     * @param offset the offset
     * @return the index of the first position which starts after the offset
     */
    private static int getFirstIndexStartingAfterOffset(List<TypedPosition> positions, int offset) {
        int i = -1;
        int j = positions.size();
        while (j - i > 1) {
            final int k = (i + j) >> 1;
            final Position p = positions.get(k);
            if (p.getOffset() >= offset) {
                j = k;
            } else {
                i = k;
            }
        }
        return j;
    }

    /**
     * Returns a content type encoded in the given token. If the token's data is not <code>null</code> and a string it is assumed that it is
     * the encoded content type.
     * <p>
     * May be replaced or extended by subclasses.
     * </p>
     *
     * @param token the token whose content type is to be determined
     * @return the token's content type
     */
    protected static String getTokenContentType(Token token) {
        final Object data = token.getData();
        if (data instanceof String) {
            return (String)data;
        }
        return null;
    }

    /**
     * Returns whether the given type is one of the legal content types.
     * <p>
     * May be extended by subclasses.
     * </p>
     *
     * @param contentType the content type to check
     * @return <code>true</code> if the content type is a legal content type
     */
    protected boolean isSupportedContentType(final String contentType) {
        if (contentType != null) {
            for (final String item : this.legalContentTypes) {
                if (item.equals(contentType)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Returns the position in the partitoner's position category which is close to the given offset. This is, the position has either an
     * offset which is the same as the given offset or an offset which is smaller than the given offset. This method profits from the
     * knowledge that a partitioning is a ordered set of disjoint position.
     * <p>
     * May be extended or replaced by subclasses.
     * </p>
     *
     * @param offset the offset for which to search the closest position
     * @return the closest position in the partitioner's category
     */
    protected TypedPosition findClosestPosition(int offset) {

        int index = -1;
        try {
            index = this.documentPositionMap.computeIndexInCategory(this.positionCategory, offset);
        } catch (final BadLocationException e) {
            Log.warn(DefaultPartitioner.class, "Bad location: " + offset + "(max:" + getContentLength() + ").");
            return null;
        } catch (final BadPositionCategoryException e) {
            Log.warn(DefaultPartitioner.class, "Bad position category: " + this.positionCategory);
            return null;
        }
        if (index == -1) {
            return null;
        }

        List<TypedPosition> category = null;
        try {
            category = getPositions();
        } catch (final BadPositionCategoryException e) {
            Log.warn(DefaultPartitioner.class, "Bad position category: " + this.positionCategory);
            return null;
        }

        if (category == null || category.size() == 0) {
            return null;
        }

        if (index < category.size()) {
            if (offset == category.get(index).offset) {
                return category.get(index);
            }
        }

        if (index > 0) {
            index--;
        }

        return category.get(index);
    }

    /**
     * Returns the partitioners positions.
     *
     * @return the partitioners positions
     * @throws BadPositionCategoryException if getting the positions from the document fails
     */
    protected final List<TypedPosition> getPositions() throws BadPositionCategoryException {
        return this.documentPositionMap.getPositions(this.positionCategory);
    }

    private int getContentLength() {
        return getDocumentHandle().getDocument().getContentsCharCount();
    }

    @Override
    public DocumentHandle getDocumentHandle() {
        return documentHandle;
    }

    @Override
    public void setDocumentHandle(DocumentHandle handle) {
        this.documentHandle = handle;
    }

    @Override
    public void release() {
    }
}
