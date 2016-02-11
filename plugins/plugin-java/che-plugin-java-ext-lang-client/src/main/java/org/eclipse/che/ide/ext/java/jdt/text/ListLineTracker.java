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

import org.eclipse.che.ide.api.text.BadLocationException;
import org.eclipse.che.ide.api.text.Region;
import org.eclipse.che.ide.api.text.RegionImpl;
import org.eclipse.che.ide.ext.java.jdt.text.AbstractLineTracker.DelimiterInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract, read-only implementation of <code>ILineTracker</code>. It lets the definition of line delimiters to subclasses.
 * Assuming that '\n' is the only line delimiter, this abstract implementation defines the following line scheme:
 * <ul>
 * <li>"" -> [0,0]
 * <li>"a" -> [0,1]
 * <li>"\n" -> [0,1], [1,0]
 * <li>"a\n" -> [0,2], [2,0]
 * <li>"a\nb" -> [0,2], [2,1]
 * <li>"a\nbc\n" -> [0,2], [2,3], [5,0]
 * </ul>
 * This class must be subclassed.
 */
abstract class ListLineTracker implements LineTracker {

    /** The line information */
    private final List<Line> fLines = new ArrayList<Line>();

    /** The length of the tracked text */
    private int fTextLength;

    /** Creates a new line tracker. */
    protected ListLineTracker() {
    }

    /**
     * Binary search for the line at a given offset.
     *
     * @param offset
     *         the offset whose line should be found
     * @return the line of the offset
     */
    private int findLine(int offset) {

        if (fLines.size() == 0)
            return -1;

        int left = 0;
        int right = fLines.size() - 1;
        int mid = 0;
        Line line = null;

        while (left < right) {

            mid = (left + right) / 2;

            line = (Line)fLines.get(mid);
            if (offset < line.offset) {
                if (left == mid)
                    right = left;
                else
                    right = mid - 1;
            } else if (offset > line.offset) {
                if (right == mid)
                    left = right;
                else
                    left = mid + 1;
            } else if (offset == line.offset) {
                left = right = mid;
            }
        }

        line = (Line)fLines.get(left);
        if (line.offset > offset)
            --left;
        return left;
    }

    /**
     * Returns the number of lines covered by the specified text range.
     *
     * @param startLine
     *         the line where the text range starts
     * @param offset
     *         the start offset of the text range
     * @param length
     *         the length of the text range
     * @return the number of lines covered by this text range
     * @throws BadLocationException
     *         if range is undefined in this tracker
     */
    private int getNumberOfLines(int startLine, int offset, int length) throws BadLocationException {

        if (length == 0)
            return 1;

        int target = offset + length;

        Line l = (Line)fLines.get(startLine);

        if (l.delimiter == null)
            return 1;

        if (l.offset + l.length > target)
            return 1;

        if (l.offset + l.length == target)
            return 2;

        return getLineNumberOfOffset(target) - startLine + 1;
    }

    /* @see org.eclipse.jface.text.ILineTracker#getLineLength(int) */
    public final int getLineLength(int line) throws BadLocationException {
        int lines = fLines.size();

        if (line < 0 || line > lines)
            throw new BadLocationException();

        if (lines == 0 || lines == line)
            return 0;

        Line l = (Line)fLines.get(line);
        return l.length;
    }

    /* @see org.eclipse.jface.text.ILineTracker#getLineNumberOfOffset(int) */
    public final int getLineNumberOfOffset(int position) throws BadLocationException {
        if (position < 0 || position > fTextLength)
            throw new BadLocationException();

        if (position == fTextLength) {

            int lastLine = fLines.size() - 1;
            if (lastLine < 0)
                return 0;

            Line l = (Line)fLines.get(lastLine);
            return (l.delimiter != null ? lastLine + 1 : lastLine);
        }

        return findLine(position);
    }

    /* @see org.eclipse.jface.text.ILineTracker#getLineInformationOfOffset(int) */
    public final Region getLineInformationOfOffset(int position) throws BadLocationException {
        if (position > fTextLength)
            throw new BadLocationException();

        if (position == fTextLength) {
            int size = fLines.size();
            if (size == 0)
                return new RegionImpl(0, 0);
            Line l = (Line)fLines.get(size - 1);
            return (l.delimiter != null ? new Line(fTextLength, 0) : new Line(fTextLength - l.length, l.length));
        }

        return getLineInformation(findLine(position));
    }

    /* @see org.eclipse.jface.text.ILineTracker#getLineInformation(int) */
    public final Region getLineInformation(int line) throws BadLocationException {
        int lines = fLines.size();

        if (line < 0 || line > lines)
            throw new BadLocationException();

        if (lines == 0)
            return new Line(0, 0);

        if (line == lines) {
            Line l = (Line)fLines.get(line - 1);
            return new Line(l.offset + l.length, 0);
        }

        Line l = (Line)fLines.get(line);
        return (l.delimiter != null ? new Line(l.offset, l.length - l.delimiter.length()) : l);
    }

