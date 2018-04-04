/**
 * ***************************************************************************** Copyright (c)
 * 2012-2015 Red Hat, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Red Hat, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.ide.ext.java.jdt.text;

import org.eclipse.che.ide.api.editor.text.BadLocationException;
import org.eclipse.che.ide.api.editor.text.BadPositionCategoryException;
import org.eclipse.che.ide.api.editor.text.Position;
import org.eclipse.che.ide.api.editor.text.Region;
import org.eclipse.che.ide.api.editor.text.TypedRegion;

/**
 * An <code>Document</code> represents text providing support for
 *
 * <ul>
 *   <li>text manipulation
 *   <li>positions
 *   <li>partitions
 *   <li>line information
 *   <li>document change listeners
 *   <li>document partition change listeners
 * </ul>
 *
 * <p>A document allows to set its content and to manipulate it. For manipulation a document
 * provides the <code>replace</code> method which substitutes a given string for a specified text
 * range in the document. On each document change, all registered document listeners are informed
 * exactly once.
 *
 * <p>Positions are stickers to the document's text that are updated when the document is changed.
 * Positions are updated by {@link PositionUpdater}s. Position updaters are managed as a list. The
 * list defines the sequence in which position updaters are invoked. This way, position updaters may
 * rely on each other. Positions are grouped into categories. A category is a ordered list of
 * positions. the document defines the order of position in a category based on the position's
 * offset based on the implementation of the method <code>computeIndexInCategory</code>. Each
 * document must support a default position category whose name is specified by this interface.
 *
 * <p>A document can be considered consisting of a sequence of not overlapping partitions. A
 * partition is defined by its offset, its length, and its type. Partitions are updated on every
 * document manipulation and ensured to be up-to-date when the document listeners are informed. A
 * document uses an <code>DocumentPartitioner</code> to manage its partitions. A document may be
 * unpartitioned which happens when there is no partitioner. In this case, the document is
 * considered as one single partition of a default type. The default type is specified by this
 * interface. If a document change changes the document's partitioning all registered partitioning
 * listeners are informed exactly once. Each partitioning has an id which must be used to refer to a
 * particular partitioning.
 *
 * <p>An <code>Document</code> provides methods to map line numbers and character positions onto
 * each other based on the document's line delimiters. When moving text between documents using
 * different line delimiters, the text must be converted to use the target document's line
 * delimiters.
 *
 * <p>An <code>Document</code> does not care about mixed line delimiters. Clients who want to ensure
 * a single line delimiter in their document should use the line delimiter returned by {@link
 * TextUtilities#getDefaultLineDelimiter(Document)} .
 *
 * <p><code>Document</code> throws <code>BadLocationException</code> if the parameters of queries or
 * manipulation requests are not inside the bounds of the document.
 *
 * <p>Clients may implement this interface and its extension interfaces or use the default
 * implementation provided by <code>AbstractDocument</code> and <code>DocumentImpl</code>.
 */
public interface Document {

  /** The identifier of the default position category. */
  static final String DEFAULT_CATEGORY = "__dflt_position_category"; // $NON-NLS-1$

  /** The identifier of the default partition content type. */
  static final String DEFAULT_CONTENT_TYPE = "__dftl_partition_content_type"; // $NON-NLS-1$

  /** The identifier of the default partitioning. */
  static final String DEFAULT_PARTITIONING = "__dftl_partitioning"; // $NON-NLS-1$

  /** The unknown modification stamp. */
  long UNKNOWN_MODIFICATION_STAMP = -1;

  /**
   * Returns the modification stamp of this document. The modification stamp is updated each time a
   * modifying operation is called on this document. If two modification stamps of the same document
   * are identical then the document content is too, however, same content does not imply same
   * modification stamp.
   *
   * <p>The magnitude or sign of the numerical difference between two modification stamps is not
   * significant.
   *
   * @return the modification stamp of this document or <code>UNKNOWN_MODIFICATION_STAMP</code>
   */
  long getModificationStamp();

  /**
   * Substitutes the given text for the specified document range. Sends a <code>DocumentEvent</code>
   * to all registered <code>DocumentListener</code>.
   *
   * @param offset the document offset
   * @param length the length of the specified range
   * @param text the substitution text
   * @param modificationStamp of the document after replacing
   * @throws BadLocationException if the offset is invalid in this document
   * @see DocumentEvent
   * @see DocumentListener
   */
  void replace(int offset, int length, String text, long modificationStamp)
      throws BadLocationException;

