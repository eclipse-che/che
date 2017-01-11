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
 * The class contains information about started workspace and provides ability to handle events when workspace starts.
 *
 * @author Dmitry Shnurenko
 */
public class WorkspaceStartedEvent extends GwtEvent<WorkspaceStartedEvent.Handler> {

    public interface Handler extends EventHandler {
        /**
         * Performs some actions when workspace started.
         *
         * @param event
         *         contains information about started workspace
         */
        void onWorkspaceStarted(WorkspaceStartedEvent event);
    }

    public static final Type<WorkspaceStartedEvent.Handler> TYPE = new Type<>();

    private final Workspace workspace;

    public WorkspaceStartedEvent(Workspace workspace) {
        this.workspace = workspace;
    }

    /** Returns started workspace. */
    public Workspace getWorkspace() {
        return workspace;
    }

    /** {@inheritDoc} */
    @Override
    public Type<WorkspaceStartedEvent.Handler> getAssociatedType() {
        return TYPE;
    }

    /** {@inheritDoc} */
    @Override
    protected void dispatch(WorkspaceStartedEvent.Handler handler) {
        handler.onWorkspaceStarted(this);
    }

}
