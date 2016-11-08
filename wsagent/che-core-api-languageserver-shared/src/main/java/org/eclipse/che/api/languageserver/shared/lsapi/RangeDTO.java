/**
 * Copyright (c) 2016 TypeFox GmbH (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.che.api.languageserver.shared.lsapi;

import io.typefox.lsapi.Range;

import org.eclipse.che.dto.shared.DTO;

/**
 * @author Sven Efftinge
 */
@DTO
public interface RangeDTO extends Range {
    /**
     * The range's start position Overridden to return the DTO type.
     */
    PositionDTO getStart();

    /**
     * The range's start position
     */
    void setStart(final PositionDTO start);

    /**
     * The range's end position Overridden to return the DTO type.
     */
    PositionDTO getEnd();

    /**
     * The range's end position
     */
    void setEnd(final PositionDTO end);
}
