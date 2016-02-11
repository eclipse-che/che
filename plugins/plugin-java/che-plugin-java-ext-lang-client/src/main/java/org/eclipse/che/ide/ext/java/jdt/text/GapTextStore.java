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

import org.eclipse.che.ide.runtime.Assert;


/**
 * Implements a gap managing text store. The gap text store relies on the assumption that consecutive changes to a document are
 * co-located. The start of the gap is always moved to the location of the last change.
 * <p>
 * <strong>Performance:</strong> Typing-style changes perform in constant time unless re-allocation becomes necessary. Generally,
 * a change that does not cause re-allocation will cause at most one {@linkplain System#arraycopy(Object, int, Object, int, int)
 * arraycopy} operation of a length of about <var>d</var>, where <var>d</var> is the distance from the previous change. Let
 * <var>a(x)</var> be the algorithmic performance of an <code>arraycopy</code> operation of the length <var>x</var>, then such a
 * change then performs in <i>O(a(x))</i>, {@linkplain #get(int, int) get(int, <var>length</var>)} performs in
 * <i>O(a(length))</i>, {@link #get(int)} in <i>O(1)</i>.
 * <p>
 * How frequently the array needs re-allocation is controlled by the constructor parameters.
 * </p>
 * <p>
 * This class is not intended to be subclassed.
 * </p>
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @see CopyOnWriteTextStore for a copy-on-write text store wrapper
 */
public class GapTextStore implements TextStore {
    /**
     * The minimum gap size allocated when re-allocation occurs.
     *
     * @since 3.3
     */
    private final int fMinGapSize;

    /**
     * The maximum gap size allocated when re-allocation occurs.
     *
     * @since 3.3
     */
    private final int fMaxGapSize;

    /**
     * The multiplier to compute the array size from the content length (1&nbsp;&lt;=&nbsp;fSizeMultiplier&nbsp;&lt;=&nbsp;2).
     *
     * @since 3.3
     */
    private final float fSizeMultiplier;

    /** The store's content */
    private char[] fContent = new char[0];

    /** Starting index of the gap */
    private int fGapStart = 0;

    /** End index of the gap */
    private int fGapEnd = 0;

    /**
     * The current high water mark. If a change would cause the gap to grow larger than this, the array is re-allocated.
     *
     * @since 3.3
     */
    private int fThreshold = 0;

    /**
     * Creates a new empty text store using the specified low and high watermarks.
     *
     * @param lowWatermark
     *         unused - at the lower bound, the array is only resized when the content does not fit
     * @param highWatermark
     *         if the gap is ever larger than this, it will automatically be shrunken (&gt;=&nbsp;0)
     * @deprecated use {@link GapTextStore#GapTextStore(int, int, float)} instead
     */
    public GapTextStore(int lowWatermark, int highWatermark) {
      /*
       * Legacy constructor. The API contract states that highWatermark is the upper bound for the gap size. Albeit this contract
       * was not previously adhered to, it is now: The allocated gap size is fixed at half the highWatermark. Since the threshold
       * is always twice the allocated gap size, the gap will never grow larger than highWatermark. Previously, the gap size was
       * initialized to highWatermark, causing re-allocation if the content length shrunk right after allocation. The fixed gap
       * size is now only half of the previous value, circumventing that problem (there was no API contract specifying the initial
       * gap size). The previous implementation did not allow the gap size to become smaller than lowWatermark, which doesn't make
       * any sense: that area of the gap was simply never ever used.
       */
        this(highWatermark / 2, highWatermark / 2, 0f);
    }

    /**
     * Equivalent to {@linkplain GapTextStore#GapTextStore(int, int, float) new GapTextStore(256, 4096, 0.1f)}.
     *
     * @since 3.3
     */
    public GapTextStore() {
        this(256, 4096, 0.1f);
    }