    /* @see org.eclipse.jface.text.ILineTracker#getLineOffset(int) */
    public final int getLineOffset(int line) throws BadLocationException {
        int lines = fLines.size();

        if (line < 0 || line > lines)
            throw new BadLocationException();

        if (lines == 0)
            return 0;

        if (line == lines) {
            Line l = (Line)fLines.get(line - 1);
            if (l.delimiter != null)
                return l.offset + l.length;
            throw new BadLocationException();
        }

        Line l = (Line)fLines.get(line);
        return l.offset;
    }

    /* @see org.eclipse.jface.text.ILineTracker#getNumberOfLines() */
    public final int getNumberOfLines() {
        int lines = fLines.size();

        if (lines == 0)
            return 1;

        Line l = (Line)fLines.get(lines - 1);
        return (l.delimiter != null ? lines + 1 : lines);
    }

    /* @see org.eclipse.jface.text.ILineTracker#getNumberOfLines(int, int) */
    public final int getNumberOfLines(int position, int length) throws BadLocationException {

        if (position < 0 || position + length > fTextLength)
            throw new BadLocationException();

        if (length == 0) // optimization
            return 1;

        return getNumberOfLines(getLineNumberOfOffset(position), position, length);
    }

    /*
     * @see org.eclipse.jface.text.ILineTracker#computeNumberOfLines(java.lang.String)
     */
    public final int computeNumberOfLines(String text) {
        int count = 0;
        int start = 0;
        DelimiterInfo delimiterInfo = nextDelimiterInfo(text, start);
        while (delimiterInfo != null && delimiterInfo.delimiterIndex > -1) {
            ++count;
            start = delimiterInfo.delimiterIndex + delimiterInfo.delimiterLength;
            delimiterInfo = nextDelimiterInfo(text, start);
        }
        return count;
    }

    /* @see org.eclipse.jface.text.ILineTracker#getLineDelimiter(int) */
    public final String getLineDelimiter(int line) throws BadLocationException {
        int lines = fLines.size();

        if (line < 0 || line > lines)
            throw new BadLocationException();

        if (lines == 0)
            return null;

        if (line == lines)
            return null;

        Line l = (Line)fLines.get(line);
        return l.delimiter;
    }

    /**
     * Returns the information about the first delimiter found in the given text starting at the given offset.
     *
     * @param text
     *         the text to be searched
     * @param offset
     *         the offset in the given text
     * @return the information of the first found delimiter or <code>null</code>
     */
    protected abstract DelimiterInfo nextDelimiterInfo(String text, int offset);

    /**
     * Creates the line structure for the given text. Newly created lines are inserted into the line structure starting at the
     * given position. Returns the number of newly created lines.
     *
     * @param text
     *         the text for which to create a line structure
     * @param insertPosition
     *         the position at which the newly created lines are inserted into the tracker's line structure
     * @param offset
     *         the offset of all newly created lines
     * @return the number of newly created lines
     */
    private int createLines(String text, int insertPosition, int offset) {

        int count = 0;
        int start = 0;
        DelimiterInfo delimiterInfo = nextDelimiterInfo(text, 0);

        while (delimiterInfo != null && delimiterInfo.delimiterIndex > -1) {

            int index = delimiterInfo.delimiterIndex + (delimiterInfo.delimiterLength - 1);

            if (insertPosition + count >= fLines.size())
                fLines.add(new Line(offset + start, offset + index, delimiterInfo.delimiter));
            else
                fLines.add(insertPosition + count, new Line(offset + start, offset + index, delimiterInfo.delimiter));

            ++count;
            start = index + 1;
            delimiterInfo = nextDelimiterInfo(text, start);
        }

        if (start < text.length()) {
            if (insertPosition + count < fLines.size()) {
                // there is a line below the current
                Line l = (Line)fLines.get(insertPosition + count);
                int delta = text.length() - start;
                l.offset -= delta;
                l.length += delta;
            } else {
                fLines.add(new Line(offset + start, offset + text.length() - 1, null));
                ++count;
            }
        }

        return count;
    }

    /*
     * @see org.eclipse.jface.text.ILineTracker#replace(int, int, java.lang.String)
     */
    public final void replace(int position, int length, String text) throws BadLocationException {
        throw new UnsupportedOperationException();
    }

    /* @see org.eclipse.jface.text.ILineTracker#set(java.lang.String) */
    public final void set(String text) {
        fLines.clear();
        if (text != null) {
            fTextLength = text.length();
            createLines(text, 0, 0);
        }
    }

    /**
     * Returns the internal data structure, a {@link List} of {@link Line}s. Used only by
     * {@link TreeLineTracker#TreeLineTracker(ListLineTracker)}.
     *
     * @return the internal list of lines.
     */
    final List<Line> getLines() {
        return fLines;
    }
}
