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
package org.eclipse.che.api.languageserver.shared.model;

import java.util.List;

/**
 * @author Anatolii Bazko
 */
public interface LanguageDescription {
    /**
     * The language id.
     */
    String getLanguageId();

    /**
     * The optional content types this language is associated with.
     */
    List<String> getMimeTypes();

    /**
     * The fileExtension this language is associated with. 
     */
    List<String> getFileExtensions();
    
    /**
     * The the list of file name patterns this language matches.
     */
    List<String> getFileNamePatterns();


    /**
     * The optional highlighting configuration to support client side syntax highlighting.
     * The format is client (editor) dependent.
     */
    String getHighlightingConfiguration();
}
