/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.text.java;

import java.util.Arrays;
import org.eclipse.che.jface.text.DocumentCommand;
import org.eclipse.che.jface.text.ITextSelection;
import org.eclipse.che.jface.text.TextSelection;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.text.edits.MalformedTreeException;

/**
 * Modifies <code>DocumentCommand</code>s inserting semicolons and opening braces to place them
 * smartly, i.e. moving them to the end of a line if that is what the user expects.
 *
 * <p>In practice, semicolons and braces (and the caret) are moved to the end of the line if they
 * are typed anywhere except for semicolons in a <code>for</code> statements definition. If the line
 * contains a semicolon or brace after the current caret position, the cursor is moved after it.
 *
 * @see DocumentCommand
 * @since 3.0
 */
public class SmartSemicolonAutoEditStrategy /*implements IAutoEditStrategy*/ {

  /** String representation of a semicolon. */
  private static final String SEMICOLON = ";"; // $NON-NLS-1$
  /** Char representation of a semicolon. */
  private static final char SEMICHAR = ';';
  /** String represenattion of a opening brace. */
  private static final String BRACE = "{"; // $NON-NLS-1$
  /** Char representation of a opening brace */
  private static final char BRACECHAR = '{';

  private char fCharacter;
  private String fPartitioning;

  /**
   * Creates a new SmartSemicolonAutoEditStrategy.
   *
   * @param partitioning the document partitioning
   */
  public SmartSemicolonAutoEditStrategy(String partitioning) {
    fPartitioning = partitioning;
  }

  /*
   * @see org.eclipse.jface.text.IAutoEditStrategy#customizeDocumentCommand(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.DocumentCommand)
   */
  public void customizeDocumentCommand(IDocument document, DocumentCommand command) {
    // 0: early pruning
    // also customize if <code>doit</code> is false (so it works in code completion situations)
    //		if (!command.doit)
    //			return;

    if (command.text == null) return;

    if (command.text.equals(SEMICOLON)) fCharacter = SEMICHAR;
    else if (command.text.equals(BRACE)) fCharacter = BRACECHAR;
    else return;

    //		IPreferenceStore store= JavaPlugin.getDefault().getPreferenceStore();
    //		if (fCharacter == SEMICHAR && !store.getBoolean(PreferenceConstants.EDITOR_SMART_SEMICOLON))
    //			return;
    //		if (fCharacter == BRACECHAR &&
    // !store.getBoolean(PreferenceConstants.EDITOR_SMART_OPENING_BRACE))
    //			return;
    //
    //		IWorkbenchPage page= JavaPlugin.getActivePage();
    //		if (page == null)
    //			return;
    //		IEditorPart part= page.getActiveEditor();
    //		if (!(part instanceof CompilationUnitEditor))
    //			return;
    //		CompilationUnitEditor editor= (CompilationUnitEditor)part;
    //		if (editor.getInsertMode() != ITextEditorExtension3.SMART_INSERT || !editor.isEditable())
    //			return;
    //		ITextEditorExtension2 extension=
    // (ITextEditorExtension2)editor.getAdapter(ITextEditorExtension2.class);
    //		if (extension != null && !extension.validateEditorInputState())
    //			return;
    //		if (isMultilineSelection(document, command))
    //			return;

    // 1: find concerned line / position in java code, location in statement
    int pos = command.offset;
    ITextSelection line;
    try {
      IRegion l = document.getLineInformationOfOffset(pos);
      line = new TextSelection(document, l.getOffset(), l.getLength());
    } catch (BadLocationException e) {
      return;
    }

    // 2: choose action based on findings (is for-Statement?)
    // for now: compute the best position to insert the new character
    int positionInLine =
        computeCharacterPosition(document, line, pos - line.getOffset(), fCharacter, fPartitioning);
    int position = positionInLine + line.getOffset();

    // never position before the current position!
    if (position < pos) return;

    // never double already existing content
    if (alreadyPresent(document, fCharacter, position)) return;

    // don't do special processing if what we do is actually the normal behaviour
    String insertion = adjustSpacing(document, position, fCharacter);
    if (command.offset == position && insertion.equals(command.text)) return;

    try {

      //			final SmartBackspaceManager manager= (SmartBackspaceManager)
      // editor.getAdapter(SmartBackspaceManager.class);
      //			if (manager != null &&
      // JavaPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.EDITOR_SMART_BACKSPACE)) {
      //				TextEdit e1= new ReplaceEdit(command.offset, command.text.length(),
      // document.get(command.offset, command.length));
      //				UndoSpec s1= new UndoSpec(command.offset + command.text.length(),
      //						new Region(command.offset, 0),
      //						new TextEdit[] {e1},
      //						0,
      //						null);
      //
      //				DeleteEdit smart= new DeleteEdit(position, insertion.length());
      //				ReplaceEdit raw= new ReplaceEdit(command.offset, command.length, command.text);
      //				UndoSpec s2= new UndoSpec(position + insertion.length(),
      //						new Region(command.offset + command.text.length(), 0),
      //						new TextEdit[] {smart, raw},
      //						2,
      //						s1);
      //				manager.register(s2);
      //			}

      // 3: modify command
      command.offset = position;
      command.length = 0;
      command.caretOffset = position;
      command.text = insertion;
      command.doit = true;
      command.owner = null;
    } catch (MalformedTreeException e) {
      JavaPlugin.log(e);
    }
  }

