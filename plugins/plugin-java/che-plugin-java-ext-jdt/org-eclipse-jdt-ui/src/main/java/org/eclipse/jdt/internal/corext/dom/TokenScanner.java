/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.dom;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.ui.JavaUIStatus;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

/** Wraps a scanner and offers convenient methods for finding tokens */
public class TokenScanner {

  public static final int END_OF_FILE = 20001;
  public static final int LEXICAL_ERROR = 20002;
  public static final int DOCUMENT_ERROR = 20003;

  private IScanner fScanner;
  private IDocument fDocument;
  private int fEndPosition;

  /**
   * Creates a TokenScanner
   *
   * @param scanner The scanner to be wrapped. The scanner has to support line information if the
   *     comment position methods are used.
   */
  public TokenScanner(IScanner scanner) {
    this(scanner, null);
  }

  /**
   * Creates a TokenScanner
   *
   * @param scanner The scanner to be wrapped
   * @param document The document used for line information if specified
   */
  public TokenScanner(IScanner scanner, IDocument document) {
    fScanner = scanner;
    fEndPosition = fScanner.getSource().length - 1;
    fDocument = document;
  }

  /**
   * Creates a TokenScanner
   *
   * @param document The textbuffer to create the scanner on
   * @param project the current Java project
   */
  public TokenScanner(IDocument document, IJavaProject project) {
    String sourceLevel = project.getOption(JavaCore.COMPILER_SOURCE, true);
    String complianceLevel = project.getOption(JavaCore.COMPILER_COMPLIANCE, true);
    fScanner =
        ToolFactory.createScanner(
            true, false, false, sourceLevel, complianceLevel); // no line info required
    fScanner.setSource(document.get().toCharArray());
    fDocument = document;
    fEndPosition = fScanner.getSource().length - 1;
  }

  /**
   * Creates a TokenScanner
   *
   * @param typeRoot The type root to scan on
   * @throws CoreException thrown if the buffer cannot be accessed
   */
  public TokenScanner(ITypeRoot typeRoot) throws CoreException {
    IJavaProject project = typeRoot.getJavaProject();
    IBuffer buffer = typeRoot.getBuffer();
    if (buffer == null) {
      throw new CoreException(
          createError(DOCUMENT_ERROR, "Element has no source", null)); // $NON-NLS-1$
    }
    String sourceLevel = project.getOption(JavaCore.COMPILER_SOURCE, true);
    String complianceLevel = project.getOption(JavaCore.COMPILER_COMPLIANCE, true);
    fScanner =
        ToolFactory.createScanner(
            true, false, true, sourceLevel, complianceLevel); // line info required

    fScanner.setSource(buffer.getCharacters());
    fDocument = null; // use scanner for line information
    fEndPosition = fScanner.getSource().length - 1;
  }

  /**
   * Returns the wrapped scanner
   *
   * @return IScanner
   */
  public IScanner getScanner() {
    return fScanner;
  }

  /**
   * Sets the scanner offset to the given offset.
   *
   * @param offset The offset to set
   */
  public void setOffset(int offset) {
    fScanner.resetTo(offset, fEndPosition);
  }

  /** @return Returns the offset after the current token */
  public int getCurrentEndOffset() {
    return fScanner.getCurrentTokenEndPosition() + 1;
  }

  /** @return Returns the start offset of the current token */
  public int getCurrentStartOffset() {
    return fScanner.getCurrentTokenStartPosition();
  }

  /** @return Returns the length of the current token */
  public int getCurrentLength() {
    return getCurrentEndOffset() - getCurrentStartOffset();
  }

