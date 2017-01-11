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
public interface FileStateUpdateDto {
    FileWatcherEventType getType();

    FileStateUpdateDto withType(FileWatcherEventType type);

    String getPath();

    FileStateUpdateDto withPath(String path);

    String getHashCode();

    FileStateUpdateDto withHashCode(String hashCode);
}