  /**
   * Returns <code>true</code> if the document command is applied on a multi line selection, <code>
   * false</code> otherwise.
   *
   * @param document the document
   * @param command the command
   * @return <code>true</code> if <code>command</code> is a multiline command
   */
  private boolean isMultilineSelection(IDocument document, DocumentCommand command) {
    try {
      return document.getNumberOfLines(command.offset, command.length) > 1;
    } catch (BadLocationException e) {
      // ignore
      return false;
    }
  }

  /**
   * Adds a space before a brace if it is inserted after a parenthesis, equal sign, or one of the
   * keywords <code>try, else, do</code>.
   *
   * @param doc the document we are working on
   * @param position the insert position of <code>character</code>
   * @param character the character to be inserted
   * @return a <code>String</code> consisting of <code>character</code> plus any additional spacing
   */
  private String adjustSpacing(IDocument doc, int position, char character) {
    if (character == BRACECHAR) {
      if (position > 0 && position <= doc.getLength()) {
        int pos = position - 1;
        if (looksLike(doc, pos, ")") // $NON-NLS-1$
            || looksLike(doc, pos, "=") // $NON-NLS-1$
            || looksLike(doc, pos, "]") // $NON-NLS-1$
            || looksLike(doc, pos, "try") // $NON-NLS-1$
            || looksLike(doc, pos, "else") // $NON-NLS-1$
            || looksLike(doc, pos, "synchronized") // $NON-NLS-1$
            || looksLike(doc, pos, "static") // $NON-NLS-1$
            || looksLike(doc, pos, "finally") // $NON-NLS-1$
            || looksLike(doc, pos, "do")) // $NON-NLS-1$
        return new String(new char[] {' ', character});
      }
    }

    return new String(new char[] {character});
  }

  /**
   * Checks whether a character to be inserted is already present at the insert location (perhaps
   * separated by some whitespace from <code>position</code>.
   *
   * @param document the document we are working on
   * @param position the insert position of <code>ch</code>
   * @param ch the character to be inserted
   * @return <code>true</code> if <code>ch</code> is already present at <code>location</code>,
   *     <code>false</code> otherwise
   */
  private boolean alreadyPresent(IDocument document, char ch, int position) {
    int pos = firstNonWhitespaceForward(document, position, fPartitioning, document.getLength());
    try {
      if (pos != -1 && document.getChar(pos) == ch) return true;
    } catch (BadLocationException e) {
    }

    return false;
  }

  /**
   * Computes the next insert position of the given character in the current line.
   *
   * @param document the document we are working on
   * @param line the line where the change is being made
   * @param offset the position of the caret in the line when <code>character</code> was typed
   * @param character the character to look for
   * @param partitioning the document partitioning
   * @return the position where <code>character</code> should be inserted / replaced
   */
  protected static int computeCharacterPosition(
      IDocument document, ITextSelection line, int offset, char character, String partitioning) {
    String text = line.getText();
    if (text == null) return 0;

    int insertPos;
    if (character == BRACECHAR) {

      insertPos = computeArrayInitializationPos(document, line, offset, partitioning);

      if (insertPos == -1) {
        insertPos = computeAfterTryDoElse(document, line, offset);
      }

      if (insertPos == -1) {
        insertPos = computeAfterParenthesis(document, line, offset, partitioning);
      }

    } else if (character == SEMICHAR) {

      if (isForStatement(text, offset)) {
        insertPos = -1; // don't do anything in for statements, as semis are vital part of these
      } else {
        int nextPartitionPos = nextPartitionOrLineEnd(document, line, offset, partitioning);
        insertPos = startOfWhitespaceBeforeOffset(text, nextPartitionPos);
        // if there is a semi present, return its location as alreadyPresent() will take it out this
        // way.
        if (insertPos > 0 && text.charAt(insertPos - 1) == character) insertPos = insertPos - 1;
        else if (insertPos > 0 && text.charAt(insertPos - 1) == '}') {
          int opening =
              scanBackward(
                  document, insertPos - 1 + line.getOffset(), partitioning, -1, new char[] {'{'});
          if (opening > -1 && opening < offset + line.getOffset()) {
            if (computeArrayInitializationPos(
                    document, line, opening - line.getOffset(), partitioning)
                == -1) {
              insertPos = offset;
            }
          }
        }
      }

    } else {
      Assert.isTrue(false);
      return -1;
    }

    return insertPos;
  }