  /**
   * Reads the next token.
   *
   * @param ignoreComments If set, comments will be overread
   * @return Return the token id.
   * @exception CoreException Thrown when the end of the file has been reached (code END_OF_FILE) or
   *     a lexical error was detected while scanning (code LEXICAL_ERROR)
   */
  public int readNext(boolean ignoreComments) throws CoreException {
    int curr = 0;
    do {
      try {
        curr = fScanner.getNextToken();
        if (curr == ITerminalSymbols.TokenNameEOF) {
          throw new CoreException(createError(END_OF_FILE, "End Of File", null)); // $NON-NLS-1$
        }
      } catch (InvalidInputException e) {
        throw new CoreException(createError(LEXICAL_ERROR, e.getMessage(), e));
      }
    } while (ignoreComments && isComment(curr));
    return curr;
  }

  /**
   * Reads the next token.
   *
   * @param ignoreComments If set, comments will be overread.
   * @return Return the token id.
   * @exception CoreException Thrown when the end of the file has been reached (code END_OF_FILE) or
   *     a lexical error was detected while scanning (code LEXICAL_ERROR)
   */
  private int readNextWithEOF(boolean ignoreComments) throws CoreException {
    int curr = 0;
    do {
      try {
        curr = fScanner.getNextToken();
      } catch (InvalidInputException e) {
        throw new CoreException(createError(LEXICAL_ERROR, e.getMessage(), e));
      }
    } while (ignoreComments && isComment(curr));
    return curr;
  }

  /**
   * Reads the next token from the given offset.
   *
   * @param offset The offset to start reading from.
   * @param ignoreComments If set, comments will be overread.
   * @return Returns the token id.
   * @exception CoreException Thrown when the end of the file has been reached (code END_OF_FILE) or
   *     a lexical error was detected while scanning (code LEXICAL_ERROR)
   */
  public int readNext(int offset, boolean ignoreComments) throws CoreException {
    setOffset(offset);
    return readNext(ignoreComments);
  }

  /**
   * Reads the next token from the given offset and returns the start offset of the token.
   *
   * @param offset The offset to start reading from.
   * @param ignoreComments If set, comments will be overread
   * @return Returns the start position of the next token.
   * @exception CoreException Thrown when the end of the file has been reached (code END_OF_FILE) or
   *     a lexical error was detected while scanning (code LEXICAL_ERROR)
   */
  public int getNextStartOffset(int offset, boolean ignoreComments) throws CoreException {
    readNext(offset, ignoreComments);
    return getCurrentStartOffset();
  }

  /**
   * Reads the next token from the given offset and returns the offset after the token.
   *
   * @param offset The offset to start reading from.
   * @param ignoreComments If set, comments will be overread
   * @return Returns the end position of the next token.
   * @exception CoreException Thrown when the end of the file has been reached (code END_OF_FILE) or
   *     a lexical error was detected while scanning (code LEXICAL_ERROR)
   */
  public int getNextEndOffset(int offset, boolean ignoreComments) throws CoreException {
    readNext(offset, ignoreComments);
    return getCurrentEndOffset();
  }

  /**
   * Reads until a token is reached.
   *
   * @param tok The token to read to.
   * @exception CoreException Thrown when the end of the file has been reached (code END_OF_FILE) or
   *     a lexical error was detected while scanning (code LEXICAL_ERROR)
   */
  public void readToToken(int tok) throws CoreException {
    int curr = 0;
    do {
      curr = readNext(false);
    } while (curr != tok);
  }

  /**
   * Reads until a token is reached, starting from the given offset.
   *
   * @param tok The token to read to.
   * @param offset The offset to start reading from.
   * @exception CoreException Thrown when the end of the file has been reached (code END_OF_FILE) or
   *     a lexical error was detected while scanning (code LEXICAL_ERROR)
   */
  public void readToToken(int tok, int offset) throws CoreException {
    setOffset(offset);
    readToToken(tok);
  }

  /**
   * Reads from the given offset until a token is reached and returns the start offset of the token.
   *
   * @param token The token to be found.
   * @param startOffset The offset to start reading from.
   * @return Returns the start position of the found token.
   * @exception CoreException Thrown when the end of the file has been reached (code END_OF_FILE) or
   *     a lexical error was detected while scanning (code LEXICAL_ERROR)
   */
  public int getTokenStartOffset(int token, int startOffset) throws CoreException {
    readToToken(token, startOffset);
    return getCurrentStartOffset();
  }

