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
package org.eclipse.che.ide.api.editor.annotation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.text.BadLocationException;
import org.eclipse.che.ide.api.editor.text.BadPositionCategoryException;
import org.eclipse.che.ide.api.editor.text.Position;
import org.eclipse.che.ide.runtime.Assert;

/** */
final class PositionHolder {

  private final Document document;
  private List<Position> positions = new ArrayList<>();

  public PositionHolder(Document document) {
    this.document = document;
  }

  public void addPosition(Position position) throws BadLocationException {
    if ((0 > position.offset)
        || (0 > position.length)
        || (position.offset + position.length > document.getContentsCharCount()))
      throw new BadLocationException();

    positions.add(computeIndexInPositionList(positions, position.offset, true), position);
  }

  /**
   * Computes the index in the list of positions at which a position with the given position would
   * be inserted. The position to insert is supposed to become the first in this list of all
   * positions with the same position.
   *
   * @param positions the list in which the index is computed
   * @param offset the offset for which the index is computed
   * @param orderedByOffset <code>true</code> if ordered by offset, false if ordered by end position
   * @return the computed index
   * @since 3.4
   */
  protected int computeIndexInPositionList(
      List<? extends Position> positions, int offset, boolean orderedByOffset) {
    if (positions.size() == 0) return 0;

    int left = 0;
    int right = positions.size() - 1;
    int mid = 0;
    Position p = null;

    while (left < right) {

      mid = (left + right) / 2;

      p = positions.get(mid);
      int pOffset = getOffset(orderedByOffset, p);
      if (offset < pOffset) {
        if (left == mid) right = left;
        else right = mid - 1;
      } else if (offset > pOffset) {
        if (right == mid) left = right;
        else left = mid + 1;
      } else if (offset == pOffset) {
        left = right = mid;
      }
    }

    int pos = left;
    p = positions.get(pos);
    int pPosition = getOffset(orderedByOffset, p);
    if (offset > pPosition) {
      // append to the end
      pos++;
    } else {
      // entry will become the first of all entries with the same offset
      do {
        --pos;
        if (pos < 0) break;
        p = positions.get(pos);
        pPosition = getOffset(orderedByOffset, p);
      } while (offset == pPosition);
      ++pos;
    }

    Assert.isTrue(0 <= pos && pos <= positions.size());

    return pos;
  }

  private int getOffset(boolean orderedByOffset, Position position) {
    if (orderedByOffset || position.getLength() == 0) return position.getOffset();
    return position.getOffset() + position.getLength() - 1;
  }

  public List<Position> getPositions(
      int offset, int length, boolean canStartBefore, boolean canEndAfter)
      throws BadPositionCategoryException {
    if (canStartBefore && canEndAfter || (!canStartBefore && !canEndAfter)) {
      List<Position> documentPositions;
      if (canStartBefore && canEndAfter) {
        if (offset < getLength() / 2) {
          documentPositions = getStartingPositions(0, offset + length);
        } else {
          documentPositions = getEndingPositions(offset, getLength() - offset + 1);
        }
      } else {
        documentPositions = getStartingPositions(offset, length);
      }

      ArrayList<Position> list = new ArrayList<>(documentPositions.size());

      Position region = new Position(offset, length);

      for (Iterator<Position> iterator = documentPositions.iterator(); iterator.hasNext(); ) {
        Position position = iterator.next();
        if (isWithinRegion(region, position, canStartBefore, canEndAfter)) {
          list.add(position);
        }
      }

      return list;
    } else if (canStartBefore) {
      List<Position> list = getEndingPositions(offset, length);
      return list;
    } else {
      Assert.isLegal(canEndAfter && !canStartBefore);

      List<Position> list = getStartingPositions(offset, length);
      return list;
    }
  }

  private boolean isWithinRegion(
      Position region, Position position, boolean canStartBefore, boolean canEndAfter) {
    if (canStartBefore && canEndAfter) {
      return region.overlapsWith(position.getOffset(), position.getLength());
    } else if (canStartBefore) {
      return region.includes(position.getOffset() + position.getLength() - 1);
    } else if (canEndAfter) {
      return region.includes(position.getOffset());
    } else {
      int start = position.getOffset();
      return region.includes(start) && region.includes(start + position.getLength() - 1);
    }
  }

  private List<Position> getStartingPositions(int offset, int length)
      throws BadPositionCategoryException {

    int indexStart = computeIndexInPositionList(positions, offset, true);
    int indexEnd = computeIndexInPositionList(positions, offset + length, true);

    return positions.subList(indexStart, indexEnd);
  }

  private List<Position> getEndingPositions(int offset, int length)
      throws BadPositionCategoryException {

    int indexStart = computeIndexInPositionList(positions, offset, false);
    int indexEnd = computeIndexInPositionList(positions, offset + length, false);

    return positions.subList(indexStart, indexEnd);
  }

  private int getLength() {
    return document.getContentsCharCount();
  }
}
