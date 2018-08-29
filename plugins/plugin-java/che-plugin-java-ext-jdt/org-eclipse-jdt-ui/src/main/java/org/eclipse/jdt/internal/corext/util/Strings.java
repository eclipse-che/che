/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2010 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.util;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.formatter.IndentManipulation;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.ILineTracker;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.osgi.util.TextProcessor;

/** Helper class to provide String manipulation functions not available in standard JDK. */
public class Strings {

  private Strings() {}

  /**
   * Tells whether we have to use the {@link TextProcessor}
   *
   * <p>This is used for performance optimization.
   *
   * @since 3.4
   */
  public static final boolean USE_TEXT_PROCESSOR;

  static {
    String testString = "args : String[]"; // $NON-NLS-1$
    USE_TEXT_PROCESSOR = testString != TextProcessor.process(testString);
  }

  private static final String JAVA_ELEMENT_DELIMITERS =
      TextProcessor.getDefaultDelimiters() + "<>(),?{} "; // $NON-NLS-1$

  /**
   * Adds special marks so that that the given styled string is readable in a BiDi environment.
   *
   * @param styledString the styled string
   * @return the processed styled string
   * @since 3.4
   */
  public static StyledString markLTR(StyledString styledString) {

    /*
     * NOTE: For performance reasons we do not call  markLTR(styledString, null)
     */

    if (!USE_TEXT_PROCESSOR) return styledString;

    String inputString = styledString.getString();
    String string = TextProcessor.process(inputString);
    if (string != inputString) insertMarks(styledString, inputString, string);
    return styledString;
  }

  /**
   * Adds special marks so that that the given styled Java element label is readable in a BiDi
   * environment.
   *
   * @param styledString the styled string
   * @return the processed styled string
   * @since 3.6
   */
  public static StyledString markJavaElementLabelLTR(StyledString styledString) {
    if (!USE_TEXT_PROCESSOR) return styledString;

    String inputString = styledString.getString();
    String string = TextProcessor.process(inputString, JAVA_ELEMENT_DELIMITERS);
    if (string != inputString) insertMarks(styledString, inputString, string);
    return styledString;
  }

  /**
   * Adds special marks so that that the given styled string is readable in a BiDi environment.
   *
   * @param styledString the styled string
   * @param delimiters the additional delimiters
   * @return the processed styled string
   * @since 3.4
   */
  public static StyledString markLTR(StyledString styledString, String delimiters) {
    if (!USE_TEXT_PROCESSOR) return styledString;

    String inputString = styledString.getString();
    String string = TextProcessor.process(inputString, delimiters);
    if (string != inputString) insertMarks(styledString, inputString, string);
    return styledString;
  }

  /**
   * Inserts the marks into the given styled string.
   *
   * @param styledString the styled string
   * @param originalString the original string
   * @param processedString the processed string
   * @since 3.5
   */
  private static void insertMarks(
      StyledString styledString, String originalString, String processedString) {
    int originalLength = originalString.length();
    int processedStringLength = processedString.length();
    char orig = originalLength > 0 ? originalString.charAt(0) : '\0';
    for (int o = 0, p = 0; p < processedStringLength; p++) {
      char processed = processedString.charAt(p);
      if (o < originalLength) {
        if (orig == processed) {
          o++;
          if (o < originalLength) orig = originalString.charAt(o);
          continue;
        }
      }
      styledString.insert(processed, p);
    }
  }

  /**
   * Adds special marks so that that the given string is readable in a BiDi environment.
   *
   * @param string the string
   * @return the processed styled string
   * @since 3.4
   */
  public static String markLTR(String string) {
    if (!USE_TEXT_PROCESSOR) return string;

    return TextProcessor.process(string);
  }

  /**
   * Adds special marks so that that the given string is readable in a BiDi environment.
   *
   * @param string the string
   * @param delimiters the delimiters
   * @return the processed styled string
   * @since 3.4
   */
  public static String markLTR(String string, String delimiters) {
    if (!USE_TEXT_PROCESSOR) return string;

    return TextProcessor.process(string, delimiters);
  }