    /**
     * Creates an empty text store that uses re-allocation thresholds relative to the content length. Re-allocation is controlled
     * by the <em>gap factor</em> , which is the quotient of the gap size and the array size. Re-allocation occurs if a change
     * causes the gap factor to go outside <code>[0,&nbsp;maxGapFactor]</code>. When re-allocation occurs, the array is sized such
     * that the gap factor is <code>0.5 * maxGapFactor</code>. The gap size computed in this manner is bounded by the
     * <code>minSize</code> and <code>maxSize</code> parameters.
     * <p>
     * A <code>maxGapFactor</code> of <code>0</code> creates a text store that never has a gap at all (if <code>minSize</code> is
     * 0); a <code>maxGapFactor</code> of <code>1</code> creates a text store that doubles its size with every re-allocation and
     * that never shrinks.
     * </p>
     * <p>
     * The <code>minSize</code> and <code>maxSize</code> parameters are absolute bounds to the allocated gap size. Use
     * <code>minSize</code> to avoid frequent re-allocation for small documents. Use <code>maxSize</code> to avoid a huge gap being
     * allocated for large documents.
     * </p>
     *
     * @param minSize
     *         the minimum gap size to allocate (&gt;=&nbsp;0; use 0 for no minimum)
     * @param maxSize
     *         the maximum gap size to allocate (&gt;=&nbsp;minSize; use {@link Integer#MAX_VALUE} for no maximum)
     * @param maxGapFactor
     *         is the maximum fraction of the array that is occupied by the gap (
     *         <code>0&nbsp;&lt;=&nbsp;maxGapFactor&nbsp;&lt;=&nbsp;1</code> )
     * @since 3.3
     */
    public GapTextStore(int minSize, int maxSize, float maxGapFactor) {
        Assert.isLegal(0f <= maxGapFactor && maxGapFactor <= 1f);
        Assert.isLegal(0 <= minSize && minSize <= maxSize);
        fMinGapSize = minSize;
        fMaxGapSize = maxSize;
        fSizeMultiplier = 1 / (1 - maxGapFactor / 2);
    }

    /* @see org.eclipse.jface.text.ITextStore#get(int) */
    public final char get(int offset) {
        if (offset < fGapStart)
            return fContent[offset];

        return fContent[offset + gapSize()];
    }

    /* @see org.eclipse.jface.text.ITextStore#get(int, int) */
    public final String get(int offset, int length) {
        if (fGapStart <= offset)
            return new String(fContent, offset + gapSize(), length);

        final int end = offset + length;

        if (end <= fGapStart)
            return new String(fContent, offset, length);

        StringBuffer buf = new StringBuffer(length);
        buf.append(fContent, offset, fGapStart - offset);
        buf.append(fContent, fGapEnd, end - fGapStart);
        return buf.toString();
    }

    /* @see org.eclipse.jface.text.ITextStore#getLength() */
    public final int getLength() {
        return fContent.length - gapSize();
    }

    /* @see org.eclipse.jface.text.ITextStore#set(java.lang.String) */
    public final void set(String text) {
      /*
       * Moves the gap to the end of the content. There is no sensible prediction of where the next change will occur, but at
       * least the next change will not trigger re-allocation. This is especially important when using the GapTextStore within a
       * CopyOnWriteTextStore, where the GTS is only initialized right before a modification.
       */
        replace(0, getLength(), text);
    }

    /* @see org.eclipse.jface.text.ITextStore#replace(int, int, java.lang.String) */
    public final void replace(int offset, int length, String text) {
        if (text == null) {
            adjustGap(offset, length, 0);
        } else {
            int textLength = text.length();
            adjustGap(offset, length, textLength);
            if (textLength != 0)
                text.getChars(0, textLength, fContent, offset);
        }
    }

    /**
     * Moves the gap to <code>offset + add</code>, moving any content after <code>offset + remove</code> behind the gap. The gap
     * size is kept between 0 and {@link #fThreshold}, leading to re-allocation if needed. The content between <code>offset</code>
     * and <code>offset + add</code> is undefined after this operation.
     *
     * @param offset
     *         the offset at which a change happens
     * @param remove
     *         the number of character which are removed or overwritten at <code>offset</code>
     * @param add
     *         the number of character which are inserted or overwriting at <code>offset</code>
     */
    private void adjustGap(int offset, int remove, int add) {
        final int oldGapSize = gapSize();
        final int newGapSize = oldGapSize - add + remove;
        final boolean reuseArray = 0 <= newGapSize && newGapSize <= fThreshold;

        final int newGapStart = offset + add;
        final int newGapEnd;

        if (reuseArray)
            newGapEnd = moveGap(offset, remove, oldGapSize, newGapSize, newGapStart);
        else
            newGapEnd = reallocate(offset, remove, oldGapSize, newGapSize, newGapStart);

        fGapStart = newGapStart;
        fGapEnd = newGapEnd;
    }

    /**
     * Moves the gap to <code>newGapStart</code>.
     *
     * @param offset
     *         the change offset
     * @param remove
     *         the number of removed / overwritten characters
     * @param oldGapSize
     *         the old gap size
     * @param newGapSize
     *         the gap size after the change
     * @param newGapStart
     *         the offset in the array to move the gap to
     * @return the new gap end
     * @since 3.3
     */
    private int moveGap(int offset, int remove, int oldGapSize, int newGapSize, int newGapStart) {
      /*
       * No re-allocation necessary. The area between the change offset and gap can be copied in at most one operation. Don't copy
       * parts that will be overwritten anyway.
       */
        final int newGapEnd = newGapStart + newGapSize;
        if (offset < fGapStart) {
            int afterRemove = offset + remove;
            if (afterRemove < fGapStart) {
                final int betweenSize = fGapStart - afterRemove;
                arrayCopy(afterRemove, fContent, newGapEnd, betweenSize);
            }
            // otherwise, only the gap gets enlarged
        } else {
            final int offsetShifted = offset + oldGapSize;
            final int betweenSize = offsetShifted - fGapEnd; // in the typing case, betweenSize is 0
            arrayCopy(fGapEnd, fContent, fGapStart, betweenSize);
        }
        return newGapEnd;
    }

