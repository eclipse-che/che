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
 * Event informing about starting a workspace.
 *
 * @author Vitaliy Guliy
 */
public class WorkspaceStartingEvent extends GwtEvent<WorkspaceStartingEvent.Handler> {

    /**
     * Implement this handler to handle the event.
     */
    public interface Handler extends EventHandler {

        void onWorkspaceStarting(WorkspaceStartingEvent event);

    }

    public static final GwtEvent.Type<WorkspaceStartingEvent.Handler> TYPE = new GwtEvent.Type<>();

    private final Workspace workspace;

    public WorkspaceStartingEvent(Workspace workspace) {
        this.workspace = workspace;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    @Override
    public Type<Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onWorkspaceStarting(this);
    }

}
