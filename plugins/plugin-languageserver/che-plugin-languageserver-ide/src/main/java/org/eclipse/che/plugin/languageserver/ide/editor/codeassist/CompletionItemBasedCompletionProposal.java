/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
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
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import org.eclipse.che.api.languageserver.shared.lsapi.CompletionItemDTO;
import org.eclipse.che.api.languageserver.shared.lsapi.RangeDTO;
import org.eclipse.che.api.languageserver.shared.lsapi.TextDocumentIdentifierDTO;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.editor.codeassist.Completion;
import org.eclipse.che.ide.api.editor.codeassist.CompletionProposal;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.text.LinearRange;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.plugin.languageserver.ide.LanguageServerResources;
import org.eclipse.che.plugin.languageserver.ide.filters.Match;
import org.eclipse.che.plugin.languageserver.ide.service.TextDocumentServiceClient;

import java.util.List;

import static org.eclipse.che.ide.api.theme.Style.theme;

/**
 * @author Anatolii Bazko
 * @author Kaloyan Raev
 */
public class CompletionItemBasedCompletionProposal implements CompletionProposal {

    private CompletionItemDTO               completionItem;
    private final TextDocumentServiceClient documentServiceClient;
    private final TextDocumentIdentifierDTO documentId;
    private final LanguageServerResources   resources;
    private final Icon                      icon;
    private final ServerCapabilities        serverCapabilities;
    private final List<Match>               highlights;
    private final int                       offset;
    private boolean resolved;

    CompletionItemBasedCompletionProposal(CompletionItemDTO completionItem,
                                          TextDocumentServiceClient documentServiceClient,
                                          TextDocumentIdentifierDTO documentId,
                                          LanguageServerResources resources,
                                          Icon icon,
                                          ServerCapabilities serverCapabilities,
                                          List<Match> highlights,
                                          int offset) {
        this.completionItem = completionItem;
        this.documentServiceClient = documentServiceClient;
        this.documentId = documentId;
        this.resources = resources;
        this.icon = icon;
        this.serverCapabilities = serverCapabilities;
        this.highlights = highlights;
        this.offset = offset;
        this.resolved = false;
    }

    @Override
    public void getAdditionalProposalInfo(final AsyncCallback<Widget> callback) {
        if (completionItem.getDocumentation() == null && canResolve()) {
            resolve().then(new Operation<CompletionItemDTO>() {
                @Override
                public void apply(CompletionItemDTO item) throws OperationException {
                    completionItem = item;
                    resolved = true;
                    callback.onSuccess(createAdditionalInfoWidget());
                }
            }).catchError(new Operation<PromiseError>() {
                @Override
                public void apply(PromiseError e) throws OperationException {
                    callback.onFailure(e.getCause());
                }
            });
        } else {
            callback.onSuccess(createAdditionalInfoWidget());
        }
    }

    private Widget createAdditionalInfoWidget() {
        String documentation = completionItem.getDocumentation();
        if (documentation == null || documentation.trim().isEmpty()) {
            documentation = "No documentation found.";
        }

        HTML widget = new HTML(documentation);
        widget.setWordWrap(true);
        widget.getElement().getStyle().setColor(theme.completionPopupItemTextColor());
        widget.getElement().getStyle().setFontSize(13, Style.Unit.PX);
        widget.getElement().getStyle().setMarginLeft(4, Style.Unit.PX);
        widget.getElement().getStyle().setOverflow(Overflow.AUTO);
        widget.getElement().getStyle().setProperty("userSelect", "text");
        widget.setHeight("100%");
        return widget;
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
        callback.onCompletion(new CompletionImpl(completionItem, offset));
    }

    private boolean canResolve() {
        return !resolved &&
               serverCapabilities.getCompletionProvider() != null &&
               serverCapabilities.getCompletionProvider().getResolveProvider() != null &&
               serverCapabilities.getCompletionProvider().getResolveProvider();
    }

    private Promise<CompletionItemDTO> resolve() {
        completionItem.setTextDocumentIdentifier(documentId);
        return documentServiceClient.resolveCompletionItem(completionItem);
    }

    private static class CompletionImpl implements Completion {

        private CompletionItemDTO completionItem;
        private int               offset;

        public CompletionImpl(CompletionItemDTO completionItem, int offset) {
            this.completionItem = completionItem;
            this.offset = offset;
        }

        @Override
        public void apply(Document document) {
            if (completionItem.getTextEdit() != null) {
                RangeDTO range = completionItem.getTextEdit().getRange();
                int startOffset = document.getIndexFromPosition(
                        new TextPosition(range.getStart().getLine(), range.getStart().getCharacter()));
                int endOffset = offset + document.getIndexFromPosition(
                        new TextPosition(range.getEnd().getLine(), range.getEnd().getCharacter()));
                document.replace(startOffset, endOffset - startOffset, completionItem.getTextEdit().getNewText());
            } else {
                String insertText = completionItem.getInsertText() == null ? completionItem.getLabel() : completionItem.getInsertText();
                document.replace(document.getCursorOffset() - offset, offset, insertText);
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
