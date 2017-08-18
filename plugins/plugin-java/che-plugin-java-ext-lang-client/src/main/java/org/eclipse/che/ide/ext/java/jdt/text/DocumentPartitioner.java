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

import org.eclipse.che.ide.api.editor.text.TypedRegion;

/**
 * A document partitioner divides a document into a set of disjoint text partitions. Each partition
 * has a content type, an offset, and a length. The document partitioner is connected to one
 * document and informed about all changes of this document before any of the document's document
 * listeners. A document partitioner can thus incrementally update on the receipt of a document
 * change event.
 *
 * <p>
 *
 * <p>In order to provided backward compatibility for clients of <code>DocumentPartitioner</code>,
 * extension interfaces are used to provide a means of evolution.
 *
 * <p>Clients may implement this interface and its extension interfaces or use the standard
 * implementation <code>DefaultPartitioner</code>.
 *
 * @see Document
 */
public interface DocumentPartitioner {

  /**
   * Connects the partitioner to a document. Connect indicates the begin of the usage of the
   * receiver as partitioner of the given document. Thus, resources the partitioner needs to be
   * operational for this document should be allocated.
   *
   * <p>
   *
   * <p>The caller of this method must ensure that this partitioner is also set as the document's
   * document partitioner.
   *
   * @param document the document to be connected to
   */
  void connect(Document document);

  /**
   * Disconnects the partitioner from the document it is connected to. Disconnect indicates the end
   * of the usage of the receiver as partitioner of the connected document. Thus, resources the
   * partitioner needed to be operation for its connected document should be deallocated.
   *
   * <p>The caller of this method should also must ensure that this partitioner is no longer the
   * document's partitioner.
   */
  void disconnect();

  /**
   * Informs about a forthcoming document change. Will be called by the connected document and is
   * not intended to be used by clients other than the connected document.
   *
   * @param event the event describing the forthcoming change
   */
  void documentAboutToBeChanged(DocumentEvent event);

  /**
   * The document has been changed. The partitioner updates the document's partitioning and returns
   * whether the structure of the document partitioning has been changed, i.e. whether partitions
   * have been added or removed. Will be called by the connected document and is not intended to be
   * used by clients other than the connected document.
   *
   * @param event the event describing the document change
   * @return <code>true</code> if partitioning changed
   */
  boolean documentChanged(DocumentEvent event);

  /**
   * Returns the set of all legal content types of this partitioner. I.e. any result delivered by
   * this partitioner may not contain a content type which would not be included in this method's
   * result.
   *
   * @return the set of legal content types
   */
  String[] getLegalContentTypes();

  /**
   * Returns the content type of the partition containing the given offset in the connected
   * document. There must be a document connected to this partitioner.
   *
   * @param offset the offset in the connected document
   * @return the content type of the offset's partition
   */
  String getContentType(int offset);

  /**
   * Returns the partitioning of the given range of the connected document. There must be a document
   * connected to this partitioner.
   *
   * @param offset the offset of the range of interest
   * @param length the length of the range of interest
   * @return the partitioning of the range
   */
  TypedRegion[] computePartitioning(int offset, int length);

  /**
   * Returns the partition containing the given offset of the connected document. There must be a
   * document connected to this partitioner.
   *
   * @param offset the offset for which to determine the partition
   * @return the partition containing the offset
   */
  TypedRegion getPartition(int offset);
}
