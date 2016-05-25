/**
 * Copyright (c) 2016 TypeFox GmbH (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.che.plugin.languageserver.shared.lsapi;

import org.eclipse.che.dto.shared.DTO;

import io.typefox.lsapi.InitializeResult;

@DTO
public interface InitializeResultDTO extends InitializeResult {
    /**
     * The capabilities the language server provides. Overridden to return the
     * DTO type.
     * 
     */
    public abstract ServerCapabilitiesDTO getCapabilities();

    /**
     * The capabilities the language server provides.
     * 
     */
    public abstract void setCapabilities(final ServerCapabilitiesDTO capabilities);
}
