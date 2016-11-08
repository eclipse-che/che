/**
 * Copyright (c) 2016 TypeFox GmbH (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.che.api.languageserver.shared.lsapi;

import io.typefox.lsapi.Location;

import org.eclipse.che.dto.shared.DTO;

/**
 * @author Sven Efftinge
 */
@DTO
public interface LocationDTO extends Location {
    void setUri(final String uri);

    /**
     * Overridden to return the DTO type.
     */
    RangeDTO getRange();

    void setRange(final RangeDTO range);
}
