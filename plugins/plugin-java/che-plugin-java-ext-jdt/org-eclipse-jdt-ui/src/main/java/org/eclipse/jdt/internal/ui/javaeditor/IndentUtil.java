/**
 * ***************************************************************************** Copyright (c) 2005,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.javaeditor;

import org.eclipse.che.jface.text.source.ILineRange;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.corext.util.CodeFormatterUtil;
import org.eclipse.jdt.internal.ui.text.JavaHeuristicScanner;
import org.eclipse.jdt.internal.ui.text.JavaIndenter;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextUtilities;

/**
 * Utility that indents a number of lines in a document.
 *
 * @since 3.1
 */
public final class IndentUtil {

  private static final String SLASHES = "//"; // $NON-NLS-1$

  /**
   * The result of an indentation operation. The result may be passed to subsequent calls to {@link
   * IndentUtil#indentLines(IDocument, ILineRange, IJavaProject, IndentResult) indentLines} to
   * obtain consistent results with respect to the indentation of line-comments.
   */
  public static final class IndentResult {
    private IndentResult(boolean[] commentLines) {
      commentLinesAtColumnZero = commentLines;
    }

    private boolean[] commentLinesAtColumnZero;
    private boolean hasChanged;
    private int leftmostLine = -1;
    /**
     * Returns <code>true</code> if the indentation operation changed the document, <code>false
     * </code> if not.
     *
     * @return <code>true</code> if the document was changed
     */
    public boolean hasChanged() {
      return hasChanged;
    }
  }

  private IndentUtil() {
    // do not instantiate
  }

  /**
   * Indents the line range specified by <code>lines</code> in <code>document</code>. The passed
   * Java project may be <code>null</code>, it is used solely to obtain formatter preferences.
   *
   * @param document the document to be changed
   * @param lines the line range to be indented
   * @param project the Java project to get the formatter preferences from, or <code>null</code> if
   *     global preferences should be used
   * @param result the result from a previous call to <code>indentLines</code>, in order to maintain
   *     comment line properties, or <code>null</code>. Note that the passed result may be changed
   *     by the call.
   * @return an indent result that may be queried for changes and can be reused in subsequent
   *     indentation operations
   * @throws BadLocationException if <code>lines</code> is not a valid line range on <code>document
   *     </code>
   */
  public static IndentResult indentLines(
      IDocument document, ILineRange lines, IJavaProject project, IndentResult result)
      throws BadLocationException {
    int numberOfLines = lines.getNumberOfLines();

    if (numberOfLines < 1) return new IndentResult(null);

    result = reuseOrCreateToken(result, numberOfLines);

    JavaHeuristicScanner scanner = new JavaHeuristicScanner(document);
    JavaIndenter indenter = new JavaIndenter(document, scanner, project);
    boolean changed = false;
    int tabSize = CodeFormatterUtil.getTabWidth(project);
    for (int line = lines.getStartLine(), last = line + numberOfLines, i = 0; line < last; line++) {
      changed |=
          indentLine(
              document, line, indenter, scanner, result.commentLinesAtColumnZero, i++, tabSize);
    }
    result.hasChanged = changed;

    return result;
  }

  /**
   * Shifts the line range specified by <code>lines</code> in <code>document</code>. The amount that
   * the lines get shifted are determined by the first line in the range, all subsequent lines are
   * adjusted accordingly. The passed Java project may be <code>null</code>, it is used solely to
   * obtain formatter preferences.
   *
   * @param document the document to be changed
   * @param lines the line range to be shifted
   * @param project the Java project to get the formatter preferences from, or <code>null</code> if
   *     global preferences should be used
   * @param result the result from a previous call to <code>shiftLines</code>, in order to maintain
   *     comment line properties, or <code>null</code>. Note that the passed result may be changed
   *     by the call.
   * @return an indent result that may be queried for changes and can be reused in subsequent
   *     indentation operations
   * @throws BadLocationException if <code>lines</code> is not a valid line range on <code>document
   *     </code>
   */
  public static IndentResult shiftLines(
      IDocument document, ILineRange lines, IJavaProject project, IndentResult result)
      throws BadLocationException {
    int numberOfLines = lines.getNumberOfLines();

    if (numberOfLines < 1) return new IndentResult(null);

    result = reuseOrCreateToken(result, numberOfLines);
    result.hasChanged = false;

    JavaHeuristicScanner scanner = new JavaHeuristicScanner(document);
    JavaIndenter indenter = new JavaIndenter(document, scanner, project);

    String current = getCurrentIndent(document, lines.getStartLine());
    StringBuffer correct =
        indenter.computeIndentation(document.getLineOffset(lines.getStartLine()));
    if (correct == null) return result; // bail out

    int tabSize = CodeFormatterUtil.getTabWidth(project);
    StringBuffer addition = new StringBuffer();
    int difference = subtractIndent(correct, current, addition, tabSize);

    if (difference == 0) return result;

    if (result.leftmostLine == -1) result.leftmostLine = getLeftMostLine(document, lines, tabSize);

    int maxReduction =
        computeVisualLength(
            getCurrentIndent(document, result.leftmostLine + lines.getStartLine()), tabSize);

    if (difference > 0) {
      for (int line = lines.getStartLine(), last = line + numberOfLines, i = 0; line < last; line++)
        addIndent(document, line, addition, result.commentLinesAtColumnZero, i++);
    } else {
      int reduction = Math.min(-difference, maxReduction);
      for (int line = lines.getStartLine(), last = line + numberOfLines, i = 0; line < last; line++)
        cutIndent(document, line, reduction, tabSize, result.commentLinesAtColumnZero, i++);
    }

    result.hasChanged = true;

    return result;
  }

