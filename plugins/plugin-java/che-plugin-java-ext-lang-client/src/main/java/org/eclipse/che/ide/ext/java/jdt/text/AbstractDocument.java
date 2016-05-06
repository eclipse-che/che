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
import org.eclipse.che.ide.api.editor.text.BadPositionCategoryException;
import org.eclipse.che.ide.api.editor.text.Position;
import org.eclipse.che.ide.api.editor.text.Region;
import org.eclipse.che.ide.api.editor.text.TypedRegion;
import org.eclipse.che.ide.api.editor.text.TypedRegionImpl;
import org.eclipse.che.ide.runtime.Assert;
import com.google.gwt.core.client.JavaScriptException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Abstract default implementation of <code>IDocument</code> and its extension interfaces
 * {@link org.eclipse.jface.text.IDocumentExtension}, {@link org.eclipse.jface.text.IDocumentExtension2},
 * {@link org.eclipse.jface.text.IDocumentExtension3}, {@link org.eclipse.jface.text.IDocumentExtension4}, as well as
 * {@link org.eclipse.jface.text.IRepairableDocument}.
 * <p/>
 * <p/>
 * An <code>AbstractDocument</code> supports the following implementation plug-ins:
 * <ul>
 * <li>a text store implementing {@link TextStore.text.ITextStore} for storing and managing the document's content,</li>
 * <li>a line tracker implementing {@link org.eclipse.che.ide.legacy.client.api.text.eclipse.LineTracker.text.ILineTracker} to map character positions to line numbers and vice
 * versa</li>
 * </ul>
 * The document can dynamically change the text store when switching between sequential rewrite mode and normal mode.
 * <p/>
 * <p/>
 * This class must be subclassed. Subclasses must configure which implementation plug-ins the document instance should use.
 * Subclasses are not intended to overwrite existing methods.
 *
 * @see TextStore.text.ITextStore
 * @see org.eclipse.che.ide.legacy.client.api.text.eclipse.LineTracker.text.ILineTracker
 */
public abstract class AbstractDocument implements Document {

    /** The document's text store */
    private TextStore fStore;

    /** The document's line tracker */
    private LineTracker fTracker;

    /** The registered document listeners */
    private ListenerList fDocumentListeners;

    /** The registered pre-notified document listeners */
    private ListenerList fPrenotifiedDocumentListeners;

    /** The registered document partitioning listeners */
    private ListenerList fDocumentPartitioningListeners;

    /** All positions managed by the document ordered by their start positions. */
    private Map<String, List<Position>> fPositions;

    /** All positions managed by the document ordered by there end positions. */
    private Map<String, List<Position>> fEndPositions;

    /** All registered document position updaters */
    private List<PositionUpdater> fPositionUpdaters;

    /** Indicates whether post notification change processing has been stopped. */
    private int fStoppedCount = 0;

    /** Indicates whether the registration of post notification changes should be ignored. */
    private boolean fAcceptPostNotificationReplaces = true;

    /** Indicates whether the notification of listeners has been stopped. */
    private int fStoppedListenerNotification = 0;

    /** The document event to be sent after listener notification has been resumed. */
    private DocumentEvent fDeferredDocumentEvent;

    /** The registered document partitioners. */
    private Map<String, DocumentPartitioner> fDocumentPartitioners;

    /** The partitioning changed event. */
    private DocumentPartitioningChangedEvent fDocumentPartitioningChangedEvent;

    /**
     * The find/replace document adapter.
     *
     * @since 3.0
     */
    private FindReplaceDocumentAdapter fFindReplaceDocumentAdapter;

    /**
     * The current modification stamp.
     *
     * @since 3.1
     */
    private long fModificationStamp = UNKNOWN_MODIFICATION_STAMP;

    /**
     * Keeps track of next modification stamp.
     *
     * @since 3.1.1
     */
    private long fNextModificationStamp = UNKNOWN_MODIFICATION_STAMP;

    /**
     * This document's default line delimiter.
     *
     * @since 3.1
     */
    private String fInitialLineDelimiter;

    /**
     * The default constructor does not perform any configuration but leaves it to the clients who must first initialize the
     * implementation plug-ins and then call <code>completeInitialization</code>. Results in the construction of an empty document.
     */
    protected AbstractDocument() {
        fModificationStamp = getNextModificationStamp();
    }

    /**
     * Returns the document's text store. Assumes that the document has been initialized with a text store.
     *
     * @return the document's text store
     */
    protected TextStore getStore() {
        Assert.isNotNull(fStore);
        return fStore;
    }

    /**
     * Returns the document's line tracker. Assumes that the document has been initialized with a line tracker.
     *
     * @return the document's line tracker
     */
    protected LineTracker getTracker() {
        Assert.isNotNull(fTracker);
        return fTracker;
    }

    /**
     * Returns the document's document listeners.
     *
     * @return the document's document listeners
     */
    protected List<Object> getDocumentListeners() {
        return Arrays.asList(fDocumentListeners.getListeners());
    }

    /**
     * Returns the document's partitioning listeners.
     *
     * @return the document's partitioning listeners
     */
    protected List getDocumentPartitioningListeners() {
        return Arrays.asList(fDocumentPartitioningListeners.getListeners());
    }

    /**
     * Returns all positions managed by the document grouped by category.
     *
     * @return the document's positions
     */
    protected Map<String, List<Position>> getDocumentManagedPositions() {
        return fPositions;
    }

    /* @see org.eclipse.jface.text.IDocument#getDocumentPartitioner() */
    public DocumentPartitioner getDocumentPartitioner() {
        return getDocumentPartitioner(DEFAULT_PARTITIONING);
    }

    // --- implementation configuration interface ------------

    /**
     * Sets the document's text store. Must be called at the beginning of the constructor.
     *
     * @param store
     *         the document's text store
     */
    protected void setTextStore(TextStore store) {
        fStore = store;
    }

    /**
     * Sets the document's line tracker. Must be called at the beginning of the constructor.
     *
     * @param tracker
     *         the document's line tracker
     */
    protected void setLineTracker(LineTracker tracker) {
        fTracker = tracker;
    }

    /*
     * @see org.eclipse.jface.text.IDocument#setDocumentPartitioner(org.eclipse.jface .text.IDocumentPartitioner)
     */
    public void setDocumentPartitioner(DocumentPartitioner partitioner) {
        setDocumentPartitioner(DEFAULT_PARTITIONING, partitioner);
    }