  /**
   * Computes an insert position for an opening brace if <code>offset</code> maps to a position in
   * <code>document</code> that looks like being the RHS of an assignment or like an array
   * definition.
   *
   * @param document the document being modified
   * @param line the current line under investigation
   * @param offset the offset of the caret position, relative to the line start.
   * @param partitioning the document partitioning
   * @return an insert position relative to the line start if <code>line</code> looks like being an
   *     array initialization at <code>offset</code>, -1 otherwise
   */
  private static int computeArrayInitializationPos(
      IDocument document, ITextSelection line, int offset, String partitioning) {
    // search backward while WS, find = (not != <= >= ==) in default partition
    int pos = offset + line.getOffset();

    if (pos == 0) return -1;

    int p = firstNonWhitespaceBackward(document, pos - 1, partitioning, -1);

    if (p == -1) return -1;

    try {

      char ch = document.getChar(p);
      if (ch != '=' && ch != ']') return -1;

      if (p == 0) return offset;

      p = firstNonWhitespaceBackward(document, p - 1, partitioning, -1);
      if (p == -1) return -1;

      ch = document.getChar(p);
      if (Character.isJavaIdentifierPart(ch) || ch == ']' || ch == '[') return offset;

    } catch (BadLocationException e) {
    }
    return -1;
  }

  /**
   * Computes an insert position for an opening brace if <code>offset</code> maps to a position in
   * <code>doc</code> involving a keyword taking a block after it. These are: <code>try</code>,
   * <code>do</code>, <code>synchronized</code>, <code>static</code>, <code>finally</code>, or
   * <code>else</code>.
   *
   * @param doc the document being modified
   * @param line the current line under investigation
   * @param offset the offset of the caret position, relative to the line start.
   * @return an insert position relative to the line start if <code>line</code> contains one of the
   *     above keywords at or before <code>offset</code>, -1 otherwise
   */
  private static int computeAfterTryDoElse(IDocument doc, ITextSelection line, int offset) {
    // search backward while WS, find 'try', 'do', 'else' in default partition
    int p = offset + line.getOffset();
    p = firstWhitespaceToRight(doc, p);
    if (p == -1) return -1;
    p--;

    if (looksLike(doc, p, "try") // $NON-NLS-1$
        || looksLike(doc, p, "do") // $NON-NLS-1$
        || looksLike(doc, p, "synchronized") // $NON-NLS-1$
        || looksLike(doc, p, "static") // $NON-NLS-1$
        || looksLike(doc, p, "finally") // $NON-NLS-1$
        || looksLike(doc, p, "else")) // $NON-NLS-1$
    return p + 1 - line.getOffset();

    return -1;
  }

  /**
   * Computes an insert position for an opening brace if <code>offset</code> maps to a position in
   * <code>document</code> with a expression in parenthesis that will take a block after the closing
   * parenthesis.
   *
   * @param document the document being modified
   * @param line the current line under investigation
   * @param offset the offset of the caret position, relative to the line start.
   * @param partitioning the document partitioning
   * @return an insert position relative to the line start if <code>line</code> contains a
   *     parenthesized expression that can be followed by a block, -1 otherwise
   */
  private static int computeAfterParenthesis(
      IDocument document, ITextSelection line, int offset, String partitioning) {
    // find the opening parenthesis for every closing parenthesis on the current line after offset
    // return the position behind the closing parenthesis if it looks like a method declaration
    // or an expression for an if, while, for, catch statement
    int pos = offset + line.getOffset();
    int length = line.getOffset() + line.getLength();
    int scanTo = scanForward(document, pos, partitioning, length, '}');
    if (scanTo == -1) scanTo = length;

    int closingParen = findClosingParenToLeft(document, pos, partitioning) - 1;

    while (true) {
      int startScan = closingParen + 1;
      closingParen = scanForward(document, startScan, partitioning, scanTo, ')');
      if (closingParen == -1) break;

      int openingParen = findOpeningParenMatch(document, closingParen, partitioning);

      // no way an expression at the beginning of the document can mean anything
      if (openingParen < 1) break;

      // only select insert positions for parenthesis currently embracing the caret
      if (openingParen > pos) continue;

      if (looksLikeAnonymousClassDef(document, openingParen - 1, partitioning))
        return closingParen + 1 - line.getOffset();

      if (looksLikeIfWhileForCatch(document, openingParen - 1, partitioning))
        return closingParen + 1 - line.getOffset();

      if (looksLikeMethodDecl(document, openingParen - 1, partitioning))
        return closingParen + 1 - line.getOffset();
    }

    return -1;
  }