  /**
   * Reads from the given offset until a token is reached and returns the offset after the token.
   *
   * @param token The token to be found.
   * @param startOffset Offset to start reading from
   * @return Returns the end position of the found token.
   * @exception CoreException Thrown when the end of the file has been reached (code END_OF_FILE) or
   *     a lexical error was detected while scanning (code LEXICAL_ERROR)
   */
  public int getTokenEndOffset(int token, int startOffset) throws CoreException {
    readToToken(token, startOffset);
    return getCurrentEndOffset();
  }

  /**
   * Reads from the given offset until a token is reached and returns the offset after the previous
   * token.
   *
   * @param token The token to be found.
   * @param startOffset The offset to start scanning from.
   * @return Returns the end offset of the token previous to the given token.
   * @exception CoreException Thrown when the end of the file has been reached (code END_OF_FILE) or
   *     a lexical error was detected while scanning (code LEXICAL_ERROR)
   */
  public int getPreviousTokenEndOffset(int token, int startOffset) throws CoreException {
    setOffset(startOffset);
    int res = startOffset;
    int curr = readNext(false);
    while (curr != token) {
      res = getCurrentEndOffset();
      curr = readNext(false);
    }
    return res;
  }

  /**
   * Evaluates the start offset of comments directly ahead of a token specified by its start offset
   *
   * @param lastPos An offset to before the node start offset. Can be 0 but better is the end
   *     location of the previous node.
   * @param nodeStart Start offset of the node to find the comments for.
   * @return Returns the start offset of comments directly ahead of a token.
   * @exception CoreException Thrown when a lexical error was detected while scanning (code
   *     LEXICAL_ERROR)
   */
  public int getTokenCommentStart(int lastPos, int nodeStart) throws CoreException {
    setOffset(lastPos);

    int prevEndPos = lastPos;
    int prevEndLine = prevEndPos > 0 ? getLineOfOffset(prevEndPos - 1) : 0;
    int nodeLine = getLineOfOffset(nodeStart);

    int res = -1;

    int curr = readNextWithEOF(false);
    int currStartPos = getCurrentStartOffset();
    int currStartLine = getLineOfOffset(currStartPos);
    while (curr != ITerminalSymbols.TokenNameEOF && nodeStart > currStartPos) {
      if (TokenScanner.isComment(curr)) {
        int linesDifference = currStartLine - prevEndLine;
        if ((linesDifference > 1)
            || (res == -1 && (linesDifference != 0 || nodeLine == currStartLine))) {
          res = currStartPos; // begin new
        }
      } else {
        res = -1;
      }

      if (curr == ITerminalSymbols.TokenNameCOMMENT_LINE) {
        prevEndLine = currStartLine;
      } else {
        prevEndLine = getLineOfOffset(getCurrentEndOffset() - 1);
      }
      curr = readNextWithEOF(false);
      currStartPos = getCurrentStartOffset();
      currStartLine = getLineOfOffset(currStartPos);
    }
    if (res == -1 || curr == ITerminalSymbols.TokenNameEOF) {
      return nodeStart;
    }
    if (currStartLine - prevEndLine > 1) {
      return nodeStart;
    }
    return res;
  }