    /**
     * Initializes document listeners, positions, and position updaters. Must be called inside the constructor after the
     * implementation plug-ins have been set.
     */
    protected void completeInitialization() {

        fPositions = new HashMap<String, List<Position>>();
        fEndPositions = new HashMap<String, List<Position>>();
        fPositionUpdaters = new ArrayList<PositionUpdater>();
        fDocumentListeners = new ListenerList(ListenerList.IDENTITY);
        fPrenotifiedDocumentListeners = new ListenerList(ListenerList.IDENTITY);
        fDocumentPartitioningListeners = new ListenerList(ListenerList.IDENTITY);
        //      fDocumentRewriteSessionListeners = new ArrayList();

        addPositionCategory(DEFAULT_CATEGORY);
        addPositionUpdater(new DefaultPositionUpdater(DEFAULT_CATEGORY));
    }

    // -------------------------------------------------------

    /*
     * @see org.eclipse.jface.text.IDocument#addDocumentListener(org.eclipse.jface .text.IDocumentListener)
     */
    public void addDocumentListener(DocumentListener listener) {
        Assert.isNotNull(listener);
        fDocumentListeners.add(listener);
    }

    /*
     * @see org.eclipse.jface.text.IDocument#removeDocumentListener(org.eclipse.jface .text.IDocumentListener)
     */
    public void removeDocumentListener(DocumentListener listener) {
        Assert.isNotNull(listener);
        fDocumentListeners.remove(listener);
    }

    /*
     * @see org.eclipse.jface.text.IDocument#addPrenotifiedDocumentListener(org.eclipse .jface.text.IDocumentListener)
     */
    public void addPrenotifiedDocumentListener(DocumentListener listener) {
        Assert.isNotNull(listener);
        fPrenotifiedDocumentListeners.add(listener);
    }

    /*
     * @see org.eclipse.jface.text.IDocument#removePrenotifiedDocumentListener(org .eclipse.jface.text.IDocumentListener)
     */
    public void removePrenotifiedDocumentListener(DocumentListener listener) {
        Assert.isNotNull(listener);
        fPrenotifiedDocumentListeners.remove(listener);
    }

    /*
     * @see org.eclipse.jface.text.IDocument#addDocumentPartitioningListener(org.eclipse .jface.text.IDocumentPartitioningListener)
     */
    public void addDocumentPartitioningListener(DocumentPartitioningListener listener) {
        Assert.isNotNull(listener);
        fDocumentPartitioningListeners.add(listener);
    }

    /*
     * @see org.eclipse.jface.text.IDocument#removeDocumentPartitioningListener(org
     * .eclipse.jface.text.IDocumentPartitioningListener)
     */
    public void removeDocumentPartitioningListener(DocumentPartitioningListener listener) {
        Assert.isNotNull(listener);
        fDocumentPartitioningListeners.remove(listener);
    }

    /*
     * @see org.eclipse.jface.text.IDocument#addPosition(java.lang.String, org.eclipse.jface.text.Position)
     */
    public void addPosition(String category, Position position) throws BadLocationException,
                                                                       BadPositionCategoryException {

        if ((0 > position.offset) || (0 > position.length) || (position.offset + position.length > getLength()))
            throw new BadLocationException();

        if (category == null)
            throw new BadPositionCategoryException();

        List<Position> list = fPositions.get(category);
        if (list == null)
            throw new BadPositionCategoryException();
        list.add(computeIndexInPositionList(list, position.offset, true), position);

        List<Position> endPositions = fEndPositions.get(category);
        if (endPositions == null)
            throw new BadPositionCategoryException();
        endPositions
                .add(computeIndexInPositionList(endPositions, position.offset + position.length - 1, false), position);
    }

    /*
     * @see org.eclipse.jface.text.IDocument#addPosition(org.eclipse.jface.text.Position )
     */
    public void addPosition(Position position) throws BadLocationException {
        try {
            addPosition(DEFAULT_CATEGORY, position);
        } catch (BadPositionCategoryException e) {
        }
    }

    /*
     * @see org.eclipse.jface.text.IDocument#addPositionCategory(java.lang.String)
     */
    public void addPositionCategory(String category) {

        if (category == null)
            return;

        if (!containsPositionCategory(category)) {
            fPositions.put(category, new ArrayList<Position>());
            fEndPositions.put(category, new ArrayList<Position>());
        }
    }

    /*
     * @see org.eclipse.jface.text.IDocument#addPositionUpdater(org.eclipse.jface. text.IPositionUpdater)
     */
    public void addPositionUpdater(PositionUpdater updater) {
        insertPositionUpdater(updater, fPositionUpdaters.size());
    }

    /*
     * @see org.eclipse.jface.text.IDocument#containsPosition(java.lang.String, int, int)
     */
    public boolean containsPosition(String category, int offset, int length) {

        if (category == null)
            return false;

        List<Position> list = fPositions.get(category);
        if (list == null)
            return false;

        int size = list.size();
        if (size == 0)
            return false;

        int index = computeIndexInPositionList(list, offset, true);
        if (index < size) {
            Position p = (Position)list.get(index);
            while (p != null && p.offset == offset) {
                if (p.length == length)
                    return true;
                ++index;
                p = (index < size) ? (Position)list.get(index) : null;
            }
        }

        return false;
    }

    /*
     * @see org.eclipse.jface.text.IDocument#containsPositionCategory(java.lang.String )
     */
    public boolean containsPositionCategory(String category) {
        if (category != null)
            return fPositions.containsKey(category);
        return false;
    }


    /**
     * Computes the index in the list of positions at which a position with the given position would be inserted. The position to
     * insert is supposed to become the first in this list of all positions with the same position.
     *
     * @param positions
     *         the list in which the index is computed
     * @param offset
     *         the offset for which the index is computed
     * @param orderedByOffset
     *         <code>true</code> if ordered by offset, false if ordered by end position
     * @return the computed index
     * @since 3.4
     */
    protected int computeIndexInPositionList(List<Position> positions, int offset, boolean orderedByOffset) {
        if (positions.size() == 0)
            return 0;

        int left = 0;
        int right = positions.size() - 1;
        int mid = 0;
        Position p = null;

        while (left < right) {

            mid = (left + right) / 2;

            p = (Position)positions.get(mid);
            int pOffset = getOffset(orderedByOffset, p);
            if (offset < pOffset) {
                if (left == mid)
                    right = left;
                else
                    right = mid - 1;
            } else if (offset > pOffset) {
                if (right == mid)
                    left = right;
                else
                    left = mid + 1;
            } else if (offset == pOffset) {
                left = right = mid;
            }

        }

        int pos = left;
        p = (Position)positions.get(pos);
        int pPosition = getOffset(orderedByOffset, p);
        if (offset > pPosition) {
            // append to the end
            pos++;
        } else {
            // entry will become the first of all entries with the same offset
            do {
                --pos;
                if (pos < 0)
                    break;
                p = (Position)positions.get(pos);
                pPosition = getOffset(orderedByOffset, p);
            }
            while (offset == pPosition);
            ++pos;
        }

        Assert.isTrue(0 <= pos && pos <= positions.size());

        return pos;
    }

