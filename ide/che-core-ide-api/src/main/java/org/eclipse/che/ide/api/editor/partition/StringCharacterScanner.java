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

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import java.util.List;

/** Implementation of {@link CharacterScanner} with a String as input. */
public class StringCharacterScanner implements CharacterScanner {

  /** The scanned content. */
  private String content;

  /** The offset of the scanner within the content. */
  private int offset = 0;

  /** The delimiters of the document. */
  private List<String> delimiters;

  @Override
  public List<String> getLegalLineDelimiters() {
    return this.delimiters;
  }

  @Override
  public int getColumn() {
    return getColumn(this.offset);
  }

  private int getColumn(int offset) {
    // Bad and slow implementation
    if (offset < 0 || offset > this.content.length()) {
      return -1;
    }
    if (this.delimiters == null || this.delimiters.isEmpty()) {
      return offset;
    }

    final StringBuilder sb = new StringBuilder("[");
    for (final String delimiter : this.delimiters) {
      sb.append("(?:").append(delimiter).append(")");
    }
    sb.append("]");
    final RegExp regexp = RegExp.compile(sb.toString());

    final String split = this.content;
    int currentIndex = 0;
    while (currentIndex < offset) {
      final MatchResult matchResult = regexp.exec(split);
      final int found = matchResult.getIndex();
      if (found < 0) {
        throw new RuntimeException("Invalid index for regexp match");
      }
      if (currentIndex + found > offset) {
        // we're on the same line as offset
        return offset - currentIndex;
      }
      currentIndex = currentIndex + found + 1;
    }
    return -1;
  }

  @Override
  public int read() {
    if (this.offset == this.content.length()) {
      return EOF;
    }
    final int result = this.content.charAt(this.offset);
    this.offset++;
    return result;
  }

  @Override
  public void unread() {
    if (this.offset != 0) {
      this.offset--;
    } // else already at doc start
  }

  public void setLegalLineDelimiters(final List<String> delimiters) {
    this.delimiters = delimiters;
  }

  public void setScannedString(final String content) {
    this.content = content;
    this.offset = 0;
  }
}
