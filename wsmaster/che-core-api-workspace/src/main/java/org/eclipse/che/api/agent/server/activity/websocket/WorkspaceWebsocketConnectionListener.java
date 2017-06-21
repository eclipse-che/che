/*
 *  [2012] - [2017] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package org.eclipse.che.api.agent.server.activity.websocket;

import org.eclipse.che.api.agent.server.activity.WorkspaceActivityNotifier;
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
