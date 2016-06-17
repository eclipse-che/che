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

import com.google.gwt.event.shared.GwtEvent;

import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;

/**
 * The class contains information about started workspace and provides ability to handle events when workspace starts.
 *
 * @author Dmitry Shnurenko
 */
public class WorkspaceStartedEvent extends GwtEvent<WorkspaceStartedHandler> {

    public static final Type<WorkspaceStartedHandler> TYPE = new Type<>();

    private final WorkspaceDto workspace;

    public WorkspaceStartedEvent(WorkspaceDto workspace) {
        this.workspace = workspace;
    }

    /** Returns started workspace. */
    public WorkspaceDto getWorkspace() {
        return workspace;
    }

    /** {@inheritDoc} */
    @Override
    public Type<WorkspaceStartedHandler> getAssociatedType() {
        return TYPE;
    }

    /** {@inheritDoc} */
    @Override
    protected void dispatch(WorkspaceStartedHandler handler) {
        handler.onWorkspaceStarted(this);
    }
}