  /**
   * Indents line <code>line</code> in <code>document</code> with <code>indent</code>. Leaves
   * leading comment signs alone.
   *
   * @param document the document
   * @param line the line
   * @param indent the indentation to insert
   * @param commentlines
   * @throws BadLocationException on concurrent document modification
   */
  private static void addIndent(
      IDocument document, int line, CharSequence indent, boolean[] commentlines, int relative)
      throws BadLocationException {
    IRegion region = document.getLineInformation(line);
    int insert = region.getOffset();
    int endOffset = region.getOffset() + region.getLength();

    // go behind line comments
    if (!commentlines[relative]) {
      while (insert < endOffset - 2 && document.get(insert, 2).equals(SLASHES)) insert += 2;
    }

    // insert indent
    document.replace(insert, 0, indent.toString());
  }

  /**
   * Cuts the visual equivalent of <code>toDelete</code> characters out of the indentation of line
   * <code>line</code> in <code>document</code>. Leaves leading comment signs alone.
   *
   * @param document the document
   * @param line the line
   * @param toDelete the number of space equivalents to delete.
   * @throws BadLocationException on concurrent document modification
   */
  private static void cutIndent(
      IDocument document, int line, int toDelete, int tabSize, boolean[] commentLines, int relative)
      throws BadLocationException {
    IRegion region = document.getLineInformation(line);
    int from = region.getOffset();
    int endOffset = region.getOffset() + region.getLength();

    // go behind line comments
    while (from < endOffset - 2 && document.get(from, 2).equals(SLASHES)) from += 2;

    int to = from;
    while (toDelete > 0 && to < endOffset) {
      char ch = document.getChar(to);
      if (!Character.isWhitespace(ch)) break;
      toDelete -= computeVisualLength(ch, tabSize);
      if (toDelete >= 0) to++;
      else break;
    }

    if (endOffset > to + 1 && document.get(to, 2).equals(SLASHES)) commentLines[relative] = true;

    document.replace(from, to - from, null);
  }

  /**
   * Computes the difference of two indentations and returns the difference in length of current and
   * correct. If the return value is positive, <code>addition</code> is initialized with a substring
   * of that length of <code>correct</code>.
   *
   * @param correct the correct indentation
   * @param current the current indentation (migth contain non-whitespace)
   * @param difference a string buffer - if the return value is positive, it will be cleared and set
   *     to the substring of <code>current</code> of that length
   * @return the difference in lenght of <code>correct</code> and <code>current</code>
   */
  private static int subtractIndent(
      CharSequence correct, CharSequence current, StringBuffer difference, int tabSize) {
    int c1 = computeVisualLength(correct, tabSize);
    int c2 = computeVisualLength(current, tabSize);
    int diff = c1 - c2;
    if (diff <= 0) return diff;

    difference.setLength(0);
    int len = 0, i = 0;
    while (len < diff) {
      char c = correct.charAt(i++);
      difference.append(c);
      len += computeVisualLength(c, tabSize);
    }

    return diff;
  }

  private static int computeVisualLength(char ch, int tabSize) {
    if (ch == '\t') return tabSize;
    else return 1;
  }

