/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.editor.minimap;

/** Interface for editor minimaps. */
public interface Minimap {

  /**
   * Add a mark on the minimap.<br>
   *
   * @param offset the offset where the mark is set
   * @param style the style of the mark
   * @param title the title text
   */
  void addMark(int offset, String style, String title);

  /**
   * Add a mark on the minimap.
   *
   * @param offset the offset where the mark is set
   * @param style the style of the mark
   * @param level the mark priority level
   * @param title the title text
   */
  void addMark(int offset, String style, int level, String title);

  /**
   * Remove the marks on the lines between the two given lines (included).
   *
   * @param lineStart the beginning of the range
   * @param lineEnd the end of the range
   */
  void removeMarks(int lineStart, int lineEnd);

  /** Clear all marks on the minimap. */
  void clearMarks();
}
