/**
 * Copyright (c) 2016 TypeFox GmbH (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.che.api.languageserver.shared.lsapi;

import io.typefox.lsapi.CodeLensParams;

import org.eclipse.che.dto.shared.DTO;

/**
 * @author Sven Efftinge
 */
@DTO
public interface CodeLensParamsDTO extends CodeLensParams {
    /**
     * The document to request code lens for. Overridden to return the DTO type.
     */
    TextDocumentIdentifierDTO getTextDocument();

    /**
     * The document to request code lens for.
     */
    void setTextDocument(final TextDocumentIdentifierDTO textDocument);
}