  /**
   * Adds special marks so that that the given Java element label is readable in a BiDi environment.
   *
   * @param string the string
   * @return the processed styled string
   * @since 3.6
   */
  public static String markJavaElementLabelLTR(String string) {
    if (!USE_TEXT_PROCESSOR) return string;

    return TextProcessor.process(string, JAVA_ELEMENT_DELIMITERS);
  }

  /**
   * Tests if a char is lower case. Fix for 26529.
   *
   * @param ch the char
   * @return return true if char is lower case
   */
  public static boolean isLowerCase(char ch) {
    return Character.toLowerCase(ch) == ch;
  }

  public static boolean startsWithIgnoreCase(String text, String prefix) {
    int textLength = text.length();
    int prefixLength = prefix.length();
    if (textLength < prefixLength) return false;
    for (int i = prefixLength - 1; i >= 0; i--) {
      if (Character.toLowerCase(prefix.charAt(i)) != Character.toLowerCase(text.charAt(i)))
        return false;
    }
    return true;
  }

  public static String removeNewLine(String message) {
    StringBuffer result = new StringBuffer();
    int current = 0;
    int index = message.indexOf('\n', 0);
    while (index != -1) {
      result.append(message.substring(current, index));
      if (current < index && index != 0) result.append(' ');
      current = index + 1;
      index = message.indexOf('\n', current);
    }
    result.append(message.substring(current));
    return result.toString();
  }

  /**
   * Converts the given string into an array of lines. The lines don't contain any line delimiter
   * characters.
   *
   * @param input the string
   * @return the string converted into an array of strings. Returns <code>
   * 	null</code> if the input string can't be converted in an array of lines.
   */
  public static String[] convertIntoLines(String input) {
    try {
      ILineTracker tracker = new DefaultLineTracker();
      tracker.set(input);
      int size = tracker.getNumberOfLines();
      String result[] = new String[size];
      for (int i = 0; i < size; i++) {
        IRegion region = tracker.getLineInformation(i);
        int offset = region.getOffset();
        result[i] = input.substring(offset, offset + region.getLength());
      }
      return result;
    } catch (BadLocationException e) {
      return null;
    }
  }

  /**
   * Returns <code>true</code> if the given string only consists of white spaces according to Java.
   * If the string is empty, <code>true
   * </code> is returned.
   *
   * @param s the string to test
   * @return <code>true</code> if the string only consists of white spaces; otherwise <code>false
   *     </code> is returned
   * @see Character#isWhitespace(char)
   */
  public static boolean containsOnlyWhitespaces(String s) {
    int size = s.length();
    for (int i = 0; i < size; i++) {
      if (!Character.isWhitespace(s.charAt(i))) return false;
    }
    return true;
  }

  /**
   * Removes leading tabs and spaces from the given string. If the string doesn't contain any
   * leading tabs or spaces then the string itself is returned.
   *
   * @param line the line
   * @return the trimmed line
   */
  public static String trimLeadingTabsAndSpaces(String line) {
    int size = line.length();
    int start = size;
    for (int i = 0; i < size; i++) {
      char c = line.charAt(i);
      if (!IndentManipulation.isIndentChar(c)) {
        start = i;
        break;
      }
    }
    if (start == 0) return line;
    else if (start == size) return ""; // $NON-NLS-1$
    else return line.substring(start);
  }

  public static String trimTrailingTabsAndSpaces(String line) {
    int size = line.length();
    int end = size;
    for (int i = size - 1; i >= 0; i--) {
      char c = line.charAt(i);
      if (IndentManipulation.isIndentChar(c)) {
        end = i;
      } else {
        break;
      }
    }
    if (end == size) return line;
    else if (end == 0) return ""; // $NON-NLS-1$
    else return line.substring(0, end);
  }