    /* @since 3.4 */
    private int getOffset(boolean orderedByOffset, Position position) {
        if (orderedByOffset || position.getLength() == 0)
            return position.getOffset();
        return position.getOffset() + position.getLength() - 1;
    }

    /*
     * @see org.eclipse.jface.text.IDocument#computeIndexInCategory(java.lang.String, int)
     */
    public int computeIndexInCategory(String category, int offset) throws BadLocationException,
                                                                          BadPositionCategoryException {

        if (0 > offset || offset > getLength())
            throw new BadLocationException();

        List<Position> c = fPositions.get(category);
        if (c == null)
            throw new BadPositionCategoryException();

        return computeIndexInPositionList(c, offset, true);
    }

    /**
     * Fires the document partitioning changed notification to all registered document partitioning listeners. Uses a robust
     * iterator.
     *
     * @param event
     *         the document partitioning changed event
     * @see DocumentPartitioningListener
     * @since 3.0
     */
    protected void fireDocumentPartitioningChanged(DocumentPartitioningChangedEvent event) {
        if (fDocumentPartitioningListeners == null)
            return;

        Object[] listeners = fDocumentPartitioningListeners.getListeners();
        for (int i = 0; i < listeners.length; i++) {
            DocumentPartitioningListener l = (DocumentPartitioningListener)listeners[i];
            try {
                l.documentPartitioningChanged(event);
            } catch (Exception ex) {
                fail(ex);
            }
        }
    }

    /**
     * Fires the given document event to all registers document listeners informing them about the forthcoming document
     * manipulation. Uses a robust iterator.
     *
     * @param event
     *         the event to be sent out
     */
    protected void fireDocumentAboutToBeChanged(DocumentEvent event) {
        if (fDocumentPartitioners != null) {
            Iterator<DocumentPartitioner> e = fDocumentPartitioners.values().iterator();
            while (e.hasNext()) {
                DocumentPartitioner p = e.next();
                try {
                    p.documentAboutToBeChanged(event);
                } catch (Exception ex) {
                    fail(ex);
                }
            }
        }

        Object[] listeners = fPrenotifiedDocumentListeners.getListeners();
        for (int i = 0; i < listeners.length; i++) {
            try {
                ((DocumentListener)listeners[i]).documentAboutToBeChanged(event);
            } catch (Exception ex) {
                fail(ex);
            }
        }

        listeners = fDocumentListeners.getListeners();
        for (int i = 0; i < listeners.length; i++) {
            try {
                ((DocumentListener)listeners[i]).documentAboutToBeChanged(event);
            } catch (Exception ex) {
                fail(ex);
            }
        }

    }

    /**
     * Updates document partitioning and document positions according to the specification given by the document event.
     *
     * @param event
     *         the document event describing the change to which structures must be adapted
     */
    protected void updateDocumentStructures(DocumentEvent event) {

        if (fDocumentPartitioners != null) {
            fDocumentPartitioningChangedEvent = new DocumentPartitioningChangedEvent(this);
            Iterator<String> e = fDocumentPartitioners.keySet().iterator();
            while (e.hasNext()) {
                String partitioning = e.next();
                DocumentPartitioner partitioner = (DocumentPartitioner)fDocumentPartitioners.get(partitioning);

                if (partitioner.documentChanged(event))
                    fDocumentPartitioningChangedEvent
                            .setPartitionChange(partitioning, 0, event.getDocument().getLength());
            }
        }

        if (fPositions.size() > 0)
            updatePositions(event);
    }

    /**
     * Notifies all listeners about the given document change. Uses a robust iterator.
     * <p/>
     * Executes all registered post notification replace operation.
     *
     * @param event
     *         the event to be sent out.
     */
    protected void doFireDocumentChanged(DocumentEvent event) {
        boolean changed = fDocumentPartitioningChangedEvent != null && !fDocumentPartitioningChangedEvent.isEmpty();
        Region change = changed ? fDocumentPartitioningChangedEvent.getCoverage() : null;
        doFireDocumentChanged(event, changed, change);
    }

    /**
     * Notifies all listeners about the given document change. Uses a robust iterator.
     * <p/>
     * Executes all registered post notification replace operation.
     *
     * @param event
     *         the event to be sent out
     * @param firePartitionChange
     *         <code>true</code> if a partition change notification should be sent
     * @param partitionChange
     *         the region whose partitioning changed
     * @since 2.0
     * @deprecated as of 3.0. Use <code>doFireDocumentChanged2(DocumentEvent)</code> instead; this method will be removed.
     */
    protected void doFireDocumentChanged(DocumentEvent event, boolean firePartitionChange, Region partitionChange) {
        doFireDocumentChanged2(event);
    }

    /**
     * Notifies all listeners about the given document change. Uses a robust iterator.
     * <p/>
     * Executes all registered post notification replace operation.
     * <p/>
     * This method will be renamed to <code>doFireDocumentChanged</code>.
     *
     * @param event
     *         the event to be sent out
     * @since 3.0
     */
    protected void doFireDocumentChanged2(DocumentEvent event) {

        DocumentPartitioningChangedEvent p = fDocumentPartitioningChangedEvent;
        fDocumentPartitioningChangedEvent = null;
        if (p != null && !p.isEmpty())
            fireDocumentPartitioningChanged(p);

        Object[] listeners = fPrenotifiedDocumentListeners.getListeners();
        for (int i = 0; i < listeners.length; i++) {
            try {
                ((DocumentListener)listeners[i]).documentChanged(event);
            } catch (Exception ex) {
                fail(ex);
            }
        }

        listeners = fDocumentListeners.getListeners();
        for (int i = 0; i < listeners.length; i++) {
            try {
                ((DocumentListener)listeners[i]).documentChanged(event);
            } catch (Exception ex) {
                fail(ex);
            }
        }
    }

