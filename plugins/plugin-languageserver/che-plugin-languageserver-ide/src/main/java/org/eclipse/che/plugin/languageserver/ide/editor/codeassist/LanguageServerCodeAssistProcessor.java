package org.eclipse.che.plugin.languageserver.ide.editor.codeassist;

import io.typefox.lsapi.ServerCapabilities;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.editor.codeassist.CodeAssistCallback;
import org.eclipse.che.ide.api.editor.codeassist.CodeAssistProcessor;
import org.eclipse.che.ide.api.editor.codeassist.CompletionProposal;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.plugin.languageserver.ide.LanguageServerResources;
import org.eclipse.che.plugin.languageserver.ide.service.TextDocumentServiceClient;
import org.eclipse.che.plugin.languageserver.shared.lsapi.CompletionItemDTO;
import org.eclipse.che.plugin.languageserver.shared.lsapi.PositionDTO;
import org.eclipse.che.plugin.languageserver.shared.lsapi.TextDocumentIdentifierDTO;
import org.eclipse.che.plugin.languageserver.shared.lsapi.TextDocumentPositionParamsDTO;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class LanguageServerCodeAssistProcessor implements CodeAssistProcessor {

    private final DtoFactory                dtoFactory;
    private final LanguageServerResources   resources;
    private final CompletionImageProvider   imageProvider;
    private final ServerCapabilities serverCapabilities;
    private       TextDocumentServiceClient documentServiceClient;
    private String lastErrorMessage;

    @Inject
    public LanguageServerCodeAssistProcessor(TextDocumentServiceClient documentServiceClient,
                                             DtoFactory dtoFactory,
                                             LanguageServerResources resources,
                                             CompletionImageProvider imageProvider,
                                             @Assisted ServerCapabilities serverCapabilities) {
        this.documentServiceClient = documentServiceClient;
        this.dtoFactory = dtoFactory;
        this.resources = resources;
        this.imageProvider = imageProvider;
        this.serverCapabilities = serverCapabilities;
    }

    @Override
    public void computeCompletionProposals(TextEditor editor, int offset, final CodeAssistCallback callback) {
        TextDocumentPositionParamsDTO documentPosition = dtoFactory.createDto(TextDocumentPositionParamsDTO.class);
        documentPosition.setUri(editor.getEditorInput().getFile().getPath());
        PositionDTO position = dtoFactory.createDto(PositionDTO.class);
        TextPosition textPos = editor.getDocument().getPositionFromIndex(offset);
        position.setCharacter(textPos.getCharacter());
        position.setLine(textPos.getLine());
        documentPosition.setPosition(position);
        final TextDocumentIdentifierDTO documentId = dtoFactory.createDto(TextDocumentIdentifierDTO.class);
        documentId.setUri(editor.getEditorInput().getFile().getPath());
        documentPosition.setTextDocument(documentId);
        this.lastErrorMessage = null;
        documentServiceClient.completion(documentPosition).then(new Operation<List<CompletionItemDTO>>() {

            @Override
            public void apply(List<CompletionItemDTO> items) throws OperationException {
                List<CompletionProposal> proposals = newArrayList();
                for (CompletionItemDTO item : items) {
                    proposals.add(new CompletionItemBasedCompletionProposal(item,
                                                                            documentServiceClient,
                                                                            documentId,
                                                                            resources,
                                                                            imageProvider.getIcon(item.getKind()),
                                                                            serverCapabilities));
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

    @Override
    public String getErrorMessage() {
        return lastErrorMessage;
    }

}
