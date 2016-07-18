/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.languageserver.ide.editor;

import com.google.inject.Inject;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.formatter.ContentFormatter;
import org.eclipse.che.ide.api.editor.text.TextRange;
import org.eclipse.che.ide.api.editor.texteditor.HandlesUndoRedo;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.editor.texteditor.UndoableEditor;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.plugin.languageserver.ide.service.TextDocumentServiceClient;
import org.eclipse.che.plugin.languageserver.shared.lsapi.DocumentFormattingParamsDTO;
import org.eclipse.che.plugin.languageserver.shared.lsapi.DocumentRangeFormattingParamsDTO;
import org.eclipse.che.plugin.languageserver.shared.lsapi.FormattingOptionsDTO;
import org.eclipse.che.plugin.languageserver.shared.lsapi.PositionDTO;
import org.eclipse.che.plugin.languageserver.shared.lsapi.RangeDTO;
import org.eclipse.che.plugin.languageserver.shared.lsapi.TextDocumentIdentifierDTO;
import org.eclipse.che.plugin.languageserver.shared.lsapi.TextEditDTO;

import java.util.List;

/**
 * @author Evgen Vidolob
 */
public class LanguageServerFormatter implements ContentFormatter {

    private final TextDocumentServiceClient client;
    private final DtoFactory                dtoFactory;
    private final NotificationManager       manager;
    private       int                       tabWidth;
    private TextEditor editor;

    @Inject
    public LanguageServerFormatter(TextDocumentServiceClient client, DtoFactory dtoFactory, NotificationManager manager) {
        this.client = client;
        this.dtoFactory = dtoFactory;
        this.manager = manager;
    }

    public void setTabWidth(int tabWidth) {
        this.tabWidth = tabWidth;
    }

    @Override
    public void format(Document document) {
        TextRange selectedRange = document.getSelectedTextRange();
        if (selectedRange != null && !selectedRange.getFrom().equals(selectedRange.getTo())) {
            //selection formatting
            formatRange(selectedRange, document);
        } else {
            //full document formatting
            formatFullDocument(document);
        }

    }

    @Override
    public void install(TextEditor editor) {
        this.editor = editor;
    }

    private void formatFullDocument(Document document) {
        DocumentFormattingParamsDTO params = dtoFactory.createDto(DocumentFormattingParamsDTO.class);

        TextDocumentIdentifierDTO identifier = dtoFactory.createDto(TextDocumentIdentifierDTO.class);
        identifier.setUri(document.getFile().getPath());

        params.setTextDocument(identifier);
        params.setOptions(getFormattingOptions());
        Promise<List<TextEditDTO>> promise = client.formatting(params);
        handleFormatting(promise, document);
    }

    private void handleFormatting(Promise<List<TextEditDTO>> promise, final Document document) {
        promise.then(new Operation<List<TextEditDTO>>() {
            @Override
            public void apply(List<TextEditDTO> arg) throws OperationException {
                applyEdits(arg, document);
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                manager.notify(arg.getMessage());
            }
        });
    }

    private void applyEdits(List<TextEditDTO> edits, Document document) {
        HandlesUndoRedo undoRedo = null;

        if (editor instanceof UndoableEditor) {
            undoRedo = ((UndoableEditor)editor).getUndoRedo();
        }
        try {
            if (undoRedo != null) {
                undoRedo.beginCompoundChange();
            }
            for (TextEditDTO change : edits) {
                RangeDTO range = change.getRange();
                document.replace(range.getStart().getLine(), range.getStart().getCharacter(),
                                 range.getEnd().getLine(), range.getEnd().getCharacter(), change.getNewText());
            }
        } catch (final Exception e) {
            Log.error(getClass(), e);
        } finally {
            if (undoRedo != null) {
                undoRedo.endCompoundChange();
            }
        }
    }

    private FormattingOptionsDTO getFormattingOptions() {
        FormattingOptionsDTO options = dtoFactory.createDto(FormattingOptionsDTO.class);
        options.setInsertSpaces(true);
        options.setTabSize(tabWidth);
        return options;
    }

    private void formatRange(TextRange selectedRange, Document document) {
        DocumentRangeFormattingParamsDTO params = dtoFactory.createDto(DocumentRangeFormattingParamsDTO.class);

        TextDocumentIdentifierDTO identifier = dtoFactory.createDto(TextDocumentIdentifierDTO.class);
        identifier.setUri(document.getFile().getPath());

        params.setTextDocument(identifier);
        params.setOptions(getFormattingOptions());

        RangeDTO range = dtoFactory.createDto(RangeDTO.class);
        PositionDTO start = dtoFactory.createDto(PositionDTO.class);
        PositionDTO end = dtoFactory.createDto(PositionDTO.class);

        start.setLine(selectedRange.getFrom().getLine());
        start.setCharacter(selectedRange.getFrom().getCharacter());

        end.setLine(selectedRange.getTo().getLine());
        end.setCharacter(selectedRange.getTo().getCharacter());

        range.setStart(start);
        range.setEnd(end);

        params.setRange(range);

        Promise<List<TextEditDTO>> promise = client.rangeFormatting(params);
        handleFormatting(promise, document);
    }

}
