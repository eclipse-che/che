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

import com.google.gwt.dom.client.Style;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import java.util.List;

import org.eclipse.che.api.languageserver.shared.lsapi.CompletionItemDTO;
import org.eclipse.che.api.languageserver.shared.lsapi.RangeDTO;
import org.eclipse.che.api.languageserver.shared.lsapi.TextDocumentIdentifierDTO;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.editor.codeassist.Completion;
import org.eclipse.che.ide.api.editor.codeassist.CompletionProposal;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.text.LinearRange;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.plugin.languageserver.ide.LanguageServerResources;
import org.eclipse.che.plugin.languageserver.ide.filters.Match;
import org.eclipse.che.plugin.languageserver.ide.service.TextDocumentServiceClient;

/**
 * @author Anatolii Bazko
 */
public class CompletionItemBasedCompletionProposal implements CompletionProposal {

    private final CompletionItemDTO         completionItem;
    private final TextDocumentServiceClient documentServiceClient;
    private final TextDocumentIdentifierDTO documentId;
    private final LanguageServerResources   resources;
    private final Icon                      icon;
    private final ServerCapabilities        serverCapabilities;
    private final List<Match>               highlights;

    CompletionItemBasedCompletionProposal(CompletionItemDTO completionItem,
                                          TextDocumentServiceClient documentServiceClient,
                                          TextDocumentIdentifierDTO documentId,
                                          LanguageServerResources resources, Icon icon,
                                          ServerCapabilities serverCapabilities,
                                          List<Match> highlights) {
        this.completionItem = completionItem;
        this.documentServiceClient = documentServiceClient;
        this.documentId = documentId;
        this.resources = resources;
        this.icon = icon;
        this.serverCapabilities = serverCapabilities;
        this.highlights = highlights;
    }

    @Override
    public Widget getAdditionalProposalInfo() {
        if (completionItem.getDocumentation() != null && !completionItem.getDocumentation().isEmpty()) {
            Label label = new Label(completionItem.getDocumentation());
            label.setWordWrap(true);
            label.getElement().getStyle().setFontSize(13, Style.Unit.PX);
            label.setSize("100%", "100%");
            return label;
        }
        return null;
    }

    @Override
    public String getDisplayString() {
        SafeHtmlBuilder builder = new SafeHtmlBuilder();
        
        String label = completionItem.getLabel();
        int pos = 0;
        for (Match highlight : highlights) {
            if (highlight.getStart() == highlight.getEnd()) {
                continue;
            }
            
            if (pos < highlight.getStart()) {
                appendPlain(builder, label.substring(pos, highlight.getStart()));
            }
            
            appendHighlighted(builder, label.substring(highlight.getStart(), highlight.getEnd()));
            pos = highlight.getEnd();
        }
        
        if (pos < label.length()) {
            appendPlain(builder, label.substring(pos));
        }
        
        if (completionItem.getDetail() != null) {
            appendDetail(builder, completionItem.getDetail());
        }
        
        return builder.toSafeHtml().asString();
    }
    
    private void appendPlain(SafeHtmlBuilder builder, String text) {
        builder.appendEscaped(text);
    }
    
    private void appendHighlighted(SafeHtmlBuilder builder, String text) {
        builder.appendHtmlConstant("<span class=\"" + resources.css().codeassistantHighlight() + "\">");
        builder.appendEscaped(text);
        builder.appendHtmlConstant("</span>");
    }
    
    private void appendDetail(SafeHtmlBuilder builder, String text) {
        builder.appendHtmlConstant(" <span class=\"" + resources.css().codeassistantDetail() + "\">");
        builder.appendEscaped(text);
        builder.appendHtmlConstant("</span>");
    }

    @Override
    public Icon getIcon() {
        return icon;
    }

    @Override
    public void getCompletion(final CompletionCallback callback) {

        if (serverCapabilities.getCompletionProvider() != null &&
            serverCapabilities.getCompletionProvider().getResolveProvider() != null &&
            serverCapabilities.getCompletionProvider().getResolveProvider()) {
            completionItem.setTextDocumentIdentifier(documentId);
            documentServiceClient.resolveCompletionItem(completionItem).then(new Operation<CompletionItemDTO>() {
                @Override
                public void apply(CompletionItemDTO arg) throws OperationException {
                    callback.onCompletion(new CompletionImpl(arg));
                }
            }).catchError(new Operation<PromiseError>() {
                @Override
                public void apply(PromiseError arg) throws OperationException {
                    Log.error(getClass(), arg);
                    //try to apply with default text
                    callback.onCompletion(new CompletionImpl(completionItem));
                }
            });
        } else {
            callback.onCompletion(new CompletionImpl(completionItem));
        }
    }

    private static class CompletionImpl implements Completion {

        private CompletionItemDTO completionItem;

        public CompletionImpl(CompletionItemDTO completionItem) {
            this.completionItem = completionItem;
        }

        @Override
        public void apply(Document document) {
            //TODO in general resolve completion item may not provide getTextEdit, need to add checks
            if (completionItem.getTextEdit() != null) {
                RangeDTO range = completionItem.getTextEdit().getRange();
                int startOffset = document.getIndexFromPosition(
                        new TextPosition(range.getStart().getLine(), range.getStart().getCharacter()));
                int endOffset = document
                        .getIndexFromPosition(new TextPosition(range.getEnd().getLine(), range.getEnd().getCharacter()));
                document.replace(startOffset, endOffset - startOffset, completionItem.getTextEdit().getNewText());
            } else {
                String insertText = completionItem.getInsertText() == null ? completionItem.getLabel() : completionItem.getInsertText();
                document.replace(document.getCursorOffset(), 0, insertText);
            }
        }

        @Override
        public LinearRange getSelection(Document document) {
            RangeDTO range = completionItem.getTextEdit().getRange();
            int startOffset = document
                                      .getIndexFromPosition(new TextPosition(range.getStart().getLine(), range.getStart().getCharacter()))
                              + completionItem.getTextEdit().getNewText().length();
            return LinearRange.createWithStart(startOffset).andLength(0);
        }

    }

}
