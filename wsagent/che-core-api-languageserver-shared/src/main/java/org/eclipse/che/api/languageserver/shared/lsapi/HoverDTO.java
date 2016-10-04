/**
 * Copyright (c) 2016 TypeFox GmbH (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.che.api.languageserver.shared.lsapi;

import io.typefox.lsapi.Hover;

import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * @author Sven Efftinge
 */
@DTO
public interface HoverDTO extends Hover {
    /**
     * The hover's content Overridden to return the DTO type.
     */
    List<MarkedStringDTO> getContents();

    /**
     * The hover's content
     */
    void setContents(final List<MarkedStringDTO> contents);

    /**
     * An optional range Overridden to return the DTO type.
     */
    RangeDTO getRange();

    /**
     * An optional range
     */
    void setRange(final RangeDTO range);
}
