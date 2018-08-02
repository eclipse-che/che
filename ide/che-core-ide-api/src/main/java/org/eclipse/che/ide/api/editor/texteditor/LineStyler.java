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
package org.eclipse.che.ide.api.editor.texteditor;

/** Interface for editors that allow line styling. */
public interface LineStyler {

  /**
   * Adds the styles to the line.
   *
   * @param lineNumber the line number
   * @param styles the styles to add
   */
  void addLineStyles(int lineNumber, String... styles);

  /**
   * Removes the styles on the line.
   *
   * @param lineNumber the line number
   * @param styles the styles to remove
   */
  void removeLineStyles(int lineNumber, String... styles);

  /**
   * Removes all styles on the line.
   *
   * @param lineNumber the line number
   */
  void clearLineStyles(int lineNumber);

  /** Components that hold a line styler. */
  interface HasLineStyler {
    /**
     * Returns the LineStyler instance
     *
     * @return the line styler instance
     */
    LineStyler getLineStyler();
  }
}
