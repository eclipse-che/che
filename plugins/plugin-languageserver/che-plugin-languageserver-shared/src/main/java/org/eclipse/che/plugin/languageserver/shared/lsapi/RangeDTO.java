/**
 * Copyright (c) 2016 TypeFox GmbH (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.che.plugin.languageserver.shared.lsapi;

import org.eclipse.che.dto.shared.DTO;

import io.typefox.lsapi.Range;

@DTO
public interface RangeDTO extends Range {
    /**
     * The range's start position Overridden to return the DTO type.
     * 
     */
    public abstract PositionDTO getStart();

    /**
     * The range's start position
     * 
     */
    public abstract void setStart(final PositionDTO start);

    /**
     * The range's end position Overridden to return the DTO type.
     * 
     */
    public abstract PositionDTO getEnd();

    /**
     * The range's end position
     * 
     */
    public abstract void setEnd(final PositionDTO end);
}
