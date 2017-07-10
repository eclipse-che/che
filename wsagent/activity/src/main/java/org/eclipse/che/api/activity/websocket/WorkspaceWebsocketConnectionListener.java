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
package org.eclipse.che.api.activity.websocket;

import org.eclipse.che.api.activity.WorkspaceActivityNotifier;
import org.everrest.websockets.WSConnection;
import org.everrest.websockets.WSConnectionContext;
import org.everrest.websockets.WSConnectionListener;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Registers {@link WorkspaceWebsocketMessageReceiver} on opened connection
 *
 * @author Mihail Kuznyetsov
 */
@Singleton
public class WorkspaceWebsocketConnectionListener implements WSConnectionListener {

    private final WorkspaceActivityNotifier workspaceActivityNotifier;

    @Inject
    public WorkspaceWebsocketConnectionListener(WorkspaceActivityNotifier workspaceActivityNotifier) {
        this.workspaceActivityNotifier = workspaceActivityNotifier;
    }

    @Override
    public void onOpen(WSConnection connection) {
        connection.registerMessageReceiver(new WorkspaceWebsocketMessageReceiver(workspaceActivityNotifier));
    }

    @Override
    public void onClose(WSConnection connection) {
    }

    @PostConstruct
    public void start() {
        WSConnectionContext.registerConnectionListener(this);
    }
}
