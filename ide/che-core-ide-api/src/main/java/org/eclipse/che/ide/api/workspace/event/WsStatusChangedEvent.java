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

import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;

public class WsStatusChangedEvent extends GwtEvent<WsStatusChangedEvent.Handler> {

    public static final Type<WsStatusChangedEvent.Handler> TYPE = new Type<>();

    private final WorkspaceStatus status;

    public WsStatusChangedEvent(WorkspaceStatus status) {
        this.status = status;
    }

    @Override
    public Type<Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onWsStatusChangedEvent(this);
    }

    public WorkspaceStatus getStatus() {
        return status;
    }

    public interface Handler extends EventHandler {
        void onWsStatusChangedEvent(WsStatusChangedEvent event);
    }
}
