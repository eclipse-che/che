/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.text;

import java.text.BreakIterator;
import java.text.CharacterIterator;
import org.eclipse.core.runtime.Assert;

/**
 * Breaks java text into word starts, also stops at line start and end. No direction dependency.
 *
 * @since 3.0
 */
public class JavaWordIterator extends BreakIterator {

  /**
   * The underlying java break iterator. It returns all breaks, including before and after every
   * whitespace.
   */
  private JavaBreakIterator fIterator;
  /** The current index for the stateful operations. */
  private int fIndex;

  /** Creates a new word iterator. */
  public JavaWordIterator() {
    fIterator = new JavaBreakIterator();
    first();
  }

  /*
   * @see java.text.BreakIterator#first()
   */
  @Override
  public int first() {
    fIndex = fIterator.first();
    return fIndex;
  }

  /*
   * @see java.text.BreakIterator#last()
   */
  @Override
  public int last() {
    fIndex = fIterator.last();
    return fIndex;
  }

  /*
   * @see java.text.BreakIterator#next(int)
   */
  @Override
  public int next(int n) {
    int next = 0;
    while (--n > 0 && next != DONE) {
      next = next();
    }
    return next;
  }

  /*
   * @see java.text.BreakIterator#next()
   */
  @Override
  public int next() {
    fIndex = following(fIndex);
    return fIndex;
  }

  /*
   * @see java.text.BreakIterator#previous()
   */
  @Override
  public int previous() {
    fIndex = preceding(fIndex);
    return fIndex;
  }

  /*
   * @see java.text.BreakIterator#preceding(int)
   */
  @Override
  public int preceding(int offset) {
    int first = fIterator.preceding(offset);
    if (isWhitespace(first, offset)) {
      int second = fIterator.preceding(first);
      if (second != DONE && !isDelimiter(second, first)) return second;
    }
    return first;
  }

  /*
   * @see java.text.BreakIterator#following(int)
   */
  @Override
  public int following(int offset) {
    int first = fIterator.following(offset);
    if (eatFollowingWhitespace(offset, first)) {
      int second = fIterator.following(first);
      if (isWhitespace(first, second)) return second;
    }
    return first;
  }

  private boolean eatFollowingWhitespace(int offset, int exclusiveEnd) {
    if (exclusiveEnd == DONE || offset == DONE) return false;

    if (isWhitespace(offset, exclusiveEnd)) return false;
    if (isDelimiter(offset, exclusiveEnd)) return false;

    return true;
  }

  /**
   * Returns <code>true</code> if the given sequence into the underlying text represents a
   * delimiter, <code>false</code> otherwise.
   *
   * @param offset the offset
   * @param exclusiveEnd the end offset
   * @return <code>true</code> if the given range is a delimiter
   */
  private boolean isDelimiter(int offset, int exclusiveEnd) {
    if (exclusiveEnd == DONE || offset == DONE) return false;

    Assert.isTrue(offset >= 0);
    Assert.isTrue(exclusiveEnd <= getText().getEndIndex());
    Assert.isTrue(exclusiveEnd > offset);

    CharSequence seq = fIterator.fText;

    while (offset < exclusiveEnd) {
      char ch = seq.charAt(offset);
      if (ch != '\n' && ch != '\r') return false;
      offset++;
    }

    return true;
  }

  /**
   * Returns <code>true</code> if the given sequence into the underlying text represents whitespace,
   * but not a delimiter, <code>false</code> otherwise.
   *
   * @param offset the offset
   * @param exclusiveEnd the end offset
   * @return <code>true</code> if the given range is whitespace
   */
  private boolean isWhitespace(int offset, int exclusiveEnd) {
    if (exclusiveEnd == DONE || offset == DONE) return false;

    Assert.isTrue(offset >= 0);
    Assert.isTrue(exclusiveEnd <= getText().getEndIndex());
    Assert.isTrue(exclusiveEnd > offset);

    CharSequence seq = fIterator.fText;

    while (offset < exclusiveEnd) {
      char ch = seq.charAt(offset);
      if (!Character.isWhitespace(ch)) return false;
      if (ch == '\n' || ch == '\r') return false;
      offset++;
    }

    return true;
  }

  /*
   * @see java.text.BreakIterator#current()
   */
  @Override
  public int current() {
    return fIndex;
  }

  /*
   * @see java.text.BreakIterator#getText()
   */
  @Override
  public CharacterIterator getText() {
    return fIterator.getText();
  }

  /**
   * Sets the text as <code>CharSequence</code>.
   *
   * @param newText the new text
   */
  public void setText(CharSequence newText) {
    fIterator.setText(newText);
    first();
  }

  /*
   * @see java.text.BreakIterator#setText(java.text.CharacterIterator)
   */
  @Override
  public void setText(CharacterIterator newText) {
    fIterator.setText(newText);
    first();
  }

  /*
   * @see java.text.BreakIterator#setText(java.lang.String)
   */
  @Override
  public void setText(String newText) {
    setText((CharSequence) newText);
  }
}
