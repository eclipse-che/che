package org.eclipse.che.api.languageserver.shared.model;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.TextDocumentIdentifier;

/**
 * Extended version of lsp4j {@link CompletionItem} for communication with the IDE.
 *
 * @author Thomas Mäder
 */
public class ExtendedCompletionItem extends CompletionItem {

    private TextDocumentIdentifier textDocumentIdentifier;

    public CompletionItem getItem() {
        return item;
    }
    
    public void setItem(CompletionItem item) {
        this.item = item;
    }
    
    public TextDocumentIdentifier getTextDocumentIdentifier() {
        return textDocumentIdentifier;
    }

    public void setTextDocumentIdentifier(TextDocumentIdentifier documentIdentifier) {
        this.textDocumentIdentifier = documentIdentifier;
    }
}