  /**
   * Finds a closing parenthesis to the left of <code>position</code> in document, where that
   * parenthesis is only separated by whitespace from <code>position</code>. If no such parenthesis
   * can be found, <code>position</code> is returned.
   *
   * @param document the document being modified
   * @param position the first character position in <code>document</code> to be considered
   * @param partitioning the document partitioning
   * @return the position of a closing parenthesis left to <code>position</code> separated only by
   *     whitespace, or <code>position</code> if no parenthesis can be found
   */
  private static int findClosingParenToLeft(IDocument document, int position, String partitioning) {
    final char CLOSING_PAREN = ')';
    try {
      if (position < 1) return position;

      int nonWS = firstNonWhitespaceBackward(document, position - 1, partitioning, -1);
      if (nonWS != -1 && document.getChar(nonWS) == CLOSING_PAREN) return nonWS;
    } catch (BadLocationException e1) {
    }
    return position;
  }

  /**
   * Finds the first whitespace character position to the right of (and including) <code>position
   * </code>.
   *
   * @param document the document being modified
   * @param position the first character position in <code>document</code> to be considered
   * @return the position of a whitespace character greater or equal than <code>position</code>
   *     separated only by whitespace, or -1 if none found
   */
  private static int firstWhitespaceToRight(IDocument document, int position) {
    int length = document.getLength();
    Assert.isTrue(position >= 0);
    Assert.isTrue(position <= length);

    try {
      while (position < length) {
        char ch = document.getChar(position);
        if (Character.isWhitespace(ch)) return position;
        position++;
      }
      return position;
    } catch (BadLocationException e) {
    }
    return -1;
  }

  /**
   * Finds the highest position in <code>document</code> such that the position is &lt;= <code>
   * position</code> and &gt; <code>bound</code> and <code>
   * Character.isWhitespace(document.getChar(pos))</code> evaluates to <code>false</code> and the
   * position is in the default partition.
   *
   * @param document the document being modified
   * @param position the first character position in <code>document</code> to be considered
   * @param partitioning the document partitioning
   * @param bound the first position in <code>document</code> to not consider any more, with <code>
   *     bound</code> &lt; <code>position</code>
   * @return the highest position of one element in <code>chars</code> in [<code>position</code>,
   *     <code>scanTo</code>) that resides in a Java partition, or <code>-1</code> if none can be
   *     found
   */
  private static int firstNonWhitespaceBackward(
      IDocument document, int position, String partitioning, int bound) {
    Assert.isTrue(position < document.getLength());
    Assert.isTrue(bound >= -1);

    try {
      while (position > bound) {
        char ch = document.getChar(position);
        if (!Character.isWhitespace(ch) && isDefaultPartition(document, position, partitioning))
          return position;
        position--;
      }
    } catch (BadLocationException e) {
    }
    return -1;
  }

  /**
   * Finds the smallest position in <code>document</code> such that the position is &gt;= <code>
   * position</code> and &lt; <code>bound</code> and <code>
   * Character.isWhitespace(document.getChar(pos))</code> evaluates to <code>false</code> and the
   * position is in the default partition.
   *
   * @param document the document being modified
   * @param position the first character position in <code>document</code> to be considered
   * @param partitioning the document partitioning
   * @param bound the first position in <code>document</code> to not consider any more, with <code>
   *     bound</code> &gt; <code>position</code>
   * @return the smallest position of one element in <code>chars</code> in [<code>position</code>,
   *     <code>scanTo</code>) that resides in a Java partition, or <code>-1</code> if none can be
   *     found
   */
  private static int firstNonWhitespaceForward(
      IDocument document, int position, String partitioning, int bound) {
    Assert.isTrue(position >= 0);
    Assert.isTrue(bound <= document.getLength());

    try {
      while (position < bound) {
        char ch = document.getChar(position);
        if (!Character.isWhitespace(ch) && isDefaultPartition(document, position, partitioning))
          return position;
        position++;
      }
    } catch (BadLocationException e) {
    }
    return -1;
  }

