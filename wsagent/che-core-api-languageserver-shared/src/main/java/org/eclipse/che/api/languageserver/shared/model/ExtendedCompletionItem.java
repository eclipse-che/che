package org.eclipse.che.api.languageserver.shared.model;

import org.eclipse.lsp4j.CompletionItem;

/**
 * Extended version of lsp4j {@link CompletionItem} for communication with the IDE.
 *
 * @author Thomas Mäder
 */
public class ExtendedCompletionItem {
    private String languageServerId;
    private CompletionItem item;

    public CompletionItem getItem() {
        return item;
    }
    
    public void setItem(CompletionItem item) {
        this.item = item;
    }
    
    public String getLanguageServerId() {
        return languageServerId;
    }
    
    public void setLanguageServerId(String languageServerId) {
        this.languageServerId = languageServerId;
    }
}
