/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.project.shared.dto.event;

import org.eclipse.che.dto.shared.DTO;

@DTO
public interface FileTrackingOperationDto {
    String getPath();

    FileTrackingOperationDto withPath(String path);

    String getOldPath();

    FileTrackingOperationDto withOldPath(String oldPath);

    Type getType();

    FileTrackingOperationDto withType(Type type);

    enum Type {
        START,
        STOP,
        SUSPEND,
        RESUME,
        MOVE
    }
}