  /**
   * Returns the existing partitionings for this document. This includes the default partitioning.
   *
   * @return the existing partitionings for this document
   */
  String[] getPartitionings();

  /**
   * Returns the set of legal content types of document partitions for the given partitioning This
   * set can be empty. The set can contain more content types than contained by the result of <code>
   * getPartitioning(partitioning, 0, getLength())</code>.
   *
   * @param partitioning the partitioning for which to return the legal content types
   * @return the set of legal content types
   * @throws BadPartitioningException if partitioning is invalid for this document
   */
  String[] getLegalContentTypes(String partitioning) throws BadPartitioningException;

  /**
   * Returns the type of the document partition containing the given offset for the given
   * partitioning. This is a convenience method for <code>
   * getPartition(partitioning, offset, boolean).getType()</code>.
   *
   * <p>If <code>preferOpenPartitions</code> is <code>true</code>, precedence is given to an open
   * partition ending at <code>offset</code> over a delimited partition starting at <code>offset
   * </code>. If it is <code>false</code>, precedence is given to the partition that does not end at
   * <code>offset</code>. This is only supported if the connected <code>DocumentPartitioner</code>
   * supports it, i.e. implements <code>DocumentPartitionerExtension2</code>. Otherwise, <code>
   * preferOpenPartitions</code> is ignored.
   *
   * @param partitioning the partitioning
   * @param offset the document offset
   * @param preferOpenPartitions <code>true</code> if precedence should be given to a open partition
   *     ending at <code>offset</code> over a closed partition starting at <code>offset</code>
   * @return the partition type
   * @throws BadLocationException if offset is invalid in this document
   * @throws BadPartitioningException if partitioning is invalid for this document
   */
  String getContentType(String partitioning, int offset, boolean preferOpenPartitions)
      throws BadLocationException, BadPartitioningException;

  /**
   * Returns the document partition of the given partitioning in which the given offset is located.
   *
   * <p>If <code>preferOpenPartitions</code> is <code>true</code>, precedence is given to an open
   * partition ending at <code>offset</code> over a delimited partition starting at <code>offset
   * </code>. If it is <code>false</code>, precedence is given to the partition that does not end at
   * <code>offset</code>. This is only supported if the connected <code>DocumentPartitioner</code>
   * supports it, i.e. implements <code>DocumentPartitionerExtension2</code>. Otherwise, <code>
   * preferOpenPartitions</code> is ignored.
   *
   * @param partitioning the partitioning
   * @param offset the document offset
   * @param preferOpenPartitions <code>true</code> if precedence should be given to a open partition
   *     ending at <code>offset</code> over a closed partition starting at <code>offset</code>
   * @return a specification of the partition
   * @throws BadLocationException if offset is invalid in this document
   * @throws BadPartitioningException if partitioning is invalid for this document
   */
  TypedRegion getPartition(String partitioning, int offset, boolean preferOpenPartitions)
      throws BadLocationException, BadPartitioningException;

  /**
   * Computes the partitioning of the given document range based on the given partitioning type.
   *
   * <p>If <code>includeZeroLengthPartitions</code> is <code>true</code>, a zero-length partition of
   * an open partition type (usually the default partition) is included between two closed
   * partitions. If it is <code>false</code>, no zero-length partitions are included. This is only
   * supported if the connected <code>DocumentPartitioner</code> supports it, i.e. implements <code>
   * DocumentPartitionerExtension2</code>. Otherwise, <code>includeZeroLengthPartitions</code> is
   * ignored.
   *
   * @param partitioning the document's partitioning type
   * @param offset the document offset at which the range starts
   * @param length the length of the document range
   * @param includeZeroLengthPartitions <code>true</code> if zero-length partitions should be
   *     returned as part of the computed partitioning
   * @return a specification of the range's partitioning
   * @throws BadLocationException if the range is invalid in this document$
   * @throws BadPartitioningException if partitioning is invalid for this document
   */
  TypedRegion[] computePartitioning(
      String partitioning, int offset, int length, boolean includeZeroLengthPartitions)
      throws BadLocationException, BadPartitioningException;

