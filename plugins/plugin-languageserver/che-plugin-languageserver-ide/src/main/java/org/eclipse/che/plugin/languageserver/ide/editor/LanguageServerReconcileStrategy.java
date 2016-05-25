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
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.plugin.languageserver.ide.service.TextDocumentServiceClient;

import static org.eclipse.che.plugin.languageserver.shared.dto.DtoClientImpls.DidChangeTextDocumentParamsDTOImpl;
import static org.eclipse.che.plugin.languageserver.shared.dto.DtoClientImpls.PositionDTOImpl;
import static org.eclipse.che.plugin.languageserver.shared.dto.DtoClientImpls.RangeDTOImpl;
import static org.eclipse.che.plugin.languageserver.shared.dto.DtoClientImpls.TextDocumentContentChangeEventDTOImpl;
import static org.eclipse.che.plugin.languageserver.shared.dto.DtoClientImpls.VersionedTextDocumentIdentifierDTOImpl;

/**
 * @author Evgen Vidolob
 */
public class LanguageServerReconcileStrategy implements ReconcilingStrategy {


    private final TextDocumentServiceClient textDocumentService;

    private int version = 0;

    @Inject
    public LanguageServerReconcileStrategy(final TextDocumentServiceClient textDocumentService) {
        this.textDocumentService = textDocumentService;
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
        TextPosition startPosition = document.getPositionFromIndex(event.getOffset());
        TextPosition endPosition = document.getPositionFromIndex(event.getOffset() + event.getLength());

        DidChangeTextDocumentParamsDTOImpl changeDTO = DidChangeTextDocumentParamsDTOImpl.make();
        String uri = document.getFile().getPath();
        changeDTO.setUri(uri);
        VersionedTextDocumentIdentifierDTOImpl versionedDocId = VersionedTextDocumentIdentifierDTOImpl.make();
        versionedDocId.setUri(uri);
        versionedDocId.setVersion(++version);
        changeDTO.setTextDocument(versionedDocId);
        TextDocumentContentChangeEventDTOImpl actualChange = TextDocumentContentChangeEventDTOImpl.make();
        RangeDTOImpl range = RangeDTOImpl.make();
        PositionDTOImpl start = PositionDTOImpl.make();
        start.setLine(startPosition.getLine());
        start.setCharacter(startPosition.getCharacter());
        PositionDTOImpl end = PositionDTOImpl.make();
        end.setLine(endPosition.getLine());
        end.setCharacter(endPosition.getCharacter());
        range.setStart(start);
        range.setEnd(end);
        actualChange.setRange(range);
        actualChange.setText(event.getText());
        changeDTO.addContentChanges(actualChange);
        textDocumentService.didChange(changeDTO);
    }

    @Override
    public void reconcile(DirtyRegion dirtyRegion, Region subRegion) {
        doReconcile();
    }

    public void doReconcile() {

    }

    @Override
    public void reconcile(Region partition) {
        doReconcile();
    }

    @Override
    public void closeReconciler() {

    }
}
