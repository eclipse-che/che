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
package org.eclipse.che.ide.workspace.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/** Fired when snapshot of the current workspace is created. */
public class SnapshotCreatedEvent extends GwtEvent<SnapshotCreatedEvent.Handler> {

    public static final Type<SnapshotCreatedEvent.Handler> TYPE = new Type<>();

    @Override
    public Type<SnapshotCreatedEvent.Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(SnapshotCreatedEvent.Handler handler) {
        handler.onSnapshotCreated(this);
    }

    public interface Handler extends EventHandler {
        void onSnapshotCreated(SnapshotCreatedEvent event);
    }
}