  /**
   * Sets this document's partitioner. The caller of this method is responsible for disconnecting
   * the document's old partitioner from the document and to connect the new partitioner to the
   * document. Informs all document partitioning listeners about this change.
   *
   * @param partitioning the partitioning for which to set the partitioner
   * @param partitioner the document's new partitioner
   * @see DocumentPartitioningListener
   */
  void setDocumentPartitioner(String partitioning, DocumentPartitioner partitioner);

  /**
   * Returns the partitioner for the given partitioning or <code>null</code> if no partitioner is
   * registered.
   *
   * @param partitioning the partitioning for which to set the partitioner
   * @return the partitioner for the given partitioning
   */
  DocumentPartitioner getDocumentPartitioner(String partitioning);

  /* --------------- text access and manipulation --------------------------- */

  /**
   * Returns the character at the given document offset in this document.
   *
   * @param offset a document offset
   * @return the character at the offset
   * @throws BadLocationException if the offset is invalid in this document
   */
  char getChar(int offset) throws BadLocationException;

  /**
   * Returns the number of characters in this document.
   *
   * @return the number of characters in this document
   */
  int getLength();

  /**
   * Returns this document's complete text.
   *
   * @return the document's complete text
   */
  String get();

  /**
   * Returns this document's text for the specified range.
   *
   * @param offset the document offset
   * @param length the length of the specified range
   * @return the document's text for the specified range
   * @throws BadLocationException if the range is invalid in this document
   */
  String get(int offset, int length) throws BadLocationException;

  /**
   * Replaces the content of the document with the given text. Sends a <code>DocumentEvent</code> to
   * all registered <code>DocumentListener</code>. This method is a convenience method for <code>
   * replace(0, getLength(), text)</code>.
   *
   * @param text the new content of the document
   * @see DocumentEvent
   * @see DocumentListener
   */
  void set(String text);

  /**
   * Substitutes the given text for the specified document range. Sends a <code>DocumentEvent</code>
   * to all registered <code>DocumentListener</code>.
   *
   * @param offset the document offset
   * @param length the length of the specified range
   * @param text the substitution text
   * @throws BadLocationException if the offset is invalid in this document
   * @see DocumentEvent
   * @see DocumentListener
   */
  void replace(int offset, int length, String text) throws BadLocationException;

  /**
   * Registers the document listener with the document. After registration the DocumentListener is
   * informed about each change of this document. If the listener is already registered nothing
   * happens.
   *
   * <p>An <code>DocumentListener</code> may call back to this method when being inside a document
   * notification.
   *
   * @param listener the listener to be registered
   */
  void addDocumentListener(DocumentListener listener);

  /**
   * Removes the listener from the document's list of document listeners. If the listener is not
   * registered with the document nothing happens.
   *
   * <p>An <code>DocumentListener</code> may call back to this method when being inside a document
   * notification.
   *
   * @param listener the listener to be removed
   */
  void removeDocumentListener(DocumentListener listener);

  // /**
  // * Adds the given document listener as one which is notified before
  // * those document listeners added with <code>addDocumentListener</code>
  // * are notified. If the given listener is also registered using
  // * <code>addDocumentListener</code> it will be notified twice.
  // * If the listener is already registered nothing happens.<p>
  // *
  // * This method is not for public use.
  // *
  // * @param documentAdapter the listener to be added as pre-notified document listener
  // *
  // * @see #removePrenotifiedDocumentListener(DocumentListener)
  // */
  // void addPrenotifiedDocumentListener(DocumentListener documentAdapter);
  //
  // /**
  // * Removes the given document listener from the document's list of
  // * pre-notified document listeners. If the listener is not registered
  // * with the document nothing happens. <p>
  // *
  // * This method is not for public use.
  // *
  // * @param documentAdapter the listener to be removed
  // *
  // * @see #addPrenotifiedDocumentListener(DocumentListener)
  // */
  // void removePrenotifiedDocumentListener(DocumentListener documentAdapter);

  /* -------------------------- positions ----------------------------------- */

  /**
   * Adds a new position category to the document. If the position category already exists nothing
   * happens.
   *
   * @param category the category to be added
   */
  void addPositionCategory(String category);

  /**
   * Deletes the position category from the document. All positions in this category are thus
   * deleted as well.
   *
   * @param category the category to be removed
   * @throws BadPositionCategoryException if category is undefined in this document
   */
  void removePositionCategory(String category) throws BadPositionCategoryException;

