/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat - initial API and implementation
 *******************************************************************************/
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
