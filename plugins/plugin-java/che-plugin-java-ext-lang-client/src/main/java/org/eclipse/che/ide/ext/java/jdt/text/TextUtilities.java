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

import org.eclipse.che.ide.api.editor.text.BadLocationException;
import org.eclipse.che.ide.api.editor.text.Region;
import org.eclipse.che.ide.api.editor.text.TypedRegion;
import org.eclipse.che.ide.api.editor.text.TypedRegionImpl;
import org.eclipse.che.ide.runtime.Assert;
import org.eclipse.che.ide.util.loging.Log;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * A collection of text functions.
 * <p>
 * This class is neither intended to be instantiated nor subclassed.
 * </p>
 *
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class TextUtilities {

    /** Default line delimiters used by the text functions of this class. */
    public final static String[] DELIMITERS = new String[]{"\n", "\r", "\r\n"}; //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$

    /**
     * Determines which one of default line delimiters appears first in the list. If none of them the hint is returned.
     *
     * @param text
     *         the text to be checked
     * @param hint
     *         the line delimiter hint
     * @return the line delimiter
     */
    public static String determineLineDelimiter(String text, String hint) {
        try {
            int[] info = indexOf(DELIMITERS, text, 0);
            return DELIMITERS[info[1]];
        } catch (ArrayIndexOutOfBoundsException x) {
        }
        return hint;
    }

    /**
     * Returns the starting position and the index of the first matching search string in the given text that is greater than the
     * given offset. If more than one search string matches with the same starting position then the longest one is returned.
     *
     * @param searchStrings
     *         the strings to search for
     * @param text
     *         the text to be searched
     * @param offset
     *         the offset at which to start the search
     * @return an <code>int[]</code> with two elements where the first is the starting offset, the second the index of the found
     * search string in the given <code>searchStrings</code> array, returns <code>[-1, -1]</code> if no match exists
     */
    public static int[] indexOf(String[] searchStrings, String text, int offset) {

        int[] result = {-1, -1};
        int zeroIndex = -1;

        for (int i = 0; i < searchStrings.length; i++) {

            int length = searchStrings[i].length();

            if (length == 0) {
                zeroIndex = i;
                continue;
            }

            int index = text.indexOf(searchStrings[i], offset);
            if (index >= 0) {

                if (result[0] == -1) {
                    result[0] = index;
                    result[1] = i;
                } else if (index < result[0]) {
                    result[0] = index;
                    result[1] = i;
                } else if (index == result[0] && length > searchStrings[result[1]].length()) {
                    result[0] = index;
                    result[1] = i;
                }
            }
        }

        if (zeroIndex > -1 && result[0] == -1) {
            result[0] = 0;
            result[1] = zeroIndex;
        }

        return result;
    }

    /**
     * Returns the index of the longest search string with which the given text ends or <code>-1</code> if none matches.
     *
     * @param searchStrings
     *         the strings to search for
     * @param text
     *         the text to search
     * @return the index in <code>searchStrings</code> of the longest string with which <code>text</code> ends or <code>-1</code>
     */
    public static int endsWith(String[] searchStrings, String text) {

        int index = -1;

        for (int i = 0; i < searchStrings.length; i++) {
            if (text.endsWith(searchStrings[i])) {
                if (index == -1 || searchStrings[i].length() > searchStrings[index].length())
                    index = i;
            }
        }

        return index;
    }

    /**
     * Returns the index of the longest search string with which the given text starts or <code>-1</code> if none matches.
     *
     * @param searchStrings
     *         the strings to search for
     * @param text
     *         the text to search
     * @return the index in <code>searchStrings</code> of the longest string with which <code>text</code> starts or <code>-1</code>
     */
    public static int startsWith(String[] searchStrings, String text) {

        int index = -1;

        for (int i = 0; i < searchStrings.length; i++) {
            if (text.startsWith(searchStrings[i])) {
                if (index == -1 || searchStrings[i].length() > searchStrings[index].length())
                    index = i;
            }
        }

        return index;
    }

    /**
     * Returns the index of the first compare string that isEquals the given text or <code>-1</code> if none is equal.
     *
     * @param compareStrings
     *         the strings to compare with
     * @param text
     *         the text to check
     * @return the index of the first equal compare string or <code>-1</code>
     */
    public static int isEquals(String[] compareStrings, String text) {
        for (int i = 0; i < compareStrings.length; i++) {
            if (text.equals(compareStrings[i]))
                return i;
        }
        return -1;
    }

    /**
     * Returns a document event which is an accumulation of a list of document events, <code>null</code> if the list of
     * documentEvents is empty. The document of the document events are ignored.
     *
     * @param unprocessedDocument
     *         the document to which the document events would be applied
     * @param documentEvents
     *         the list of document events to merge
     * @return returns the merged document event
     * @throws BadLocationException
     *         might be thrown if document is not in the correct state with respect to document events
     */
    public static DocumentEvent mergeUnprocessedDocumentEvents(Document unprocessedDocument, List<DocumentEvent> documentEvents)
            throws BadLocationException {

        if (documentEvents.size() == 0)
            return null;

        final Iterator<DocumentEvent> iterator = documentEvents.iterator();
        final DocumentEvent firstEvent = iterator.next();

        // current merged event
        final Document document = unprocessedDocument;
        int offset = firstEvent.getOffset();
        int length = firstEvent.getLength();
        final StringBuffer text = new StringBuffer(firstEvent.getText() == null ? "" : firstEvent.getText()); //$NON-NLS-1$

        while (iterator.hasNext()) {

            final int delta = text.length() - length;

            final DocumentEvent event = (DocumentEvent)iterator.next();
            final int eventOffset = event.getOffset();
            final int eventLength = event.getLength();
            final String eventText = event.getText() == null ? "" : event.getText(); //$NON-NLS-1$

            // event is right from merged event
            if (eventOffset > offset + length + delta) {
                final String string = document.get(offset + length, (eventOffset - delta) - (offset + length));
                text.append(string);
                text.append(eventText);

                length = (eventOffset - delta) + eventLength - offset;

                // event is left from merged event
            } else if (eventOffset + eventLength < offset) {
                final String string = document.get(eventOffset + eventLength, offset - (eventOffset + eventLength));
                text.insert(0, string);
                text.insert(0, eventText);

                length = offset + length - eventOffset;
                offset = eventOffset;

                // events overlap each other
            } else {
                final int start = Math.max(0, eventOffset - offset);
                final int end = Math.min(text.length(), eventLength + eventOffset - offset);
                text.replace(start, end, eventText);

                offset = Math.min(offset, eventOffset);
                final int totalDelta = delta + eventText.length() - eventLength;
                length = text.length() - totalDelta;
            }
        }

        return new DocumentEvent(document, offset, length, text.toString());
    }

    /**
     * Returns a document event which is an accumulation of a list of document events, <code>null</code> if the list of document
     * events is empty. The document events being merged must all refer to the same document, to which the document changes have
     * been already applied.
     *
     * @param documentEvents
     *         the list of document events to merge
     * @return returns the merged document event
     * @throws BadLocationException
     *         might be thrown if document is not in the correct state with respect to document events
     */
    public static DocumentEvent mergeProcessedDocumentEvents(List<DocumentEvent> documentEvents) throws BadLocationException {

        if (documentEvents.size() == 0)
            return null;

        final ListIterator<DocumentEvent> iterator = documentEvents.listIterator(documentEvents.size());
        final DocumentEvent firstEvent = iterator.previous();

        // current merged event
        final Document document = firstEvent.getDocument();
        int offset = firstEvent.getOffset();
        int length = firstEvent.getLength();
        int textLength = firstEvent.getText() == null ? 0 : firstEvent.getText().length();

        while (iterator.hasPrevious()) {

            final int delta = length - textLength;

            final DocumentEvent event = (DocumentEvent)iterator.previous();
            final int eventOffset = event.getOffset();
            final int eventLength = event.getLength();
            final int eventTextLength = event.getText() == null ? 0 : event.getText().length();

            // event is right from merged event
            if (eventOffset > offset + textLength + delta) {
                length = (eventOffset - delta) - (offset + textLength) + length + eventLength;
                textLength = (eventOffset - delta) + eventTextLength - offset;

                // event is left from merged event
            } else if (eventOffset + eventTextLength < offset) {
                length = offset - (eventOffset + eventTextLength) + length + eventLength;
                textLength = offset + textLength - eventOffset;
                offset = eventOffset;

                // events overlap each other
            } else {
                final int start = Math.max(0, eventOffset - offset);
                final int end = Math.min(length, eventTextLength + eventOffset - offset);
                length += eventLength - (end - start);

                offset = Math.min(offset, eventOffset);
                final int totalDelta = delta + eventLength - eventTextLength;
                textLength = length - totalDelta;
            }
        }

        final String text = document.get(offset, textLength);
        return new DocumentEvent(document, offset, length, text);
    }

    /**
     * Removes all connected document partitioners from the given document and stores them under their partitioning name in a map.
     * This map is returned. After this method has been called the given document is no longer connected to any document
     * partitioner.
     *
     * @param document
     *         the document
     * @return the map containing the removed partitioners (key type: {@link String}, value type: {@link DocumentPartitioner})
     */
    public static Map<String, DocumentPartitioner> removeDocumentPartitioners(Document document) {
        Map<String, DocumentPartitioner> partitioners = new HashMap<String, DocumentPartitioner>();

        String[] partitionings = document.getPartitionings();
        for (int i = 0; i < partitionings.length; i++) {
            DocumentPartitioner partitioner = document.getDocumentPartitioner(partitionings[i]);
            if (partitioner != null) {
                document.setDocumentPartitioner(partitionings[i], null);
                partitioner.disconnect();
                partitioners.put(partitionings[i], partitioner);
            }
        }
        //      }
        //      else
        //      {
        //         IDocumentPartitioner partitioner = document.getDocumentPartitioner();
        //         if (partitioner != null)
        //         {
        //            document.setDocumentPartitioner(null);
        //            partitioner.disconnect();
        //            partitioners.put(IDocumentExtension3.DEFAULT_PARTITIONING, partitioner);
        //         }
        //      }
        return partitioners;
    }

    /**
     * Connects the given document with all document partitioners stored in the given map under their partitioning name. This
     * method cleans the given map.
     *
     * @param document
     *         the document
     * @param partitioners
     *         the map containing the partitioners to be connected (key type: {@link String}, value type:
     *         {@link DocumentPartitioner})
     */
    public static void addDocumentPartitioners(Document document, Map<String, DocumentPartitioner> partitioners) {
        Iterator<String> e = partitioners.keySet().iterator();
        while (e.hasNext()) {
            String partitioning = e.next();
            DocumentPartitioner partitioner = partitioners.get(partitioning);
            partitioner.connect(document);
            document.setDocumentPartitioner(partitioning, partitioner);
        }
        partitioners.clear();
        //      }
        //      else
        //      {
        //         IDocumentPartitioner partitioner =
        //            (IDocumentPartitioner)partitioners.get(IDocumentExtension3.DEFAULT_PARTITIONING);
        //         partitioner.connect(document);
        //         document.setDocumentPartitioner(partitioner);
        //      }
    }

    /**
     * Returns the content type at the given offset of the given document.
     *
     * @param document
     *         the document
     * @param partitioning
     *         the partitioning to be used
     * @param offset
     *         the offset
     * @param preferOpenPartitions
     *         <code>true</code> if precedence should be given to a open partition ending at
     *         <code>offset</code> over a closed partition starting at <code>offset</code>
     * @return the content type at the given offset of the document
     * @throws BadLocationException
     *         if offset is invalid in the document
     */
    public static String getContentType(Document document, String partitioning, int offset, boolean preferOpenPartitions)
            throws BadLocationException {
        try {
            return document.getContentType(partitioning, offset, preferOpenPartitions);
        } catch (BadPartitioningException x) {
            return Document.DEFAULT_CONTENT_TYPE;
        }

        //      return document.getContentType(offset);
    }

    /**
     * Returns the partition of the given offset of the given document.
     *
     * @param document
     *         the document
     * @param partitioning
     *         the partitioning to be used
     * @param offset
     *         the offset
     * @param preferOpenPartitions
     *         <code>true</code> if precedence should be given to a open partition ending at
     *         <code>offset</code> over a closed partition starting at <code>offset</code>
     * @return the content type at the given offset of this viewer's input document
     * @throws BadLocationException
     *         if offset is invalid in the given document
     */
    public static TypedRegion getPartition(Document document, String partitioning, int offset,
                                           boolean preferOpenPartitions) throws BadLocationException {
        try {
            return document.getPartition(partitioning, offset, preferOpenPartitions);
        } catch (BadPartitioningException x) {
            return new TypedRegionImpl(0, document.getLength(), Document.DEFAULT_CONTENT_TYPE);
        }
        //      return document.getPartition(offset);
    }

    /**
     * Computes and returns the partitioning for the given region of the given document for the given partitioning name.
     *
     * @param document
     *         the document
     * @param partitioning
     *         the partitioning name
     * @param offset
     *         the region offset
     * @param length
     *         the region length
     * @param includeZeroLengthPartitions
     *         whether to include zero-length partitions
     * @return the partitioning for the given region of the given document for the given partitioning name
     * @throws BadLocationException
     *         if the given region is invalid for the given document
     */
    public static TypedRegion[] computePartitioning(Document document, String partitioning, int offset, int length,
                                                    boolean includeZeroLengthPartitions) throws BadLocationException {
        try {
            return document.computePartitioning(partitioning, offset, length, includeZeroLengthPartitions);
        } catch (BadPartitioningException x) {
            return new TypedRegion[0];
        }

        //      return document.computePartitioning(offset, length);
    }

    // /**
    // * Computes and returns the partition managing position categories for the given document or <code>null</code> if this was
    // * impossible.
    // *
    // * @param document the document
    // * @return the partition managing position categories or <code>null</code>
    // * @since 3.0
    // */
    // public static String[] computePartitionManagingCategories(IDocument document)
    // {
    // if (document instanceof IDocumentExtension3)
    // {
    // IDocumentExtension3 extension3 = (IDocumentExtension3)document;
    // String[] partitionings = extension3.getPartitionings();
    // if (partitionings != null)
    // {
    // Set categories = new HashSet();
    // for (int i = 0; i < partitionings.length; i++)
    // {
    // IDocumentPartitioner p = extension3.getDocumentPartitioner(partitionings[i]);
    // if (p instanceof IDocumentPartitionerExtension2)
    // {
    // IDocumentPartitionerExtension2 extension2 = (IDocumentPartitionerExtension2)p;
    // String[] c = extension2.getManagingPositionCategories();
    // if (c != null)
    // {
    // for (int j = 0; j < c.length; j++)
    // categories.add(c[j]);
    // }
    // }
    // }
    // String[] result = new String[categories.size()];
    // categories.toArray(result);
    // return result;
    // }
    // }
    // return null;
    // }

    /**
     * Returns the default line delimiter for the given document. This is either the delimiter of the first line, or the platform
     * line delimiter if it is a legal line delimiter or the first one of the legal line delimiters. The default line delimiter
     * should be used when performing document manipulations that span multiple lines.
     *
     * @param document
     *         the document
     * @return the document's default line delimiter
     */
    public static String getDefaultLineDelimiter(Document document) {

        // if (document instanceof IDocumentExtension4)
        // return ((IDocumentExtension4)document).getDefaultLineDelimiter();

        String lineDelimiter = null;

        try {
            lineDelimiter = document.getLineDelimiter(0);
        } catch (BadLocationException x) {
        }

        if (lineDelimiter != null)
            return lineDelimiter;

        String sysLineDelimiter = "\n"; //System.getProperty("line.separator"); //$NON-NLS-1$
        String[] delimiters = document.getLegalLineDelimiters();
        Assert.isTrue(delimiters.length > 0);
        for (int i = 0; i < delimiters.length; i++) {
            if (delimiters[i].equals(sysLineDelimiter)) {
                lineDelimiter = sysLineDelimiter;
                break;
            }
        }

        if (lineDelimiter == null)
            lineDelimiter = delimiters[0];

        return lineDelimiter;
    }

    /**
     * Returns <code>true</code> if the two regions overlap. Returns <code>false</code> if one of the arguments is
     * <code>null</code>.
     *
     * @param left
     *         the left region
     * @param right
     *         the right region
     * @return <code>true</code> if the two regions overlap, <code>false</code> otherwise
     */
    public static boolean overlaps(Region left, Region right) {

        if (left == null || right == null)
            return false;

        int rightEnd = right.getOffset() + right.getLength();
        int leftEnd = left.getOffset() + left.getLength();

        if (right.getLength() > 0) {
            if (left.getLength() > 0)
                return left.getOffset() < rightEnd && right.getOffset() < leftEnd;
            return right.getOffset() <= left.getOffset() && left.getOffset() < rightEnd;
        }

        if (left.getLength() > 0)
            return left.getOffset() <= right.getOffset() && right.getOffset() < leftEnd;

        return left.getOffset() == right.getOffset();
    }

    /**
     * Returns a copy of the given string array.
     *
     * @param array
     *         the string array to be copied
     * @return a copy of the given string array or <code>null</code> when <code>array</code> is <code>null</code>
     */
    public static String[] copy(String[] array) {
        if (array != null) {
            String[] copy = new String[array.length];
            System.arraycopy(array, 0, copy, 0, array.length);
            return copy;
        }
        return null;
    }

    /**
     * Returns a copy of the given integer array.
     *
     * @param array
     *         the integer array to be copied
     * @return a copy of the given integer array or <code>null</code> when <code>array</code> is <code>null</code>
     */
    public static int[] copy(int[] array) {
        if (array != null) {
            int[] copy = new int[array.length];
            System.arraycopy(array, 0, copy, 0, array.length);
            return copy;
        }
        return null;
    }

    /**
     * Return document offset by cursor position(line, column)
     *
     * @param document
     *         the document to offset
     * @param line
     *         the cursor line
     * @param column
     *         the cursor column
     * @return offset of cursor or <code>-1</code> if BadLocationException occurs
     */
    public static int getOffset(Document document, int line, int column) {
        try {
            int lineOffset = document.getLineOffset(line);
            return lineOffset + column;
        } catch (BadLocationException e) {
            Log.error(TextUtilities.class, e);
            return -1;
        }
    }

    /**
     * @param document
     * @param offset
     * @return
     */
    public static int getLineLineNumber(Document document, int offset) {
        try {
            return document.getLineOfOffset(offset);
        } catch (BadLocationException e) {
            return 0;
        }
    }
}
