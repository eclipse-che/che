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

import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.events.DocumentChangeEvent;
import org.eclipse.che.ide.api.editor.events.DocumentChangeHandler;
import org.eclipse.che.ide.api.editor.reconciler.DirtyRegion;
import org.eclipse.che.ide.api.editor.reconciler.ReconcilingStrategy;
import org.eclipse.che.ide.api.editor.text.Region;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.plugin.languageserver.ide.service.TextDocumentServiceClient;
import org.eclipse.che.plugin.languageserver.shared.lsapi.DidChangeTextDocumentParamsDTO;
import org.eclipse.che.plugin.languageserver.shared.lsapi.TextDocumentContentChangeEventDTO;
import org.eclipse.che.plugin.languageserver.shared.lsapi.VersionedTextDocumentIdentifierDTO;

import java.util.Collections;

/**
 * @author Evgen Vidolob
 */
public class LanguageServerReconcileStrategy implements ReconcilingStrategy {


    private final TextDocumentServiceClient textDocumentService;
    private final DtoFactory                dtoFactory;

    private int version = 0;

    @Inject
    public LanguageServerReconcileStrategy(final TextDocumentServiceClient textDocumentService, final DtoFactory dtoFactory) {
        this.textDocumentService = textDocumentService;
        this.dtoFactory = dtoFactory;
    }

    @Override
    public void setDocument(Document document) {
        document.getDocumentHandle().getDocEventBus().addHandler(DocumentChangeEvent.TYPE, new DocumentChangeHandler() {
            @Override
            public void onDocumentChange(DocumentChangeEvent event) {
                handleDocumentChange(event);
            }
        });
    }

    private void handleDocumentChange(DocumentChangeEvent event) {
        Document document = event.getDocument().getDocument();
//        TextPosition startPosition = document.getPositionFromIndex(event.getOffset());
//        TextPosition endPosition;
//        if (event.getRemoveCharCount() != 0) {
//            endPosition = new TextPosition(startPosition.getLine(),startPosition.getCharacter()+ event.getRemoveCharCount());
//        } else {
//            endPosition = new TextPosition(startPosition.getLine(),startPosition.getCharacter()+ event.getLength());
//        }

        DidChangeTextDocumentParamsDTO changeDTO = dtoFactory.createDto(DidChangeTextDocumentParamsDTO.class);
        String uri = document.getFile().getPath();
        changeDTO.setUri(uri);
        VersionedTextDocumentIdentifierDTO versionedDocId = dtoFactory.createDto(VersionedTextDocumentIdentifierDTO.class);
        versionedDocId.setUri(uri);
        versionedDocId.setVersion(++version);
        changeDTO.setTextDocument(versionedDocId);
        TextDocumentContentChangeEventDTO actualChange = dtoFactory.createDto(TextDocumentContentChangeEventDTO.class);
        //TODO for now all vscode LS uses TextDocumentSyncKind#Full by default, we need to support all Sync kind

//        RangeDTO range = dtoFactory.createDto(RangeDTO.class);
//        PositionDTO start = dtoFactory.createDto(PositionDTO.class);
//        start.setLine(startPosition.getLine());
//        start.setCharacter(startPosition.getCharacter());
//        PositionDTO end = dtoFactory.createDto(PositionDTO.class);
//        end.setLine(endPosition.getLine());
//        end.setCharacter(endPosition.getCharacter());
//        range.setStart(start);
//        range.setEnd(end);
//        actualChange.setRange(range);

        actualChange.setText(event.getDocument().getDocument().getContents());
        changeDTO.setContentChanges(Collections.singletonList(actualChange));
        textDocumentService.didChange(changeDTO);
    }

    @Override
    public void reconcile(DirtyRegion dirtyRegion, Region subRegion) {
        doReconcile();
    }

    public void doReconcile() {
        //TODO use DocumentHighlight to add additional highlight for file
    }

    @Override
    public void reconcile(Region partition) {
        doReconcile();
    }

    @Override
    public void closeReconciler() {

    }
}
