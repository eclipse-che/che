package org.eclipse.che.api.languageserver.registry;

public class DocumentFilter {

    private final String globPattern;
    private final String languageId;
    private final String scheme;

    public DocumentFilter(String languageId, String globPattern, String scheme) {
        this.globPattern = globPattern;
        this.languageId = languageId;
        this.scheme = scheme;
    }
    
    public String getLanguageId() {
        return languageId;
    }

    public String getPathRegex() {
        return globPattern;
    }

    public String getScheme() {
        return scheme;
    }

}