  /**
   * Returns all position categories of this document. This includes the default position category.
   *
   * @return the document's position categories
   */
  String[] getPositionCategories();

  /**
   * Checks the presence of the specified position category.
   *
   * @param category the category to check
   * @return <code>true</code> if category is defined
   */
  boolean containsPositionCategory(String category);

  /**
   * Adds the position to the document's default position category. This is a convenience method for
   * <code>addPosition(DEFAULT_CATEGORY, position)</code>.
   *
   * @param position the position to be added
   * @throws BadLocationException if position describes an invalid range in this document
   */
  void addPosition(Position position) throws BadLocationException;

  /**
   * Removes the given position from the document's default position category. This is a convenience
   * method for <code>removePosition(DEFAULT_CATEGORY, position)</code>.
   *
   * @param position the position to be removed
   */
  void removePosition(Position position);

  /**
   * Adds the position to the specified position category of the document. Positions may be added
   * multiple times. The order of the category is maintained.
   *
   * <p><strong>Note:</strong> The position is only updated on each change applied to the document
   * if a {@link PositionUpdater} has been registered that handles the given category.
   *
   * @param category the category to which to add
   * @param position the position to be added
   * @throws BadLocationException if position describes an invalid range in this document
   * @throws BadPositionCategoryException if the category is undefined in this document
   */
  void addPosition(String category, Position position)
      throws BadLocationException, BadPositionCategoryException;

  /**
   * Removes the given position from the specified position category. If the position is not part of
   * the specified category nothing happens. If the position has been added multiple times, only the
   * first occurrence is deleted.
   *
   * @param category the category from which to delete
   * @param position the position to be deleted
   * @throws BadPositionCategoryException if category is undefined in this document
   */
  void removePosition(String category, Position position) throws BadPositionCategoryException;

  /**
   * Returns all positions of the given position category. The positions are ordered according to
   * the category's order. Manipulating this list does not affect the document, but manipulating the
   * position does affect the document.
   *
   * @param category the category
   * @return the list of all positions
   * @throws BadPositionCategoryException if category is undefined in this document
   */
  Position[] getPositions(String category) throws BadPositionCategoryException;

  /**
   * Determines whether a position described by the parameters is managed by this document.
   *
   * @param category the category to check
   * @param offset the offset of the position to find
   * @param length the length of the position to find
   * @return <code>true</code> if position is found
   */
  boolean containsPosition(String category, int offset, int length);

  /**
   * Computes the index at which a <code>Position</code> with the specified offset would be inserted
   * into the given category. As the ordering inside a category only depends on the offset, the
   * index must be chosen to be the first of all positions with the same offset.
   *
   * @param category the category in which would be added
   * @param offset the position offset to be considered
   * @return the index into the category
   * @throws BadLocationException if offset is invalid in this document
   * @throws BadPositionCategoryException if category is undefined in this document
   */
  int computeIndexInCategory(String category, int offset)
      throws BadLocationException, BadPositionCategoryException;

  /**
   * Appends a new position updater to the document's list of position updaters. Position updaters
   * may be added multiple times.
   *
   * <p>An <code>IPositionUpdater</code> may call back to this method when being inside a document
   * notification.
   *
   * @param updater the updater to be added
   */
  void addPositionUpdater(PositionUpdater updater);

  /**
   * Removes the position updater from the document's list of position updaters. If the position
   * updater has multiple occurrences only the first occurrence is removed. If the position updater
   * is not registered with this document, nothing happens.
   *
   * <p>An <code>IPositionUpdater</code> may call back to this method when being inside a document
   * notification.
   *
   * @param updater the updater to be removed
   */
  void removePositionUpdater(PositionUpdater updater);

  /**
   * Inserts the position updater at the specified index in the document's list of position
   * updaters. Positions updaters may be inserted multiple times.
   *
   * <p>An <code>IPositionUpdater</code> may call back to this method when being inside a document
   * notification.
   *
   * @param updater the updater to be inserted
   * @param index the index in the document's updater list
   */
  void insertPositionUpdater(PositionUpdater updater, int index);

  /**
   * Returns the list of position updaters attached to the document.
   *
   * @return the list of position updaters
   */
  PositionUpdater[] getPositionUpdaters();

  /* -------------------------- partitions ---------------------------------- */

