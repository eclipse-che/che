/**
 * Copyright (c) 2016 TypeFox GmbH (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.che.api.languageserver.shared.lsapi;

import io.typefox.lsapi.DocumentSymbolParams;

import org.eclipse.che.dto.shared.DTO;

/**
 * @author Sven Efftinge
 */
@DTO
public interface DocumentSymbolParamsDTO extends DocumentSymbolParams {
    /**
     * The text document. Overridden to return the DTO type.
     */
    TextDocumentIdentifierDTO getTextDocument();

    /**
     * The text document.
     */
    void setTextDocument(final TextDocumentIdentifierDTO textDocument);
}
