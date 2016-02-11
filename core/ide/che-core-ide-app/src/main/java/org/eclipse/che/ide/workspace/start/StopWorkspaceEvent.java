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
package org.eclipse.che.ide.workspace.start;

import com.google.gwt.event.shared.GwtEvent;

import org.eclipse.che.api.workspace.shared.dto.UsersWorkspaceDto;

/**
 * The class contains business logic which allows fire special event which contains information about stopped workspace via message bus.
 *
 * @author Dmitry Shnurenko
 */
public class StopWorkspaceEvent extends GwtEvent<StopWorkspaceHandler> {

    public static final Type<StopWorkspaceHandler> TYPE = new Type<>();

    private final UsersWorkspaceDto workspace;

    public StopWorkspaceEvent(UsersWorkspaceDto workspace) {
        this.workspace = workspace;
    }

    @Override
    public Type<StopWorkspaceHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(StopWorkspaceHandler handler) {
        handler.onWorkspaceStopped(workspace);
    }
}
