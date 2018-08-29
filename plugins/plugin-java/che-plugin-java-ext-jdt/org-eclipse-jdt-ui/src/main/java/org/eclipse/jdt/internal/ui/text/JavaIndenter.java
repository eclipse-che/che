/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2014 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.text;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.internal.corext.util.CodeFormatterUtil;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

/**
 * Uses the {@link org.eclipse.jdt.internal.ui.text.JavaHeuristicScanner} to get the indentation
 * level for a certain position in a document.
 *
 * <p>An instance holds some internal position in the document and is therefore not threadsafe.
 *
 * @since 3.0
 */
public final class JavaIndenter {

  /**
   * The JDT Core preferences.
   *
   * @since 3.2
   */
  private final class CorePrefs {
    final boolean prefUseTabs;
    final int prefTabSize;
    final int prefIndentationSize;
    final boolean prefArrayDimensionsDeepIndent;
    final int prefArrayIndent;
    final boolean prefArrayDeepIndent;
    final boolean prefTernaryDeepAlign;
    final int prefTernaryIndent;
    final int prefCaseIndent;
    final int prefCaseBlockIndent;
    final int prefSimpleIndent;
    final int prefBracketIndent;
    final boolean prefMethodDeclDeepIndent;
    final int prefMethodDeclIndent;
    final boolean prefMethodCallDeepIndent;
    final int prefMethodCallIndent;
    final boolean prefParenthesisDeepIndent;
    final int prefParenthesisIndent;
    final int prefBlockIndent;
    final int prefMethodBodyIndent;
    final int prefTypeIndent;
    final boolean prefIndentBracesForBlocks;
    final boolean prefIndentBracesForArrays;
    final boolean prefIndentBracesForMethods;
    final boolean prefIndentBracesForTypes;
    final int prefContinuationIndent;
    final boolean prefHasGenerics;
    final String prefTabChar;

    private final IJavaProject fProject;

    /**
     * Returns <code>true</code> if the class is used outside the workbench, <code>false</code> in
     * normal mode
     *
     * @return <code>true</code> if the plug-ins are not available
     */
    private boolean isStandalone() {
      return JavaCore.getPlugin() == null;
    }

    /**
     * Returns the possibly project-specific core preference defined under <code>key</code>.
     *
     * @param key the key of the preference
     * @return the value of the preference
     * @since 3.1
     */
    private String getCoreFormatterOption(String key) {
      if (fProject == null) return JavaCore.getOption(key);
      return fProject.getOption(key, true);
    }

    CorePrefs(IJavaProject project) {
      fProject = project;
      if (isStandalone()) {
        prefUseTabs = true;
        prefTabSize = 4;
        prefIndentationSize = 4;
        prefArrayDimensionsDeepIndent = true;
        prefContinuationIndent = 2;
        prefBlockIndent = 1;
        prefArrayIndent = prefContinuationIndent;
        prefArrayDeepIndent = true;
        prefTernaryDeepAlign = false;
        prefTernaryIndent = prefContinuationIndent;
        prefCaseIndent = 0;
        prefCaseBlockIndent = prefBlockIndent;
        prefIndentBracesForBlocks = false;
        prefSimpleIndent =
            (prefIndentBracesForBlocks && prefBlockIndent == 0) ? 1 : prefBlockIndent;
        prefBracketIndent = prefBlockIndent;
        prefMethodDeclDeepIndent = true;
        prefMethodDeclIndent = 1;
        prefMethodCallDeepIndent = false;
        prefMethodCallIndent = 1;
        prefParenthesisDeepIndent = false;
        prefParenthesisIndent = prefContinuationIndent;
        prefMethodBodyIndent = 1;
        prefTypeIndent = 1;
        prefIndentBracesForArrays = false;
        prefIndentBracesForMethods = false;
        prefIndentBracesForTypes = false;
        prefHasGenerics = false;
        prefTabChar = JavaCore.TAB;
      } else {
        prefUseTabs = prefUseTabs();
        prefTabSize = prefTabSize();
        prefIndentationSize = prefIndentationSize();
        prefArrayDimensionsDeepIndent = prefArrayDimensionsDeepIndent();
        prefContinuationIndent = prefContinuationIndent();
        prefBlockIndent = prefBlockIndent();
        prefArrayIndent = prefArrayIndent();
        prefArrayDeepIndent = prefArrayDeepIndent();
        prefTernaryDeepAlign = prefTernaryDeepAlign();
        prefTernaryIndent = prefTernaryIndent();
        prefCaseIndent = prefCaseIndent();
        prefCaseBlockIndent = prefCaseBlockIndent();
        prefIndentBracesForBlocks = prefIndentBracesForBlocks();
        prefSimpleIndent = prefSimpleIndent();
        prefBracketIndent = prefBracketIndent();
        prefMethodDeclDeepIndent = prefMethodDeclDeepIndent();
        prefMethodDeclIndent = prefMethodDeclIndent();
        prefMethodCallDeepIndent = prefMethodCallDeepIndent();
        prefMethodCallIndent = prefMethodCallIndent();
        prefParenthesisDeepIndent = prefParenthesisDeepIndent();
        prefParenthesisIndent = prefParenthesisIndent();
        prefMethodBodyIndent = prefMethodBodyIndent();
        prefTypeIndent = prefTypeIndent();
        prefIndentBracesForArrays = prefIndentBracesForArrays();
        prefIndentBracesForMethods = prefIndentBracesForMethods();
        prefIndentBracesForTypes = prefIndentBracesForTypes();
        prefHasGenerics = hasGenerics();
        prefTabChar = getCoreFormatterOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR);
      }
    }

