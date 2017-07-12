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
