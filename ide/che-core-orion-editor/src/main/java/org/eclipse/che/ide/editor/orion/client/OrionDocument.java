/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.editor.orion.client;

import static org.eclipse.che.ide.editor.orion.client.jso.OrionTextViewShowOptionsOverlay.ViewAnchorValue.CENTER;

import com.google.web.bindery.event.shared.HandlerRegistration;
import org.eclipse.che.ide.api.editor.document.AbstractDocument;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.events.CursorActivityHandler;
import org.eclipse.che.ide.api.editor.events.DocumentChangedEvent;
import org.eclipse.che.ide.api.editor.events.DocumentChangingEvent;
import org.eclipse.che.ide.api.editor.events.HasCursorActivityHandlers;
import org.eclipse.che.ide.api.editor.position.PositionConverter;
import org.eclipse.che.ide.api.editor.text.LinearRange;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.editor.text.TextRange;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.editor.orion.client.jso.ModelChangedEventOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionEditorOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionPixelPositionOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionSelectionOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionTextModelOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionTextModelOverlay.EventHandler;
import org.eclipse.che.ide.editor.orion.client.jso.OrionTextViewOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionTextViewShowOptionsOverlay;

/**
 * The implementation of {@link Document} for Orion.
 *
 * @author "Mickaël Leduque"
 */
public class OrionDocument extends AbstractDocument {

  /**
   * The maximum number of lines that may be visible at the top of the text view after setting
   * selection range.
   */
  private static final int MARGIN_TOP = 15;

  private final OrionTextViewOverlay textViewOverlay;
  private final OrionPositionConverter positionConverter;
  private final HasCursorActivityHandlers hasCursorActivityHandlers;
  private final OrionEditorOverlay editorOverlay;

  public OrionDocument(
      OrionTextViewOverlay textViewOverlay,
      HasCursorActivityHandlers hasCursorActivityHandlers,
      OrionEditorOverlay editorOverlay) {
    this.textViewOverlay = textViewOverlay;
    this.hasCursorActivityHandlers = hasCursorActivityHandlers;
    this.editorOverlay = editorOverlay;
    this.positionConverter = new OrionPositionConverter();

    this.editorOverlay
        .getModel()
        .addEventListener(
            "Changed",
            new EventHandler<ModelChangedEventOverlay>() {
              @Override
              public void onEvent(ModelChangedEventOverlay parameter) {
                fireDocumentChangeEvent(parameter);
              }
            },
            true);

    this.editorOverlay
        .getModel()
        .addEventListener(
            "Changing",
            new EventHandler<ModelChangedEventOverlay>() {
              @Override
              public void onEvent(ModelChangedEventOverlay parameter) {
                fireDocumentChangingEvent(parameter);
              }
            },
            true);
  }

  private void fireDocumentChangeEvent(final ModelChangedEventOverlay param) {
    int startOffset = param.start();
    int addedCharCount = param.addedCharCount();
    int removedCharCount = param.removedCharCount();

    String text = editorOverlay.getModel().getText(startOffset, startOffset + addedCharCount);

    final DocumentChangedEvent event =
        new DocumentChangedEvent(this, startOffset, addedCharCount, text, removedCharCount);
    // according to https://github.com/codenvy/che-core/pull/122
    getDocEventBus().fireEvent(event);
  }

  private void fireDocumentChangingEvent(final ModelChangedEventOverlay param) {
    int startOffset = param.start();
    int addedCharCount = param.addedCharCount();
    int removedCharCount = param.removedCharCount();

    String text = param.getText();

    final DocumentChangingEvent event =
        new DocumentChangingEvent(this, startOffset, addedCharCount, text, removedCharCount);
    // according to https://github.com/codenvy/che-core/pull/122
    getDocEventBus().fireEvent(event);
  }

  @Override
  public TextPosition getPositionFromIndex(final int index) {
    final int line = this.editorOverlay.getModel().getLineAtOffset(index);
    if (line == -1) {
      return null;
    }
    final int lineStart = this.editorOverlay.getModel().getLineStart(line);
    if (lineStart == -1) {
      return null;
    }
    final int character = index - lineStart;
    if (character < 0) {
      return null;
    }
    return new TextPosition(line, character);
  }

  @Override
  public int getIndexFromPosition(final TextPosition position) {
    final int lineStart = this.editorOverlay.getModel().getLineStart(position.getLine());
    if (lineStart == -1) {
      return -1;
    }

    final int result = lineStart + position.getCharacter();
    final int lineEnd = this.editorOverlay.getModel().getLineEnd(position.getLine());

    if (lineEnd < result) {
      return -1;
    }
    return result;
  }

  @Override
  public int getLineAtOffset(int offset) {
    return this.editorOverlay.getTextView().getLineAtOffset(offset);
  }

  @Override
  public int getLineStart(int lineIndex) {
    return editorOverlay.getTextView().getLineStart(lineIndex);
  }

  @Override
  public TextPosition getCursorPosition() {
    final int offset = this.editorOverlay.getCaretOffset();
    return getPositionFromIndex(offset);
  }

  @Override
  public void setCursorPosition(final TextPosition position) {
    OrionTextViewShowOptionsOverlay showOptionsOverlay = OrionTextViewShowOptionsOverlay.create();
    showOptionsOverlay.setViewAnchor(CENTER.getValue());
    this.editorOverlay.setCaretOffset(getIndexFromPosition(position), showOptionsOverlay);
  }

