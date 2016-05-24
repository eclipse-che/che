package org.eclipse.che.plugin.languageserver.ide.editor.codeassist;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.editor.codeassist.CodeAssistCallback;
import org.eclipse.che.ide.api.editor.codeassist.CodeAssistProcessor;
import org.eclipse.che.ide.api.editor.codeassist.CompletionProposal;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.plugin.languageserver.ide.service.TextDocumentServiceClient;
import org.eclipse.che.plugin.languageserver.shared.dto.DtoClientImpls;
import org.eclipse.che.plugin.languageserver.shared.dto.DtoClientImpls.PositionDTOImpl;
import org.eclipse.che.plugin.languageserver.shared.dto.DtoClientImpls.TextDocumentIdentifierDTOImpl;
import org.eclipse.che.plugin.languageserver.shared.dto.DtoClientImpls.TextDocumentPositionParamsDTOImpl;
import org.eclipse.che.plugin.languageserver.shared.lsapi.CompletionItemDTO;

import com.google.inject.Inject;

public class LanguageServerCodeAssistProcessor implements CodeAssistProcessor {

    private TextDocumentServiceClient documentServiceClient;

    @Inject
    public LanguageServerCodeAssistProcessor(TextDocumentServiceClient documentServiceClient) {
        this.documentServiceClient = documentServiceClient;
    }

    @Override
    public void computeCompletionProposals(TextEditor editor, int offset, final CodeAssistCallback callback) {
        TextDocumentPositionParamsDTOImpl documentPosition = DtoClientImpls.TextDocumentPositionParamsDTOImpl.make();
        documentPosition.setUri(editor.getEditorInput().getFile().getContentUrl());
        PositionDTOImpl position = DtoClientImpls.PositionDTOImpl.make();
        TextPosition textPos = editor.getDocument().getPositionFromIndex(offset);
        position.setCharacter(textPos.getCharacter());
        position.setLine(textPos.getLine());
        documentPosition.setPosition(position);
        TextDocumentIdentifierDTOImpl documentId = DtoClientImpls.TextDocumentIdentifierDTOImpl.make();
        documentId.setUri(editor.getEditorInput().getFile().getContentUrl());
        documentPosition.setTextDocument(documentId);
        this.lastErrorMessage = null;
        documentServiceClient.completion(documentPosition).then(new Operation<List<CompletionItemDTO>>() {

            @Override
            public void apply(List<CompletionItemDTO> items) throws OperationException {
                List<CompletionProposal> proposals = newArrayList();
                for (CompletionItemDTO item : items) {
                    proposals.add(new CompletionItemBasedCompletionProposal(item));
                }
                callback.proposalComputed(proposals);
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                lastErrorMessage = error.getMessage();
            }
        });
    }

    private String lastErrorMessage;

    @Override
    public String getErrorMessage() {
        return lastErrorMessage;
    }

}
