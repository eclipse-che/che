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
package org.eclipse.che.api.languageserver.shared.model.impl;


import org.eclipse.che.api.languageserver.shared.model.LanguageDescription;

import java.util.List;
import java.util.Objects;

/**
 * @author Anatolii Bazko
 */
public class LanguageDescriptionImpl implements LanguageDescription {
    /**
     * The language id.
     */
    private String languageId;
    /**
     * The optional content types this language is associated with.
     */
    private List<String> mimeTypes;
    /**
     * The fileExtension this language is associated with. 
     */
    private List<String> fileExtensions;
    /**
     * The optional file name patterns for this language.
     */
    private List<String> fileNamePatterns;
    /**
     * The optional highlighting configuration to support client side syntax highlighting.
     * The format is client (editor) dependent.
     */
    private String highlightingConfiguration;

    @Override
    public String getLanguageId() {
        return this.languageId;
    }

    public void setLanguageId(final String languageId) {
        this.languageId = languageId;
    }

    @Override
    public List<String> getMimeTypes() {
        return this.mimeTypes;
    }

    public void setMimeTypes(final List<String> mimeTypes) {
        this.mimeTypes = mimeTypes;
    }

    @Override
    public List<String> getFileExtensions() {
        return this.fileExtensions;
    }

    public void setFileExtensions(final List<String> fileExtensions) {
        this.fileExtensions = fileExtensions;
    }

    @Override
    public List<String> getFileNamePatterns() {
        return fileNamePatterns;
    }
    
    public void setFilenamePatterns(List<String> patterns) {
        this.fileNamePatterns= patterns;
    }
    
    @Override
    public String getHighlightingConfiguration() {
        return this.highlightingConfiguration;
    }

    public void setHighlightingConfiguration(final String highlightingConfiguration) {
        this.highlightingConfiguration = highlightingConfiguration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LanguageDescriptionImpl that = (LanguageDescriptionImpl)o;
        return Objects.equals(languageId, that.languageId) &&
               Objects.equals(mimeTypes, that.mimeTypes) &&
               Objects.equals(fileExtensions, that.fileExtensions) &&
               Objects.equals(highlightingConfiguration, that.highlightingConfiguration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(languageId, mimeTypes, fileExtensions, highlightingConfiguration);
    }

    @Override
    public String toString() {
        return "LanguageDescriptionImpl{" +
               "languageId='" + languageId + '\'' +
               ", mimeTypes=" + mimeTypes +
               ", fileExtensions=" + fileExtensions +
               ", highlightingConfiguration='" + highlightingConfiguration + '\'' +
               '}';
    }
}
