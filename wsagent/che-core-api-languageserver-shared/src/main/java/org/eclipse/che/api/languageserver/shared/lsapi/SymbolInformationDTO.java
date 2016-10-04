/**
 * Copyright (c) 2016 TypeFox GmbH (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.che.api.languageserver.shared.lsapi;

import io.typefox.lsapi.SymbolInformation;
import io.typefox.lsapi.SymbolKind;

import org.eclipse.che.dto.shared.DTO;

/**
 * @author Sven Efftinge
 */
@DTO
public interface SymbolInformationDTO extends SymbolInformation {
    /**
     * The name of this symbol.
     */
    void setName(final String name);

    /**
     * The kind of this symbol.
     */
    void setKind(final SymbolKind kind);

    /**
     * The location of this symbol. Overridden to return the DTO type.
     */
    LocationDTO getLocation();

    /**
     * The location of this symbol.
     */
    void setLocation(final LocationDTO location);

    /**
     * The name of the symbol containing this symbol.
     */
    void setContainerName(final String container);
}
