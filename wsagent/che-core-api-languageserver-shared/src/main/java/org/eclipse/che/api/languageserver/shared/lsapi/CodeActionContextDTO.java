/**
 * Copyright (c) 2016 TypeFox GmbH (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.che.api.languageserver.shared.lsapi;

import io.typefox.lsapi.CodeActionContext;

import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * @author Sven Efftinge
 */
@DTO
public interface CodeActionContextDTO extends CodeActionContext {
    /**
     * An array of diagnostics. Overridden to return the DTO type.
     */
    List<DiagnosticDTO> getDiagnostics();

    /**
     * An array of diagnostics.
     */
    void setDiagnostics(final List<DiagnosticDTO> diagnostics);
}
