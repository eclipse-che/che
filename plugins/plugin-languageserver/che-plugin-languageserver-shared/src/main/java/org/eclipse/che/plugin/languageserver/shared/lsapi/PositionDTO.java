/**
 * Copyright (c) 2016 TypeFox GmbH (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.che.plugin.languageserver.shared.lsapi;

import org.eclipse.che.dto.shared.DTO;

import io.typefox.lsapi.Position;

@DTO
public interface PositionDTO extends Position {
    /**
     * Line position in a document (zero-based).
     * 
     */
    public abstract void setLine(final int line);

    /**
     * Character offset on a line in a document (zero-based).
     * 
     */
    public abstract void setCharacter(final int character);
}