    /**
     * Reallocates a new array and copies the data from the previous one.
     *
     * @param offset
     *         the change offset
     * @param remove
     *         the number of removed / overwritten characters
     * @param oldGapSize
     *         the old gap size
     * @param newGapSize
     *         the gap size after the change if no re-allocation would occur (can be negative)
     * @param newGapStart
     *         the offset in the array to move the gap to
     * @return the new gap end
     * @since 3.3
     */
    private int reallocate(int offset, int remove, final int oldGapSize, int newGapSize, final int newGapStart) {
        // the new content length (without any gap)
        final int newLength = fContent.length - newGapSize;
        // the new array size based on the gap factor
        int newArraySize = (int)(newLength * fSizeMultiplier);
        newGapSize = newArraySize - newLength;

        // bound the gap size within min/max
        if (newGapSize < fMinGapSize) {
            newGapSize = fMinGapSize;
            newArraySize = newLength + newGapSize;
        } else if (newGapSize > fMaxGapSize) {
            newGapSize = fMaxGapSize;
            newArraySize = newLength + newGapSize;
        }

        // the upper threshold is always twice the gapsize
        fThreshold = newGapSize * 2;
        final char[] newContent = allocate(newArraySize);
        final int newGapEnd = newGapStart + newGapSize;

      /*
       * Re-allocation: The old content can be copied in at most 3 operations to the newly allocated array. Either one of change
       * offset and the gap may come first. - unchanged area before the change offset / gap - area between the change offset and
       * the gap (either one may be first) - rest area after the change offset / after the gap
       */
        if (offset < fGapStart) {
            // change comes before gap
            arrayCopy(0, newContent, 0, offset);
            int afterRemove = offset + remove;
            if (afterRemove < fGapStart) {
                // removal is completely before the gap
                final int betweenSize = fGapStart - afterRemove;
                arrayCopy(afterRemove, newContent, newGapEnd, betweenSize);
                final int restSize = fContent.length - fGapEnd;
                arrayCopy(fGapEnd, newContent, newGapEnd + betweenSize, restSize);
            } else {
                // removal encompasses the gap
                afterRemove += oldGapSize;
                final int restSize = fContent.length - afterRemove;
                arrayCopy(afterRemove, newContent, newGapEnd, restSize);
            }
        } else {
            // gap comes before change
            arrayCopy(0, newContent, 0, fGapStart);
            final int offsetShifted = offset + oldGapSize;
            final int betweenSize = offsetShifted - fGapEnd;
            arrayCopy(fGapEnd, newContent, fGapStart, betweenSize);
            final int afterRemove = offsetShifted + remove;
            final int restSize = fContent.length - afterRemove;
            arrayCopy(afterRemove, newContent, newGapEnd, restSize);
        }

        fContent = newContent;
        return newGapEnd;
    }

    /**
     * Allocates a new <code>char[size]</code>.
     *
     * @param size
     *         the length of the new array.
     * @return a newly allocated char array
     * @since 3.3
     */
    private char[] allocate(int size) {
        return new char[size];
    }

    /*
     * Executes System.arraycopy if length != 0. A length < 0 cannot happen -> don't hide coding errors by checking for negative
     * lengths.
     * @since 3.3
     */
    private void arrayCopy(int srcPos, char[] dest, int destPos, int length) {
        if (length != 0) {
            System.arraycopy(fContent, srcPos, dest, destPos, length);
        }
    }

    /**
     * Returns the gap size.
     *
     * @return the gap size
     * @since 3.3
     */
    private int gapSize() {
        return fGapEnd - fGapStart;
    }

    /**
     * Returns a copy of the content of this text store. For internal use only.
     *
     * @return a copy of the content of this text store
     */
    protected String getContentAsString() {
        return new String(fContent);
    }

    /**
     * Returns the start index of the gap managed by this text store. For internal use only.
     *
     * @return the start index of the gap managed by this text store
     */
    protected int getGapStartIndex() {
        return fGapStart;
    }

    /**
     * Returns the end index of the gap managed by this text store. For internal use only.
     *
     * @return the end index of the gap managed by this text store
     */
    protected int getGapEndIndex() {
        return fGapEnd;
    }
}
