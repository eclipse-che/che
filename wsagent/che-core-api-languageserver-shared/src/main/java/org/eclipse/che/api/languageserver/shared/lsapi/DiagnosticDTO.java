/**
 * Copyright (c) 2016 TypeFox GmbH (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.che.api.languageserver.shared.lsapi;

import io.typefox.lsapi.Diagnostic;
import io.typefox.lsapi.DiagnosticSeverity;

import org.eclipse.che.dto.shared.DTO;

@DTO
public interface DiagnosticDTO extends Diagnostic {
    /**
     * The range at which the message applies Overridden to return the DTO type.
     */
    RangeDTO getRange();

    /**
     * The range at which the message applies
     */
    void setRange(final RangeDTO range);

    /**
     * The diagnostic's severity. Can be omitted. If omitted it is up to the
     * client to interpret diagnostics as error, warning, info or hint.
     */
    void setSeverity(final DiagnosticSeverity severity);

    /**
     * The diagnostic's code. Can be omitted.
     */
    void setCode(final String code);

    /**
     * A human-readable string describing the source of this diagnostic, e.g.
     * 'typescript' or 'super lint'.
     */
    void setSource(final String source);

    /**
     * The diagnostic's message.
     */
    void setMessage(final String message);
}
