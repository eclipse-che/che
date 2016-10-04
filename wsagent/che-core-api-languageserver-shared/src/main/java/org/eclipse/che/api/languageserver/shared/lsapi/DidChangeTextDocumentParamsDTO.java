/**
 * Copyright (c) 2016 TypeFox GmbH (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.che.api.languageserver.shared.lsapi;

import io.typefox.lsapi.DidChangeTextDocumentParams;

import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * @author Sven Efftinge
 */
@DTO
public interface DidChangeTextDocumentParamsDTO extends DidChangeTextDocumentParams {
    /**
     * The document that did change. The version number points to the version
     * after all provided content changes have been applied. Overridden to
     * return the DTO type.
     */
    VersionedTextDocumentIdentifierDTO getTextDocument();

    /**
     * The document that did change. The version number points to the version
     * after all provided content changes have been applied.
     */
    void setTextDocument(final VersionedTextDocumentIdentifierDTO textDocument);

    /**
     * Legacy property to support protocol version 1.0 requests.
     */
    void setUri(final String uri);

    /**
     * The actual content changes. Overridden to return the DTO type.
     */
    List<TextDocumentContentChangeEventDTO> getContentChanges();

    /**
     * The actual content changes.
     */
    void setContentChanges(final List<TextDocumentContentChangeEventDTO> contentChanges);
}
