/**
 * Copyright (c) 2016 TypeFox GmbH (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.che.api.languageserver.shared.lsapi;

import io.typefox.lsapi.DocumentRangeFormattingParams;

import org.eclipse.che.dto.shared.DTO;

/**
 * @author Sven Efftinge
 */
@DTO
public interface DocumentRangeFormattingParamsDTO extends DocumentRangeFormattingParams {
    /**
     * The range to format Overridden to return the DTO type.
     */
    RangeDTO getRange();

    /**
     * The range to format
     */
    void setRange(final RangeDTO range);

    /**
     * The document to format. Overridden to return the DTO type.
     */
    TextDocumentIdentifierDTO getTextDocument();

    /**
     * The document to format.
     */
    void setTextDocument(final TextDocumentIdentifierDTO textDocument);

    /**
     * The format options Overridden to return the DTO type.
     */
    FormattingOptionsDTO getOptions();

    /**
     * The format options
     */
    void setOptions(final FormattingOptionsDTO options);
}