    /**
     * Updates the internal document structures and informs all document listeners if listener notification has been enabled.
     * Otherwise it remembers the event to be sent to the listeners on resume.
     *
     * @param event
     *         the document event to be sent out
     */
    protected void fireDocumentChanged(DocumentEvent event) {
        updateDocumentStructures(event);

        if (fStoppedListenerNotification == 0)
            doFireDocumentChanged(event);
        else
            fDeferredDocumentEvent = event;
    }

    /* @see org.eclipse.jface.text.IDocument#getChar(int) */
    public char getChar(int pos) throws BadLocationException {
        if ((0 > pos) || (pos >= getLength()))
            throw new BadLocationException();
        return getStore().get(pos);
    }

    /* @see org.eclipse.jface.text.IDocument#getContentType(int) */
    public String getContentType(int offset) throws BadLocationException {
        String contentType = null;
        try {
            contentType = getContentType(DEFAULT_PARTITIONING, offset, false);
            Assert.isNotNull(contentType);
        } catch (BadPartitioningException e) {
            Assert.isTrue(false);
        }
        return contentType;
    }

    /* @see org.eclipse.jface.text.IDocument#getLegalContentTypes() */
    public String[] getLegalContentTypes() {
        String[] contentTypes = null;
        try {
            contentTypes = getLegalContentTypes(DEFAULT_PARTITIONING);
            Assert.isNotNull(contentTypes);
        } catch (BadPartitioningException e) {
            Assert.isTrue(false);
        }
        return contentTypes;
    }

    /* @see org.eclipse.jface.text.IDocument#getLength() */
    public int getLength() {
        return getStore().getLength();
    }

    /* @see org.eclipse.jface.text.IDocument#getLineDelimiter(int) */
    public String getLineDelimiter(int line) throws BadLocationException {
        return getTracker().getLineDelimiter(line);
    }

    /* @see org.eclipse.jface.text.IDocument#getLegalLineDelimiters() */
    public String[] getLegalLineDelimiters() {
        return getTracker().getLegalLineDelimiters();
    }

