/**
 * ***************************************************************************** Copyright (c) 2008,
 * 2013 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jface.viewers;

/**
 * A mutable string with styled ranges. All ranges mark substrings of the string and do not overlap.
 * Styles are applied using instances of {@link Styler} to compute the result of {@link
 * #getStyleRanges()}.
 *
 * <p>The styled string can be built in the following two ways:
 *
 * <ul>
 *   <li>new strings with stylers can be appended
 *   <li>stylers can by applied to ranges of the existing string
 * </ul>
 *
 * <p>This class may be instantiated; it is not intended to be subclassed.
 *
 * @since 3.4
 */
public class StyledString {

  public abstract static class Styler {

    public abstract String applyStyles(String text);
  }

  public static Styler QUALIFIER_STYLER = new DefaultStyler("#FQN#");

  public static Styler COUNTER_STYLER = new DefaultStyler("#COUNTER#");

  private StringBuilder builder;

  /** @param string */
  public StyledString(String string) {
    this();
    builder.append(string);
  }

  /** */
  public StyledString() {
    builder = new StringBuilder();
  }

  /** @param cs */
  public StyledString(char[] cs) {
    this();
    append(cs);
  }

  /**
   * @param name
   * @param styler
   */
  public StyledString(String name, Styler styler) {
    this();
    append(name, styler);
  }

  /** @return */
  public String getString() {
    return builder.toString();
  }

  /** @param completion */
  public StyledString append(char[] completion) {
    builder.append(htmlEncode(new String(completion)));
    return this;
  }

  /** @param c */
  public StyledString append(char c) {
    builder.append(c);
    return this;
  }

  /** @param returnTypeSeparator */
  public StyledString append(String returnTypeSeparator) {
    builder.append(returnTypeSeparator);
    return this;
  }

  /** @param qualifier */
  public StyledString append(String qualifier, Styler styler) {
    builder.append(styler.applyStyles(qualifier));
    return this;
  }

  /** @param c */
  public StyledString append(char c, Styler styler) {
    builder.append(styler.applyStyles(String.valueOf(c)));
    return this;
  }

  /** @param declaration */
  public StyledString append(char[] declaration, Styler styler) {
    builder.append(styler.applyStyles(new String(declaration)));
    return this;
  }
  /**
   * Inserts the character at the given offset. The inserted character will get the styler that is
   * already at the given offset.
   *
   * @param ch the character to insert
   * @param offset the insertion index
   * @return returns a reference to this object
   * @throws StringIndexOutOfBoundsException if <code>offset</code> is less than zero, or if <code>
   *     offset</code> is greater than the length of this object
   * @since 3.5
   */
  public StyledString insert(char ch, int offset) throws StringIndexOutOfBoundsException {
    if (offset < 0 || offset > builder.length()) {
      throw new StringIndexOutOfBoundsException(
          "Invalid offset (" + offset + ")"); // $NON-NLS-1$//$NON-NLS-2$
    }
    //        if (hasRuns()) {
    //            int runIndex = findRun(offset);
    //            if (runIndex < 0) {
    //                runIndex = -runIndex - 1;
    //            } else {
    //                runIndex = runIndex + 1;
    //            }
    //            StyleRunList styleRuns = getStyleRuns();
    //            final int size = styleRuns.size();
    //            for (int i = runIndex; i < size; i++) {
    //                StyleRun run = styleRuns.getRun(i);
    //                run.offset++;
    //            }
    //        }
    builder.insert(offset, ch);
    return this;
  }

  /**
   * Appends a string with styles to the {@link StyledString}.
   *
   * @param string the string to append
   * @return returns a reference to this object
   */
  public StyledString append(StyledString string) {
    if (string.length() == 0) {
      return this;
    }

    //        int offset = fBuffer.length();
    builder.append(string.toString());

    //        List otherRuns = string.fStyleRuns;
    //        if (otherRuns != null && !otherRuns.isEmpty()) {
    //            for (int i = 0; i < otherRuns.size(); i++) {
    //                StyleRun curr = (StyleRun) otherRuns.get(i);
    //                if (i == 0 && curr.offset != 0) {
    //                    appendStyleRun(null, offset); // appended string will
    //                    // start with the default
    //                    // color
    //                }
    //                appendStyleRun(curr.style, offset + curr.offset);
    //            }
    //        } else {
    //            appendStyleRun(null, offset); // appended string will start with
    //            // the default color
    //        }
    return this;
  }

  /**
   * Sets a styler to use for the given source range. The range must be subrange of actual string of
   * this {@link StyledString}. Stylers previously set for that range will be overwritten.
   *
   * @param offset the start offset of the range
   * @param length the length of the range
   * @param styler the styler to set
   * @throws StringIndexOutOfBoundsException if <code>start</code> is less than zero, or if offset
   *     plus length is greater than the length of this object.
   */
  public void setStyle(int offset, int length, Styler styler)
      throws StringIndexOutOfBoundsException {
    builder.replace(
        offset, offset + length, styler.applyStyles(builder.substring(offset, offset + length)));
  }

  /**
   * Returns the length of the string of this {@link StyledString}.
   *
   * @return the length of the current string
   */
  public int length() {
    return builder.length();
  }

  /**
   * HTML-encode a string. This simple method only replaces the five characters &, <, >, ", and '.
   *
   * @param input the String to convert
   * @return a new String with HTML encoded characters
   */
  public static String htmlEncode(String input) {
    String output = input.replaceAll("&", "&amp;");
    output = output.replaceAll("<", "&lt;");
    output = output.replaceAll(">", "&gt;");
    output = output.replaceAll("\"", "&quot;");
    output = output.replaceAll("'", "&#039;");
    return output;
  }
}
