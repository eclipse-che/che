/**
 * Copyright (c) 2016 TypeFox GmbH (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.che.plugin.languageserver.shared.lsapi;

import org.eclipse.che.dto.shared.DTO;

import io.typefox.lsapi.SymbolInformation;

@DTO
public interface SymbolInformationDTO extends SymbolInformation {
    /**
     * The name of this symbol.
     * 
     */
    public abstract void setName(final String name);

    /**
     * The kind of this symbol.
     * 
     */
    public abstract void setKind(final int kind);

    /**
     * The location of this symbol. Overridden to return the DTO type.
     * 
     */
    public abstract LocationDTO getLocation();

    /**
     * The location of this symbol.
     * 
     */
    public abstract void setLocation(final LocationDTO location);

    /**
     * The name of the symbol containing this symbol.
     * 
     */
    public abstract void setContainer(final String container);
}
