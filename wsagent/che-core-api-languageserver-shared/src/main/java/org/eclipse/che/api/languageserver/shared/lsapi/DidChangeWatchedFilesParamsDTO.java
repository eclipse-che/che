/**
 * Copyright (c) 2016 TypeFox GmbH (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.che.api.languageserver.shared.lsapi;

import io.typefox.lsapi.DidChangeWatchedFilesParams;

import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * @author Sven Efftinge
 */
@DTO
public interface DidChangeWatchedFilesParamsDTO extends DidChangeWatchedFilesParams {
    /**
     * The actual file events. Overridden to return the DTO type.
     */
    List<FileEventDTO> getChanges();

    /**
     * The actual file events.
     */
    void setChanges(final List<FileEventDTO> changes);
}
