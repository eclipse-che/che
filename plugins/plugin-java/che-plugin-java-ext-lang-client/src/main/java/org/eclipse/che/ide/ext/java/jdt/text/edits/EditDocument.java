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
import org.eclipse.che.ide.api.text.BadPositionCategoryException;
import org.eclipse.che.ide.api.text.Position;
import org.eclipse.che.ide.api.text.Region;
import org.eclipse.che.ide.api.text.TypedRegion;
import org.eclipse.che.ide.ext.java.jdt.text.BadPartitioningException;
import org.eclipse.che.ide.ext.java.jdt.text.Document;
import org.eclipse.che.ide.ext.java.jdt.text.DocumentListener;
import org.eclipse.che.ide.ext.java.jdt.text.DocumentPartitioner;
import org.eclipse.che.ide.ext.java.jdt.text.DocumentPartitioningListener;
import org.eclipse.che.ide.ext.java.jdt.text.PositionUpdater;

class EditDocument implements Document {

    private StringBuffer fBuffer;

    public EditDocument(String content) {
        fBuffer = new StringBuffer(content);
    }

    /** {@inheritDoc} */
    @Override
    public void addPosition(Position position) throws BadLocationException {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public void addPosition(String category, Position position) throws BadLocationException, BadPositionCategoryException {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public void addPositionUpdater(PositionUpdater updater) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public int computeNumberOfLines(String text) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public TypedRegion[] computePartitioning(int offset, int length) throws BadLocationException {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public boolean containsPosition(String category, int offset, int length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int computeIndexInCategory(String category, int offset) throws BadLocationException, BadPositionCategoryException {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public boolean containsPositionCategory(String category) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public String get() {
        return fBuffer.toString();
    }

    /** {@inheritDoc} */
    @Override
    public String get(int offset, int length) throws BadLocationException {
        return fBuffer.substring(offset, offset + length);
    }

    /** {@inheritDoc} */
    @Override
    public char getChar(int offset) throws BadLocationException {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public String getContentType(int offset) throws BadLocationException {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public DocumentPartitioner getDocumentPartitioner() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public String[] getLegalContentTypes() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public String[] getLegalLineDelimiters() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public int getLength() {
        return fBuffer.length();
    }

    /** {@inheritDoc} */
    @Override
    public String getLineDelimiter(int line) throws BadLocationException {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public Position[] getPositions(String category, int offset, int length, boolean canStartBefore,
                                   boolean canEndAfter) throws BadPositionCategoryException {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public Region getLineInformation(int line) throws BadLocationException {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public Region getLineInformationOfOffset(int offset) throws BadLocationException {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public int getLineLength(int line) throws BadLocationException {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public int getLineOffset(int line) throws BadLocationException {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public int getLineOfOffset(int offset) throws BadLocationException {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public int getNumberOfLines() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public int getNumberOfLines(int offset, int length) throws BadLocationException {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public TypedRegion getPartition(int offset) throws BadLocationException {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public String[] getPositionCategories() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public Position[] getPositions(String category) throws BadPositionCategoryException {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public PositionUpdater[] getPositionUpdaters() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public void insertPositionUpdater(PositionUpdater updater, int index) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public void removeDocumentListener(DocumentListener listener) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public void removeDocumentPartitioningListener(DocumentPartitioningListener listener) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public void removePosition(Position position) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public void removePosition(String category, Position position) throws BadPositionCategoryException {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public void removePositionCategory(String category) throws BadPositionCategoryException {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public void removePositionUpdater(PositionUpdater updater) {
        throw new UnsupportedOperationException();
    }

    public void removePrenotifiedDocumentListener(DocumentListener documentAdapter) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public void replace(int offset, int length, String text) throws BadLocationException {
        fBuffer.replace(offset, offset + length, text);
    }

    /** {@inheritDoc} */
    @Deprecated
    public int search(int startOffset, String findString, boolean forwardSearch, boolean caseSensitive, boolean wholeWord)
            throws BadLocationException {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public void set(String text) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public void addPositionCategory(String category) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public void setDocumentPartitioner(DocumentPartitioner partitioner) {
        throw new UnsupportedOperationException();
    }


    /** {@inheritDoc} */
    @Override
    public void addDocumentListener(DocumentListener listener) {
        throw new UnsupportedOperationException();
    }


    /** {@inheritDoc} */
    @Override
    public void addDocumentPartitioningListener(DocumentPartitioningListener listener) {
        throw new UnsupportedOperationException();
    }

    /** @see org.eclipse.che.ide.ext.java.jdt.text.Document#getPartitionings() */
    @Override
    public String[] getPartitionings() {
        throw new UnsupportedOperationException();
    }

    /** @see org.eclipse.che.ide.ext.java.jdt.text.Document#getLegalContentTypes(java.lang.String) */
    @Override
    public String[] getLegalContentTypes(String partitioning) throws BadPartitioningException {
        throw new UnsupportedOperationException();
    }

    /** @see org.eclipse.che.ide.ext.java.jdt.text.Document#getContentType(java.lang.String, int, boolean) */
    @Override
    public String getContentType(String partitioning, int offset, boolean preferOpenPartitions)
            throws BadLocationException, BadPartitioningException {
        throw new UnsupportedOperationException();
    }

    /** @see org.eclipse.che.ide.ext.java.jdt.text.Document#getPartition(java.lang.String, int, boolean) */
    @Override
    public TypedRegion getPartition(String partitioning, int offset, boolean preferOpenPartitions)
            throws BadLocationException, BadPartitioningException {
        throw new UnsupportedOperationException();
    }

    /** @see org.eclipse.che.ide.ext.java.jdt.text.Document#computePartitioning(java.lang.String, int, int, boolean) */
    @Override
    public TypedRegion[] computePartitioning(String partitioning, int offset, int length,
                                             boolean includeZeroLengthPartitions) throws BadLocationException, BadPartitioningException {
        throw new UnsupportedOperationException();
    }

    /** @see org.eclipse.che.ide.ext.java.jdt.text.Document#setDocumentPartitioner(java.lang.String, org.eclipse.che.ide.ext.java.jdt.text.DocumentPartitioner) */
    @Override
    public void setDocumentPartitioner(String partitioning, DocumentPartitioner partitioner) {
        throw new UnsupportedOperationException();
    }

    /** @see org.eclipse.che.ide.ext.java.jdt.text.Document#getDocumentPartitioner(java.lang.String) */
    @Override
    public DocumentPartitioner getDocumentPartitioner(String partitioning) {
        throw new UnsupportedOperationException();
    }

    /** @see org.eclipse.che.ide.ext.java.jdt.text.Document#getModificationStamp() */
    @Override
    public long getModificationStamp() {
        throw new UnsupportedOperationException();
    }

    /** @see org.eclipse.che.ide.ext.java.jdt.text.Document#replace(int, int, java.lang.String, long) */
    @Override
    public void replace(int offset, int length, String text, long modificationStamp) throws BadLocationException {
        throw new UnsupportedOperationException();
    }
}
