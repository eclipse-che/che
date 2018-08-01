/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.editor.partition;

import java.util.List;

/** A {@link TokenScanner} that detects partitions. */
public interface PartitionScanner extends TokenScanner {

  /**
   * Set the list of line delimiters.
   *
   * @param delimiters the delimiters
   */
  void setLegalLineDelimiters(final List<String> delimiters);

  /**
   * Set the string to scan.
   *
   * @param content the new content to parse
   */
  @Override
  void setScannedString(String content);
}
