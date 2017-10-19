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
 * A java break iterator. It returns all breaks, including before and after whitespace, and it
 * returns all camel case breaks.
 *
 * <p>A line break may be any of "\n", "\r", "\r\n", "\n\r".
 *
 * @since 3.0
 */
public class JavaBreakIterator extends BreakIterator {

  /** A run of common characters. */
  protected abstract static class Run {
    /** The length of this run. */
    protected int length;

    public Run() {
      init();
    }

    /**
     * Returns <code>true</code> if this run consumes <code>ch</code>, <code>false</code> otherwise.
     * If <code>true</code> is returned, the length of the receiver is adjusted accordingly.
     *
     * @param ch the character to test
     * @return <code>true</code> if <code>ch</code> was consumed
     */
    protected boolean consume(char ch) {
      if (isValid(ch)) {
        length++;
        return true;
      }
      return false;
    }

    /**
     * Whether this run accepts that character; does not update state. Called from the default
     * implementation of <code>consume</code>.
     *
     * @param ch the character to test
     * @return <code>true</code> if <code>ch</code> is accepted
     */
    protected abstract boolean isValid(char ch);

    /** Resets this run to the initial state. */
    protected void init() {
      length = 0;
    }
  }

  static final class Whitespace extends Run {
    @Override
    protected boolean isValid(char ch) {
      return Character.isWhitespace(ch) && ch != '\n' && ch != '\r';
    }
  }

  static final class LineDelimiter extends Run {
    /** State: INIT -> delimiter -> EXIT. */
    private char fState;

    private static final char INIT = '\0';
    private static final char EXIT = '\1';

    /*
     * @see org.eclipse.jdt.internal.ui.text.JavaBreakIterator.Run#init()
     */
    @Override
    protected void init() {
      super.init();
      fState = INIT;
    }

    /*
     * @see org.eclipse.jdt.internal.ui.text.JavaBreakIterator.Run#consume(char)
     */
    @Override
    protected boolean consume(char ch) {
      if (!isValid(ch) || fState == EXIT) return false;

      if (fState == INIT) {
        fState = ch;
        length++;
        return true;
      } else if (fState != ch) {
        fState = EXIT;
        length++;
        return true;
      } else {
        return false;
      }
    }

    @Override
    protected boolean isValid(char ch) {
      return ch == '\n' || ch == '\r';
    }
  }

  static final class Identifier extends Run {
    /*
     * @see org.eclipse.jdt.internal.ui.text.JavaBreakIterator.Run#isValid(char)
     */
    @Override
    protected boolean isValid(char ch) {
      return Character.isJavaIdentifierPart(ch);
    }
  }

  static final class CamelCaseIdentifier extends Run {
    /* states */
    private static final int S_INIT = 0;
    private static final int S_LOWER = 1;
    private static final int S_ONE_CAP = 2;
    private static final int S_ALL_CAPS = 3;
    private static final int S_EXIT = 4;
    private static final int S_EXIT_MINUS_ONE = 5;

    /* character types */
    private static final int K_INVALID = 0;
    private static final int K_LOWER = 1;
    private static final int K_UPPER = 2;
    private static final int K_OTHER = 3;

    private int fState;

    private static final int[][] MATRIX =
        new int[][] {
          // K_INVALID, K_LOWER,           K_UPPER,    K_OTHER
          {S_EXIT, S_LOWER, S_ONE_CAP, S_LOWER}, // S_INIT
          {S_EXIT, S_LOWER, S_EXIT, S_LOWER}, // S_LOWER
          {S_EXIT, S_LOWER, S_ALL_CAPS, S_LOWER}, // S_ONE_CAP
          {S_EXIT, S_EXIT_MINUS_ONE, S_ALL_CAPS, S_LOWER}, // S_ALL_CAPS
        };

    /*
     * @see org.eclipse.jdt.internal.ui.text.JavaBreakIterator.Run#init()
     */
    @Override
    protected void init() {
      super.init();
      fState = S_INIT;
    }

    /*
     * @see org.eclipse.jdt.internal.ui.text.JavaBreakIterator.Run#consumes(char)
     */
    @Override
    protected boolean consume(char ch) {
      int kind = getKind(ch);
      fState = MATRIX[fState][kind];
      switch (fState) {
        case S_LOWER:
        case S_ONE_CAP:
        case S_ALL_CAPS:
          length++;
          return true;
        case S_EXIT:
          return false;
        case S_EXIT_MINUS_ONE:
          length--;
          return false;
        default:
          Assert.isTrue(false);
          return false;
      }
    }

    /**
     * Determines the kind of a character.
     *
     * @param ch the character to test
     */
    private int getKind(char ch) {
      if (Character.isUpperCase(ch)) return K_UPPER;
      if (Character.isLowerCase(ch)) return K_LOWER;
      if (Character.isJavaIdentifierPart(ch)) // _, digits...
      return K_OTHER;
      return K_INVALID;
    }

