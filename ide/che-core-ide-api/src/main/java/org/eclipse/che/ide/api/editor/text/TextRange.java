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
package org.eclipse.che.ide.api.editor.text;

/** Oriented range of text. */
public class TextRange {

  /** The start position of the range. */
  private final TextPosition from;

  /** The end position of the range. */
  private final TextPosition to;

  /**
   * TextRange constructor.
   *
   * @param from the start of the range
   * @param to the end of the range
   */
  public TextRange(final TextPosition from, final TextPosition to) {
    this.from = from;
    this.to = to;
  }

  /**
   * Return the start of the range (line-character position).
   *
   * @return the start of the range
   */
  public TextPosition getFrom() {
    return this.from;
  }

  /**
   * Return the end of the range (line-character position).
   *
   * @return the end of the range
   */
  public TextPosition getTo() {
    return this.to;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((from == null) ? 0 : from.hashCode());
    result = prime * result + ((to == null) ? 0 : to.hashCode());
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
    if (!(obj instanceof TextRange)) {
      return false;
    }
    final TextRange other = (TextRange) obj;
    if (from == null) {
      if (other.from != null) {
        return false;
      }
    } else if (!from.equals(other.from)) {
      return false;
    }
    if (to == null) {
      if (other.to != null) {
        return false;
      }
    } else if (!to.equals(other.to)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "TextRange [from=" + from + ", to=" + to + "]";
  }
}
