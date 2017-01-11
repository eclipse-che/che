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
package org.eclipse.che.api.workspace.shared.dto.event;

import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.notification.EventOrigin;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.dto.shared.DTO;

/**
 * Describes workspace status changes.
 *
 * @author Alexander Garagatyi
 * @author Yevhenii Voevodin
 */
@EventOrigin("workspace")
@DTO
public interface WorkspaceStatusEvent {

    /** Defines event type for workspace status event changes. */
    enum EventType {
        STARTING, RUNNING, STOPPING, STOPPED, ERROR, SNAPSHOT_CREATING, SNAPSHOT_CREATED, SNAPSHOT_CREATION_ERROR
    }

    /**
     * Returns the type of this event.
     *
     * @deprecated use #getStatus() instead
     */
    @Deprecated
    EventType getEventType();

    void setEventType(EventType eventType);

    WorkspaceStatusEvent withEventType(EventType eventType);

    WorkspaceStatus getStatus();

    void setStatus(WorkspaceStatus status);

    WorkspaceStatusEvent withStatus(WorkspaceStatus status);

    /**
     * Returns previous workspace status.
     *
     * @see WorkspaceStatus for more information about certain values
     */
    WorkspaceStatus getPrevStatus();

    void setPrevStatus(WorkspaceStatus status);

    WorkspaceStatusEvent withPrevStatus(WorkspaceStatus status);

    /** The id of the workspace to which this event belongs to . */
    String getWorkspaceId();

    void setWorkspaceId(String machineId);

    WorkspaceStatusEvent withWorkspaceId(String machineId);

    /**
     * Returns an error message value if and only if the type of this event is
     * either {@link EventType#ERROR} or {@link EventType#SNAPSHOT_CREATION_ERROR}.
     */
    @Nullable
    String getError();

    void setError(String error);

    WorkspaceStatusEvent withError(String error);
}
