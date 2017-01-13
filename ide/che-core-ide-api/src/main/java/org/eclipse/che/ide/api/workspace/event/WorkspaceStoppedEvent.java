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
package org.eclipse.che.ide.api.workspace.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import org.eclipse.che.api.core.model.workspace.Workspace;

/**
 * Event fired when workspace has been stopped.
 *
 * @author Vitaliy Guliy
 */
public class WorkspaceStoppedEvent extends GwtEvent<WorkspaceStoppedEvent.Handler> {

    public interface Handler extends EventHandler {
        /**
         * Perform actions when workspace is stopped.
         *
         * @param event
         *         workspace stopped event
         */
        void onWorkspaceStopped(WorkspaceStoppedEvent event);
    }

    public static final Type<WorkspaceStoppedEvent.Handler> TYPE = new Type<>();

    private final Workspace workspace;

    public WorkspaceStoppedEvent(Workspace workspace) {
        this.workspace = workspace;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    @Override
    public Type<WorkspaceStoppedEvent.Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(WorkspaceStoppedEvent.Handler handler) {
        handler.onWorkspaceStopped(this);
    }

}
