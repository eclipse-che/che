/**
 * Copyright (c) 2016 TypeFox GmbH (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.che.plugin.languageserver.shared.lsapi;

import org.eclipse.che.dto.shared.DTO;

import io.typefox.lsapi.TextDocumentItem;

@DTO
public interface TextDocumentItemDTO extends TextDocumentItem {
    /**
     * The text document's uri.
     * 
     */
    public abstract void setUri(final String uri);

    /**
     * The text document's language identifier
     * 
     */
    public abstract void setLanguageId(final String languageId);

    /**
     * The version number of this document (it will strictly increase after each
     * change, including undo/redo).
     * 
     */
    public abstract void setVersion(final int version);

    /**
     * The content of the opened text document.
     * 
     */
    public abstract void setText(final String text);
}
