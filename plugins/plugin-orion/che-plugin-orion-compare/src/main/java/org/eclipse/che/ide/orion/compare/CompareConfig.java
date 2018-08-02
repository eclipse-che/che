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
package org.eclipse.che.ide.orion.compare;

/**
 * This object describes the options for <code>compare</code>.
 *
 * @author Evgen Vidolob
 */
public interface CompareConfig {

  /**
   * the options of the file that is original.
   *
   * @param oldFile
   */
  void setOldFile(FileOptions oldFile);

  /**
   * the options of the file that is compared against the original.
   *
   * @param newFile
   */
  void setNewFile(FileOptions newFile);

  /**
   * whether or not to show the two file names on each side of the compare view.
   *
   * @param showTitle
   */
  void setShowTitle(boolean showTitle);

  /**
   * whether or not to show the current line and column number fo the caret on each side of the
   * view.
   *
   * @param showLineStatus
   */
  void setShowLineStatus(boolean showLineStatus);

  /**
   * Convert this object to JSON.
   *
   * @return JSON string
   */
  String toJson();
}
