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
package org.eclipse.che.ide.api.editor.text;

/**
 * Range of text described using linear position (ie by character index starting from the text
 * beginning).<br>
 * The range is normalized so that length is always non negative.
 */
public final class LinearRange {

  /** The offset of the start of the range. */
  private final int startOffset;
  /** The length of the range. */
  private final int length;

  private LinearRange(final int startIndex, final int length) {
    this.startOffset = startIndex;
    this.length = length;
  }

  /**
   * Returns the offset of the start of the range.
   *
   * @return the start offset
   */
  public int getStartOffset() {
    return startOffset;
  }

  /**
   * Returns the length the range.<br>
   * The length is guaranteed to be non-negative
   *
   * @return the length
   */
  public int getLength() {
    return length;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + length;
    result = prime * result + startOffset;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    LinearRange other = (LinearRange) obj;
    if (length != other.length) {
      return false;
    }
    if (startOffset != other.startOffset) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "LinearRange [startOffset=" + startOffset + ", length=" + length + "]";
  }

  /**
   * Begins range instantiation with the given range start.
   *
   * @param startOffset the range start
   * @return a {@link PartialLinearRange}
   */
  public static PartialLinearRange createWithStart(final int startOffset) {
    return new PartialLinearRange(startOffset);
  }

  /** Intermediate class for partially initialized {@link LinearRange} instances. */
  public static class PartialLinearRange {

    /** the start offset. */
    private final int startOffset;

    private PartialLinearRange(final int startOffset) {
      this.startOffset = startOffset;
    }

    /**
     * Completes {@link LinearRange} instantiation with the given length.
     *
     * @param length the range length
     * @return the range
     */
    public LinearRange andLength(int length) {
      if (length >= 0) {
        return new LinearRange(this.startOffset, length);
      } else {
        throw new IllegalArgumentException(
            "Incoherent range - start=" + this.startOffset + " length=" + length);
      }
    }

    /**
     * Completes {@link LinearRange} instantiation with the given end.
     *
     * @param length the range end
     * @return the range
     */
    public LinearRange andEnd(int endOffset) {
      if (endOffset > this.startOffset) {
        return new LinearRange(startOffset, endOffset - startOffset);
      } else {
        return new LinearRange(endOffset, startOffset - endOffset);
      }
    }
  }
}
