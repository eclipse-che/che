/**
 * ***************************************************************************** Copyright (c) 2011
 * IBM Corporation and others. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.text.correction.proposals;

import org.eclipse.jdt.internal.corext.util.Strings;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.text.edits.CopyTargetEdit;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MoveSourceEdit;
import org.eclipse.text.edits.MoveTargetEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditVisitor;

/**
 * Class to annotate edits made by a quick fix/assist to be shown via the quick fix pop-up preview.
 * E.g. the added changes are shown in bold.
 *
 * @since 3.8
 */
public class EditAnnotator extends TextEditVisitor {
  private int fWrittenToPos = 0;

  private final StringBuffer fBuf;

  private final IDocument fPreviewDocument;

  public EditAnnotator(StringBuffer buffer, IDocument previewDoc) {
    fBuf = buffer;
    fPreviewDocument = previewDoc;
  }

  public void unchangedUntil(int pos) {
    if (pos > fWrittenToPos) {
      appendContent(fPreviewDocument, fWrittenToPos, pos, true);
      fWrittenToPos = pos;
    }
  }

  @Override
  public boolean visit(MoveTargetEdit edit) {
    return true; // rangeAdded(edit);
  }

  @Override
  public boolean visit(CopyTargetEdit edit) {
    return true; // return rangeAdded(edit);
  }

  @Override
  public boolean visit(InsertEdit edit) {
    return rangeAdded(edit);
  }

  @Override
  public boolean visit(ReplaceEdit edit) {
    if (edit.getLength() > 0) return rangeAdded(edit);
    return rangeRemoved(edit);
  }

  @Override
  public boolean visit(MoveSourceEdit edit) {
    return rangeRemoved(edit);
  }

  @Override
  public boolean visit(DeleteEdit edit) {
    return rangeRemoved(edit);
  }

  protected boolean rangeRemoved(TextEdit edit) {
    unchangedUntil(edit.getOffset());
    return false;
  }

  private boolean rangeAdded(TextEdit edit) {
    return annotateEdit(edit, "<b>", "</b>"); // $NON-NLS-1$ //$NON-NLS-2$
  }

  protected boolean annotateEdit(TextEdit edit, String startTag, String endTag) {
    unchangedUntil(edit.getOffset());
    fBuf.append(startTag);
    appendContent(fPreviewDocument, edit.getOffset(), edit.getExclusiveEnd(), false);
    fBuf.append(endTag);
    fWrittenToPos = edit.getExclusiveEnd();
    return false;
  }

  private void appendContent(
      IDocument text, int startOffset, int endOffset, boolean surroundLinesOnly) {
    final int surroundLines = 1;
    try {
      int startLine = text.getLineOfOffset(startOffset);
      int endLine = text.getLineOfOffset(endOffset);

      boolean dotsAdded = false;
      if (surroundLinesOnly && startOffset == 0) { // no surround lines for the top no-change range
        startLine = Math.max(endLine - surroundLines, 0);
        fBuf.append("...<br>"); // $NON-NLS-1$
        dotsAdded = true;
      }

      for (int i = startLine; i <= endLine; i++) {
        if (surroundLinesOnly) {
          if ((i - startLine > surroundLines) && (endLine - i > surroundLines)) {
            if (!dotsAdded) {
              fBuf.append("...<br>"); // $NON-NLS-1$
              dotsAdded = true;
            } else if (endOffset == text.getLength()) {
              return; // no surround lines for the bottom no-change range
            }
            continue;
          }
        }

        IRegion lineInfo = text.getLineInformation(i);
        int start = lineInfo.getOffset();
        int end = start + lineInfo.getLength();

        int from = Math.max(start, startOffset);
        int to = Math.min(end, endOffset);
        String content = text.get(from, to - from);
        if (surroundLinesOnly && (from == start) && Strings.containsOnlyWhitespaces(content)) {
          continue; // ignore empty lines except when range started in the middle of a line
        }
        for (int k = 0; k < content.length(); k++) {
          char ch = content.charAt(k);
          if (ch == '<') {
            fBuf.append("&lt;"); // $NON-NLS-1$
          } else if (ch == '>') {
            fBuf.append("&gt;"); // $NON-NLS-1$
          } else {
            fBuf.append(ch);
          }
        }
        if (to == end
            && to != endOffset) { // new line when at the end of the line, and not end of range
          fBuf.append("<br>"); // $NON-NLS-1$
        }
      }
    } catch (BadLocationException e) {
      // ignore
    }
  }
}
