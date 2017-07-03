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

/** Fired when creating snapshot of the current workspace was stopped due to error. */
public class SnapshotCreationErrorEvent extends GwtEvent<SnapshotCreationErrorEvent.Handler> {

    public static final Type<SnapshotCreationErrorEvent.Handler> TYPE = new Type<>();

    private final String errorMessage;

    public SnapshotCreationErrorEvent(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Returns an error message if snapshot creation was stopped due to error.
     *
     * @return error message if snapshot creation was stopped due to error
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public Type<SnapshotCreationErrorEvent.Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(SnapshotCreationErrorEvent.Handler handler) {
        handler.onSnapshotCreated(this);
    }

    public interface Handler extends EventHandler {
        void onSnapshotCreated(SnapshotCreationErrorEvent event);
    }
}
