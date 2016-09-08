/**
 * Copyright (c) 2016 TypeFox GmbH (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.che.plugin.languageserver.shared.model;

import java.util.List;

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
     * The fileExtension this language is associated with. At least one extension must be provided.
     */
    List<String> getFileExtensions();

    /**
     * The optional highlighting configuration to support client side syntax highlighting.
     * The format is client (editor) dependent.
     */
    String getHighlightingConfiguration();
}
