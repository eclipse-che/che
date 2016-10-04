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
package org.eclipse.che.ide.ui.loaders;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Fire this event to download logs of the current workspace.
 *
 * @author Vitaliy Guliy
 */
public class DownloadWorkspaceOutputEvent extends GwtEvent<DownloadWorkspaceOutputEvent.Handler> {

    public interface Handler extends EventHandler {

        void onDownloadWorkspaceOutput(DownloadWorkspaceOutputEvent event);

    }

    public static final Type<Handler> TYPE = new Type<>();

    @Override
    public Type<Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onDownloadWorkspaceOutput(this);
    }

}
