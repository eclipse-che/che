/**
 * Copyright (c) 2016 TypeFox GmbH (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.che.plugin.languageserver.shared.lsapi;

import org.eclipse.che.dto.shared.DTO;

import io.typefox.lsapi.DocumentOnTypeFormattingParams;

@DTO
public interface DocumentOnTypeFormattingParamsDTO extends DocumentOnTypeFormattingParams {
    /**
     * The position at which this request was send. Overridden to return the DTO
     * type.
     * 
     */
    public abstract PositionDTO getPosition();

    /**
     * The position at which this request was send.
     * 
     */
    public abstract void setPosition(final PositionDTO position);

    /**
     * The character that has been typed.
     * 
     */
    public abstract void setCh(final String ch);

    /**
     * The document to format. Overridden to return the DTO type.
     * 
     */
    public abstract TextDocumentIdentifierDTO getTextDocument();

    /**
     * The document to format.
     * 
     */
    public abstract void setTextDocument(final TextDocumentIdentifierDTO textDocument);

    /**
     * The format options Overridden to return the DTO type.
     * 
     */
    public abstract FormattingOptionsDTO getOptions();

    /**
     * The format options
     * 
     */
    public abstract void setOptions(final FormattingOptionsDTO options);
}