    private boolean prefUseTabs() {
      return !JavaCore.SPACE.equals(
          getCoreFormatterOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR));
    }

    private int prefTabSize() {
      return CodeFormatterUtil.getTabWidth(fProject);
    }

    private int prefIndentationSize() {
      return CodeFormatterUtil.getIndentWidth(fProject);
    }

    private boolean prefArrayDimensionsDeepIndent() {
      return true; // sensible default, no formatter setting
    }

    private int prefArrayIndent() {
      String option =
          getCoreFormatterOption(
              DefaultCodeFormatterConstants
                  .FORMATTER_ALIGNMENT_FOR_EXPRESSIONS_IN_ARRAY_INITIALIZER);
      try {
        if (DefaultCodeFormatterConstants.getIndentStyle(option)
            == DefaultCodeFormatterConstants.INDENT_BY_ONE) return 1;
      } catch (IllegalArgumentException e) {
        // ignore and return default
      }

      return prefContinuationIndent(); // default
    }

    private boolean prefArrayDeepIndent() {
      String option =
          getCoreFormatterOption(
              DefaultCodeFormatterConstants
                  .FORMATTER_ALIGNMENT_FOR_EXPRESSIONS_IN_ARRAY_INITIALIZER);
      try {
        return DefaultCodeFormatterConstants.getIndentStyle(option)
            == DefaultCodeFormatterConstants.INDENT_ON_COLUMN;
      } catch (IllegalArgumentException e) {
        // ignore and return default
      }

      return true;
    }

    private boolean prefTernaryDeepAlign() {
      String option =
          getCoreFormatterOption(
              DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_CONDITIONAL_EXPRESSION);
      try {
        return DefaultCodeFormatterConstants.getIndentStyle(option)
            == DefaultCodeFormatterConstants.INDENT_ON_COLUMN;
      } catch (IllegalArgumentException e) {
        // ignore and return default
      }
      return false;
    }

    private int prefTernaryIndent() {
      String option =
          getCoreFormatterOption(
              DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_CONDITIONAL_EXPRESSION);
      try {
        if (DefaultCodeFormatterConstants.getIndentStyle(option)
            == DefaultCodeFormatterConstants.INDENT_BY_ONE) return 1;
        else return prefContinuationIndent();
      } catch (IllegalArgumentException e) {
        // ignore and return default
      }

      return prefContinuationIndent();
    }

    private int prefCaseIndent() {
      if (DefaultCodeFormatterConstants.TRUE.equals(
          getCoreFormatterOption(
              DefaultCodeFormatterConstants.FORMATTER_INDENT_SWITCHSTATEMENTS_COMPARE_TO_SWITCH)))
        return prefBlockIndent();
      else return 0;
    }

    private int prefCaseBlockIndent() {
      if (DefaultCodeFormatterConstants.TRUE.equals(
          getCoreFormatterOption(
              DefaultCodeFormatterConstants.FORMATTER_INDENT_SWITCHSTATEMENTS_COMPARE_TO_CASES)))
        return prefBlockIndent();
      else return 0;
    }

    private int prefSimpleIndent() {
      if (prefIndentBracesForBlocks() && prefBlockIndent() == 0) return 1;
      else return prefBlockIndent();
    }

    private int prefBracketIndent() {
      return prefBlockIndent();
    }

    private boolean prefMethodDeclDeepIndent() {
      String option =
          getCoreFormatterOption(
              DefaultCodeFormatterConstants
                  .FORMATTER_ALIGNMENT_FOR_PARAMETERS_IN_METHOD_DECLARATION);
      try {
        return DefaultCodeFormatterConstants.getIndentStyle(option)
            == DefaultCodeFormatterConstants.INDENT_ON_COLUMN;
      } catch (IllegalArgumentException e) {
        // ignore and return default
      }

      return true;
    }

    private int prefMethodDeclIndent() {
      String option =
          getCoreFormatterOption(
              DefaultCodeFormatterConstants
                  .FORMATTER_ALIGNMENT_FOR_PARAMETERS_IN_METHOD_DECLARATION);
      try {
        if (DefaultCodeFormatterConstants.getIndentStyle(option)
            == DefaultCodeFormatterConstants.INDENT_BY_ONE) return 1;
        else return prefContinuationIndent();
      } catch (IllegalArgumentException e) {
        // ignore and return default
      }
      return 1;
    }

    private boolean prefMethodCallDeepIndent() {
      String option =
          getCoreFormatterOption(
              DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_ARGUMENTS_IN_METHOD_INVOCATION);
      try {
        return DefaultCodeFormatterConstants.getIndentStyle(option)
            == DefaultCodeFormatterConstants.INDENT_ON_COLUMN;
      } catch (IllegalArgumentException e) {
        // ignore and return default
      }
      return false; // sensible default
    }

    private int prefMethodCallIndent() {
      String option =
          getCoreFormatterOption(
              DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_ARGUMENTS_IN_METHOD_INVOCATION);
      try {
        if (DefaultCodeFormatterConstants.getIndentStyle(option)
            == DefaultCodeFormatterConstants.INDENT_BY_ONE) return 1;
        else return prefContinuationIndent();
      } catch (IllegalArgumentException e) {
        // ignore and return default
      }

      return 1; // sensible default
    }

    private boolean prefParenthesisDeepIndent() {
      return false; // don't do parenthesis deep indentation (check rev. 1.60 for experimental code)
    }

    private int prefParenthesisIndent() {
      return prefContinuationIndent();
    }

    private int prefBlockIndent() {
      String option =
          getCoreFormatterOption(
              DefaultCodeFormatterConstants.FORMATTER_INDENT_STATEMENTS_COMPARE_TO_BLOCK);
      if (DefaultCodeFormatterConstants.FALSE.equals(option)) return 0;

      return 1; // sensible default
    }

    private int prefMethodBodyIndent() {
      if (DefaultCodeFormatterConstants.FALSE.equals(
          getCoreFormatterOption(
              DefaultCodeFormatterConstants.FORMATTER_INDENT_STATEMENTS_COMPARE_TO_BODY))) return 0;

      return 1; // sensible default
    }

    private int prefTypeIndent() {
      String option =
          getCoreFormatterOption(
              DefaultCodeFormatterConstants
                  .FORMATTER_INDENT_BODY_DECLARATIONS_COMPARE_TO_TYPE_HEADER);
      if (DefaultCodeFormatterConstants.FALSE.equals(option)) return 0;

      return 1; // sensible default
    }

    private boolean prefIndentBracesForBlocks() {
      return DefaultCodeFormatterConstants.NEXT_LINE_SHIFTED.equals(
          getCoreFormatterOption(DefaultCodeFormatterConstants.FORMATTER_BRACE_POSITION_FOR_BLOCK));
    }

    private boolean prefIndentBracesForArrays() {
      return DefaultCodeFormatterConstants.NEXT_LINE_SHIFTED.equals(
          getCoreFormatterOption(
              DefaultCodeFormatterConstants.FORMATTER_BRACE_POSITION_FOR_ARRAY_INITIALIZER));
    }

    private boolean prefIndentBracesForMethods() {
      return DefaultCodeFormatterConstants.NEXT_LINE_SHIFTED.equals(
          getCoreFormatterOption(
              DefaultCodeFormatterConstants.FORMATTER_BRACE_POSITION_FOR_METHOD_DECLARATION));
    }

    private boolean prefIndentBracesForTypes() {
      return DefaultCodeFormatterConstants.NEXT_LINE_SHIFTED.equals(
          getCoreFormatterOption(
              DefaultCodeFormatterConstants.FORMATTER_BRACE_POSITION_FOR_TYPE_DECLARATION));
    }

    private int prefContinuationIndent() {
      try {
        return Integer.parseInt(
            getCoreFormatterOption(
                DefaultCodeFormatterConstants.FORMATTER_CONTINUATION_INDENTATION));
      } catch (NumberFormatException e) {
        // ignore and return default
      }

      return 2; // sensible default
    }

    private boolean hasGenerics() {
      return JavaCore.VERSION_1_5.compareTo(getCoreFormatterOption(JavaCore.COMPILER_SOURCE)) <= 0;
    }
  }

  /** The document being scanned. */
  private final IDocument fDocument;
  /** The indentation accumulated by <code>findReferencePosition</code>. */
  private int fIndent;
  /**
   * The absolute (character-counted) indentation offset for special cases (method defs, array
   * initializers)
   */
  private int fAlign;
  /** The stateful scanposition for the indentation methods. */
  private int fPosition;
  /** The previous position. */
  private int fPreviousPos;
  /** The most recent token. */
  private int fToken;
  /** The line of <code>fPosition</code>. */
  private int fLine;
  /**
   * The scanner we will use to scan the document. It has to be installed on the same document as
   * the one we get.
   */
  private final JavaHeuristicScanner fScanner;
  /**
   * The JDT Core preferences.
   *
   * @since 3.2
   */
  private final CorePrefs fPrefs;

  /**
   * Creates a new instance.
   *
   * @param document the document to scan
   * @param scanner the {@link JavaHeuristicScanner} to be used for scanning the document. It must
   *     be installed on the same <code>IDocument</code>.
   */
  public JavaIndenter(IDocument document, JavaHeuristicScanner scanner) {
    this(document, scanner, null);
  }

  /**
   * Creates a new instance.
   *
   * @param document the document to scan
   * @param scanner the {@link JavaHeuristicScanner}to be used for scanning the document. It must be
   *     installed on the same <code>IDocument</code>.
   * @param project the java project to get the formatter preferences from, or <code>null</code> to
   *     use the workspace settings
   * @since 3.1
   */
  public JavaIndenter(IDocument document, JavaHeuristicScanner scanner, IJavaProject project) {
    Assert.isNotNull(document);
    Assert.isNotNull(scanner);
    fDocument = document;
    fScanner = scanner;
    fPrefs = new CorePrefs(project);
  }

  /**
   * Computes the indentation at the reference point of <code>position</code>.
   *
   * @param offset the offset in the document
   * @return a String which reflects the indentation at the line in which the reference position to
   *     <code>offset</code> resides, or <code>null</code> if it cannot be determined
   */
  public StringBuffer getReferenceIndentation(int offset) {
    return getReferenceIndentation(offset, false);
  }

  /**
   * Computes the indentation at the reference point of <code>position</code>.
   *
   * @param offset the offset in the document
   * @param assumeOpeningBrace <code>true</code> if an opening brace should be assumed
   * @return a String which reflects the indentation at the line in which the reference position to
   *     <code>offset</code> resides, or <code>null</code> if it cannot be determined
   */
  private StringBuffer getReferenceIndentation(int offset, boolean assumeOpeningBrace) {

    int unit;
    if (assumeOpeningBrace) unit = findReferencePosition(offset, Symbols.TokenLBRACE);
    else unit = findReferencePosition(offset, peekChar(offset));

    // if we were unable to find anything, return null
    if (unit == JavaHeuristicScanner.NOT_FOUND) return null;

    return getLeadingWhitespace(unit);
  }

  /**
   * Computes the indentation at <code>offset</code>.
   *
   * @param offset the offset in the document
   * @return a String which reflects the correct indentation for the line in which offset resides,
   *     or <code>null</code> if it cannot be determined
   */
  public StringBuffer computeIndentation(int offset) {
    return computeIndentation(offset, false);
  }

  /**
   * Computes the indentation at <code>offset</code>.
   *
   * @param offset the offset in the document
   * @param assumeOpeningBrace <code>true</code> if an opening brace should be assumed
   * @return a String which reflects the correct indentation for the line in which offset resides,
   *     or <code>null</code> if it cannot be determined
   */
  public StringBuffer computeIndentation(int offset, boolean assumeOpeningBrace) {

    StringBuffer reference = getReferenceIndentation(offset, assumeOpeningBrace);

    // handle special alignment
    if (fAlign != JavaHeuristicScanner.NOT_FOUND) {
      try {
        // a special case has been detected.
        IRegion line = fDocument.getLineInformationOfOffset(fAlign);
        int lineOffset = line.getOffset();
        return createIndent(lineOffset, fAlign, false);
      } catch (BadLocationException e) {
        return null;
      }
    }

    if (reference == null) return null;

    // add additional indent
    return createReusingIndent(reference, fIndent);
  }

  /**
   * Computes the length of a <code>CharacterSequence</code>, counting a tab character as the size
   * until the next tab stop and every other character as one.
   *
   * @param indent the string to measure
   * @return the visual length in characters
   */
  private int computeVisualLength(CharSequence indent) {
    final int tabSize = fPrefs.prefTabSize;
    int length = 0;
    for (int i = 0; i < indent.length(); i++) {
      char ch = indent.charAt(i);
      switch (ch) {
        case '\t':
          if (tabSize > 0) {
            int reminder = length % tabSize;
            length += tabSize - reminder;
          }
          break;
        case ' ':
          length++;
          break;
      }
    }
    return length;
  }

  /**
   * Strips any characters off the end of <code>reference</code> that exceed <code>indentLength
   * </code>.
   *
   * @param reference the string to measure
   * @param indentLength the maximum visual indentation length
   * @return the stripped <code>reference</code>
   */
  private StringBuffer stripExceedingChars(StringBuffer reference, int indentLength) {
    final int tabSize = fPrefs.prefTabSize;
    int measured = 0;
    int chars = reference.length();
    int i = 0;
    for (; measured < indentLength && i < chars; i++) {
      char ch = reference.charAt(i);
      switch (ch) {
        case '\t':
          if (tabSize > 0) {
            int reminder = measured % tabSize;
            measured += tabSize - reminder;
          }
          break;
        case ' ':
          measured++;
          break;
      }
    }
    int deleteFrom = measured > indentLength ? i - 1 : i;

    return reference.delete(deleteFrom, chars);
  }

  /**
   * Returns the indentation of the line at <code>offset</code> as a <code>StringBuffer</code>. If
   * the offset is not valid, the empty string is returned.
   *
   * @param offset the offset in the document
   * @return the indentation (leading whitespace) of the line in which <code>offset</code> is
   *     located
   */
  private StringBuffer getLeadingWhitespace(int offset) {
    StringBuffer indent = new StringBuffer();
    try {
      IRegion line = fDocument.getLineInformationOfOffset(offset);
      int lineOffset = line.getOffset();
      int nonWS =
          fScanner.findNonWhitespaceForwardInAnyPartition(
              lineOffset, lineOffset + line.getLength());
      indent.append(fDocument.get(lineOffset, nonWS - lineOffset));
      return indent;
    } catch (BadLocationException e) {
      return indent;
    }
  }

  /**
   * Creates an indentation string of the length indent - start, consisting of the content in <code>
   * fDocument</code> in the range [start, indent), with every character replaced by a space except
   * for tabs, which are kept as such.
   *
   * <p>If <code>convertSpaceRunsToTabs</code> is <code>true</code>, every run of the number of
   * spaces that make up a tab are replaced by a tab character. If it is not set, no conversion
   * takes place, but tabs in the original range are still copied verbatim.
   *
   * @param start the start of the document region to copy the indent from
   * @param indent the exclusive end of the document region to copy the indent from
   * @param convertSpaceRunsToTabs whether to convert consecutive runs of spaces to tabs
   * @return the indentation corresponding to the document content specified by <code>start</code>
   *     and <code>indent</code>
   */
  private StringBuffer createIndent(
      int start, final int indent, final boolean convertSpaceRunsToTabs) {
    final boolean convertTabs = fPrefs.prefUseTabs && convertSpaceRunsToTabs;
    final int tabLen = fPrefs.prefTabSize;
    final StringBuffer ret = new StringBuffer();
    try {
      int spaces = 0;
      while (start < indent) {

        char ch = fDocument.getChar(start);
        if (ch == '\t') {
          ret.append('\t');
          spaces = 0;
        } else if (convertTabs) {
          spaces++;
          if (spaces == tabLen) {
            ret.append('\t');
            spaces = 0;
          }
        } else {
          ret.append(' ');
        }

        start++;
      }
      // remainder
      while (spaces-- > 0) ret.append(' ');

    } catch (BadLocationException e) {
    }

    return ret;
  }

  /**
   * Creates a string with a visual length of the given <code>indentationSize</code>.
   *
   * @param buffer the original indent to reuse if possible
   * @param additional the additional indentation units to add or subtract to reference
   * @return the modified <code>buffer</code> reflecting the indentation adapted to <code>additional
   *     </code>
   */
  private StringBuffer createReusingIndent(StringBuffer buffer, int additional) {
    int refLength = computeVisualLength(buffer);
    int addLength = fPrefs.prefIndentationSize * additional; // may be < 0
    int totalLength = Math.max(0, refLength + addLength);

    // copy the reference indentation for the indent up to the last tab
    // stop within the maxCopy area
    int minLength = Math.min(totalLength, refLength);
    int tabSize = fPrefs.prefTabSize;
    int maxCopyLength =
        tabSize > 0 ? minLength - minLength % tabSize : minLength; // maximum indent to copy
    stripExceedingChars(buffer, maxCopyLength);

    // add additional indent
    int missing = totalLength - maxCopyLength;
    final int tabs, spaces;
    if (JavaCore.SPACE.equals(fPrefs.prefTabChar)) {
      tabs = 0;
      spaces = missing;
    } else if (JavaCore.TAB.equals(fPrefs.prefTabChar)) {
      tabs = tabSize > 0 ? missing / tabSize : 0;
      spaces = tabSize > 0 ? missing % tabSize : missing;
    } else if (DefaultCodeFormatterConstants.MIXED.equals(fPrefs.prefTabChar)) {
      tabs = tabSize > 0 ? missing / tabSize : 0;
      spaces = tabSize > 0 ? missing % tabSize : missing;
    } else {
      Assert.isTrue(false);
      return null;
    }
    for (int i = 0; i < tabs; i++) buffer.append('\t');
    for (int i = 0; i < spaces; i++) buffer.append(' ');
    return buffer;
  }

  /**
   * Returns the reference position regarding to indentation for <code>offset</code>, or <code>
   * NOT_FOUND</code>. This method calls {@link #findReferencePosition(int, int)
   * findReferencePosition(offset, nextChar)} where <code>nextChar</code> is the next character
   * after <code>offset</code>.
   *
   * @param offset the offset for which the reference is computed
   * @return the reference statement relative to which <code>offset</code> should be indented, or
   *     {@link JavaHeuristicScanner#NOT_FOUND}
   */
  public int findReferencePosition(int offset) {
    return findReferencePosition(offset, peekChar(offset));
  }

  /**
   * Peeks the next char in the document that comes after <code>offset</code> on the same line as
   * <code>offset</code>.
   *
   * @param offset the offset into document
   * @return the token symbol of the next element, or TokenEOF if there is none
   */
  private int peekChar(int offset) {
    if (offset < fDocument.getLength()) {
      try {
        IRegion line = fDocument.getLineInformationOfOffset(offset);
        int lineOffset = line.getOffset();
        int next = fScanner.nextToken(offset, lineOffset + line.getLength());
        return next;
      } catch (BadLocationException e) {
      }
    }
    return Symbols.TokenEOF;
  }

  /**
   * Returns the reference position regarding to indentation for <code>position</code>, or <code>
   * NOT_FOUND</code>.
   *
   * <p>If <code>peekNextChar</code> is <code>true</code>, the next token after <code>offset</code>
   * is read and taken into account when computing the indentation. Currently, if the next token is
   * the first token on the line (i.e. only preceded by whitespace), the following tokens are
   * specially handled:
   *
   * <ul>
   *   <li><code>switch</code> labels are indented relative to the switch block
   *   <li>opening curly braces are aligned correctly with the introducing code
   *   <li>closing curly braces are aligned properly with the introducing code of the matching
   *       opening brace
   *   <li>closing parenthesis' are aligned with their opening peer
   *   <li>the <code>else</code> keyword is aligned with its <code>if</code>, anything else is
   *       aligned normally (i.e. with the base of any introducing statements).
   *   <li>if there is no token on the same line after <code>offset</code>, the indentation is the
   *       same as for an <code>else</code> keyword
   * </ul>
   *
   * @param offset the offset for which the reference is computed
   * @param nextToken the next token to assume in the document
   * @return the reference statement relative to which <code>offset</code> should be indented, or
   *     {@link JavaHeuristicScanner#NOT_FOUND}
   */
  public int findReferencePosition(int offset, int nextToken) {
    boolean danglingElse = false;
    boolean unindent = false;
    boolean indent = false;
    boolean matchBrace = false;
    boolean matchParen = false;
    boolean matchCase = false;
    boolean throwsClause = false;

    // account for un-indentation characters already typed in, but after position
    // if they are on a line by themselves, the indentation gets adjusted
    // accordingly
    //
    // also account for a dangling else
    if (offset < fDocument.getLength()) {
      try {
        IRegion line = fDocument.getLineInformationOfOffset(offset);
        int lineOffset = line.getOffset();
        int prevPos = Math.max(offset - 1, 0);
        boolean isFirstTokenOnLine =
            fDocument.get(lineOffset, prevPos + 1 - lineOffset).trim().length() == 0;
        int prevToken = fScanner.previousToken(prevPos, JavaHeuristicScanner.UNBOUND);
        boolean bracelessBlockStart =
            fScanner.isBracelessBlockStart(prevPos, JavaHeuristicScanner.UNBOUND);

        switch (nextToken) {
          case Symbols.TokenELSE:
            danglingElse = true;
            break;
          case Symbols.TokenDEFAULT:
            fScanner.nextToken(offset, lineOffset + line.getLength());
            int next = fScanner.nextToken(fScanner.getPosition(), JavaHeuristicScanner.UNBOUND);
            if (next != Symbols.TokenCOLON) {
              break;
            }
            // $FALL-THROUGH$
          case Symbols.TokenCASE:
            if (isFirstTokenOnLine) matchCase = true;
            break;
          case Symbols.TokenLBRACE: // for opening-brace-on-new-line style
            if (bracelessBlockStart && !fPrefs.prefIndentBracesForBlocks) unindent = true;
            else if ((prevToken == Symbols.TokenCOLON || prevToken == Symbols.TokenEQUAL)
                && !fPrefs.prefIndentBracesForArrays) unindent = true;
            else if (!bracelessBlockStart && fPrefs.prefIndentBracesForMethods) indent = true;
            break;
          case Symbols.TokenRBRACE: // closing braces get unindented
            if (isFirstTokenOnLine) matchBrace = true;
            break;
          case Symbols.TokenRPAREN:
            if (isFirstTokenOnLine) matchParen = true;
            break;
          case Symbols.TokenTHROWS:
            throwsClause = true;
            break;
          case Symbols.TokenPLUS:
            if (isStringContinuation(offset)) {
              if (isSecondLineOfStringContinuation(offset)) {
                fAlign = JavaHeuristicScanner.NOT_FOUND;
                fIndent = fPrefs.prefContinuationIndent;
              } else {
                int previousLineOffset =
                    fDocument.getLineOffset(fDocument.getLineOfOffset(offset) - 1);
                fAlign =
                    fScanner.findNonWhitespaceForwardInAnyPartition(
                        previousLineOffset, JavaHeuristicScanner.UNBOUND);
              }
              return fPosition;
            }
            break;
        }
      } catch (BadLocationException e) {
      }
    } else {
      // don't assume an else could come if we are at the end of file
      danglingElse = false;
    }

    int ref =
        findReferencePosition(
            offset, danglingElse, matchBrace, matchParen, matchCase, throwsClause);
    if (unindent) fIndent--;
    if (indent) fIndent++;
    return ref;
  }

  /**
   * Tells whether the given string is a continuation expression.
   *
   * @param offset the offset for which the check is done
   * @return <code>true</code> if the offset is part of a string continuation, <code>false</code>
   *     otherwise
   * @since 3.7
   */
  private boolean isStringContinuation(int offset) {
    int nextNonWSCharPosition =
        fScanner.findNonWhitespaceBackwardInAnyPartition(offset - 1, JavaHeuristicScanner.UNBOUND);
    try {
      if (nextNonWSCharPosition != JavaHeuristicScanner.NOT_FOUND
          && fDocument.getChar(nextNonWSCharPosition) == '"') return true;
      else return false;
    } catch (BadLocationException e) {
      JavaPlugin.log(e);
      return false;
    }
  }

  /**
   * Checks if extra indentation for second line of string continuation is required.
   *
   * @param offset the offset for which the check is done
   * @return returns <code>true</code> if extra indentation for second line of string continuation
   *     is required
   * @since 3.7
   */
  private boolean isSecondLineOfStringContinuation(int offset) {
    try {
      int offsetLine = fDocument.getLineOfOffset(offset);
      fPosition = offset;
      while (true) {
        nextToken();
        switch (fToken) {
            // scopes: skip them
          case Symbols.TokenRPAREN:
          case Symbols.TokenRBRACKET:
          case Symbols.TokenRBRACE:
          case Symbols.TokenGREATERTHAN:
            skipScope();
            break;

          case Symbols.TokenPLUS:
            if ((offsetLine - fLine) > 1) {
              return false;
            }
            break;

          case Symbols.TokenCOMMA:
          case Symbols.TokenLPAREN:
          case Symbols.TokenLBRACE:
          case Symbols.TokenEQUAL:
            int stringStartingOffset =
                fScanner.findNonWhitespaceForwardInAnyPartition(
                    fPosition + 1, JavaHeuristicScanner.UNBOUND);
            int stringStartingLine = fDocument.getLineOfOffset(stringStartingOffset);
            if ((offsetLine - stringStartingLine) == 1) {
              fPosition = stringStartingOffset;
              return true;
            } else {
              return false;
            }
          case Symbols.TokenLBRACKET:
          case Symbols.TokenEOF:
            if ((offsetLine - fLine) == 1) return true;
            else return false;
        }
      }
    } catch (BadLocationException e) {
      JavaPlugin.log(e);
      return false;
    }
  }

  /**
   * Returns the reference position regarding to indentation for <code>position</code>, or <code>
   * NOT_FOUND</code>.<code>fIndent</code> will contain the relative indentation (in indentation
   * units, not characters) after the call. If there is a special alignment (e.g. for a method
   * declaration where parameters should be aligned), <code>fAlign</code> will contain the absolute
   * position of the alignment reference in <code>fDocument</code>, otherwise <code>fAlign</code> is
   * set to <code>JavaHeuristicScanner.NOT_FOUND</code>. This method calls {@link
   * #findReferencePosition(int, boolean, boolean, boolean, boolean, boolean)
   * findReferencePosition(offset, danglingElse, matchBrace, matchParen, matchCase, throwsClause)}
   * where <code>throwsClause</code> indicates whether a throws clause was found at <code>position
   * </code>.
   *
   * @param offset the offset for which the reference is computed
   * @param danglingElse whether a dangling else should be assumed at <code>position</code>
   * @param matchBrace whether the position of the matching brace should be returned instead of
   *     doing code analysis
   * @param matchParen whether the position of the matching parenthesis should be returned instead
   *     of doing code analysis
   * @param matchCase whether the position of a switch statement reference should be returned
   *     (either an earlier case statement or the switch block brace)
   * @return the reference statement relative to which <code>position</code> should be indented, or
   *     {@link JavaHeuristicScanner#NOT_FOUND}
   */
  public int findReferencePosition(
      int offset, boolean danglingElse, boolean matchBrace, boolean matchParen, boolean matchCase) {
    return findReferencePosition(offset, danglingElse, matchBrace, matchParen, matchCase, false);
  }

  /**
   * Returns the reference position regarding to indentation for <code>position</code>, or <code>
   * NOT_FOUND</code>.<code>fIndent</code> will contain the relative indentation (in indentation
   * units, not characters) after the call. If there is a special alignment (e.g. for a method
   * declaration where parameters should be aligned), <code>fAlign</code> will contain the absolute
   * position of the alignment reference in <code>fDocument</code>, otherwise <code>fAlign</code> is
   * set to <code>JavaHeuristicScanner.NOT_FOUND</code>.
   *
   * @param offset the offset for which the reference is computed
   * @param danglingElse whether a dangling else should be assumed at <code>position</code>
   * @param matchBrace whether the position of the matching brace should be returned instead of
   *     doing code analysis
   * @param matchParen whether the position of the matching parenthesis should be returned instead
   *     of doing code analysis
   * @param matchCase whether the position of a switch statement reference should be returned
   *     (either an earlier case statement or the switch block brace)
   * @param throwsClause whether a throws clause was found at <code>position</code>
   * @return the reference statement relative to which <code>position</code> should be indented, or
   *     {@link JavaHeuristicScanner#NOT_FOUND}
   * @since 3.7
   */
  public int findReferencePosition(
      int offset,
      boolean danglingElse,
      boolean matchBrace,
      boolean matchParen,
      boolean matchCase,
      boolean throwsClause) {
    fIndent = 0; // the indentation modification
    fAlign = JavaHeuristicScanner.NOT_FOUND;
    fPosition = offset;

    // forward cases
    // an unindentation happens sometimes if the next token is special, namely on braces, parens and
    // case labels
    // align braces, but handle the case where we align with the method declaration start instead of
    // the opening brace.
    if (matchBrace) {
      if (skipScope(Symbols.TokenLBRACE, Symbols.TokenRBRACE)) {
        try {
          // align with the opening brace that is on a line by its own
          int lineOffset = fDocument.getLineOffset(fLine);
          if (lineOffset <= fPosition
              && fDocument.get(lineOffset, fPosition - lineOffset).trim().length() == 0)
            return fPosition;
        } catch (BadLocationException e) {
          // concurrent modification - walk default path
        }
        // if the opening brace is not on the start of the line, skip to the start
        int pos = skipToStatementStart(true, true);
        fIndent = 0; // indent is aligned with reference position
        return pos;
      } else {
        // if we can't find the matching brace, the heuristic is to unindent
        // by one against the normal position
        int pos =
            findReferencePosition(offset, danglingElse, false, matchParen, matchCase, throwsClause);
        fIndent--;
        return pos;
      }
    }

    // align parenthesis'
    if (matchParen) {
      if (skipScope(Symbols.TokenLPAREN, Symbols.TokenRPAREN)) {
        fIndent = fPrefs.prefContinuationIndent;
        return fPosition;
      } else {
        // if we can't find the matching paren, the heuristic is to unindent
        // by one against the normal position
        int pos =
            findReferencePosition(offset, danglingElse, matchBrace, false, matchCase, throwsClause);
        fIndent--;
        return pos;
      }
    }

    // the only reliable way to get case labels aligned (due to many different styles of using
    // braces in a block)
    // is to go for another case statement, or the scope opening brace
    if (matchCase) {
      return matchCaseAlignment();
    }

    int storedPos = fPosition;
    if (peekChar(offset) == Symbols.TokenLBRACE && looksLikeMethodDeclLBrace(offset)) {
      return skipToStatementStart(danglingElse, false);
    }
    fPosition = storedPos;

    nextToken();
    switch (fToken) {
      case Symbols.TokenGREATERTHAN:
      case Symbols.TokenRBRACE:
        // skip the block and fall through
        // if we can't complete the scope, reset the scan position
        int pos = fPosition;
        if (!skipScope()) fPosition = pos;
        return skipToStatementStart(danglingElse, false);
      case Symbols.TokenSEMICOLON:
        // this is the 90% case: after a statement block
        // the end of the previous statement / block previous.end
        // search to the end of the statement / block before the previous; the token just after that
        // is previous.start
        pos = fPosition;
        if (isSemicolonPartOfForStatement()) {
          fIndent = fPrefs.prefContinuationIndent;
          return fPosition;
        } else {
          fPosition = pos;
          if (isTryWithResources()) {
            fIndent = fPrefs.prefContinuationIndent;
            return fPosition;
          } else {
            fPosition = pos;
            return skipToStatementStart(danglingElse, false);
          }
        }
        // scope introduction: special treat who special is
      case Symbols.TokenLPAREN:
      case Symbols.TokenLBRACE:
      case Symbols.TokenLBRACKET:
        return handleScopeIntroduction(offset + 1);

      case Symbols.TokenEOF:
        // trap when hitting start of document
        return JavaHeuristicScanner.NOT_FOUND;

      case Symbols.TokenEQUAL:
        // indent assignments
        return handleEqual();

      case Symbols.TokenCOLON:
        // TODO handle ternary deep indentation
        fIndent = fPrefs.prefCaseBlockIndent;
        return fPosition;

      case Symbols.TokenQUESTIONMARK:
        if (fPrefs.prefTernaryDeepAlign) {
          setFirstElementAlignment(fPosition, offset + 1);
          return fPosition;
        } else {
          fIndent = fPrefs.prefTernaryIndent;
          return fPosition;
        }

        // indentation for blockless introducers:
      case Symbols.TokenDO:
      case Symbols.TokenWHILE:
      case Symbols.TokenELSE:
        fIndent = fPrefs.prefSimpleIndent;
        return fPosition;

      case Symbols.TokenTRY:
        return skipToStatementStart(danglingElse, false);

      case Symbols.TokenRBRACKET:
        fIndent = fPrefs.prefContinuationIndent;
        return fPosition;

      case Symbols.TokenRPAREN:
        if (throwsClause) {
          fIndent = fPrefs.prefContinuationIndent;
          return fPosition;
        }
        int line = fLine;
        if (skipScope(Symbols.TokenLPAREN, Symbols.TokenRPAREN)) {
          int scope = fPosition;
          nextToken();
          if (fToken == Symbols.TokenIF
              || fToken == Symbols.TokenWHILE
              || fToken == Symbols.TokenFOR) {
            fIndent = fPrefs.prefSimpleIndent;
            return fPosition;
          }
          fPosition = scope;
          if (fToken == Symbols.TokenCATCH) {
            return skipToStatementStart(danglingElse, false);
          }
          fPosition = scope;
          if (looksLikeAnonymousTypeDecl()) {
            return skipToStatementStart(danglingElse, false);
          }
          fPosition = scope;
          if (looksLikeAnnotation()) {
            return skipToStatementStart(danglingElse, false);
          }
        }
        // restore
        fPosition = offset;
        fLine = line;

        return skipToPreviousListItemOrListStart();
      case Symbols.TokenRETURN:
        fIndent = fPrefs.prefContinuationIndent;
        return fPosition;
      case Symbols.TokenPLUS:
        if (isStringContinuation(fPosition)) {
          try {
            if (isSecondLineOfStringContinuation(offset)) {
              fAlign = JavaHeuristicScanner.NOT_FOUND;
              fIndent = fPrefs.prefContinuationIndent;
            } else {
              int previousLineOffset =
                  fDocument.getLineOffset(fDocument.getLineOfOffset(offset) - 1);
              fAlign =
                  fScanner.findNonWhitespaceForwardInAnyPartition(
                      previousLineOffset, JavaHeuristicScanner.UNBOUND);
            }
          } catch (BadLocationException e) {
            JavaPlugin.log(e);
          }
          return fPosition;
        }
        fPosition = offset;
        return skipToPreviousListItemOrListStart();
      case Symbols.TokenCOMMA:
        // inside a list of some type
        // easy if there is already a list item before with its own indentation - we just align
        // if not: take the start of the list ( LPAREN, LBRACE, LBRACKET ) and either align or
        // indent by list-indent
      default:
        // inside whatever we don't know about: similar to the list case:
        // if we are inside a continued expression, then either align with a previous line that has
        // indentation
        // or indent from the expression start line (either a scope introducer or the start of the
        // expr).
        return skipToPreviousListItemOrListStart();
    }
  }

  /**
   * Checks whether the Symbols.TokenLBRACE after <code>offset</code> probably represents the
   * beginning of a method body declaration.
   *
   * @param offset the document offset for which {@link #peekChar(int) peekChar(offset)} returns
   *     Symbols.TokenLBRACE
   * @return <code>true</code> if the left brace after <code>offset</code> looks like the beginning
   *     of a method body declaration, <code>false</code> otherwise
   * @since 3.9
   */
  private boolean looksLikeMethodDeclLBrace(int offset) {
    nextToken();
    while (true) {
      switch (fToken) {
        case Symbols.TokenTHROWS:
        case Symbols.TokenIDENT: // identifier for exception/annotation/annotation value
        case Symbols.TokenCOMMA:
        case Symbols.TokenAT:
        case Symbols.TokenLBRACKET: // for array valued return type
        case Symbols.TokenRBRACKET:
          break; // possible tokens between '{' and ')' in method declaration
        case Symbols.TokenOTHER: // dot of qualification
          try {
            if (fDocument.getChar(fPosition) != '.') {
              return false;
            }
          } catch (BadLocationException e) {
            return false;
          }
          break;
        case Symbols.TokenRPAREN: // parenthesis for annotation value / method declaration
          skipScope();
          int storedPos = fPosition;
          if (looksLikeMethodDecl()) return true;
          fPosition = storedPos;
          break;
        case Symbols.TokenEOF:
          return false;
        default:
          return false;
      }
      nextToken();
    }
  }

  /**
   * Checks if the statement at position is itself a continuation of the previous, else sets the
   * indentation to Continuation Indent.
   *
   * @return the position of the token
   * @since 3.7
   */
  private int handleEqual() {
    try {
      // If this line is itself continuation of the previous then do nothing
      IRegion line = fDocument.getLineInformationOfOffset(fPosition);
      int nonWS =
          fScanner.findNonWhitespaceBackward(line.getOffset(), JavaHeuristicScanner.UNBOUND);
      if (nonWS != Symbols.TokenEOF) {
        int tokenAtPreviousLine = fScanner.nextToken(nonWS, nonWS + 1);
        if (tokenAtPreviousLine != Symbols.TokenSEMICOLON
            && tokenAtPreviousLine != Symbols.TokenRBRACE
            && tokenAtPreviousLine != Symbols.TokenLBRACE
            && tokenAtPreviousLine != Symbols.TokenEOF) return fPosition;
      }
    } catch (BadLocationException e) {
      return fPosition;
    }

    fIndent = fPrefs.prefContinuationIndent;
    return fPosition;
  }

  /**
   * Checks if the semicolon at the current position is part of a for statement.
   *
   * @return returns <code>true</code> if current position is part of for statement
   * @since 3.7
   */
  private boolean isSemicolonPartOfForStatement() {
    int semiColonCount = 1;
    while (true) {
      nextToken();
      switch (fToken) {
        case Symbols.TokenFOR:
          return true;
        case Symbols.TokenLBRACE:
          return false;
        case Symbols.TokenSEMICOLON:
          semiColonCount++;
          if (semiColonCount > 2) return false;
          break;
        case Symbols.TokenCOLON:
          return false;
        case Symbols.TokenEOF:
          return false;
      }
    }
  }

  /**
   * Checks if the semicolon at the current position is part of a try with resources statement.
   *
   * @return returns <code>true</code> if current position is part of try with resources statement
   * @since 3.7
   */
  private boolean isTryWithResources() {
    while (true) {
      nextToken();
      switch (fToken) {
        case Symbols.TokenTRY:
          return true;
        case Symbols.TokenLBRACE:
          return false;
        case Symbols.TokenEOF:
          return false;
      }
    }
  }

  /**
   * Skips to the start of a statement that ends at the current position.
   *
   * @param danglingElse whether to indent aligned with the last <code>if</code>
   * @param isInBlock whether the current position is inside a block, which limits the search scope
   *     to the next scope introducer
   * @return the reference offset of the start of the statement
   */
  private int skipToStatementStart(boolean danglingElse, boolean isInBlock) {
    final int NOTHING = 0;
    final int READ_PARENS = 1;
    final int READ_IDENT = 2;
    int mayBeMethodBody = NOTHING;
    boolean isTypeBody = false;
    while (true) {
      nextToken();

      if (isInBlock) {
        switch (fToken) {
            // exit on all block introducers
          case Symbols.TokenIF:
          case Symbols.TokenELSE:
          case Symbols.TokenCATCH:
          case Symbols.TokenDO:
          case Symbols.TokenWHILE:
          case Symbols.TokenFINALLY:
          case Symbols.TokenFOR:
          case Symbols.TokenTRY:
            return fPosition;

          case Symbols.TokenSTATIC:
            mayBeMethodBody = READ_IDENT; // treat static blocks like methods
            break;

          case Symbols.TokenSYNCHRONIZED:
            // if inside a method declaration, use body indentation
            // else use block indentation.
            if (mayBeMethodBody != READ_IDENT) return fPosition;
            break;

          case Symbols.TokenCLASS:
          case Symbols.TokenINTERFACE:
          case Symbols.TokenENUM:
            isTypeBody = true;
            break;

          case Symbols.TokenSWITCH:
            fIndent = fPrefs.prefCaseIndent;
            return fPosition;
        }
      }

      switch (fToken) {
          // scope introduction through: LPAREN, LBRACE, LBRACKET
          // search stop on SEMICOLON, RBRACE, COLON, EOF
          // -> the next token is the start of the statement (i.e. previousPos when backward
          // scanning)
        case Symbols.TokenLPAREN:
        case Symbols.TokenLBRACE:
        case Symbols.TokenLBRACKET:
        case Symbols.TokenSEMICOLON:
        case Symbols.TokenEOF:
          if (isInBlock) fIndent = getBlockIndent(mayBeMethodBody == READ_IDENT, isTypeBody);
          // else: fIndent set by previous calls
          return fPreviousPos;

        case Symbols.TokenCOLON:
          int pos = fPreviousPos;
          if (!isConditional()) return pos;
          break;

        case Symbols.TokenRBRACE:
          // RBRACE is a little tricky: it can be the end of an array definition, but
          // usually it is the end of a previous block
          pos = fPreviousPos; // store state
          if (skipScope() && looksLikeArrayInitializerIntro()) {
            continue; // it's an array
          } else {
            if (isInBlock) fIndent = getBlockIndent(mayBeMethodBody == READ_IDENT, isTypeBody);
            return pos; // it's not - do as with all the above
          }

          // scopes: skip them
        case Symbols.TokenRPAREN:
          if (isInBlock) mayBeMethodBody = READ_PARENS;
          // $FALL-THROUGH$
        case Symbols.TokenRBRACKET:
        case Symbols.TokenGREATERTHAN:
          pos = fPreviousPos;
          if (skipScope()) break;
          else return pos;

          // IF / ELSE: align the position after the conditional block with the if
          // so we are ready for an else, except if danglingElse is false
          // in order for this to work, we must skip an else to its if
        case Symbols.TokenIF:
          if (danglingElse) return fPosition;
          else break;
        case Symbols.TokenELSE:
          // skip behind the next if, as we have that one covered
          pos = fPosition;
          if (skipNextIF()) break;
          else return pos;

        case Symbols.TokenCATCH:
        case Symbols.TokenFINALLY:
          pos = fPosition;
          if (skipNextTRY()) break;
          else return pos;

        case Symbols.TokenDO:
          // align the WHILE position with its do
          return fPosition;

        case Symbols.TokenWHILE:
          // this one is tricky: while can be the start of a while loop
          // or the end of a do - while
          pos = fPosition;
          if (hasMatchingDo()) {
            // continue searching from the DO on
            break;
          } else {
            // continue searching from the WHILE on
            fPosition = pos;
            break;
          }
        case Symbols.TokenIDENT:
          if (mayBeMethodBody == READ_PARENS) mayBeMethodBody = READ_IDENT;
          break;

        default:
          // keep searching

      }
    }
  }

  private int getBlockIndent(boolean isMethodBody, boolean isTypeBody) {
    if (isTypeBody) return fPrefs.prefTypeIndent + (fPrefs.prefIndentBracesForTypes ? 1 : 0);
    else if (isMethodBody)
      return fPrefs.prefMethodBodyIndent + (fPrefs.prefIndentBracesForMethods ? 1 : 0);
    else return fIndent;
  }

  /**
   * Returns true if the colon at the current position is part of a conditional (ternary)
   * expression, false otherwise.
   *
   * @return true if the colon at the current position is part of a conditional
   */
  private boolean isConditional() {
    while (true) {
      nextToken();
      switch (fToken) {

          // search for case labels, which consist of (possibly qualified) identifiers or numbers
        case Symbols.TokenIDENT:
        case Symbols.TokenOTHER: // dots for qualified constants
          continue;
        case Symbols.TokenCASE:
        case Symbols.TokenDEFAULT:
          return false;
        default:
          return true;
      }
    }
  }

  /**
   * Returns as a reference any previous <code>switch</code> labels (<code>case</code> or <code>
   * default</code>) or the offset of the brace that scopes the switch statement. Sets <code>fIndent
   * </code> to <code>prefCaseIndent</code> upon a match.
   *
   * @return the reference offset for a <code>switch</code> label
   */
  private int matchCaseAlignment() {
    while (true) {
      nextToken();
      switch (fToken) {
          // invalid cases: another case label or an LBRACE must come before a case
          // -> bail out with the current position
        case Symbols.TokenLPAREN:
        case Symbols.TokenLBRACKET:
        case Symbols.TokenEOF:
          return fPosition;
        case Symbols.TokenLBRACE:
          // opening brace of switch statement
          fIndent = fPrefs.prefCaseIndent;
          return fPosition;
        case Symbols.TokenCASE:
        case Symbols.TokenDEFAULT:
          // align with previous label
          fIndent = 0;
          return fPosition;

          // scopes: skip them
        case Symbols.TokenRPAREN:
        case Symbols.TokenRBRACKET:
        case Symbols.TokenRBRACE:
        case Symbols.TokenGREATERTHAN:
          skipScope();
          break;

        default:
          // keep searching
          continue;
      }
    }
  }

  /**
   * Returns the reference position for a list element. The algorithm tries to match any previous
   * indentation on the same list. If there is none, the reference position returned is determined
   * depending on the type of list: The indentation will either match the list scope introducer
   * (e.g. for method declarations), so called deep indents, or simply increase the indentation by a
   * number of standard indents. See also {@link #handleScopeIntroduction(int)}.
   *
   * @return the reference position for a list item: either a previous list item that has its own
   *     indentation, or the list introduction start.
   */
  private int skipToPreviousListItemOrListStart() {
    int startLine = fLine;
    int startPosition = fPosition;
    while (true) {
      nextToken();

      // if any line item comes with its own indentation, adapt to it
      if (fLine < startLine) {
        try {
          int lineOffset = fDocument.getLineOffset(startLine);
          int bound = Math.min(fDocument.getLength(), startPosition + 1);
          fAlign = fScanner.findNonWhitespaceForwardInAnyPartition(lineOffset, bound);
        } catch (BadLocationException e) {
          // ignore and return just the position
        }
        return startPosition;
      }

      switch (fToken) {
          // scopes: skip them
        case Symbols.TokenRPAREN:
        case Symbols.TokenRBRACKET:
        case Symbols.TokenRBRACE:
        case Symbols.TokenGREATERTHAN:
          skipScope();
          break;

          // scope introduction: special treat who special is
        case Symbols.TokenLPAREN:
        case Symbols.TokenLBRACE:
        case Symbols.TokenLBRACKET:
          return handleScopeIntroduction(startPosition + 1);

        case Symbols.TokenSEMICOLON:
          int savedPosition = fPosition;
          if (isSemicolonPartOfForStatement()) fIndent = fPrefs.prefContinuationIndent;
          else fPosition = savedPosition;
          return fPosition;
        case Symbols.TokenQUESTIONMARK:
          if (fPrefs.prefTernaryDeepAlign) {
            setFirstElementAlignment(fPosition - 1, fPosition + 1);
            return fPosition;
          } else {
            fIndent = fPrefs.prefTernaryIndent;
            return fPosition;
          }
        case Symbols.TokenRETURN:
          fIndent = fPrefs.prefContinuationIndent;
          return fPosition;
        case Symbols.TokenEQUAL:
          return handleEqual();
        case Symbols.TokenEOF:
          return 0;
      }
    }
  }

  /**
   * Skips a scope and positions the cursor (<code>fPosition</code>) on the token that opens the
   * scope. Returns <code>true</code> if a matching peer could be found, <code>false</code>
   * otherwise. The current token when calling must be one out of <code>Symbols.TokenRPAREN</code>,
   * <code>Symbols.TokenRBRACE</code>, and <code>Symbols.TokenRBRACKET</code>.
   *
   * @return <code>true</code> if a matching peer was found, <code>false</code> otherwise
   */
  private boolean skipScope() {
    switch (fToken) {
      case Symbols.TokenRPAREN:
        return skipScope(Symbols.TokenLPAREN, Symbols.TokenRPAREN);
      case Symbols.TokenRBRACKET:
        return skipScope(Symbols.TokenLBRACKET, Symbols.TokenRBRACKET);
      case Symbols.TokenRBRACE:
        return skipScope(Symbols.TokenLBRACE, Symbols.TokenRBRACE);
      case Symbols.TokenGREATERTHAN:
        if (!fPrefs.prefHasGenerics) return false;
        int storedPosition = fPosition;
        int storedToken = fToken;
        nextToken();
        switch (fToken) {
          case Symbols.TokenLESSTHAN:
            return true;
          case Symbols.TokenIDENT:
            boolean isGenericStarter;
            try {
              isGenericStarter = !JavaHeuristicScanner.isGenericStarter(getTokenContent());
            } catch (BadLocationException e) {
              return false;
            }
            if (isGenericStarter) break;
            // $FALL-THROUGH$
          case Symbols.TokenQUESTIONMARK:
          case Symbols.TokenGREATERTHAN:
            if (skipScope(Symbols.TokenLESSTHAN, Symbols.TokenGREATERTHAN)) return true;
        }
        // <...> are harder to detect - restore the position if we fail
        fPosition = storedPosition;
        fToken = storedToken;
        return false;

      default:
        Assert.isTrue(false);
        return false;
    }
  }

  /**
   * Returns the contents of the current token.
   *
   * @return the contents of the current token
   * @throws BadLocationException if the indices are out of bounds
   * @since 3.1
   */
  private CharSequence getTokenContent() throws BadLocationException {
    return new DocumentCharacterIterator(fDocument, fPosition, fPreviousPos);
  }

  /**
   * Handles the introduction of a new scope. The current token must be one out of <code>
   * Symbols.TokenLPAREN</code>, <code>Symbols.TokenLBRACE</code>, and <code>Symbols.TokenLBRACKET
   * </code>. Returns as the reference position either the token introducing the scope or - if
   * available - the first java token after that.
   *
   * <p>Depending on the type of scope introduction, the indentation will align (deep indenting)
   * with the reference position (<code>fAlign</code> will be set to the reference position) or
   * <code>fIndent</code> will be set to the number of indentation units.
   *
   * @param bound the bound for the search for the first token after the scope introduction.
   * @return the indent
   */
  private int handleScopeIntroduction(int bound) {
    switch (fToken) {
        // scope introduction: special treat who special is
      case Symbols.TokenLPAREN:
        int pos = fPosition; // store

        // special: method declaration deep indentation
        if (looksLikeMethodDecl()) {
          if (fPrefs.prefMethodDeclDeepIndent) return setFirstElementAlignment(pos, bound);
          else {
            fIndent = fPrefs.prefMethodDeclIndent;
            return pos;
          }
        } else {
          fPosition = pos;
          if (looksLikeMethodCall()) {
            if (fPrefs.prefMethodCallDeepIndent) return setFirstElementAlignment(pos, bound);
            else {
              fIndent = fPrefs.prefMethodCallIndent;
              return pos;
            }
          } else if (fPrefs.prefParenthesisDeepIndent) return setFirstElementAlignment(pos, bound);
        }

        // normal: return the parenthesis as reference
        fIndent = fPrefs.prefParenthesisIndent;
        return pos;

      case Symbols.TokenLBRACE:
        pos = fPosition; // store

        // special: array initializer
        if (looksLikeArrayInitializerIntro())
          if (fPrefs.prefArrayDeepIndent) return setFirstElementAlignment(pos, bound);
          else fIndent = fPrefs.prefArrayIndent;
        else fIndent = fPrefs.prefBlockIndent;

        // normal: skip to the statement start before the scope introducer
        // opening braces are often on differently ending indents than e.g. a method definition
        if (looksLikeArrayInitializerIntro() && !fPrefs.prefIndentBracesForArrays
            || !fPrefs.prefIndentBracesForBlocks) {
          fPosition = pos; // restore
          return skipToStatementStart(true, true); // set to true to match the first if
        } else {
          return pos;
        }

      case Symbols.TokenLBRACKET:
        pos = fPosition; // store

        // special: method declaration deep indentation
        if (fPrefs.prefArrayDimensionsDeepIndent) {
          return setFirstElementAlignment(pos, bound);
        }

        // normal: return the bracket as reference
        fIndent = fPrefs.prefBracketIndent;
        return pos; // restore

      default:
        Assert.isTrue(false);
        return -1; // dummy
    }
  }

  /**
   * Sets the deep indent offset (<code>fAlign</code>) to either the offset right after <code>
   * scopeIntroducerOffset</code> or - if available - the first Java token after <code>
   * scopeIntroducerOffset</code>, but before <code>bound</code>.
   *
   * @param scopeIntroducerOffset the offset of the scope introducer
   * @param bound the bound for the search for another element
   * @return the reference position
   */
  private int setFirstElementAlignment(int scopeIntroducerOffset, int bound) {
    int firstPossible =
        scopeIntroducerOffset + 1; // align with the first position after the scope intro
    fAlign = fScanner.findNonWhitespaceForwardInAnyPartition(firstPossible, bound);
    if (fAlign == JavaHeuristicScanner.NOT_FOUND) fAlign = firstPossible;
    return fAlign;
  }

  /**
   * Returns <code>true</code> if the next token received after calling <code>nextToken</code> is
   * either an equal sign or an array designator ('[]').
   *
   * @return <code>true</code> if the next elements look like the start of an array definition
   */
  private boolean looksLikeArrayInitializerIntro() {
    nextToken();
    if (fToken == Symbols.TokenEQUAL || skipBrackets()) {
      return true;
    }
    return false;
  }

  /**
   * Skips over the next <code>if</code> keyword. The current token when calling this method must be
   * an <code>else</code> keyword. Returns <code>true</code> if a matching <code>if</code> could be
   * found, <code>false</code> otherwise. The cursor (<code>fPosition</code>) is set to the offset
   * of the <code>if</code> token.
   *
   * @return <code>true</code> if a matching <code>if</code> token was found, <code>false</code>
   *     otherwise
   */
  private boolean skipNextIF() {
    Assert.isTrue(fToken == Symbols.TokenELSE);

    while (true) {
      nextToken();
      switch (fToken) {
          // scopes: skip them
        case Symbols.TokenRPAREN:
        case Symbols.TokenRBRACKET:
        case Symbols.TokenRBRACE:
        case Symbols.TokenGREATERTHAN:
          skipScope();
          break;

        case Symbols.TokenIF:
          // found it, return
          return true;
        case Symbols.TokenELSE:
          // recursively skip else-if blocks
          skipNextIF();
          break;

          // shortcut scope starts
        case Symbols.TokenLPAREN:
        case Symbols.TokenLBRACE:
        case Symbols.TokenLBRACKET:
        case Symbols.TokenEOF:
          return false;
      }
    }
  }

  /**
   * Skips over the next <code>try</code> keyword. The current token when calling this method must
   * be a <code>catch</code> or <code>finally</code> keyword. Returns <code>true</code> if a
   * matching <code>try</code> could be found, <code>false</code> otherwise. The cursor ( <code>
   * fPosition</code>) is set to the offset of the <code>try</code> token.
   *
   * @return <code>true</code> if a matching <code>try</code> token was found, <code>false</code>
   *     otherwise
   * @since 3.7
   */
  private boolean skipNextTRY() {
    Assert.isTrue(fToken == Symbols.TokenCATCH || fToken == Symbols.TokenFINALLY);

    while (true) {
      nextToken();
      switch (fToken) {
          // scopes: skip them
        case Symbols.TokenRPAREN:
        case Symbols.TokenRBRACKET:
        case Symbols.TokenRBRACE:
        case Symbols.TokenGREATERTHAN:
          skipScope();
          break;

        case Symbols.TokenTRY:
          // found it
          return true;

          // shortcut scope starts
        case Symbols.TokenLPAREN:
        case Symbols.TokenLBRACE:
        case Symbols.TokenLBRACKET:
        case Symbols.TokenEOF:
          return false;
      }
    }
  }

  /**
   * while(condition); is ambiguous when parsed backwardly, as it is a valid statement by its own,
   * so we have to check whether there is a matching do. A <code>do</code> can either be separated
   * from the while by a block, or by a single statement, which limits our search distance.
   *
   * @return <code>true</code> if the <code>while</code> currently in <code>fToken</code> has a
   *     matching <code>do</code>.
   */
  private boolean hasMatchingDo() {
    Assert.isTrue(fToken == Symbols.TokenWHILE);
    nextToken();
    switch (fToken) {
      case Symbols.TokenRBRACE:
        skipScope();
        // $FALL-THROUGH$
      case Symbols.TokenSEMICOLON:
        skipToStatementStart(false, false);
        return fToken == Symbols.TokenDO;
    }
    return false;
  }

  /**
   * Skips brackets if the current token is a RBRACKET. There can be nothing but whitespace in
   * between, this is only to be used for <code>[]</code> elements.
   *
   * @return <code>true</code> if a <code>[]</code> could be scanned, the current token is left at
   *     the LBRACKET.
   */
  private boolean skipBrackets() {
    if (fToken == Symbols.TokenRBRACKET) {
      nextToken();
      if (fToken == Symbols.TokenLBRACKET) {
        return true;
      }
    }
    return false;
  }

  /**
   * Reads the next token in backward direction from the heuristic scanner and sets the fields
   * <code>fToken, fPreviousPosition</code> and <code>fPosition</code> accordingly.
   */
  private void nextToken() {
    nextToken(fPosition);
  }

  /**
   * Reads the next token in backward direction of <code>start</code> from the heuristic scanner and
   * sets the fields <code>fToken, fPreviousPosition</code> and <code>fPosition</code> accordingly.
   *
   * @param start the start offset from which to scan backwards
   */
  private void nextToken(int start) {
    fToken = fScanner.previousToken(start - 1, JavaHeuristicScanner.UNBOUND);
    fPreviousPos = start;
    fPosition = fScanner.getPosition() + 1;
    try {
      fLine = fDocument.getLineOfOffset(fPosition);
    } catch (BadLocationException e) {
      fLine = -1;
    }
  }

  /**
   * Returns <code>true</code> if the current tokens look like a method declaration header (i.e.
   * only the return type and method name). The heuristic calls <code>nextToken</code> and expects
   * an identifier (method name) and a type declaration (an identifier with optional brackets) which
   * also covers the visibility modifier of constructors; it does not recognize package visible
   * constructors.
   *
   * @return <code>true</code> if the current position looks like a method declaration header.
   */
  private boolean looksLikeMethodDecl() {
    /*
     * TODO This heuristic does not recognize package private constructors
     * since those do have neither type nor visibility keywords.
     * One option would be to go over the parameter list, but that might
     * be empty as well, or not typed in yet - hard to do without an AST...
     */

    nextToken();
    if (fToken == Symbols.TokenIDENT) { // method name
      do nextToken();
      while (skipBrackets()); // optional brackets for array valued return types

      return fToken == Symbols.TokenIDENT; // return type name
    }
    return false;
  }

  /**
   * Returns <code>true</code> if the current tokens look like an annotation (i.e. an annotation
   * name (potentially qualified) preceded by an at-sign).
   *
   * @return <code>true</code> if the current position looks like an annotation.
   * @since 3.7
   */
  private boolean looksLikeAnnotation() {
    nextToken();
    if (fToken == Symbols.TokenIDENT) { // Annotation name
      nextToken();
      while (fToken == Symbols.TokenOTHER) { // dot of qualification
        nextToken();
        if (fToken != Symbols.TokenIDENT) // qualifying name
        return false;
        nextToken();
      }
      return fToken == Symbols.TokenAT;
    }
    return false;
  }

  /**
   * Returns <code>true</code> if the current tokens look like an anonymous type declaration header
   * (i.e. a type name (potentially qualified) and a new keyword). The heuristic calls <code>
   * nextToken</code> and expects a possibly qualified identifier (type name) and a new keyword
   *
   * @return <code>true</code> if the current position looks like a anonymous type declaration
   *     header.
   */
  private boolean looksLikeAnonymousTypeDecl() {

    nextToken();
    if (fToken == Symbols.TokenIDENT) { // type name
      nextToken();
      while (fToken == Symbols.TokenOTHER) { // dot of qualification
        nextToken();
        if (fToken != Symbols.TokenIDENT) // qualifying name
        return false;
        nextToken();
      }
      return fToken == Symbols.TokenNEW;
    }
    return false;
  }

  /**
   * Returns <code>true</code> if the current tokens look like a method call header (i.e. an
   * identifier as opposed to a keyword taking parenthesized parameters such as <code>if</code>).
   *
   * <p>The heuristic calls <code>nextToken</code> and expects an identifier (method name).
   *
   * @return <code>true</code> if the current position looks like a method call header.
   */
  private boolean looksLikeMethodCall() {
    // TODO [5.0] add awareness for constructor calls with generic types: new ArrayList<String>()
    nextToken();
    return fToken == Symbols.TokenIDENT; // method name
  }

  /**
   * Scans tokens for the matching opening peer. The internal cursor (<code>fPosition</code>) is set
   * to the offset of the opening peer if found.
   *
   * @param openToken the opening peer token
   * @param closeToken the closing peer token
   * @return <code>true</code> if a matching token was found, <code>false</code> otherwise
   */
  private boolean skipScope(int openToken, int closeToken) {

    int depth = 1;

    while (true) {
      nextToken();

      if (fToken == closeToken) {
        depth++;
      } else if (fToken == openToken) {
        depth--;
        if (depth == 0) return true;
      } else if (fToken == Symbols.TokenEOF) {
        return false;
      }
    }
  }
}
