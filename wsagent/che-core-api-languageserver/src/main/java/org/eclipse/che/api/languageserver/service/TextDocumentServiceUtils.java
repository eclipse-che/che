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
package org.eclipse.che.api.languageserver.service;

/**
 * Text document service utilities
 */
class TextDocumentServiceUtils {
    private static final String FILE_PROJECTS = "file:///projects";

    static String prefixURI(String relativePath) {
        return FILE_PROJECTS + relativePath;
    }

    static String removePrefixUri(String uri) {
        return uri.startsWith(FILE_PROJECTS) ? uri.substring(FILE_PROJECTS.length()) : uri;
    }
    
    static boolean truish(Boolean b) {
        return b != null && b;
    }

}