  /**
   * Returns the visual length of a given <code>CharSequence</code> taking into account the visual
   * tabulator length.
   *
   * @param seq the string to measure
   * @return the visual length of <code>seq</code>
   */
  private static int computeVisualLength(CharSequence seq, int tablen) {
    int size = 0;

    for (int i = 0; i < seq.length(); i++) {
      char ch = seq.charAt(i);
      if (ch == '\t') {
        if (tablen != 0) size += tablen - size % tablen;
        // else: size stays the same
      } else {
        size++;
      }
    }
    return size;
  }

  /**
   * Returns the indentation of the line <code>line</code> in <code>document</code>. The returned
   * string may contain pairs of leading slashes that are considered part of the indentation. The
   * space before the asterix in a javadoc-like comment is not considered part of the indentation.
   *
   * @param document the document
   * @param line the line
   * @return the indentation of <code>line</code> in <code>document</code>
   * @throws BadLocationException if the document is changed concurrently
   */
  private static String getCurrentIndent(IDocument document, int line) throws BadLocationException {
    IRegion region = document.getLineInformation(line);
    int from = region.getOffset();
    int endOffset = region.getOffset() + region.getLength();

    // go behind line comments
    int to = from;
    while (to < endOffset - 2 && document.get(to, 2).equals(SLASHES)) to += 2;

    while (to < endOffset) {
      char ch = document.getChar(to);
      if (!Character.isWhitespace(ch)) break;
      to++;
    }

    // don't count the space before javadoc like, asterix-style comment lines
    if (to > from && to < endOffset - 1 && document.get(to - 1, 2).equals(" *")) { // $NON-NLS-1$
      String type =
          TextUtilities.getContentType(document, IJavaPartitions.JAVA_PARTITIONING, to, true);
      if (type.equals(IJavaPartitions.JAVA_DOC)
          || type.equals(IJavaPartitions.JAVA_MULTI_LINE_COMMENT)) to--;
    }

    return document.get(from, to - from);
  }

  private static int getLeftMostLine(IDocument document, ILineRange lines, int tabSize)
      throws BadLocationException {
    int numberOfLines = lines.getNumberOfLines();
    int first = lines.getStartLine();
    int minLine = -1;
    int minIndent = Integer.MAX_VALUE;
    for (int line = 0; line < numberOfLines; line++) {
      int length = computeVisualLength(getCurrentIndent(document, line + first), tabSize);
      if (length < minIndent) {
        minIndent = length;
        minLine = line;
      }
    }
    return minLine;
  }

  private static IndentResult reuseOrCreateToken(IndentResult token, int numberOfLines) {
    if (token == null) token = new IndentResult(new boolean[numberOfLines]);
    else if (token.commentLinesAtColumnZero == null)
      token.commentLinesAtColumnZero = new boolean[numberOfLines];
    else if (token.commentLinesAtColumnZero.length != numberOfLines) {
      boolean[] commentBooleans = new boolean[numberOfLines];
      System.arraycopy(
          token.commentLinesAtColumnZero,
          0,
          commentBooleans,
          0,
          Math.min(numberOfLines, token.commentLinesAtColumnZero.length));
      token.commentLinesAtColumnZero = commentBooleans;
    }
    return token;
  }