  public int getCursorOffset() {
    return this.editorOverlay.getTextView().getCaretOffset();
  }

  @Override
  public int getLineCount() {
    return this.editorOverlay.getModel().getLineCount();
  }

  @Override
  public HandlerRegistration addCursorHandler(final CursorActivityHandler handler) {
    return this.hasCursorActivityHandlers.addCursorActivityHandler(handler);
  }

  @Override
  public String getContents() {
    return editorOverlay.getText();
  }

  @Override
  public String getContentRange(final int offset, final int length) {
    return this.editorOverlay.getModel().getText(offset, offset + length);
  }

  @Override
  public String getContentRange(final TextRange range) {
    final int startOffset = getIndexFromPosition(range.getFrom());
    final int endOffset = getIndexFromPosition(range.getTo());
    return this.editorOverlay.getModel().getText(startOffset, endOffset);
  }

  public PositionConverter getPositionConverter() {
    return this.positionConverter;
  }

  public void replace(int offset, int length, String text) {
    this.editorOverlay.getModel().setText(text, offset, offset + length);
    updateModificationTimeStamp();
  }

  @Override
  public void replace(int startLine, int startChar, int endLine, int endChar, String text) {
    OrionTextModelOverlay model = editorOverlay.getModel();
    int lineStart = model.getLineStart(startLine);
    int lineEnd = model.getLineStart(endLine);
    editorOverlay.setText(text, lineStart + startChar, lineEnd + endChar);
    updateModificationTimeStamp();
  }

  private void updateModificationTimeStamp() {
    VirtualFile file = this.getFile();
    if (file instanceof File) {
      ((File) file).updateModificationStamp(editorOverlay.getText());
    }
  }

  public int getContentsCharCount() {
    return this.editorOverlay.getModel().getCharCount();
  }

  @Override
  public String getLineContent(final int line) {
    return this.editorOverlay.getModel().getLine(line);
  }

  @Override
  public TextRange getTextRangeForLine(final int line) {
    final int startOffset = this.textViewOverlay.getModel().getLineStart(line);
    final int endOffset = this.textViewOverlay.getModel().getLineEnd(line);
    final int length = endOffset - startOffset;
    return new TextRange(new TextPosition(line, 0), new TextPosition(line, length));
  }

  @Override
  public LinearRange getLinearRangeForLine(final int line) {
    return LinearRange.createWithStart(this.textViewOverlay.getModel().getLineStart(line))
        .andEnd(textViewOverlay.getModel().getLineEnd(line));
  }

  @Override
  public TextRange getSelectedTextRange() {
    final OrionSelectionOverlay selection = this.textViewOverlay.getSelection();
    final int start = selection.getStart();
    final TextPosition startPosition = getPositionFromIndex(start);
    final int end = selection.getEnd();
    final TextPosition endPosition = getPositionFromIndex(end);
    return new TextRange(startPosition, endPosition);
  }

  @Override
  public LinearRange getSelectedLinearRange() {
    final OrionSelectionOverlay selection = this.textViewOverlay.getSelection();

    final int start = selection.getStart();
    final int end = selection.getEnd();
    return LinearRange.createWithStart(start).andEnd(end);
  }

  /** {@inheritDoc} */
  @Override
  public void setSelectedRange(LinearRange range, boolean show) {
    int startOffset = range.getStartOffset();
    editorOverlay.setSelection(startOffset, startOffset + range.getLength(), show);

    TextPosition position = getPositionFromIndex(startOffset);
    if (show && position != null) {
      int lineNumber = position.getLine();
      int topIndex = lineNumber - MARGIN_TOP;
      editorOverlay.getTextView().setTopIndex(topIndex > 0 ? topIndex : 0);
    }
  }

  @Override
  public void setSelectedRange(TextRange range) {
    setSelectedRange(range, false);
  }

  @Override
  public void setSelectedRange(TextRange range, boolean show) {
    int lineStart = getLineStart(range.getFrom().getLine());
    int lineEnd = getLineStart(range.getTo().getLine());
    LinearRange linearRange =
        LinearRange.createWithStart(lineStart + range.getFrom().getCharacter())
            .andEnd(lineEnd + range.getTo().getCharacter());
    setSelectedRange(linearRange, show);
  }

  private class OrionPositionConverter implements PositionConverter {

    @Override
    public PixelCoordinates textToPixel(TextPosition textPosition) {
      final int textOffset = getIndexFromPosition(textPosition);
      return offsetToPixel(textOffset);
    }

    @Override
    public PixelCoordinates offsetToPixel(int textOffset) {
      OrionPixelPositionOverlay location = textViewOverlay.getLocationAtOffset(textOffset);
      location.setY(location.getY() + textViewOverlay.getLineHeight());
      location = textViewOverlay.convert(location, "document", "page");
      return new PixelCoordinates(location.getX(), location.getY());
    }

    @Override
    public TextPosition pixelToText(PixelCoordinates coordinates) {
      final int offset = pixelToOffset(coordinates);
      return getPositionFromIndex(offset);
    }

    @Override
    public int pixelToOffset(PixelCoordinates coordinates) {
      return textViewOverlay.getOffsetAtLocation(coordinates.getX(), coordinates.getY());
    }
  }
}