  /**
   * Finds the highest position in <code>document</code> such that the position is &lt;= <code>
   * position</code> and &gt; <code>bound</code> and <code>document.getChar(position) == ch</code>
   * evaluates to <code>true</code> for at least one ch in <code>chars</code> and the position is in
   * the default partition.
   *
   * @param document the document being modified
   * @param position the first character position in <code>document</code> to be considered
   * @param partitioning the document partitioning
   * @param bound the first position in <code>document</code> to not consider any more, with <code>
   *     scanTo</code> &gt; <code>position</code>
   * @param chars an array of <code>char</code> to search for
   * @return the highest position of one element in <code>chars</code> in (<code>bound</code>,
   *     <code>position</code>] that resides in a Java partition, or <code>-1</code> if none can be
   *     found
   */
  private static int scanBackward(
      IDocument document, int position, String partitioning, int bound, char[] chars) {
    Assert.isTrue(bound >= -1);
    Assert.isTrue(position < document.getLength());

    Arrays.sort(chars);

    try {
      while (position > bound) {

        if (Arrays.binarySearch(chars, document.getChar(position)) >= 0
            && isDefaultPartition(document, position, partitioning)) return position;

        position--;
      }
    } catch (BadLocationException e) {
    }
    return -1;
  }

  //	/**
  //	 * Finds the highest position in <code>document</code> such that the position is &lt;=
  // <code>position</code>
  //	 * and &gt; <code>bound</code> and <code>document.getChar(position) == ch</code> evaluates to
  // <code>true</code>
  //	 * and the position is in the default partition.
  //	 *
  //	 * @param document the document being modified
  //	 * @param position the first character position in <code>document</code> to be considered
  //	 * @param bound the first position in <code>document</code> to not consider any more, with
  // <code>scanTo</code> &gt; <code>position</code>
  //	 * @param chars an array of <code>char</code> to search for
  //	 * @return the highest position of one element in <code>chars</code> in [<code>position</code>,
  // <code>scanTo</code>) that resides in a Java partition, or <code>-1</code> if none can be found
  //	 */
  //	private static int scanBackward(IDocument document, int position, int bound, char ch) {
  //		return scanBackward(document, position, bound, new char[] {ch});
  //	}
  //
  /**
   * Finds the lowest position in <code>document</code> such that the position is &gt;= <code>
   * position</code> and &lt; <code>bound</code> and <code>document.getChar(position) == ch</code>
   * evaluates to <code>true</code> for at least one ch in <code>chars</code> and the position is in
   * the default partition.
   *
   * @param document the document being modified
   * @param position the first character position in <code>document</code> to be considered
   * @param partitioning the document partitioning
   * @param bound the first position in <code>document</code> to not consider any more, with <code>
   *     scanTo</code> &gt; <code>position</code>
   * @param chars an array of <code>char</code> to search for
   * @return the lowest position of one element in <code>chars</code> in [<code>position</code>,
   *     <code>bound</code>) that resides in a Java partition, or <code>-1</code> if none can be
   *     found
   */
  private static int scanForward(
      IDocument document, int position, String partitioning, int bound, char[] chars) {
    Assert.isTrue(position >= 0);
    Assert.isTrue(bound <= document.getLength());

    Arrays.sort(chars);

    try {
      while (position < bound) {

        if (Arrays.binarySearch(chars, document.getChar(position)) >= 0
            && isDefaultPartition(document, position, partitioning)) return position;

        position++;
      }
    } catch (BadLocationException e) {
    }
    return -1;
  }

  /**
   * Finds the lowest position in <code>document</code> such that the position is &gt;= <code>
   * position</code> and &lt; <code>bound</code> and <code>document.getChar(position) == ch</code>
   * evaluates to <code>true</code> and the position is in the default partition.
   *
   * @param document the document being modified
   * @param position the first character position in <code>document</code> to be considered
   * @param partitioning the document partitioning
   * @param bound the first position in <code>document</code> to not consider any more, with <code>
   *     scanTo</code> &gt; <code>position</code>
   * @param ch a <code>char</code> to search for
   * @return the lowest position of one element in <code>chars</code> in [<code>position</code>,
   *     <code>bound</code>) that resides in a Java partition, or <code>-1</code> if none can be
   *     found
   */
  private static int scanForward(
      IDocument document, int position, String partitioning, int bound, char ch) {
    return scanForward(document, position, partitioning, bound, new char[] {ch});
  }