  /**
   * Returns the set of legal content types of document partitions. This set can be empty. The set
   * can contain more content types than contained by the result of <code>
   * getPartitioning(0, getLength())</code>.
   *
   * <p>Use {@link #getLegalContentTypes(String)} when the document supports multiple partitionings.
   * In that case this method is equivalent to:
   *
   * <p>
   *
   * <pre>
   * DocumentExtension3 extension = (DocumentExtension3)document;
   * return extension.getLegalContentTypes(DocumentExtension3.DEFAULT_PARTITIONING);
   * </pre>
   *
   * @return the set of legal content types
   */
  String[] getLegalContentTypes();

  /**
   * Returns the type of the document partition containing the given offset. This is a convenience
   * method for <code>getPartition(offset).getType()</code>.
   *
   * <p>Use {@link #getContentType(String, int, boolean)} when the document supports multiple
   * partitionings. In that case this method is equivalent to:
   *
   * <p>
   *
   * <pre>
   * DocumentExtension3 extension = (DocumentExtension3)document;
   * return extension.getContentType(DocumentExtension3.DEFAULT_PARTITIONING, offset, false);
   * </pre>
   *
   * @param offset the document offset
   * @return the partition type
   * @throws BadLocationException if offset is invalid in this document
   */
  String getContentType(int offset) throws BadLocationException;

  /**
   * Returns the document partition in which the position is located.
   *
   * <p>Use {@link #getPartition(String, int, boolean)} when the document supports multiple
   * partitionings. In that case this method is equivalent:
   *
   * <pre>
   * DocumentExtension3 extension= (DocumentExtension3) document;
   * return extension.getPartition(DocumentExtension3.DEFAULT_PARTITIONING, offset, false);
   * </pre>
   *
   * @param offset the document offset
   * @return a specification of the partition
   * @throws BadLocationException if offset is invalid in this document
   */
  TypedRegion getPartition(int offset) throws BadLocationException;

  /**
   * Computes the partitioning of the given document range using the document's partitioner.
   *
   * <p>Use {@link #computePartitioning(String, int, int, boolean)} when the document supports
   * multiple partitionings. In that case this method is equivalent:
   *
   * <pre>
   * DocumentExtension3 extension= (DocumentExtension3) document;
   * return extension.computePartitioning(DocumentExtension3.DEFAULT_PARTITIONING, offset, length, false);
   * </pre>
   *
   * @param offset the document offset at which the range starts
   * @param length the length of the document range
   * @return a specification of the range's partitioning
   * @throws BadLocationException if the range is invalid in this document
   */
  TypedRegion[] computePartitioning(int offset, int length) throws BadLocationException;

  /**
   * Registers the document partitioning listener with the document. After registration the document
   * partitioning listener is informed about each partition change cause by a document manipulation
   * or by changing the document's partitioner. If a document partitioning listener is also a
   * document listener, the following notification sequence is guaranteed if a document manipulation
   * changes the document partitioning:
   *
   * <ul>
   *   <li>listener.documentAboutToBeChanged(DocumentEvent);
   *   <li>listener.documentPartitioningChanged();
   *   <li>listener.documentChanged(DocumentEvent);
   * </ul>
   *
   * If the listener is already registered nothing happens.
   *
   * <p>An <code>DocumentPartitioningListener</code> may call back to this method when being inside
   * a document notification.
   *
   * @param listener the listener to be added
   */
  void addDocumentPartitioningListener(DocumentPartitioningListener listener);

  /**
   * Removes the listener from this document's list of document partitioning listeners. If the
   * listener is not registered with the document nothing happens.
   *
   * <p>An <code>DocumentPartitioningListener</code> may call back to this method when being inside
   * a document notification.
   *
   * @param listener the listener to be removed
   */
  void removeDocumentPartitioningListener(DocumentPartitioningListener listener);

  /**
   * Sets this document's partitioner. The caller of this method is responsible for disconnecting
   * the document's old partitioner from the document and to connect the new partitioner to the
   * document. Informs all document partitioning listeners about this change.
   *
   * <p>Use {@link #setDocumentPartitioner(String, DocumentPartitioner)} when the document supports
   * multiple partitionings. In that case this method is equivalent to:
   *
   * <pre>
   * DocumentExtension3 extension= (DocumentExtension3) document;
   * extension.setDocumentPartitioner(DocumentExtension3.DEFAULT_PARTITIONING, partitioner);
   * </pre>
   *
   * @param partitioner the document's new partitioner
   * @see DocumentPartitioningListener
   */
  void setDocumentPartitioner(DocumentPartitioner partitioner);

