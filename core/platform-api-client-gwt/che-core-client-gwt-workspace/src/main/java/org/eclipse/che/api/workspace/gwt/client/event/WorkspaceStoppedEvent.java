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
package org.eclipse.che.api.workspace.gwt.client.event;

import com.google.gwt.event.shared.GwtEvent;
import org.eclipse.che.api.workspace.shared.dto.UsersWorkspaceDto;

/**
 * Event fired when workspace has been stopped.
 *
 * @author Vitaliy Guliy
 */
public class WorkspaceStoppedEvent extends GwtEvent<WorkspaceStoppedHandler> {

    public static final Type<WorkspaceStoppedHandler> TYPE = new Type<>();

    private final UsersWorkspaceDto workspace;

    public WorkspaceStoppedEvent(UsersWorkspaceDto workspace) {
        this.workspace = workspace;
    }

    public UsersWorkspaceDto getWorkspace() {
        return workspace;
    }

    @Override
    public Type<WorkspaceStoppedHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(WorkspaceStoppedHandler handler) {
        handler.onWorkspaceStopped(this);
    }

}
