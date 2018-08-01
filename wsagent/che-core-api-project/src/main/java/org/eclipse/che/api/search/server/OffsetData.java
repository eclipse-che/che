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
package org.eclipse.che.api.search.server;

public class OffsetData {

  private final String phrase;
  private final int startOffset;
  private final int endOffset;
  private final float score;
  private final int lineNum;
  private final String line;

  public OffsetData(
      String phrase, int startOffset, int endOffset, float score, int lineNum, String line) {
    this.phrase = phrase;
    this.startOffset = startOffset;
    this.endOffset = endOffset;
    this.score = score;
    this.lineNum = lineNum;
    this.line = line;
  }

  public String getPhrase() {
    return phrase;
  }

  public int getStartOffset() {
    return startOffset;
  }

  public int getEndOffset() {
    return endOffset;
  }

  public float getScore() {
    return score;
  }

  public int getLineNum() {
    return lineNum;
  }

  public String getLine() {
    return line;
  }

  @Override
  public String toString() {
    return "OffsetData{"
        + "phrase='"
        + phrase
        + '\''
        + ", startOffset="
        + startOffset
        + ", endOffset="
        + endOffset
        + ", score="
        + score
        + ", lineNum="
        + lineNum
        + ", line='"
        + line
        + '\''
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof OffsetData)) {
      return false;
    }

    OffsetData that = (OffsetData) o;

    if (getStartOffset() != that.getStartOffset()) {
      return false;
    }
    if (getEndOffset() != that.getEndOffset()) {
      return false;
    }
    if (Float.compare(that.getScore(), getScore()) != 0) {
      return false;
    }
    if (getLineNum() != that.getLineNum()) {
      return false;
    }
    if (getPhrase() != null ? !getPhrase().equals(that.getPhrase()) : that.getPhrase() != null) {
      return false;
    }
    return getLine() != null ? getLine().equals(that.getLine()) : that.getLine() == null;
  }

  @Override
  public int hashCode() {
    int result = getPhrase() != null ? getPhrase().hashCode() : 0;
    result = 31 * result + getStartOffset();
    result = 31 * result + getEndOffset();
    result = 31 * result + (getScore() != +0.0f ? Float.floatToIntBits(getScore()) : 0);
    result = 31 * result + getLineNum();
    result = 31 * result + (getLine() != null ? getLine().hashCode() : 0);
    return result;
  }
}