  /**
   * Checks whether the content of <code>document</code> in the range (<code>offset</code>, <code>
   * length</code>) contains the <code>new</code> keyword.
   *
   * @param document the document being modified
   * @param offset the first character position in <code>document</code> to be considered
   * @param length the length of the character range to be considered
   * @param partitioning the document partitioning
   * @return <code>true</code> if the specified character range contains a <code>new</code> keyword,
   *     <code>false</code> otherwise.
   */
  private static boolean isNewMatch(
      IDocument document, int offset, int length, String partitioning) {
    Assert.isTrue(length >= 0);
    Assert.isTrue(offset >= 0);
    Assert.isTrue(offset + length < document.getLength() + 1);

    try {
      String text = document.get(offset, length);
      int pos = text.indexOf("new"); // $NON-NLS-1$

      while (pos != -1 && !isDefaultPartition(document, pos + offset, partitioning))
        pos = text.indexOf("new", pos + 2); // $NON-NLS-1$

      if (pos < 0) return false;

      if (pos != 0 && Character.isJavaIdentifierPart(text.charAt(pos - 1))) return false;

      if (pos + 3 < length && Character.isJavaIdentifierPart(text.charAt(pos + 3))) return false;

      return true;

    } catch (BadLocationException e) {
    }
    return false;
  }

  /**
   * Checks whether the content of <code>document</code> at <code>position</code> looks like an
   * anonymous class definition. <code>position</code> must be to the left of the opening
   * parenthesis of the definition's parameter list.
   *
   * @param document the document being modified
   * @param position the first character position in <code>document</code> to be considered
   * @param partitioning the document partitioning
   * @return <code>true</code> if the content of <code>document</code> looks like an anonymous class
   *     definition, <code>false</code> otherwise
   */
  private static boolean looksLikeAnonymousClassDef(
      IDocument document, int position, String partitioning) {
    int previousCommaParenEqual =
        scanBackward(document, position - 1, partitioning, -1, new char[] {',', '(', '='});
    if (previousCommaParenEqual == -1
        || position < previousCommaParenEqual + 5) // 2 for borders, 3 for "new"
    return false;

    if (isNewMatch(
        document,
        previousCommaParenEqual + 1,
        position - previousCommaParenEqual - 2,
        partitioning)) return true;

    return false;
  }

  /**
   * Checks whether <code>position</code> resides in a default (Java) partition of <code>document
   * </code>.
   *
   * @param document the document being modified
   * @param position the position to be checked
   * @param partitioning the document partitioning
   * @return <code>true</code> if <code>position</code> is in the default partition of <code>
   *     document</code>, <code>false</code> otherwise
   */
  private static boolean isDefaultPartition(IDocument document, int position, String partitioning) {
    Assert.isTrue(position >= 0);
    Assert.isTrue(position <= document.getLength());

    try {
      // don't use getPartition2 since we're interested in the scanned character's partition
      ITypedRegion region = TextUtilities.getPartition(document, partitioning, position, false);
      return region.getType().equals(IDocument.DEFAULT_CONTENT_TYPE);

    } catch (BadLocationException e) {
    }

    return false;
  }

  /**
   * Finds the position of the parenthesis matching the closing parenthesis at <code>position</code>
   * .
   *
   * @param document the document being modified
   * @param position the position in <code>document</code> of a closing parenthesis
   * @param partitioning the document partitioning
   * @return the position in <code>document</code> of the matching parenthesis, or -1 if none can be
   *     found
   */
  private static int findOpeningParenMatch(IDocument document, int position, String partitioning) {
    final char CLOSING_PAREN = ')';
    final char OPENING_PAREN = '(';

    Assert.isTrue(position < document.getLength());
    Assert.isTrue(position >= 0);
    Assert.isTrue(isDefaultPartition(document, position, partitioning));

    try {

      Assert.isTrue(document.getChar(position) == CLOSING_PAREN);

      int depth = 1;
      while (true) {
        position =
            scanBackward(
                document,
                position - 1,
                partitioning,
                -1,
                new char[] {CLOSING_PAREN, OPENING_PAREN});
        if (position == -1) return -1;

        if (document.getChar(position) == CLOSING_PAREN) depth++;
        else depth--;

        if (depth == 0) return position;
      }

    } catch (BadLocationException e) {
      return -1;
    }
  }

