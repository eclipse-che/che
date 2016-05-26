/**
 * Copyright (c) 2016 TypeFox GmbH (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.che.plugin.languageserver.shared.lsapi;

import java.util.List;

import org.eclipse.che.dto.shared.DTO;

import io.typefox.lsapi.DidChangeWatchedFilesParams;

@DTO
public interface DidChangeWatchedFilesParamsDTO extends DidChangeWatchedFilesParams {
    /**
     * The actual file events. Overridden to return the DTO type.
     * 
     */
    public abstract List<FileEventDTO> getChanges();

    /**
     * The actual file events.
     * 
     */
    public abstract void setChanges(final List<FileEventDTO> changes);
}
