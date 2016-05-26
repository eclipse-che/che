/**
 * Copyright (c) 2016 TypeFox GmbH (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.che.plugin.languageserver.shared.lsapi;

import java.util.List;

import org.eclipse.che.dto.shared.DTO;

import io.typefox.lsapi.DidChangeTextDocumentParams;

@DTO
public interface DidChangeTextDocumentParamsDTO extends DidChangeTextDocumentParams {
    /**
     * The document that did change. The version number points to the version
     * after all provided content changes have been applied. Overridden to
     * return the DTO type.
     * 
     */
    public abstract VersionedTextDocumentIdentifierDTO getTextDocument();

    /**
     * The document that did change. The version number points to the version
     * after all provided content changes have been applied.
     * 
     */
    public abstract void setTextDocument(final VersionedTextDocumentIdentifierDTO textDocument);

    /**
     * Legacy property to support protocol version 1.0 requests.
     * 
     */
    public abstract void setUri(final String uri);

    /**
     * The actual content changes. Overridden to return the DTO type.
     * 
     */
    public abstract List<TextDocumentContentChangeEventDTO> getContentChanges();

    /**
     * The actual content changes.
     * 
     */
    public abstract void setContentChanges(final List<TextDocumentContentChangeEventDTO> contentChanges);
}
