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

/** Fired when snapshot of the current workspace started to be created. */
public class SnapshotCreatingEvent extends GwtEvent<SnapshotCreatingEvent.Handler> {

    public static final Type<SnapshotCreatingEvent.Handler> TYPE = new Type<>();

    @Override
    public Type<SnapshotCreatingEvent.Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(SnapshotCreatingEvent.Handler handler) {
        handler.onSnapshotCreating(this);
    }

    public interface Handler extends EventHandler {
        void onSnapshotCreating(SnapshotCreatingEvent event);
    }
}