  /**
   * Checks whether, to the left of <code>position</code> and separated only by whitespace, <code>
   * document</code> contains a keyword taking a parameter list and a block after it. These are:
   * <code>if</code>, <code>while</code>, <code>catch</code>, <code>for</code>, <code>synchronized
   * </code>, <code>switch</code>.
   *
   * @param document the document being modified
   * @param position the first character position in <code>document</code> to be considered
   * @param partitioning the document partitioning
   * @return <code>true</code> if <code>document</code> contains any of the above keywords to the
   *     left of <code>position</code>, <code>false</code> otherwise
   */
  private static boolean looksLikeIfWhileForCatch(
      IDocument document, int position, String partitioning) {
    position = firstNonWhitespaceBackward(document, position, partitioning, -1);
    if (position == -1) return false;

    return looksLike(document, position, "if") // $NON-NLS-1$
        || looksLike(document, position, "while") // $NON-NLS-1$
        || looksLike(document, position, "catch") // $NON-NLS-1$
        || looksLike(document, position, "synchronized") // $NON-NLS-1$
        || looksLike(document, position, "switch") // $NON-NLS-1$
        || looksLike(document, position, "for"); // $NON-NLS-1$
  }

  /**
   * Checks whether code>document</code> contains the <code>String</code> <code>like</code> such
   * that its last character is at <code>position</code>. If <code>like</code> starts with a
   * identifier part (as determined by {@link Character#isJavaIdentifierPart(char)}), it is also
   * made sure that <code>like</code> is preceded by some non-identifier character or stands at the
   * document start.
   *
   * @param document the document being modified
   * @param position the first character position in <code>document</code> to be considered
   * @param like the <code>String</code> to look for.
   * @return <code>true</code> if <code>document</code> contains <code>like</code> such that it ends
   *     at <code>position</code>, <code>false</code> otherwise
   */
  private static boolean looksLike(IDocument document, int position, String like) {
    int length = like.length();
    if (position < length - 1) return false;

    try {
      if (!like.equals(document.get(position - length + 1, length))) return false;

      if (position >= length
          && Character.isJavaIdentifierPart(like.charAt(0))
          && Character.isJavaIdentifierPart(document.getChar(position - length))) return false;

    } catch (BadLocationException e) {
      return false;
    }

    return true;
  }

  /**
   * Checks whether the content of <code>document</code> at <code>position</code> looks like a
   * method declaration header (i.e. only the return type and method name). <code>position</code>
   * must be just left of the opening parenthesis of the parameter list.
   *
   * @param document the document being modified
   * @param position the first character position in <code>document</code> to be considered
   * @param partitioning the document partitioning
   * @return <code>true</code> if the content of <code>document</code> looks like a method
   *     definition, <code>false</code> otherwise
   */
  private static boolean looksLikeMethodDecl(
      IDocument document, int position, String partitioning) {

    // method name
    position = eatIdentToLeft(document, position, partitioning);
    if (position < 1) return false;

    position = eatBrackets(document, position - 1, partitioning);
    if (position < 1) return false;

    position = eatIdentToLeft(document, position - 1, partitioning);

    return position != -1;
  }

  /**
   * From <code>position</code> to the left, eats any whitespace and then a pair of brackets as used
   * to declare an array return type like
   *
   * <pre>String [ ]</pre>
   *
   * . The return value is either the position of the opening bracket or <code>position</code> if no
   * pair of brackets can be parsed.
   *
   * @param document the document being modified
   * @param position the first character position in <code>document</code> to be considered
   * @param partitioning the document partitioning
   * @return the smallest character position of bracket pair or <code>position</code>
   */
  private static int eatBrackets(IDocument document, int position, String partitioning) {
    // accept array return type
    int pos = firstNonWhitespaceBackward(document, position, partitioning, -1);
    try {
      if (pos > 1 && document.getChar(pos) == ']') {
        pos = firstNonWhitespaceBackward(document, pos - 1, partitioning, -1);
        if (pos > 0 && document.getChar(pos) == '[') return pos;
      }
    } catch (BadLocationException e) {
      // won't happen
    }
    return position;
  }

  /**
   * From <code>position</code> to the left, eats any whitespace and the first identifier, returning
   * the position of the first identifier character (in normal read order).
   *
   * <p>When called on a document with content <code>" some string  "</code> and positionition 13,
   * the return value will be 6 (the first letter in <code>string</code>).
   *
   * @param document the document being modified
   * @param position the first character position in <code>document</code> to be considered
   * @param partitioning the document partitioning
   * @return the smallest character position of an identifier or -1 if none can be found; always
   *     &lt;= <code>position</code>
   */
  private static int eatIdentToLeft(IDocument document, int position, String partitioning) {
    if (position < 0) return -1;
    Assert.isTrue(position < document.getLength());

    int p = firstNonWhitespaceBackward(document, position, partitioning, -1);
    if (p == -1) return -1;

    try {
      while (p >= 0) {

        char ch = document.getChar(p);
        if (Character.isJavaIdentifierPart(ch)) {
          p--;
          continue;
        }

        // length must be > 0
        if (Character.isWhitespace(ch) && p != position) return p + 1;
        else return -1;
      }

      // start of document reached
      return 0;

    } catch (BadLocationException e) {
    }
    return -1;
  }