  /**
   * Returns the indent of the given string in indentation units. Odd spaces are not counted.
   *
   * @param line the text line
   * @param project the java project from which to get the formatter preferences, or <code>null
   *     </code> for global preferences
   * @return the number of indent units
   * @since 3.1
   */
  public static int computeIndentUnits(String line, IJavaProject project) {
    return IndentManipulation.measureIndentUnits(
        line, CodeFormatterUtil.getTabWidth(project), CodeFormatterUtil.getIndentWidth(project));
  }

  /**
   * Returns the indent of the given string in indentation units. Odd spaces are not counted.
   *
   * @param line the text line
   * @param tabWidth the width of the '\t' character in space equivalents
   * @param indentWidth the width of one indentation unit in space equivalents
   * @return the indent of the given string in indentation units
   * @since 3.1
   */
  public static int computeIndentUnits(String line, int tabWidth, int indentWidth) {
    return IndentManipulation.measureIndentUnits(line, tabWidth, indentWidth);
  }

  /**
   * Computes the visual length of the indentation of a <code>CharSequence</code>, counting a tab
   * character as the size until the next tab stop and every other whitespace character as one.
   *
   * @param line the string to measure the indent of
   * @param tabSize the visual size of a tab in space equivalents
   * @return the visual length of the indentation of <code>line</code>
   * @since 3.1
   */
  public static int measureIndentLength(CharSequence line, int tabSize) {
    return IndentManipulation.measureIndentInSpaces(line, tabSize);
  }

  /**
   * Removes the given number of indents from the line. Asserts that the given line has the
   * requested number of indents. If <code>indentsToRemove <= 0</code> the line is returned.
   *
   * @param line the line
   * @param indentsToRemove the indents to remove
   * @param project the java project from which to get the formatter preferences, or <code>null
   *     </code> for global preferences
   * @return the trimmed line
   * @since 3.1
   */
  public static String trimIndent(String line, int indentsToRemove, IJavaProject project) {
    return IndentManipulation.trimIndent(
        line,
        indentsToRemove,
        CodeFormatterUtil.getTabWidth(project),
        CodeFormatterUtil.getIndentWidth(project));
  }

  /**
   * Removes the given number of indents from the line. Asserts that the given line has the
   * requested number of indents. If <code>indentsToRemove <= 0</code> the line is returned.
   *
   * @param line the line
   * @param indentsToRemove the indents to remove
   * @param tabWidth the tab width
   * @param indentWidth the indent width
   * @return the trimmed line
   * @since 3.1
   */
  public static String trimIndent(String line, int indentsToRemove, int tabWidth, int indentWidth) {
    return IndentManipulation.trimIndent(line, indentsToRemove, tabWidth, indentWidth);
  }

  /**
   * Removes the common number of indents from all lines. If a line only consists out of white space
   * it is ignored.
   *
   * @param lines the lines
   * @param project the java project from which to get the formatter preferences, or <code>null
   *     </code> for global preferences
   * @since 3.1
   */
  public static void trimIndentation(String[] lines, IJavaProject project) {
    trimIndentation(
        lines,
        CodeFormatterUtil.getTabWidth(project),
        CodeFormatterUtil.getIndentWidth(project),
        true);
  }
  /**
   * Removes the common number of indents from all lines. If a line only consists out of white space
   * it is ignored.
   *
   * @param lines the lines
   * @param tabWidth the size of one tab in space equivalents
   * @param indentWidth the size of the indent in space equivalents
   * @since 3.1
   */
  public static void trimIndentation(String[] lines, int tabWidth, int indentWidth) {
    trimIndentation(lines, tabWidth, indentWidth, true);
  }

