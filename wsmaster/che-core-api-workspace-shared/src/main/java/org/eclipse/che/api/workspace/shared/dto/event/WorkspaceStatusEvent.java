/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.workspace.shared.dto.event;

import org.eclipse.che.api.core.notification.EventOrigin;
import org.eclipse.che.dto.shared.DTO;

/**
 * Describes changes of state of a workspace.
 *
 * @author Alexander Garagatyi
 */
@EventOrigin("workspace")
@DTO
public interface WorkspaceStatusEvent {
    enum EventType {
        STARTING, RUNNING, STOPPING, STOPPED, ERROR, SNAPSHOT_CREATING, SNAPSHOT_CREATED, SNAPSHOT_CREATION_ERROR
    }

    EventType getEventType();

    void setEventType(EventType eventType);

    WorkspaceStatusEvent withEventType(EventType eventType);

    String getWorkspaceId();

    void setWorkspaceId(String machineId);

    WorkspaceStatusEvent withWorkspaceId(String machineId);

    String getError();

    void setError(String error);

    WorkspaceStatusEvent withError(String error);
}