    /*
     * @see org.eclipse.jdt.internal.ui.text.JavaBreakIterator.Run#isValid(char)
     */
    @Override
    protected boolean isValid(char ch) {
      return Character.isJavaIdentifierPart(ch);
    }
  }

  static final class Other extends Run {
    /*
     * @see org.eclipse.jdt.internal.ui.text.JavaBreakIterator.Run#isValid(char)
     */
    @Override
    protected boolean isValid(char ch) {
      return !Character.isWhitespace(ch) && !Character.isJavaIdentifierPart(ch);
    }
  }

  private static final Run WHITESPACE = new Whitespace();
  private static final Run DELIMITER = new LineDelimiter();
  private static final Run CAMELCASE = new CamelCaseIdentifier(); // new Identifier();
  private static final Run OTHER = new Other();

  /** The platform break iterator (word instance) used as a base. */
  protected final BreakIterator fIterator;
  /** The text we operate on. */
  protected CharSequence fText;
  /** our current position for the stateful methods. */
  private int fIndex;

  /** Creates a new break iterator. */
  public JavaBreakIterator() {
    fIterator = BreakIterator.getWordInstance();
    fIndex = fIterator.current();
  }

  /*
   * @see java.text.BreakIterator#current()
   */
  @Override
  public int current() {
    return fIndex;
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
   * @see java.text.BreakIterator#following(int)
   */
  @Override
  public int following(int offset) {
    // work around too eager IAEs in standard implementation
    if (offset == getText().getEndIndex()) return DONE;

    int next = fIterator.following(offset);
    if (next == DONE) return DONE;

    // TODO deal with complex script word boundaries
    // Math.min(offset + run.length, next) does not work
    // since BreakIterator.getWordInstance considers _ as boundaries
    // seems to work fine, however
    Run run = consumeRun(offset);
    return offset + run.length;
  }

  /**
   * Consumes a run of characters at the limits of which we introduce a break.
   *
   * @param offset the offset to start at
   * @return the run that was consumed
   */
  private Run consumeRun(int offset) {
    // assert offset < length

    char ch = fText.charAt(offset);
    int length = fText.length();
    Run run = getRun(ch);
    while (run.consume(ch) && offset < length - 1) {
      offset++;
      ch = fText.charAt(offset);
    }

    return run;
  }

  /**
   * Returns a run based on a character.
   *
   * @param ch the character to test
   * @return the correct character given <code>ch</code>
   */
  private Run getRun(char ch) {
    Run run;
    if (WHITESPACE.isValid(ch)) run = WHITESPACE;
    else if (DELIMITER.isValid(ch)) run = DELIMITER;
    else if (CAMELCASE.isValid(ch)) run = CAMELCASE;
    else if (OTHER.isValid(ch)) run = OTHER;
    else {
      Assert.isTrue(false);
      return null;
    }

    run.init();
    return run;
  }

  /*
   * @see java.text.BreakIterator#getText()
   */
  @Override
  public CharacterIterator getText() {
    return fIterator.getText();
  }

  /*
   * @see java.text.BreakIterator#isBoundary(int)
   */
  @Override
  public boolean isBoundary(int offset) {
    if (offset == getText().getBeginIndex()) return true;
    else return following(offset - 1) == offset;
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
   * @see java.text.BreakIterator#next()
   */
  @Override
  public int next() {
    fIndex = following(fIndex);
    return fIndex;
  }

  /*
   * @see java.text.BreakIterator#next(int)
   */
  @Override
  public int next(int n) {
    return fIterator.next(n);
  }

  /*
   * @see java.text.BreakIterator#preceding(int)
   */
  @Override
  public int preceding(int offset) {
    if (offset == getText().getBeginIndex()) return DONE;

    if (isBoundary(offset - 1)) return offset - 1;

    int previous = offset - 1;
    do {
      previous = fIterator.preceding(previous);
    } while (!isBoundary(previous));

    int last = DONE;
    while (previous < offset) {
      last = previous;
      previous = following(previous);
    }

    return last;
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
   * @see java.text.BreakIterator#setText(java.lang.String)
   */
  @Override
  public void setText(String newText) {
    setText((CharSequence) newText);
  }

  /**
   * Creates a break iterator given a char sequence.
   *
   * @param newText the new text
   */
  public void setText(CharSequence newText) {
    fText = newText;
    fIterator.setText(new SequenceCharacterIterator(newText));
    first();
  }

  /*
   * @see java.text.BreakIterator#setText(java.text.CharacterIterator)
   */
  @Override
  public void setText(CharacterIterator newText) {
    if (newText instanceof CharSequence) {
      fText = (CharSequence) newText;
      fIterator.setText(newText);
      first();
    } else {
      throw new UnsupportedOperationException("CharacterIterator not supported"); // $NON-NLS-1$
    }
  }
}