  /**
   * Removes the common number of indents from all lines. If a line only consists out of white space
   * it is ignored. If <code>
   * considerFirstLine</code> is false the first line will be ignored.
   *
   * @param lines the lines
   * @param project the java project from which to get the formatter preferences, or <code>null
   *     </code> for global preferences
   * @param considerFirstLine If <code>considerFirstLine</code> is false the first line will be
   *     ignored.
   * @since 3.1
   */
  public static void trimIndentation(
      String[] lines, IJavaProject project, boolean considerFirstLine) {
    trimIndentation(
        lines,
        CodeFormatterUtil.getTabWidth(project),
        CodeFormatterUtil.getIndentWidth(project),
        considerFirstLine);
  }

  /**
   * Removes the common number of indents from all lines. If a line only consists out of white space
   * it is ignored. If <code>
   * considerFirstLine</code> is false the first line will be ignored.
   *
   * @param lines the lines
   * @param tabWidth the size of one tab in space equivalents
   * @param indentWidth the size of the indent in space equivalents
   * @param considerFirstLine If <code> considerFirstLine</code> is false the first line will be
   *     ignored.
   * @since 3.1
   */
  public static void trimIndentation(
      String[] lines, int tabWidth, int indentWidth, boolean considerFirstLine) {
    String[] toDo = new String[lines.length];
    // find indentation common to all lines
    int minIndent = Integer.MAX_VALUE; // very large
    for (int i = considerFirstLine ? 0 : 1; i < lines.length; i++) {
      String line = lines[i];
      if (containsOnlyWhitespaces(line)) continue;
      toDo[i] = line;
      int indent = computeIndentUnits(line, tabWidth, indentWidth);
      if (indent < minIndent) {
        minIndent = indent;
      }
    }

    if (minIndent > 0) {
      // remove this indent from all lines
      for (int i = considerFirstLine ? 0 : 1; i < toDo.length; i++) {
        String s = toDo[i];
        if (s != null) lines[i] = trimIndent(s, minIndent, tabWidth, indentWidth);
        else {
          String line = lines[i];
          int indent = computeIndentUnits(line, tabWidth, indentWidth);
          if (indent > minIndent) lines[i] = trimIndent(line, minIndent, tabWidth, indentWidth);
          else lines[i] = trimLeadingTabsAndSpaces(line);
        }
      }
    }
  }

  /**
   * Returns that part of the indentation of <code>line</code> that makes up a multiple of
   * indentation units.
   *
   * @param line the line to scan
   * @param project the java project from which to get the formatter preferences, or <code>null
   *     </code> for global preferences
   * @return the indent part of <code>line</code>, but no odd spaces
   * @since 3.1
   */
  public static String getIndentString(String line, IJavaProject project) {
    return IndentManipulation.extractIndentString(
        line, CodeFormatterUtil.getTabWidth(project), CodeFormatterUtil.getIndentWidth(project));
  }

  /**
   * Returns that part of the indentation of <code>line</code> that makes up a multiple of
   * indentation units.
   *
   * @param line the line to scan
   * @param tabWidth the size of one tab in space equivalents
   * @param indentWidth the size of the indent in space equivalents
   * @return the indent part of <code>line</code>, but no odd spaces
   * @since 3.1
   */
  public static String getIndentString(String line, int tabWidth, int indentWidth) {
    return IndentManipulation.extractIndentString(line, tabWidth, indentWidth);
  }

  public static String[] removeTrailingEmptyLines(String[] sourceLines) {
    int lastNonEmpty = findLastNonEmptyLineIndex(sourceLines);
    String[] result = new String[lastNonEmpty + 1];
    for (int i = 0; i < result.length; i++) {
      result[i] = sourceLines[i];
    }
    return result;
  }

  private static int findLastNonEmptyLineIndex(String[] sourceLines) {
    for (int i = sourceLines.length - 1; i >= 0; i--) {
      if (!sourceLines[i].trim().equals("")) // $NON-NLS-1$
      return i;
    }
    return -1;
  }

