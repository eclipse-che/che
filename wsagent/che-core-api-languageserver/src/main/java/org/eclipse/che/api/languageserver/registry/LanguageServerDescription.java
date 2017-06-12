package org.eclipse.che.api.languageserver.registry;

import java.util.List;

public class LanguageServerDescription  {
    private final String id;
    private final List<String> languageIds;
    private final List<DocumentFilter> documentFilters;

    public LanguageServerDescription(String id, List<String> languageIds, List<DocumentFilter> documentFilters) {
        this.id = id;
        this.languageIds = languageIds;
        this.documentFilters = documentFilters;
    }

    public String getId() {
        return id;
    }

    public List<String> getLanguageIds() {
        return languageIds;
    }

    public List<DocumentFilter> getDocumentFilters() {
        return documentFilters;
    }

}
