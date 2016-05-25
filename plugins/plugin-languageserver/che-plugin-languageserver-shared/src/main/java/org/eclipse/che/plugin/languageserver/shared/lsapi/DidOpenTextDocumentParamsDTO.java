/**
 * Copyright (c) 2016 TypeFox GmbH (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.che.plugin.languageserver.shared.lsapi;

import org.eclipse.che.dto.shared.DTO;

import io.typefox.lsapi.DidOpenTextDocumentParams;

@DTO
public interface DidOpenTextDocumentParamsDTO extends DidOpenTextDocumentParams {
    /**
     * The document that was opened. Overridden to return the DTO type.
     * 
     */
    public abstract TextDocumentItemDTO getTextDocument();

    /**
     * The document that was opened.
     * 
     */
    public abstract void setTextDocument(final TextDocumentItemDTO textDocument);

    /**
     * Legacy property to support protocol version 1.0 requests.
     * 
     */
    public abstract void setText(final String text);

    /**
     * The text document's uri.
     * 
     */
    public abstract void setUri(final String uri);
}
