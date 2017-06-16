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
import org.eclipse.che.ide.api.app.AppContext;

/** Fired when some the current workspace goes into a starting state. */
public class WorkspaceStartingEvent extends GwtEvent<WorkspaceStartingEvent.Handler> {

    public static final Type<WorkspaceStartingEvent.Handler> TYPE = new Type<>();

    private final Workspace workspace;

    public WorkspaceStartingEvent(Workspace workspace) {
        this.workspace = workspace;
    }

    /** @deprecated use {@link AppContext#getWorkspace()} */
    @Deprecated
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

    public interface Handler extends EventHandler {
        void onWorkspaceStarting(WorkspaceStartingEvent event);
    }
}