  /**
   * Looks for comments after a node and returns the end position of the comment still belonging to
   * the node.
   *
   * @param nodeEnd The end position of the node
   * @param nextTokenStart The start positoion of the next node. Optional, can be -1 the line
   *     information shoould be taken from the scanner object
   * @return Returns returns the end position of the comment still belonging to the node.
   * @exception CoreException Thrown when the end of the file has been reached (code END_OF_FILE) or
   *     a lexical error was detected while scanning (code LEXICAL_ERROR)
   */
  public int getTokenCommentEnd(int nodeEnd, int nextTokenStart) throws CoreException {
    // assign comments to the previous comments as long they are all on the same line as the
    // node end position or if they are on the next line but there is a separation from the next
    // node
    // } //aa
    // // aa
    //
    // // bb
    // public void b...
    //
    // } /* cc */ /*
    // cc/*
    // /*dd*/
    // public void d...

    int prevEndLine = getLineOfOffset(nodeEnd - 1);
    int prevEndPos = nodeEnd;
    int res = nodeEnd;
    boolean sameLineComment = true;

    setOffset(nodeEnd);

    int curr = readNextWithEOF(false);
    while (curr == ITerminalSymbols.TokenNameCOMMENT_LINE
        || curr == ITerminalSymbols.TokenNameCOMMENT_BLOCK) {
      int currStartLine = getLineOfOffset(getCurrentStartOffset());
      int linesDifference = currStartLine - prevEndLine;

      if (linesDifference > 1) {
        return prevEndPos; // separated comments
      }

      if (curr == ITerminalSymbols.TokenNameCOMMENT_LINE) {
        prevEndPos = getLineEnd(currStartLine);
        prevEndLine = currStartLine;
      } else {
        prevEndPos = getCurrentEndOffset();
        prevEndLine = getLineOfOffset(prevEndPos - 1);
      }
      if (sameLineComment) {
        if (linesDifference == 0) {
          res = prevEndPos;
        } else {
          sameLineComment = false;
        }
      }
      curr = readNextWithEOF(false);
    }
    if (curr == ITerminalSymbols.TokenNameEOF) {
      return prevEndPos;
    }
    int currStartLine = getLineOfOffset(getCurrentStartOffset());
    int linesDifference = currStartLine - prevEndLine;
    if (linesDifference > 1) {
      return prevEndPos; // separated comments
    }
    return res;
  }

  public int getLineOfOffset(int offset) throws CoreException {
    if (fDocument != null) {
      try {
        return fDocument.getLineOfOffset(offset);
      } catch (BadLocationException e) {
        String message = "Illegal offset: " + offset; // $NON-NLS-1$
        throw new CoreException(createError(DOCUMENT_ERROR, message, e));
      }
    }
    return getScanner().getLineNumber(offset);
  }

  public int getLineEnd(int line) throws CoreException {
    if (fDocument != null) {
      try {
        IRegion region = fDocument.getLineInformation(line);
        return region.getOffset() + region.getLength();
      } catch (BadLocationException e) {
        String message = "Illegal line: " + line; // $NON-NLS-1$
        throw new CoreException(createError(DOCUMENT_ERROR, message, e));
      }
    }
    return getScanner().getLineEnd(line);
  }

  public static boolean isComment(int token) {
    return token == ITerminalSymbols.TokenNameCOMMENT_BLOCK
        || token == ITerminalSymbols.TokenNameCOMMENT_JAVADOC
        || token == ITerminalSymbols.TokenNameCOMMENT_LINE;
  }

  public static boolean isModifier(int token) {
    switch (token) {
      case ITerminalSymbols.TokenNamepublic:
      case ITerminalSymbols.TokenNameprotected:
      case ITerminalSymbols.TokenNameprivate:
      case ITerminalSymbols.TokenNamestatic:
      case ITerminalSymbols.TokenNamefinal:
      case ITerminalSymbols.TokenNameabstract:
      case ITerminalSymbols.TokenNamenative:
      case ITerminalSymbols.TokenNamevolatile:
      case ITerminalSymbols.TokenNamestrictfp:
      case ITerminalSymbols.TokenNametransient:
      case ITerminalSymbols.TokenNamesynchronized:
        return true;
      default:
        return false;
    }
  }

  private IStatus createError(int code, String message, Throwable e) {
    return JavaUIStatus.createError(code, message, e);
  }
}