    /*
     * @see org.eclipse.jface.text.IDocumentExtension4#getDefaultLineDelimiter()
     * @since 3.1
     */
    public String getDefaultLineDelimiter() {

        String lineDelimiter = null;

        try {
            lineDelimiter = getLineDelimiter(0);
        } catch (BadLocationException x) {
        }

        if (lineDelimiter != null)
            return lineDelimiter;

        if (fInitialLineDelimiter != null)
            return fInitialLineDelimiter;

        String sysLineDelimiter = "\n"; //$NON-NLS-1$
        String[] delimiters = getLegalLineDelimiters();
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

    /*
     * @see org.eclipse.jface.text.IDocumentExtension4#setInitialLineDelimiter(java .lang.String)
     * @since 3.1
     */
    public void setInitialLineDelimiter(String lineDelimiter) {
        Assert.isNotNull(lineDelimiter);
        fInitialLineDelimiter = lineDelimiter;
    }

    /* @see org.eclipse.jface.text.IDocument#getLineLength(int) */
    public int getLineLength(int line) throws BadLocationException {
        return getTracker().getLineLength(line);
    }

    /* @see org.eclipse.jface.text.IDocument#getLineOfOffset(int) */
    public int getLineOfOffset(int pos) throws BadLocationException {
        return getTracker().getLineNumberOfOffset(pos);
    }

    /* @see org.eclipse.jface.text.IDocument#getLineOffset(int) */
    public int getLineOffset(int line) throws BadLocationException {
        return getTracker().getLineOffset(line);
    }

    /* @see org.eclipse.jface.text.IDocument#getLineInformation(int) */
    public Region getLineInformation(int line) throws BadLocationException {
        return getTracker().getLineInformation(line);
    }

    /* @see org.eclipse.jface.text.IDocument#getLineInformationOfOffset(int) */
    public Region getLineInformationOfOffset(int offset) throws BadLocationException {
        return getTracker().getLineInformationOfOffset(offset);
    }

    /* @see org.eclipse.jface.text.IDocument#getNumberOfLines() */
    public int getNumberOfLines() {
        return getTracker().getNumberOfLines();
    }

    /* @see org.eclipse.jface.text.IDocument#getNumberOfLines(int, int) */
    public int getNumberOfLines(int offset, int length) throws BadLocationException {
        return getTracker().getNumberOfLines(offset, length);
    }

    /*
     * @see org.eclipse.jface.text.IDocument#computeNumberOfLines(java.lang.String)
     */
    public int computeNumberOfLines(String text) {
        return getTracker().computeNumberOfLines(text);
    }

    /* @see org.eclipse.jface.text.IDocument#getPartition(int) */
    public TypedRegion getPartition(int offset) throws BadLocationException {
        TypedRegion partition = null;
        try {
            partition = getPartition(DEFAULT_PARTITIONING, offset, false);
            Assert.isNotNull(partition);
        } catch (BadPartitioningException e) {
            Assert.isTrue(false);
        }
        return partition;
    }

    /* @see org.eclipse.jface.text.IDocument#computePartitioning(int, int) */
    public TypedRegion[] computePartitioning(int offset, int length) throws BadLocationException {
        TypedRegion[] partitioning = null;
        try {
            partitioning = computePartitioning(DEFAULT_PARTITIONING, offset, length, false);
            Assert.isNotNull(partitioning);
        } catch (BadPartitioningException e) {
            Assert.isTrue(false);
        }
        return partitioning;
    }

    /* @see org.eclipse.jface.text.IDocument#getPositions(java.lang.String) */
    public Position[] getPositions(String category) throws BadPositionCategoryException {

        if (category == null)
            throw new BadPositionCategoryException();

        List<Position> c = fPositions.get(category);
        if (c == null)
            throw new BadPositionCategoryException();

        Position[] positions = new Position[c.size()];
        c.toArray(positions);
        return positions;
    }

    /* @see org.eclipse.jface.text.IDocument#getPositionCategories() */
    public String[] getPositionCategories() {
        String[] categories = new String[fPositions.size()];
        Iterator<String> keys = fPositions.keySet().iterator();
        for (int i = 0; i < categories.length; i++)
            categories[i] = keys.next();
        return categories;
    }

    /* @see org.eclipse.jface.text.IDocument#getPositionUpdaters() */
    public PositionUpdater[] getPositionUpdaters() {
        PositionUpdater[] updaters = new PositionUpdater[fPositionUpdaters.size()];
        fPositionUpdaters.toArray(updaters);
        return updaters;
    }

    /* @see org.eclipse.jface.text.IDocument#get() */
    public String get() {
        return getStore().get(0, getLength());
    }

    /* @see org.eclipse.jface.text.IDocument#get(int, int) */
    public String get(int pos, int length) throws BadLocationException {
        int myLength = getLength();
        if ((0 > pos) || (0 > length) || (pos + length > myLength))
            throw new BadLocationException();
        return getStore().get(pos, length);
    }

    /*
     * @see org.eclipse.jface.text.IDocument#insertPositionUpdater(org.eclipse.jface .text.IPositionUpdater, int)
     */
    public void insertPositionUpdater(PositionUpdater updater, int index) {

        for (int i = fPositionUpdaters.size() - 1; i >= 0; i--) {
            if (fPositionUpdaters.get(i) == updater)
                return;
        }

        if (index == fPositionUpdaters.size())
            fPositionUpdaters.add(updater);
        else
            fPositionUpdaters.add(index, updater);
    }

    /*
     * @see org.eclipse.jface.text.IDocument#removePosition(java.lang.String, org.eclipse.jface.text.Position)
     */
    public void removePosition(String category, Position position) throws BadPositionCategoryException {

        if (position == null)
            return;

        if (category == null)
            throw new BadPositionCategoryException();

        List<Position> c = fPositions.get(category);
        if (c == null)
            throw new BadPositionCategoryException();
        removeFromPositionsList(c, position, true);

        List<Position> endPositions = fEndPositions.get(category);
        if (endPositions == null)
            throw new BadPositionCategoryException();
        removeFromPositionsList(endPositions, position, false);
    }

    /**
     * Remove the given position form the given list of positions based on identity not equality.
     *
     * @param positions
     *         a list of positions
     * @param position
     *         the position to remove
     * @param orderedByOffset
     *         true if <code>positions</code> is ordered by offset, false if ordered by end position
     * @since 3.4
     */
    private void removeFromPositionsList(List<Position> positions, Position position, boolean orderedByOffset) {
        int size = positions.size();

        // Assume position is somewhere near it was before
        int index =
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
     * @see org.eclipse.jface.text.IDocument#removePosition(org.eclipse.jface.text .Position)
     */
    public void removePosition(Position position) {
        try {
            removePosition(DEFAULT_CATEGORY, position);
        } catch (BadPositionCategoryException e) {
        }
    }

    /*
     * @see org.eclipse.jface.text.IDocument#removePositionCategory(java.lang.String)
     */
    public void removePositionCategory(String category) throws BadPositionCategoryException {

        if (category == null)
            return;

        if (!containsPositionCategory(category))
            throw new BadPositionCategoryException();

        fPositions.remove(category);
        fEndPositions.remove(category);
    }

    /*
     * @see org.eclipse.jface.text.IDocument#removePositionUpdater(org.eclipse.jface .text.IPositionUpdater)
     */
    public void removePositionUpdater(PositionUpdater updater) {
        for (int i = fPositionUpdaters.size() - 1; i >= 0; i--) {
            if (fPositionUpdaters.get(i) == updater) {
                fPositionUpdaters.remove(i);
                return;
            }
        }
    }

    private long getNextModificationStamp() {
        if (fNextModificationStamp == Long.MAX_VALUE || fNextModificationStamp == UNKNOWN_MODIFICATION_STAMP)
            fNextModificationStamp = 0;
        else
            fNextModificationStamp = fNextModificationStamp + 1;

        return fNextModificationStamp;
    }

    /*
     * @see org.eclipse.jface.text.IDocumentExtension4#getModificationStamp()
     */
    public long getModificationStamp() {
        return fModificationStamp;
    }

    /*
     * @see org.eclipse.jface.text.IDocument#replace(int, int, java.lang.String)
     */
    public void replace(int pos, int length, String text, long modificationStamp) throws BadLocationException {
        if ((0 > pos) || (0 > length) || (pos + length > getLength()))
            throw new BadLocationException();

        DocumentEvent e = new DocumentEvent(this, pos, length, text);
        fireDocumentAboutToBeChanged(e);

        getStore().replace(pos, length, text);
        getTracker().replace(pos, length, text);

        fModificationStamp = modificationStamp;
        fNextModificationStamp = Math.max(fModificationStamp, fNextModificationStamp);
        e.fModificationStamp = fModificationStamp;

        fireDocumentChanged(e);
    }

    /**
     * {@inheritDoc}
     *
     * @since 3.4
     */
    public boolean isLineInformationRepairNeeded(int offset, int length, String text) throws BadLocationException {
        return false;
    }

    /* @see org.eclipse.jface.text.IDocument#replace(int, int, java.lang.String) */
    public void replace(int pos, int length, String text) throws BadLocationException {
        if (length == 0 && (text == null || text.length() == 0))
            replace(pos, length, text, getModificationStamp());
        else
            replace(pos, length, text, getNextModificationStamp());
    }

    /* @see org.eclipse.jface.text.IDocument#set(java.lang.String) */
    public void set(String text) {
        set(text, getNextModificationStamp());
    }

    /*
     * @see org.eclipse.jface.text.IDocumentExtension4#set(java.lang.String, long)
     * @since 3.1
     */
    public void set(String text, long modificationStamp) {
        int length = getStore().getLength();

        DocumentEvent e = new DocumentEvent(this, 0, length, text);
        fireDocumentAboutToBeChanged(e);

        getStore().set(text);
        getTracker().set(text);

        fModificationStamp = modificationStamp;
        fNextModificationStamp = Math.max(fModificationStamp, fNextModificationStamp);
        e.fModificationStamp = fModificationStamp;

        fireDocumentChanged(e);
    }

    /**
     * Updates all positions of all categories to the change described by the document event. All registered document updaters are
     * called in the sequence they have been arranged. Uses a robust iterator.
     *
     * @param event
     *         the document event describing the change to which to adapt the positions
     */
    protected void updatePositions(DocumentEvent event) {
        ArrayList<PositionUpdater> list = new ArrayList<PositionUpdater>(fPositionUpdaters);
        Iterator<PositionUpdater> e = list.iterator();
        while (e.hasNext()) {
            PositionUpdater u = e.next();
            u.update(event);
        }
    }


    /**
     * Returns the find/replace adapter for this document.
     *
     * @return this document's find/replace document adapter
     */
    private FindReplaceDocumentAdapter getFindReplaceDocumentAdapter() {
        if (fFindReplaceDocumentAdapter == null)
            fFindReplaceDocumentAdapter = new FindReplaceDocumentAdapter(this);

        return fFindReplaceDocumentAdapter;
    }

    /*
     * @see org.eclipse.jface.text.IDocumentExtension2#acceptPostNotificationReplaces ()
     * @since 2.1
     */
    public void acceptPostNotificationReplaces() {
        fAcceptPostNotificationReplaces = true;
    }

    /*
     * @see org.eclipse.jface.text.IDocumentExtension2#ignorePostNotificationReplaces ()
     * @since 2.1
     */
    public void ignorePostNotificationReplaces() {
        fAcceptPostNotificationReplaces = false;
    }

    /*
     * @see org.eclipse.jface.text.IDocumentExtension#stopPostNotificationProcessing()
     * @since 2.0
     */
    public void stopPostNotificationProcessing() {
        ++fStoppedCount;
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.0
     * @deprecated since 3.1. Use {@link IDocumentExtension4#startRewriteSession(DocumentRewriteSessionType)} instead.
     */
    public void startSequentialRewrite(boolean normalized) {
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.0
     * @deprecated As of 3.1, replaced by {@link IDocumentExtension4#stopRewriteSession(DocumentRewriteSession)}
     */
    public void stopSequentialRewrite() {
    }

    /*
     * @see org.eclipse.jface.text.IDocumentExtension2#resumeListenerNotification()
     * @since 2.1
     */
    public void resumeListenerNotification() {
        --fStoppedListenerNotification;
        if (fStoppedListenerNotification == 0) {
            resumeDocumentListenerNotification();
        }
    }

    /*
     * @see org.eclipse.jface.text.IDocumentExtension2#stopListenerNotification()
     * @since 2.1
     */
    public void stopListenerNotification() {
        ++fStoppedListenerNotification;
    }

    /**
     * Resumes the document listener notification by sending out the remembered partition changed and document event.
     *
     * @since 2.1
     */
    private void resumeDocumentListenerNotification() {
        if (fDeferredDocumentEvent != null) {
            DocumentEvent event = fDeferredDocumentEvent;
            fDeferredDocumentEvent = null;
            doFireDocumentChanged(event);
        }
    }

    /*
     * @see org.eclipse.jface.text.IDocumentExtension3#computeZeroLengthPartitioning (java.lang.String, int, int)
     * @since 3.0
     */
    public TypedRegion[] computePartitioning(String partitioning, int offset, int length,
                                             boolean includeZeroLengthPartitions) throws BadLocationException, BadPartitioningException {
        if ((0 > offset) || (0 > length) || (offset + length > getLength()))
            throw new BadLocationException();

        DocumentPartitioner partitioner = getDocumentPartitioner(partitioning);
        //
        // if (partitioner instanceof IDocumentPartitionerExtension2)
        // {
        // checkStateOfPartitioner(partitioner, partitioning);
        // return ((IDocumentPartitionerExtension2)partitioner).computePartitioning(offset, length,
        // includeZeroLengthPartitions);
        // }
        // else
        if (partitioner != null) {
            checkStateOfPartitioner(partitioner, partitioning);
            return partitioner.computePartitioning(offset, length);
        } else if (DEFAULT_PARTITIONING.equals(partitioning))
            return new TypedRegionImpl[]{new TypedRegionImpl(offset, length, DEFAULT_CONTENT_TYPE)};
        else
            throw new BadPartitioningException();
    }

    /*
     * @see org.eclipse.jface.text.IDocumentExtension3#getZeroLengthContentType(java .lang.String, int)
     * @since 3.0
     */
    public String getContentType(String partitioning, int offset, boolean preferOpenPartitions)
            throws BadLocationException, BadPartitioningException {
        if ((0 > offset) || (offset > getLength()))
            throw new BadLocationException();

        DocumentPartitioner partitioner = getDocumentPartitioner(partitioning);

        // if (partitioner instanceof IDocumentPartitionerExtension2)
        // {
        // checkStateOfPartitioner(partitioner, partitioning);
        // return ((IDocumentPartitionerExtension2)partitioner).getContentType(offset, preferOpenPartitions);
        // }
        // else
        if (partitioner != null) {
            checkStateOfPartitioner(partitioner, partitioning);
            return partitioner.getContentType(offset);
        } else if (DEFAULT_PARTITIONING.equals(partitioning))
            return DEFAULT_CONTENT_TYPE;
        else
            throw new BadPartitioningException();
    }

    /*
     * @see org.eclipse.jface.text.IDocumentExtension3#getDocumentPartitioner(java .lang.String)
     * @since 3.0
     */
    public DocumentPartitioner getDocumentPartitioner(String partitioning) {
        return fDocumentPartitioners != null ? (DocumentPartitioner)fDocumentPartitioners.get(partitioning) : null;
    }

    /*
     * @see org.eclipse.jface.text.IDocumentExtension3#getLegalContentTypes(java.lang .String)
     * @since 3.0
     */
    public String[] getLegalContentTypes(String partitioning) throws BadPartitioningException {
        DocumentPartitioner partitioner = getDocumentPartitioner(partitioning);
        if (partitioner != null)
            return partitioner.getLegalContentTypes();
        if (DEFAULT_PARTITIONING.equals(partitioning))
            return new String[]{DEFAULT_CONTENT_TYPE};
        throw new BadPartitioningException();
    }

    /*
     * @see org.eclipse.jface.text.IDocumentExtension3#getZeroLengthPartition(java .lang.String, int)
     * @since 3.0
     */
    public TypedRegion getPartition(String partitioning, int offset, boolean preferOpenPartitions)
            throws BadLocationException, BadPartitioningException {
        if ((0 > offset) || (offset > getLength()))
            throw new BadLocationException();

        DocumentPartitioner partitioner = getDocumentPartitioner(partitioning);

        // if (partitioner instanceof IDocumentPartitionerExtension2)
        // {
        // checkStateOfPartitioner(partitioner, partitioning);
        // return ((IDocumentPartitionerExtension2)partitioner).getPartition(offset, preferOpenPartitions);
        // }
        // else
        if (partitioner != null) {
            checkStateOfPartitioner(partitioner, partitioning);
            return partitioner.getPartition(offset);
        } else if (DEFAULT_PARTITIONING.equals(partitioning))
            return new TypedRegionImpl(0, getLength(), DEFAULT_CONTENT_TYPE);
        else
            throw new BadPartitioningException();
    }

    /*
     * @see org.eclipse.jface.text.IDocumentExtension3#getPartitionings()
     * @since 3.0
     */
    public String[] getPartitionings() {
        if (fDocumentPartitioners == null)
            return new String[0];
        String[] partitionings = new String[fDocumentPartitioners.size()];
        fDocumentPartitioners.keySet().toArray(partitionings);
        return partitionings;
    }

    /*
     * @see org.eclipse.jface.text.IDocumentExtension3#setDocumentPartitioner(java .lang.String,
     * org.eclipse.jface.text.IDocumentPartitioner)
     * @since 3.0
     */
    public void setDocumentPartitioner(String partitioning, DocumentPartitioner partitioner) {
        if (partitioner == null) {
            if (fDocumentPartitioners != null) {
                fDocumentPartitioners.remove(partitioning);
                if (fDocumentPartitioners.size() == 0)
                    fDocumentPartitioners = null;
            }
        } else {
            if (fDocumentPartitioners == null)
                fDocumentPartitioners = new HashMap<String, DocumentPartitioner>();
            fDocumentPartitioners.put(partitioning, partitioner);
        }
        DocumentPartitioningChangedEvent event = new DocumentPartitioningChangedEvent(this);
        event.setPartitionChange(partitioning, 0, getLength());
        fireDocumentPartitioningChanged(event);
    }

    /*
     * @see org.eclipse.jface.text.IRepairableDocument#repairLineInformation()
     * @since 3.0
     */
    public void repairLineInformation() {
        getTracker().set(get());
    }

    // /**
    // * Fires the given event to all registered rewrite session listeners. Uses robust iterators.
    // *
    // * @param event the event to be fired
    // * @since 3.1
    // */
    // protected void fireRewriteSessionChanged(DocumentRewriteSessionEvent event)
    // {
    // if (fDocumentRewriteSessionListeners.size() > 0)
    // {
    // List list = new ArrayList(fDocumentRewriteSessionListeners);
    // Iterator e = list.iterator();
    // while (e.hasNext())
    // {
    // try
    // {
    // IDocumentRewriteSessionListener l = (IDocumentRewriteSessionListener)e.next();
    // l.documentRewriteSessionChanged(event);
    // }
    // catch (Exception ex)
    // {
    // log(ex);
    // }
    // }
    // }
    // }

    // /*
    // * @see org.eclipse.jface.text.IDocumentExtension4#getActiveRewriteSession()
    // */
    // public final DocumentRewriteSession getActiveRewriteSession()
    // {
    // return fDocumentRewriteSession;
    // }
    //
    // /*
    // * @see org.eclipse.jface.text.IDocumentExtension4#startRewriteSession(org.eclipse.jface.text.DocumentRewriteSessionType)
    // * @since 3.1
    // */
    // public DocumentRewriteSession startRewriteSession(DocumentRewriteSessionType sessionType)
    // {
    //
    // if (getActiveRewriteSession() != null)
    // throw new IllegalStateException();
    //
    // fDocumentRewriteSession = new DocumentRewriteSession(sessionType);
    // if (DEBUG)
    //         System.out.println("AbstractDocument: Starting rewrite session: " + fDocumentRewriteSession); //$NON-NLS-1$
    //
    // fireRewriteSessionChanged(new DocumentRewriteSessionEvent(this, fDocumentRewriteSession,
    // DocumentRewriteSessionEvent.SESSION_START));
    //
    // startRewriteSessionOnPartitioners(fDocumentRewriteSession);
    //
    // ILineTracker tracker = getTracker();
    // if (tracker instanceof ILineTrackerExtension)
    // {
    // ILineTrackerExtension extension = (ILineTrackerExtension)tracker;
    // extension.startRewriteSession(fDocumentRewriteSession);
    // }
    //
    // if (DocumentRewriteSessionType.SEQUENTIAL == sessionType)
    // startSequentialRewrite(false);
    // else if (DocumentRewriteSessionType.STRICTLY_SEQUENTIAL == sessionType)
    // startSequentialRewrite(true);
    //
    // return fDocumentRewriteSession;
    // }

    // /**
    // * Starts the given rewrite session.
    // *
    // * @param session the rewrite session
    // * @since 3.1
    // */
    // protected final void startRewriteSessionOnPartitioners(DocumentRewriteSession session)
    // {
    // if (fDocumentPartitioners != null)
    // {
    // Iterator e = fDocumentPartitioners.values().iterator();
    // while (e.hasNext())
    // {
    // Object partitioner = e.next();
    // if (partitioner instanceof IDocumentPartitionerExtension3)
    // {
    // IDocumentPartitionerExtension3 extension = (IDocumentPartitionerExtension3)partitioner;
    // extension.startRewriteSession(session);
    // }
    // }
    // }
    // }
    //
    // /*
    // * @see org.eclipse.jface.text.IDocumentExtension4#stopRewriteSession(org.eclipse.jface.text.DocumentRewriteSession)
    // * @since 3.1
    // */
    // public void stopRewriteSession(DocumentRewriteSession session)
    // {
    // if (fDocumentRewriteSession == session)
    // {
    //
    // if (DEBUG)
    //            System.out.println("AbstractDocument: Stopping rewrite session: " + session); //$NON-NLS-1$
    //
    // DocumentRewriteSessionType sessionType = session.getSessionType();
    // if (DocumentRewriteSessionType.SEQUENTIAL == sessionType
    // || DocumentRewriteSessionType.STRICTLY_SEQUENTIAL == sessionType)
    // stopSequentialRewrite();
    //
    // ILineTracker tracker = getTracker();
    // if (tracker instanceof ILineTrackerExtension)
    // {
    // ILineTrackerExtension extension = (ILineTrackerExtension)tracker;
    // extension.stopRewriteSession(session, get());
    // }
    //
    // stopRewriteSessionOnPartitioners(fDocumentRewriteSession);
    //
    // fDocumentRewriteSession = null;
    // fireRewriteSessionChanged(new DocumentRewriteSessionEvent(this, session,
    // DocumentRewriteSessionEvent.SESSION_STOP));
    // }
    // }
    //
    // /**
    // * Stops the given rewrite session.
    // *
    // * @param session the rewrite session
    // * @since 3.1
    // */
    // protected final void stopRewriteSessionOnPartitioners(DocumentRewriteSession session)
    // {
    // if (fDocumentPartitioners != null)
    // {
    // DocumentPartitioningChangedEvent event = new DocumentPartitioningChangedEvent(this);
    // Iterator e = fDocumentPartitioners.keySet().iterator();
    // while (e.hasNext())
    // {
    // String partitioning = (String)e.next();
    // IDocumentPartitioner partitioner = (IDocumentPartitioner)fDocumentPartitioners.get(partitioning);
    // if (partitioner instanceof IDocumentPartitionerExtension3)
    // {
    // IDocumentPartitionerExtension3 extension = (IDocumentPartitionerExtension3)partitioner;
    // extension.stopRewriteSession(session);
    // event.setPartitionChange(partitioning, 0, getLength());
    // }
    // }
    // if (!event.isEmpty())
    // fireDocumentPartitioningChanged(event);
    // }
    // }

    // /*
    // * @see
    // org.eclipse.jface.text.IDocumentExtension4#addDocumentRewriteSessionListener(org.eclipse.jface.text.IDocumentRewriteSessionListener)
    // * @since 3.1
    // */
    // public void addDocumentRewriteSessionListener(IDocumentRewriteSessionListener listener)
    // {
    // Assert.isNotNull(listener);
    // if (!fDocumentRewriteSessionListeners.contains(listener))
    // fDocumentRewriteSessionListeners.add(listener);
    // }
    //
    // /*
    // * @see
    // org.eclipse.jface.text.IDocumentExtension4#removeDocumentRewriteSessionListener(org.eclipse.jface.text
    // .IDocumentRewriteSessionListener)
    // * @since 3.1
    // */
    // public void removeDocumentRewriteSessionListener(IDocumentRewriteSessionListener listener)
    // {
    // Assert.isNotNull(listener);
    // fDocumentRewriteSessionListeners.remove(listener);
    // }

    /**
     * Checks the state for the given partitioner and stops the active rewrite session.
     *
     * @param partitioner
     *         the document partitioner to be checked
     * @param partitioning
     *         the document partitioning the partitioner is registered for
     * @since 3.1
     */
    protected final void checkStateOfPartitioner(DocumentPartitioner partitioner, String partitioning) {
        // if (!(partitioner instanceof IDocumentPartitionerExtension3))
        // return;
        //
        // IDocumentPartitionerExtension3 extension = (IDocumentPartitionerExtension3)partitioner;
        // DocumentRewriteSession session = extension.getActiveRewriteSession();
        // if (session != null)
        // {
        // extension.stopRewriteSession(session);
        //
        // if (DEBUG)
        //            System.out.println("AbstractDocument: Flushing rewrite session for partition type: " + partitioning); //$NON-NLS-1$
        //
        // DocumentPartitioningChangedEvent event = new DocumentPartitioningChangedEvent(this);
        // event.setPartitionChange(partitioning, 0, getLength());
        // fireDocumentPartitioningChanged(event);
        // }
    }


    /** {@inheritDoc} */
    @Override
    public Position[] getPositions(String category, int offset, int length, boolean canStartBefore,
                                   boolean canEndAfter) throws BadPositionCategoryException {
        if (canStartBefore && canEndAfter || (!canStartBefore && !canEndAfter)) {
            List<Position> documentPositions;
            if (canStartBefore && canEndAfter) {
                if (offset < getLength() / 2) {
                    documentPositions = getStartingPositions(category, 0, offset + length);
                } else {
                    documentPositions = getEndingPositions(category, offset, getLength() - offset + 1);
                }
            } else {
                documentPositions = getStartingPositions(category, offset, length);
            }

            ArrayList<Position> list = new ArrayList<Position>(documentPositions.size());

            Position region = new Position(offset, length);

            for (Iterator<Position> iterator = documentPositions.iterator(); iterator.hasNext(); ) {
                Position position = iterator.next();
                if (isWithinRegion(region, position, canStartBefore, canEndAfter)) {
                    list.add(position);
                }
            }

            Position[] positions = new Position[list.size()];
            list.toArray(positions);
            return positions;
        } else if (canStartBefore) {
            List<Position> list = getEndingPositions(category, offset, length);
            Position[] positions = new Position[list.size()];
            list.toArray(positions);
            return positions;
        } else {
            Assert.isLegal(canEndAfter && !canStartBefore);

            List<Position> list = getStartingPositions(category, offset, length);
            Position[] positions = new Position[list.size()];
            list.toArray(positions);
            return positions;
        }
    }

    /* @since 3.4 */
    private boolean isWithinRegion(Position region, Position position, boolean canStartBefore, boolean canEndAfter) {
        if (canStartBefore && canEndAfter) {
            return region.overlapsWith(position.getOffset(), position.getLength());
        } else if (canStartBefore) {
            return region.includes(position.getOffset() + position.getLength() - 1);
        } else if (canEndAfter) {
            return region.includes(position.getOffset());
        } else {
            int start = position.getOffset();
            return region.includes(start) && region.includes(start + position.getLength() - 1);
        }
    }

    /**
     * A list of positions in the given category with an offset inside the given region. The order of the positions is arbitrary.
     *
     * @param category
     *         the position category
     * @param offset
     *         the offset of the region
     * @param length
     *         the length of the region
     * @return a list of the positions in the region
     * @throws BadPositionCategoryException
     *         if category is undefined in this document
     */
    private List<Position> getStartingPositions(String category, int offset, int length)
            throws BadPositionCategoryException {
        List<Position> positions = fPositions.get(category);
        if (positions == null)
            throw new BadPositionCategoryException();

        int indexStart = computeIndexInPositionList(positions, offset, true);
        int indexEnd = computeIndexInPositionList(positions, offset + length, true);

        return positions.subList(indexStart, indexEnd);
    }

    /**
     * A list of positions in the given category with an end position inside the given region. The order of the positions is
     * arbitrary.
     *
     * @param category
     *         the position category
     * @param offset
     *         the offset of the region
     * @param length
     *         the length of the region
     * @return a list of the positions in the region
     * @throws BadPositionCategoryException
     *         if category is undefined in this document
     * @since 3.4
     */
    private List<Position> getEndingPositions(String category, int offset, int length)
            throws BadPositionCategoryException {
        List<Position> positions = fEndPositions.get(category);
        if (positions == null)
            throw new BadPositionCategoryException();

        int indexStart = computeIndexInPositionList(positions, offset, false);
        int indexEnd = computeIndexInPositionList(positions, offset + length, false);

        return positions.subList(indexStart, indexEnd);
    }

    /**
     * Logs the given exception by reusing the code in {@link SafeRunner}.
     *
     * @param ex
     *         the exception
     * @since 3.6
     */
    private static void fail(final Exception ex) {
        throw new JavaScriptException(ex);
    }

}
