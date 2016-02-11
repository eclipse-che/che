/*******************************************************************************
 * Copyright (c) 2014-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.editor.orion.client;

import com.google.web.bindery.event.shared.HandlerRegistration;

import org.eclipse.che.ide.api.text.Region;
import org.eclipse.che.ide.editor.orion.client.jso.ModelChangedEventOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionEditorOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionPixelPositionOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionSelectionOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionTextModelOverlay.EventHandler;
import org.eclipse.che.ide.editor.orion.client.jso.OrionTextViewOverlay;
import org.eclipse.che.ide.jseditor.client.document.AbstractEmbeddedDocument;
import org.eclipse.che.ide.jseditor.client.document.Document;
import org.eclipse.che.ide.jseditor.client.events.CursorActivityHandler;
import org.eclipse.che.ide.jseditor.client.events.DocumentChangeEvent;
import org.eclipse.che.ide.jseditor.client.events.HasCursorActivityHandlers;
import org.eclipse.che.ide.jseditor.client.position.PositionConverter;
import org.eclipse.che.ide.jseditor.client.text.LinearRange;
import org.eclipse.che.ide.jseditor.client.text.TextPosition;
import org.eclipse.che.ide.jseditor.client.text.TextRange;

/**
 * The implementation of {@link Document} for Orion.
 *
 * @author "Mickaël Leduque"
 */
public class OrionDocument extends AbstractEmbeddedDocument {

    private final OrionTextViewOverlay      textViewOverlay;
    private final OrionPositionConverter    positionConverter;
    private final HasCursorActivityHandlers hasCursorActivityHandlers;
    private final OrionEditorOverlay        editorOverlay;

    public OrionDocument(OrionTextViewOverlay textViewOverlay,
                         HasCursorActivityHandlers hasCursorActivityHandlers,
                         OrionEditorOverlay editorOverlay) {

        this.textViewOverlay = textViewOverlay;
        this.hasCursorActivityHandlers = hasCursorActivityHandlers;
        this.editorOverlay = editorOverlay;
        this.positionConverter = new OrionPositionConverter();

        this.editorOverlay.getModel().addEventListener("Changed", new EventHandler<ModelChangedEventOverlay>() {
            @Override
            public void onEvent(ModelChangedEventOverlay parameter) {
                fireDocumentChangeEvent(parameter);
            }
        }, true);
    }

    private void fireDocumentChangeEvent(final ModelChangedEventOverlay param) {
        int startOffset = param.start();
        int addedCharCount = param.addedCharCount();
        int removedCharCount = param.removedCharCount();
        int length = 0;

        if (addedCharCount != 0) {
            //adding
            length = addedCharCount;
        } else if (removedCharCount != 0) {
            //deleting
            //TODO there may be bug
            length = removedCharCount;
            startOffset = startOffset - length;
        }
        String text = editorOverlay.getModel().getText(startOffset, startOffset + length);

        final DocumentChangeEvent event = new DocumentChangeEvent(this,
                                                                  startOffset,
                                                                  length,
                                                                  text,
                                                                  removedCharCount);//TODO: need check removedCharCount add it for fix
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
    public void setCursorPosition(final TextPosition position) {
        this.editorOverlay.setCaretOffset(getIndexFromPosition(position));

    }

    @Override
    public TextPosition getCursorPosition() {
        final int offset = this.editorOverlay.getCaretOffset();
        return getPositionFromIndex(offset);
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

    public void replace(int offset, int length, String text) {
        this.editorOverlay.setText(text, offset, offset + length);
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
        return new TextRange(new TextPosition(line, 0), new TextPosition(line, length - 1));
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
    }
}
