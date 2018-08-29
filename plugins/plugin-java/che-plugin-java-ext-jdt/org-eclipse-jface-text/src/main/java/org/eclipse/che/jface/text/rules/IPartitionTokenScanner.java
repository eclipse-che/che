/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.jface.text.rules;

import org.eclipse.jface.text.IDocument;

/**
 * A partition token scanner returns tokens that represent partitions. For that reason, a partition
 * token scanner is vulnerable in respect to the document offset it starts scanning. In a simple
 * case, a partition token scanner must always start at a partition boundary. A partition token
 * scanner can also start in the middle of a partition, if it knows the type of the partition.
 *
 * @since 2.0
 */
public interface IPartitionTokenScanner extends ITokenScanner {

  /**
   * Configures the scanner by providing access to the document range that should be scanned. The
   * range may not only contain complete partitions but starts at the beginning of a line in the
   * middle of a partition of the given content type. This requires that a partition delimiter can
   * not contain a line delimiter.
   *
   * @param document the document to scan
   * @param offset the offset of the document range to scan
   * @param length the length of the document range to scan
   * @param contentType the content type at the given offset
   * @param partitionOffset the offset at which the partition of the given offset starts
   */
  void setPartialRange(
      IDocument document, int offset, int length, String contentType, int partitionOffset);
}
