/**
 * Copyright (c) 2016 TypeFox GmbH (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.che.api.languageserver.shared.lsapi;

import io.typefox.lsapi.FileChangeType;
import io.typefox.lsapi.FileEvent;

import org.eclipse.che.dto.shared.DTO;

/**
 * @author Sven Efftinge
 */
@DTO
public interface FileEventDTO extends FileEvent {
    /**
     * The file's uri.
     */
    void setUri(final String uri);

    /**
     * The change type.
     */
    void setType(final FileChangeType type);
}
