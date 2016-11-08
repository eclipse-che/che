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

import org.eclipse.che.api.languageserver.shared.lsapi.CompletionItemDTO;
import org.eclipse.che.api.languageserver.shared.lsapi.TextDocumentIdentifierDTO;
import org.eclipse.che.api.languageserver.shared.lsapi.TextDocumentPositionParamsDTO;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.editor.codeassist.CodeAssistCallback;
import org.eclipse.che.ide.api.editor.codeassist.CodeAssistProcessor;
import org.eclipse.che.ide.api.editor.codeassist.CompletionProposal;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.plugin.languageserver.ide.LanguageServerResources;
import org.eclipse.che.plugin.languageserver.ide.filters.FuzzyMatches;
import org.eclipse.che.plugin.languageserver.ide.filters.Match;
import org.eclipse.che.plugin.languageserver.ide.service.TextDocumentServiceClient;
import org.eclipse.che.plugin.languageserver.ide.util.DtoBuildHelper;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Implement code assist with LS
 */
public class LanguageServerCodeAssistProcessor implements CodeAssistProcessor {

    private final DtoBuildHelper            dtoBuildHelper;
    private final LanguageServerResources   resources;
    private final CompletionImageProvider   imageProvider;
    private final ServerCapabilities serverCapabilities;
    private final TextDocumentServiceClient documentServiceClient;
    private final FuzzyMatches fuzzyMatches;
    private String lastErrorMessage;

    @Inject
    public LanguageServerCodeAssistProcessor(TextDocumentServiceClient documentServiceClient,
                                             DtoBuildHelper dtoBuildHelper,
                                             LanguageServerResources resources,
                                             CompletionImageProvider imageProvider,
                                             @Assisted ServerCapabilities serverCapabilities,
                                             FuzzyMatches fuzzyMatches) {
        this.documentServiceClient = documentServiceClient;
        this.dtoBuildHelper = dtoBuildHelper;
        this.resources = resources;
        this.imageProvider = imageProvider;
        this.serverCapabilities = serverCapabilities;
        this.fuzzyMatches = fuzzyMatches;
    }

    @Override
    public void computeCompletionProposals(TextEditor editor, int offset, final CodeAssistCallback callback) {
        TextDocumentPositionParamsDTO documentPosition = dtoBuildHelper.createTDPP(editor.getDocument(), offset);
        final TextDocumentIdentifierDTO documentId = documentPosition.getTextDocument();
        String currentLine = editor.getDocument().getLineContent(documentPosition.getPosition().getLine());
        final String currentIdentifier = getCurrentIdentifier(currentLine, documentPosition.getPosition().getCharacter());
        this.lastErrorMessage = null;
        documentServiceClient.completion(documentPosition).then(new Operation<List<CompletionItemDTO>>() {

            @Override
            public void apply(List<CompletionItemDTO> items) throws OperationException {
                List<CompletionProposal> proposals = newArrayList();
                for (CompletionItemDTO item : items) {
                    List<Match> highlights = filter(currentIdentifier, item);
                    if (highlights != null ) {
                        proposals.add(new CompletionItemBasedCompletionProposal(item,
                                                                                documentServiceClient,
                                                                                documentId,
                                                                                resources,
                                                                                imageProvider.getIcon(item.getKind()),
                                                                                serverCapabilities,
                                                                                highlights));
                    }
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

    private String getCurrentIdentifier(String text, int offset) {
        int i = offset - 1;
        while (i >= 0 && isIdentifierChar(text.charAt(i))) {
            i--;
        }
        return text.substring(i + 1, offset);
    }
    
    private boolean isIdentifierChar(char c) {
        return c >= 'a' && c <= 'z' ||
            c >= 'A' && c <= 'Z' ||
            c >= '0' && c <= '9' ||
            c >= '\u007f' && c <= '\u00ff' ||
            c == '$' ||
            c == '_' ||
            c == '-';
    }
    
    private List<Match> filter(String word, CompletionItemDTO item) {
        return filter(word, item.getLabel(), item.getFilterText());
    }
    
    private List<Match> filter(String word, String label, String filterText) {
        if (filterText == null || filterText.isEmpty()) {
            filterText = label;
        }
        
        // check if the word matches the filterText
        if (fuzzyMatches.fuzzyMatch(word, filterText) != null) {
            // return the highlights based on the label
            List<Match> highlights = fuzzyMatches.fuzzyMatch(word, label);
            // return empty list of highlights if nothing matches the label
            return (highlights == null) ? new ArrayList<Match>() : highlights;
        }
        
        return null;
    }

}
