package org.eclipse.che.api.languageserver.shared.model;

import org.eclipse.lsp4j.CompletionList;

import java.util.List;

/**
 * Version of {@link CompletionList} that holds {@link ExtendedCompletionItem}s
 *
 * @author Thomas Mäder
 */
public class ExtendedCompletionList {
    private boolean                      inComplete;
    private List<ExtendedCompletionItem> items;

    public ExtendedCompletionList(boolean incomplete, List<ExtendedCompletionItem> items) {
        this.inComplete = incomplete;
        this.items = items;
    }

    public ExtendedCompletionList() {
    }

    public List<ExtendedCompletionItem> getItems() {
        return items;
    }

    public void setItems(List<ExtendedCompletionItem> items) {
        this.items = items;
    }

    public boolean isInComplete() {
        return inComplete;
    }

    public void setInComplete(boolean inComplete) {
        this.inComplete = inComplete;
    }
}
