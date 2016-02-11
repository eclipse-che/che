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
 * The class contains information about started workspace and provides ability to handle events when workspace starts.
 *
 * @author Dmitry Shnurenko
 */
public class StartWorkspaceEvent extends GwtEvent<StartWorkspaceHandler> {

    public static final Type<StartWorkspaceHandler> TYPE = new Type<>();

    private final UsersWorkspaceDto workspace;

    public StartWorkspaceEvent(UsersWorkspaceDto workspace) {
        this.workspace = workspace;
    }

    /** Returns started workspace. */
    public UsersWorkspaceDto getWorkspace() {
        return workspace;
    }

    /** {@inheritDoc} */
    @Override
    public Type<StartWorkspaceHandler> getAssociatedType() {
        return TYPE;
    }

    /** {@inheritDoc} */
    @Override
    protected void dispatch(StartWorkspaceHandler handler) {
        handler.onWorkspaceStarted(workspace);
    }
}