  /**
   * Returns this document's partitioner.
   *
   * <p>Use {@link #getDocumentPartitioner(String)} when the document supports multiple
   * partitionings. In that case this method is equivalent to:
   *
   * <pre>
   * DocumentExtension3 extension= (DocumentExtension3) document;
   * return extension.getDocumentPartitioner(DocumentExtension3.DEFAULT_PARTITIONING);
   * </pre>
   *
   * @return this document's partitioner
   */
  DocumentPartitioner getDocumentPartitioner();

  /* ---------------------- line information -------------------------------- */

  /**
   * Returns the length of the given line including the line's delimiter.
   *
   * @param line the line of interest
   * @return the length of the line
   * @throws BadLocationException if the line number is invalid in this document
   */
  int getLineLength(int line) throws BadLocationException;

  /**
   * Returns the number of the line at which the character of the specified position is located. The
   * first line has the line number 0. A new line starts directly after a line delimiter. <code>
   * (offset == document length)</code> is a valid argument although there is no corresponding
   * character.
   *
   * @param offset the document offset
   * @return the number of the line
   * @throws BadLocationException if the offset is invalid in this document
   */
  int getLineOfOffset(int offset) throws BadLocationException;

  /**
   * Determines the offset of the first character of the given line.
   *
   * @param line the line of interest
   * @return the document offset
   * @throws BadLocationException if the line number is invalid in this document
   */
  int getLineOffset(int line) throws BadLocationException;

  /**
   * Returns a description of the specified line. The line is described by its offset and its length
   * excluding the line's delimiter.
   *
   * @param line the line of interest
   * @return a line description
   * @throws BadLocationException if the line number is invalid in this document
   */
  Region getLineInformation(int line) throws BadLocationException;

  /**
   * Returns a description of the line at the given offset. The description contains the offset and
   * the length of the line excluding the line's delimiter.
   *
   * @param offset the offset whose line should be described
   * @return a region describing the line
   * @throws BadLocationException if offset is invalid in this document
   */
  Region getLineInformationOfOffset(int offset) throws BadLocationException;

  /**
   * Returns the number of lines in this document
   *
   * @return the number of lines in this document
   */
  int getNumberOfLines();

  /**
   * Returns the number of lines which are occupied by a given text range.
   *
   * @param offset the offset of the specified text range
   * @param length the length of the specified text range
   * @return the number of lines occupied by the specified range
   * @throws BadLocationException if specified range is invalid in this tracker
   */
  int getNumberOfLines(int offset, int length) throws BadLocationException;

  /**
   * Computes the number of lines in the given text. For a given implementer of this interface this
   * method returns the same result as <code>set(text); getNumberOfLines()</code>.
   *
   * @param text the text whose number of lines should be computed
   * @return the number of lines in the given text
   */
  int computeNumberOfLines(String text);

  /* ------------------ line delimiter conversion --------------------------- */

  /**
   * Returns the document's legal line delimiters.
   *
   * @return the document's legal line delimiters
   */
  String[] getLegalLineDelimiters();

  /**
   * Returns the line delimiter of that line or <code>null</code> if the line is not closed with a
   * line delimiter.
   *
   * @param line the line of interest
   * @return the line's delimiter or <code>null</code> if line does not have a delimiter
   * @throws BadLocationException if the line number is invalid in this document
   */
  String getLineDelimiter(int line) throws BadLocationException;

  /**
   * Returns all positions of the given category that are inside the given region.
   *
   * @param category the position category
   * @param offset the start position of the region, must be >= 0
   * @param length the length of the region, must be >= 0
   * @param canStartBefore if <code>true</code> then positions are included which start before the
   *     region if they end at or after the regions start
   * @param canEndAfter if <code>true</code> then positions are included which end after the region
   *     if they start at or before the regions end
   * @return all positions inside the region of the given category
   * @throws BadPositionCategoryException if category is undefined in this document
   */
  Position[] getPositions(
      String category, int offset, int length, boolean canStartBefore, boolean canEndAfter)
      throws BadPositionCategoryException;
}