  /**
   * Indents a single line using the java heuristic scanner. Javadoc and multi line comments are
   * indented as specified by the <code>JavaDocAutoIndentStrategy</code>.
   *
   * @param document the document
   * @param line the line to be indented
   * @param indenter the java indenter
   * @param scanner the heuristic scanner
   * @param commentLines the indent token comment booleans
   * @param lineIndex the zero-based line index
   * @return <code>true</code> if the document was modified, <code>false</code> if not
   * @throws BadLocationException if the document got changed concurrently
   */
  private static boolean indentLine(
      IDocument document,
      int line,
      JavaIndenter indenter,
      JavaHeuristicScanner scanner,
      boolean[] commentLines,
      int lineIndex,
      int tabSize)
      throws BadLocationException {
    IRegion currentLine = document.getLineInformation(line);
    final int offset = currentLine.getOffset();
    int wsStart =
        offset; // where we start searching for non-WS; after the "//" in single line comments

    String indent = null;
    if (offset < document.getLength()) {
      ITypedRegion partition =
          TextUtilities.getPartition(document, IJavaPartitions.JAVA_PARTITIONING, offset, true);
      ITypedRegion startingPartition =
          TextUtilities.getPartition(document, IJavaPartitions.JAVA_PARTITIONING, offset, false);
      String type = partition.getType();
      if (type.equals(IJavaPartitions.JAVA_DOC)
          || type.equals(IJavaPartitions.JAVA_MULTI_LINE_COMMENT)) {
        indent = computeJavadocIndent(document, line, scanner, startingPartition);
      } else if (!commentLines[lineIndex]
          && startingPartition.getOffset() == offset
          && startingPartition.getType().equals(IJavaPartitions.JAVA_SINGLE_LINE_COMMENT)) {
        return false;
      }
    }

    // standard java indentation
    if (indent == null) {
      StringBuffer computed = indenter.computeIndentation(offset);
      if (computed != null) indent = computed.toString();
      else indent = new String();
    }

    // change document:
    // get current white space
    int lineLength = currentLine.getLength();
    int end = scanner.findNonWhitespaceForwardInAnyPartition(wsStart, offset + lineLength);
    if (end == JavaHeuristicScanner.NOT_FOUND) end = offset + lineLength;
    int length = end - offset;
    String currentIndent = document.get(offset, length);

    // memorize the fact that a line is a single line comment (but not at column 0) and should be
    // treated like code
    // as opposed to commented out code, which should keep its slashes at column 0
    if (length > 0) {
      ITypedRegion partition =
          TextUtilities.getPartition(document, IJavaPartitions.JAVA_PARTITIONING, end, false);
      if (partition.getOffset() == end
          && IJavaPartitions.JAVA_SINGLE_LINE_COMMENT.equals(partition.getType())) {
        commentLines[lineIndex] = true;
      }
    }

    // only change the document if it is a real change
    if (!indent.equals(currentIndent)) {
      document.replace(offset, length, indent);
      return true;
    }

    return false;
  }

  /**
   * Computes and returns the indentation for a javadoc line. The line must be inside a javadoc
   * comment.
   *
   * @param document the document
   * @param line the line in document
   * @param scanner the scanner
   * @param partition the comment partition
   * @return the indent, or <code>null</code> if not computable
   * @throws BadLocationException
   */
  private static String computeJavadocIndent(
      IDocument document, int line, JavaHeuristicScanner scanner, ITypedRegion partition)
      throws BadLocationException {
    if (line == 0) // impossible - the first line is never inside a javadoc comment
    return null;

    // don't make any assumptions if the line does not start with \s*\* - it might be
    // commented out code, for which we don't want to change the indent
    final IRegion lineInfo = document.getLineInformation(line);
    final int lineStart = lineInfo.getOffset();
    final int lineLength = lineInfo.getLength();
    final int lineEnd = lineStart + lineLength;
    int nonWS = scanner.findNonWhitespaceForwardInAnyPartition(lineStart, lineEnd);
    if (nonWS == JavaHeuristicScanner.NOT_FOUND || document.getChar(nonWS) != '*') {
      if (nonWS == JavaHeuristicScanner.NOT_FOUND) return document.get(lineStart, lineLength);
      return document.get(lineStart, nonWS - lineStart);
    }

    // take the indent from the previous line and reuse
    IRegion previousLine = document.getLineInformation(line - 1);
    int previousLineStart = previousLine.getOffset();
    int previousLineLength = previousLine.getLength();
    int previousLineEnd = previousLineStart + previousLineLength;

    StringBuffer buf = new StringBuffer();
    int previousLineNonWS =
        scanner.findNonWhitespaceForwardInAnyPartition(previousLineStart, previousLineEnd);
    if (previousLineNonWS == JavaHeuristicScanner.NOT_FOUND
        || document.getChar(previousLineNonWS) != '*') {
      // align with the comment start if the previous line is not an asterix line
      previousLine = document.getLineInformationOfOffset(partition.getOffset());
      previousLineStart = previousLine.getOffset();
      previousLineLength = previousLine.getLength();
      previousLineEnd = previousLineStart + previousLineLength;
      previousLineNonWS =
          scanner.findNonWhitespaceForwardInAnyPartition(previousLineStart, previousLineEnd);
      if (previousLineNonWS == JavaHeuristicScanner.NOT_FOUND) previousLineNonWS = previousLineEnd;

      // add the initial space
      // TODO this may be controlled by a formatter preference in the future
      buf.append(' ');
    }

    String indentation = document.get(previousLineStart, previousLineNonWS - previousLineStart);
    buf.insert(0, indentation);
    return buf.toString();
  }
}
