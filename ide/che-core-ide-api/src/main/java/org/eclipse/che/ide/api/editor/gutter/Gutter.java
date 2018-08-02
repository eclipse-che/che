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
package org.eclipse.che.ide.api.editor.gutter;

import elemental.dom.Element;

/** Interface for components that expose a gutter. */
public interface Gutter {
  /**
   * Adds a gutter item.
   *
   * @param line the line for the item
   * @param gutterId the gutter identifier
   * @param element the (DOM) element to add
   */
  void addGutterItem(int line, String gutterId, Element element);

  /**
   * Adds a gutter item.
   *
   * @param lineStart the first line of the item
   * @param lineEnd the last line of the item
   * @param gutterId the gutter identifier
   * @param element the (DOM) element to add
   */
  default void addGutterItem(int lineStart, int lineEnd, String gutterId, Element element) {
    for (int i = lineStart; i <= lineEnd; i++) {
      addGutterItem(i, gutterId, element);
    }
  }

  /**
   * Adds a gutter item.
   *
   * @param line the line for the item
   * @param gutterId the gutter identifier
   * @param element the (DOM) element to add
   * @param lineCallback callback to call when the line is removed
   */
  void addGutterItem(
      int line, String gutterId, Element element, LineNumberingChangeCallback lineCallback);

  /**
   * Remove a gutter item.
   *
   * @param line the line of the item
   * @param gutterId the gutter
   */
  void removeGutterItem(int line, String gutterId);

  /**
   * Returns the gutter item at th given line for the given gutter (if present).
   *
   * @param line the line
   * @param gutterId the gutter
   * @return the gutter element or null
   */
  Element getGutterItem(int line, String gutterId);

  /**
   * Clears the given gutter. Removes all gutter items.
   *
   * @param gutterId the gutter identifier
   */
  void clearGutter(String gutterId);

  /**
   * Sets a gutter item. If any item exists then it has to be replaced.
   *
   * @param line the line for the item
   * @param gutterId the gutter
   * @param element the (DOM) element to add
   */
  void setGutterItem(int line, String gutterId, Element element);

  /** Callback to be warned when line numbering changes (lines are removed or inserted). */
  interface LineNumberingChangeCallback {
    /** Method called when the line numbering changes. */
    void onLineNumberingChange(int fromLine, int linesRemoved, int linesAdded);
  }
}
