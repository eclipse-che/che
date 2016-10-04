/**
 * Copyright (c) 2016 TypeFox GmbH (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.che.api.languageserver.shared.lsapi;

import io.typefox.lsapi.CodeLens;

import org.eclipse.che.dto.shared.DTO;

/**
 * @author Sven Efftinge
 */
@DTO
public interface CodeLensDTO extends CodeLens {
    /**
     * The range in which this code lens is valid. Should only span a single
     * line. Overridden to return the DTO type.
     */
    RangeDTO getRange();

    /**
     * The range in which this code lens is valid. Should only span a single
     * line.
     */
    void setRange(final RangeDTO range);

    /**
     * The command this code lens represents. Overridden to return the DTO type.
     */
    CommandDTO getCommand();

    /**
     * The command this code lens represents.
     */
    void setCommand(final CommandDTO command);

    /**
     * An data entry field that is preserved on a code lens item between a code
     * lens and a code lens resolve request.
     */
    void setData(final Object data);
}