  /**
   * Returns a position in the first java partition after the last non-empty and non-comment
   * partition. There is no non-whitespace from the returned position to the end of the partition it
   * is contained in.
   *
   * @param document the document being modified
   * @param line the line under investigation
   * @param offset the caret offset into <code>line</code>
   * @param partitioning the document partitioning
   * @return the position of the next Java partition, or the end of <code>line</code>
   */
  private static int nextPartitionOrLineEnd(
      IDocument document, ITextSelection line, int offset, String partitioning) {
    // run relative to document
    final int docOffset = offset + line.getOffset();
    final int eol = line.getOffset() + line.getLength();
    int nextPartitionPos = eol; // init with line end
    int validPosition = docOffset;

    try {
      ITypedRegion partition =
          TextUtilities.getPartition(document, partitioning, nextPartitionPos, true);
      validPosition = getValidPositionForPartition(document, partition, eol);
      while (validPosition == -1) {
        nextPartitionPos = partition.getOffset() - 1;
        if (nextPartitionPos < docOffset) {
          validPosition = docOffset;
          break;
        }
        partition = TextUtilities.getPartition(document, partitioning, nextPartitionPos, false);
        validPosition = getValidPositionForPartition(document, partition, eol);
      }
    } catch (BadLocationException e) {
    }

    validPosition = Math.max(validPosition, docOffset);
    // make relative to line
    validPosition -= line.getOffset();
    return validPosition;
  }

  /**
   * Returns a valid insert location (except for whitespace) in <code>partition</code> or -1 if
   * there is no valid insert location. An valid insert location is right after any java string or
   * character partition, or at the end of a java default partition, but never behind <code>
   * maxOffset</code>. Comment partitions or empty java partitions do never yield valid insert
   * positions.
   *
   * @param doc the document being modified
   * @param partition the current partition
   * @param maxOffset the maximum offset to consider
   * @return a valid insert location in <code>partition</code>, or -1 if there is no valid insert
   *     location
   */
  private static int getValidPositionForPartition(
      IDocument doc, ITypedRegion partition, int maxOffset) {
    final int INVALID = -1;

    if (IJavaPartitions.JAVA_DOC.equals(partition.getType())) return INVALID;
    if (IJavaPartitions.JAVA_MULTI_LINE_COMMENT.equals(partition.getType())) return INVALID;
    if (IJavaPartitions.JAVA_SINGLE_LINE_COMMENT.equals(partition.getType())) return INVALID;

    int endOffset = Math.min(maxOffset, partition.getOffset() + partition.getLength());

    if (IJavaPartitions.JAVA_CHARACTER.equals(partition.getType())) return endOffset;
    if (IJavaPartitions.JAVA_STRING.equals(partition.getType())) return endOffset;
    if (IDocument.DEFAULT_CONTENT_TYPE.equals(partition.getType())) {
      try {
        if (doc.get(partition.getOffset(), endOffset - partition.getOffset()).trim().length() == 0)
          return INVALID;
        else return endOffset;
      } catch (BadLocationException e) {
        return INVALID;
      }
    }
    // default: we don't know anything about the partition - assume valid
    return endOffset;
  }

  /**
   * Determines whether the current line contains a for statement. Algorithm: any "for" word in the
   * line is a positive, "for" contained in a string literal will produce a false positive.
   *
   * @param line the line where the change is being made
   * @param offset the position of the caret
   * @return <code>true</code> if <code>line</code> contains <code>for</code>, <code>false</code>
   *     otherwise
   */
  private static boolean isForStatement(String line, int offset) {
    /* searching for (^|\s)for(\s|$) */
    int forPos = line.indexOf("for"); // $NON-NLS-1$
    if (forPos != -1) {
      if ((forPos == 0 || !Character.isJavaIdentifierPart(line.charAt(forPos - 1)))
          && (line.length() == forPos + 3
              || !Character.isJavaIdentifierPart(line.charAt(forPos + 3)))) return true;
    }
    return false;
  }

  /**
   * Returns the position in <code>text</code> after which there comes only whitespace, up to <code>
   * offset</code>.
   *
   * @param text the text being searched
   * @param offset the maximum offset to search for
   * @return the smallest value <code>v</code> such that <code>text.substring(v, offset).trim() == 0
   *     </code>
   */
  private static int startOfWhitespaceBeforeOffset(String text, int offset) {
    int i = Math.min(offset, text.length());
    for (; i >= 1; i--) {
      if (!Character.isWhitespace(text.charAt(i - 1))) break;
    }
    return i;
  }
}
