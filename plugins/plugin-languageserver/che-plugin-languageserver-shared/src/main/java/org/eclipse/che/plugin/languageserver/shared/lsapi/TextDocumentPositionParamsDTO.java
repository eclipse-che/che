/**
 * Copyright (c) 2016 TypeFox GmbH (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.che.plugin.languageserver.shared.lsapi;

import org.eclipse.che.dto.shared.DTO;

import io.typefox.lsapi.TextDocumentPositionParams;

@DTO
public interface TextDocumentPositionParamsDTO extends TextDocumentPositionParams {
    /**
     * The text document. Overridden to return the DTO type.
     * 
     */
    public abstract TextDocumentIdentifierDTO getTextDocument();

    /**
     * The text document.
     * 
     */
    public abstract void setTextDocument(final TextDocumentIdentifierDTO textDocument);

    /**
     * Legacy property to support protocol version 1.0 requests.
     * 
     */
    public abstract void setUri(final String uri);

    /**
     * The position inside the text document. Overridden to return the DTO type.
     * 
     */
    public abstract PositionDTO getPosition();

    /**
     * The position inside the text document.
     * 
     */
    public abstract void setPosition(final PositionDTO position);
}
