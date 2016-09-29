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
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.plugin.languageserver.ide.LanguageServerResources;
import org.eclipse.che.plugin.languageserver.ide.service.TextDocumentServiceClient;
import org.eclipse.che.plugin.languageserver.ide.util.DtoBuildHelper;
import org.eclipse.che.plugin.languageserver.shared.lsapi.CompletionItemDTO;
import org.eclipse.che.plugin.languageserver.shared.lsapi.TextDocumentIdentifierDTO;
import org.eclipse.che.plugin.languageserver.shared.lsapi.TextDocumentPositionParamsDTO;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Implement code assist with LS
 */
public class LanguageServerCodeAssistProcessor implements CodeAssistProcessor {

    private final DtoBuildHelper            dtoBuildHelper;
    private final LanguageServerResources   resources;
    private final CompletionImageProvider   imageProvider;
    private final ServerCapabilities        serverCapabilities;
    private       TextDocumentServiceClient documentServiceClient;
    private       String                    lastErrorMessage;

    @Inject
    public LanguageServerCodeAssistProcessor(TextDocumentServiceClient documentServiceClient,
                                             DtoBuildHelper dtoBuildHelper,
                                             LanguageServerResources resources,
                                             CompletionImageProvider imageProvider,
                                             @Assisted ServerCapabilities serverCapabilities) {
        this.documentServiceClient = documentServiceClient;
        this.dtoBuildHelper = dtoBuildHelper;
        this.resources = resources;
        this.imageProvider = imageProvider;
        this.serverCapabilities = serverCapabilities;
    }

    @Override
    public void computeCompletionProposals(TextEditor editor, int offset, final CodeAssistCallback callback) {
        TextDocumentPositionParamsDTO documentPosition = dtoBuildHelper.createTDPP(editor.getDocument(), offset);
        final TextDocumentIdentifierDTO documentId = documentPosition.getTextDocument();
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
