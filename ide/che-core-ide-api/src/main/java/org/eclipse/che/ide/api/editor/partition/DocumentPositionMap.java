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
package org.eclipse.che.ide.api.editor.partition;

import java.util.List;
import org.eclipse.che.ide.api.editor.text.BadLocationException;
import org.eclipse.che.ide.api.editor.text.BadPositionCategoryException;
import org.eclipse.che.ide.api.editor.text.TypedPosition;

/** Model for document partitions. */
public interface DocumentPositionMap {

  public static final class Categories {
    /** The identifier of the default position category. */
    public static final String DEFAULT_CATEGORY = "__dflt_position_category";
  }

  /**
   * Add a position category.
   *
   * @param category the category
   */
  void addPositionCategory(String category);

  /**
   * Tells if the given category contains a position for the given range.
   *
   * @param category the category
   * @param offset the start of the range
   * @param length the length of the range
   * @return true iff the categroy contain the position
   */
  boolean containsPosition(String category, int offset, int length);

  /**
   * Tells if the given category is known.
   *
   * @param category the category
   * @return true iff the category is known
   */
  boolean containsPositionCategory(String category);

  /**
   * Returns the positions in the given category.
   *
   * @param category the category
   * @return the opsitions
   * @throws BadPositionCategoryException if the category is invalid
   */
  List<TypedPosition> getPositions(String category) throws BadPositionCategoryException;

  int computeIndexInCategory(String category, int offset)
      throws BadLocationException, BadPositionCategoryException;

  /**
   * Returns all known categories
   *
   * @return the categories
   */
  List<String> getPositionCategories();

  void removePosition(String category, TypedPosition position) throws BadPositionCategoryException;

  /**
   * Remove a position category.
   *
   * @param category the category
   */
  void removePositionCategory(String category) throws BadPositionCategoryException;

  /**
   * Add a position with the default category.
   *
   * @param position the position
   * @throws BadLocationException when the position is invalid
   */
  void addPosition(TypedPosition position) throws BadLocationException;

  /**
   * Add a position in the category.
   *
   * @param position the position
   * @param category the category
   * @throws BadLocationException when the position is invalid
   */
  void addPosition(String category, TypedPosition position)
      throws BadLocationException, BadPositionCategoryException;

  List<TypedPosition> getPositions(
      String category, int offset, int length, boolean canStartBefore, boolean canEndAfter)
      throws BadPositionCategoryException;

  List<TypedPosition> getPositions(
      int offset, int length, boolean canStartBefore, boolean canEndAfter)
      throws BadPositionCategoryException;

  /**
   * Sets the length of the mapped content.
   *
   * @param newLength the new value
   */
  void setContentLength(int newLength);

  /** Clear known positions. */
  void resetPositions();
}
