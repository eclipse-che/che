/**
 * Copyright (c) 2016 TypeFox GmbH (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.che.api.languageserver.shared.lsapi;

import io.typefox.lsapi.RenameParams;

import org.eclipse.che.dto.shared.DTO;

/**
 * @author Sven Efftinge
 */
@DTO
public interface RenameParamsDTO extends RenameParams {
    /**
     * The document in which to find the symbol. Overridden to return the DTO
     * type.
     */
    TextDocumentIdentifierDTO getTextDocument();

    /**
     * The document in which to find the symbol.
     */
    void setTextDocument(final TextDocumentIdentifierDTO textDocument);

    /**
     * The position at which this request was send. Overridden to return the DTO
     * type.
     */
    PositionDTO getPosition();

    /**
     * The position at which this request was send.
     */
    void setPosition(final PositionDTO position);

    /**
     * The new name of the symbol. If the given name is not valid the request
     * must return a ResponseError with an appropriate message set.
     */
    void setNewName(final String newName);
}