  /**
   * Change the indent of, possible multi-line, code range. The current indent is removed, a new
   * indent added. The first line of the code will not be changed. (It is considered to have no
   * indent as it might start in the middle of a line)
   *
   * @param code the code
   * @param codeIndentLevel level of indentation
   * @param project the java project from which to get the formatter preferences, or <code>null
   *     </code> for global preferences
   * @param newIndent new indent
   * @param lineDelim line delimiter
   * @return the changed code
   * @since 3.1
   */
  public static String changeIndent(
      String code, int codeIndentLevel, IJavaProject project, String newIndent, String lineDelim) {
    return IndentManipulation.changeIndent(
        code,
        codeIndentLevel,
        CodeFormatterUtil.getTabWidth(project),
        CodeFormatterUtil.getIndentWidth(project),
        newIndent,
        lineDelim);
  }

  /**
   * Change the indent of, possible muti-line, code range. The current indent is removed, a new
   * indent added. The first line of the code will not be changed. (It is considered to have no
   * indent as it might start in the middle of a line)
   *
   * @param code the code
   * @param codeIndentLevel indent level
   * @param tabWidth the size of one tab in space equivalents
   * @param indentWidth the size of the indent in space equivalents
   * @param newIndent new indent
   * @param lineDelim line delimiter
   * @return the changed code
   * @since 3.1
   */
  public static String changeIndent(
      String code,
      int codeIndentLevel,
      int tabWidth,
      int indentWidth,
      String newIndent,
      String lineDelim) {
    return IndentManipulation.changeIndent(
        code, codeIndentLevel, tabWidth, indentWidth, newIndent, lineDelim);
  }

  public static String trimIndentation(
      String source, IJavaProject project, boolean considerFirstLine) {
    return trimIndentation(
        source,
        CodeFormatterUtil.getTabWidth(project),
        CodeFormatterUtil.getIndentWidth(project),
        considerFirstLine);
  }

  public static String trimIndentation(
      String source, int tabWidth, int indentWidth, boolean considerFirstLine) {
    try {
      ILineTracker tracker = new DefaultLineTracker();
      tracker.set(source);
      int size = tracker.getNumberOfLines();
      if (size == 1) return source;
      String lines[] = new String[size];
      for (int i = 0; i < size; i++) {
        IRegion region = tracker.getLineInformation(i);
        int offset = region.getOffset();
        lines[i] = source.substring(offset, offset + region.getLength());
      }
      Strings.trimIndentation(lines, tabWidth, indentWidth, considerFirstLine);
      StringBuffer result = new StringBuffer();
      int last = size - 1;
      for (int i = 0; i < size; i++) {
        result.append(lines[i]);
        if (i < last) result.append(tracker.getLineDelimiter(i));
      }
      return result.toString();
    } catch (BadLocationException e) {
      Assert.isTrue(false, "Can not happend"); // $NON-NLS-1$
      return null;
    }
  }

  /**
   * Concatenate the given strings into one strings using the passed line delimiter as a delimiter.
   * No delimiter is added to the last line.
   *
   * @param lines the lines
   * @param delimiter line delimiter
   * @return the concatenated lines
   */
  public static String concatenate(String[] lines, String delimiter) {
    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < lines.length; i++) {
      if (i > 0) buffer.append(delimiter);
      buffer.append(lines[i]);
    }
    return buffer.toString();
  }

  public static boolean equals(String s, char[] c) {
    if (s.length() != c.length) return false;

    for (int i = c.length; --i >= 0; ) if (s.charAt(i) != c[i]) return false;
    return true;
  }

  public static String removeTrailingCharacters(String text, char toRemove) {
    int size = text.length();
    int end = size;
    for (int i = size - 1; i >= 0; i--) {
      char c = text.charAt(i);
      if (c == toRemove) {
        end = i;
      } else {
        break;
      }
    }
    if (end == size) return text;
    else if (end == 0) return ""; // $NON-NLS-1$
    else return text.substring(0, end);
  }

  //	public static String removeMnemonicIndicator(String string) {
  //		return LegacyActionTools.removeMnemonics(string);
  //	}
}
