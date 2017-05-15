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
package org.eclipse.che.ide.connection;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.workspace.event.WorkspaceStartedEvent;
import org.eclipse.che.ide.api.ConnectionClosedInformer;
import org.eclipse.che.ide.api.event.HttpSessionDestroyedEvent;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.MessageBusProvider;
import org.eclipse.che.ide.websocket.events.ConnectionClosedHandler;
import org.eclipse.che.ide.websocket.events.ConnectionOpenedHandler;
import org.eclipse.che.ide.websocket.events.WebSocketClosedEvent;

/**
 * @author Evgen Vidolob
 */
public class WsConnectionListener implements ConnectionClosedHandler, ConnectionOpenedHandler {

    private final EventBus                 eventBus;
    private final ConnectionClosedInformer connectionClosedInformer;

    private MessageBus messageBus;

    @Inject
    public WsConnectionListener(EventBus eventBus,
                                final MessageBusProvider messageBusProvider,
                                ConnectionClosedInformer connectionClosedInformer) {
        this.eventBus = eventBus;
        this.connectionClosedInformer = connectionClosedInformer;

        eventBus.addHandler(WorkspaceStartedEvent.TYPE, new WorkspaceStartedEvent.Handler() {
            @Override
            public void onWorkspaceStarted(WorkspaceStartedEvent workspace) {
                messageBus = messageBusProvider.getMessageBus();

                messageBus.addOnCloseHandler(WsConnectionListener.this);
            }
        });
    }

    @Override
    public void onClose(WebSocketClosedEvent event) {
        messageBus.removeOnCloseHandler(this);
        Log.info(getClass(), "WebSocket is closed, the status code is " + event.getCode() + ", the reason is " + event.getReason());

        if (event.getCode() == WebSocketClosedEvent.CLOSE_NORMAL && "Http session destroyed".equals(event.getReason())) {
            eventBus.fireEvent(new HttpSessionDestroyedEvent());
            return;
        }
        connectionClosedInformer.onConnectionClosed(event);
    }

    @Override
    public void onOpen() {
        messageBus.addOnCloseHandler(this);
    }
}
