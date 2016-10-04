/**
 * Copyright (c) 2016 TypeFox GmbH (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.che.api.languageserver.shared.lsapi;

import io.typefox.lsapi.CodeActionParams;

import org.eclipse.che.dto.shared.DTO;

/**
 * @author Sven Efftinge
 */
@DTO
public interface CodeActionParamsDTO extends CodeActionParams {
    /**
     * The document in which the command was invoked. Overridden to return the
     * DTO type.
     */
    TextDocumentIdentifierDTO getTextDocument();

    /**
     * The document in which the command was invoked.
     */
    void setTextDocument(final TextDocumentIdentifierDTO textDocument);

    /**
     * The range for which the command was invoked. Overridden to return the DTO
     * type.
     */
    RangeDTO getRange();

    /**
     * The range for which the command was invoked.
     */
    void setRange(final RangeDTO range);

    /**
     * Context carrying additional information. Overridden to return the DTO
     * type.
     */
    CodeActionContextDTO getContext();

    /**
     * Context carrying additional information.
     */
    void setContext(final CodeActionContextDTO context);
}
