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
package org.eclipse.che.ide.api.workspace.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import org.eclipse.che.api.core.model.workspace.Workspace;

/**
 * Event fired when workspace has been stopped.
 *
 * @author Vitaliy Guliy
 */
public class WorkspaceCorruptedEvent extends GwtEvent<WorkspaceCorruptedEvent.Handler> {

    public interface Handler extends EventHandler {
        /**
         * Perform actions when workspace is stopped.
         *
         * @param event
         *         workspace stopped event
         */
        void onWorkspaceCorrupted(WorkspaceCorruptedEvent event);
    }

    public static final Type<WorkspaceCorruptedEvent.Handler> TYPE = new Type<>();

//    private final Workspace workspace;

    public WorkspaceCorruptedEvent() {
//        this.workspace = workspace;
    }

//    public Workspace getWorkspace() {
//        return workspace;
//    }

    @Override
    public Type<WorkspaceCorruptedEvent.Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(WorkspaceCorruptedEvent.Handler handler) {
        handler.onWorkspaceCorrupted(this);
    }

}
