/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2012 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.text.correction.proposals;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.corext.dom.TokenScanner;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.JavaUIStatus;
import org.eclipse.jdt.internal.ui.text.correction.CorrectionMessages;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.correction.CUCorrectionProposal;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

public class TaskMarkerProposal extends CUCorrectionProposal {

  private IProblemLocation fLocation;

  public TaskMarkerProposal(ICompilationUnit cu, IProblemLocation location, int relevance) {
    super("", cu, relevance, null); // $NON-NLS-1$
    fLocation = location;

    setDisplayName(CorrectionMessages.TaskMarkerProposal_description);
    setImage(JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE));
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.ui.text.correction.CUCorrectionProposal#addEdits(org.eclipse.jdt.internal.corext.textmanipulation
   * .TextBuffer)
   */
  @Override
  protected void addEdits(IDocument document, TextEdit rootEdit) throws CoreException {
    super.addEdits(document, rootEdit);

    try {
      Position pos = getUpdatedPosition(document);
      if (pos != null) {
        rootEdit.addChild(new ReplaceEdit(pos.getOffset(), pos.getLength(), "")); // $NON-NLS-1$
      } else {
        rootEdit.addChild(
            new ReplaceEdit(fLocation.getOffset(), fLocation.getLength(), "")); // $NON-NLS-1$
      }
    } catch (BadLocationException e) {
      throw new CoreException(JavaUIStatus.createError(IStatus.ERROR, e));
    }
  }

  private Position getUpdatedPosition(IDocument document) throws BadLocationException {
    IScanner scanner = ToolFactory.createScanner(true, false, false, false);
    scanner.setSource(document.get().toCharArray());

    int token = getSurroundingComment(scanner);
    if (token == ITerminalSymbols.TokenNameEOF) {
      return null;
    }
    int commentStart = scanner.getCurrentTokenStartPosition();
    int commentEnd = scanner.getCurrentTokenEndPosition() + 1;

    int contentStart = commentStart + 2;
    int contentEnd = commentEnd;
    if (token == ITerminalSymbols.TokenNameCOMMENT_JAVADOC) {
      contentStart = commentStart + 3;
      contentEnd = commentEnd - 2;
    } else if (token == ITerminalSymbols.TokenNameCOMMENT_BLOCK) {
      contentEnd = commentEnd - 2;
    }
    if (hasContent(document, contentStart, fLocation.getOffset())
        || hasContent(document, contentEnd, fLocation.getOffset() + fLocation.getLength())) {

      return new Position(fLocation.getOffset(), fLocation.getLength());
    }

    IRegion startRegion = document.getLineInformationOfOffset(commentStart);
    int start = startRegion.getOffset();
    boolean contentAtBegining = hasContent(document, start, commentStart);

    if (contentAtBegining) {
      start = commentStart;
    }

    int end;
    if (token == ITerminalSymbols.TokenNameCOMMENT_LINE) {
      if (contentAtBegining) {
        end = startRegion.getOffset() + startRegion.getLength(); // only to the end of the line
      } else {
        end = commentEnd; // includes new line
      }
    } else {
      int endLine = document.getLineOfOffset(commentEnd - 1);
      if (endLine + 1 == document.getNumberOfLines() || contentAtBegining) {
        IRegion endRegion = document.getLineInformation(endLine);
        end = endRegion.getOffset() + endRegion.getLength();
      } else {
        IRegion endRegion = document.getLineInformation(endLine + 1);
        end = endRegion.getOffset();
      }
    }
    if (hasContent(document, commentEnd, end)) {
      end = commentEnd;
      start = commentStart; // only remove comment
    }
    return new Position(start, end - start);
  }

  private int getSurroundingComment(IScanner scanner) {
    try {
      int start = fLocation.getOffset();
      int end = start + fLocation.getLength();

      int token = scanner.getNextToken();
      while (token != ITerminalSymbols.TokenNameEOF) {
        if (TokenScanner.isComment(token)) {
          int currStart = scanner.getCurrentTokenStartPosition();
          int currEnd = scanner.getCurrentTokenEndPosition() + 1;
          if (currStart <= start && end <= currEnd) {
            return token;
          }
        }
        token = scanner.getNextToken();
      }

    } catch (InvalidInputException e) {
      // ignore
    }
    return ITerminalSymbols.TokenNameEOF;
  }

  private boolean hasContent(IDocument document, int start, int end) throws BadLocationException {
    for (int i = start; i < end; i++) {
      char ch = document.getChar(i);
      if (!Character.isWhitespace(ch)) {
        return true;
      }
    }
    return false;
  }
}
