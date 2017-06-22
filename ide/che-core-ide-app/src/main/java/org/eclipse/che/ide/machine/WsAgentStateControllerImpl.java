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
package org.eclipse.che.ide.machine;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.WsAgentState;
import org.eclipse.che.ide.api.machine.WsAgentStateController;
import org.eclipse.che.ide.api.machine.events.WsAgentServerRunningEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.workspace.model.MachineImpl;
import org.eclipse.che.ide.bootstrap.BasicIDEInitializedEvent;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.MessageBusProvider;
import org.eclipse.che.ide.websocket.events.ConnectionErrorHandler;
import org.eclipse.che.ide.websocket.events.ConnectionOpenedHandler;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper.createFromAsyncRequest;
import static org.eclipse.che.ide.api.machine.WsAgentState.STARTED;
import static org.eclipse.che.ide.api.machine.WsAgentState.STOPPED;
import static org.eclipse.che.ide.websocket.MessageBus.ReadyState.OPEN;

@Singleton
@Deprecated
public class WsAgentStateControllerImpl implements WsAgentStateController,
                                                   ConnectionOpenedHandler,
                                                   ConnectionErrorHandler {
    private final EventBus           eventBus;
    private final MessageBusProvider messageBusProvider;
    private final AppContext         appContext;
    private       MessageBus         messageBus;
    private       WsAgentState       state;
    private List<AsyncCallback<MessageBus>> messageBusCallbacks = newArrayList();

    @Inject
    public WsAgentStateControllerImpl(EventBus eventBus,
                                      MessageBusProvider messageBusProvider,
                                      AppContext appContext) {
        this.eventBus = eventBus;
        this.messageBusProvider = messageBusProvider;
        this.appContext = appContext;

        eventBus.addHandler(BasicIDEInitializedEvent.TYPE, e -> {
            if (RUNNING == appContext.getWorkspace().getStatus()) {
                establishConnection();
            }
        });

        eventBus.addHandler(WsAgentServerRunningEvent.TYPE, e -> establishConnection());
    }

    @Deprecated
    public void initialize(MachineImpl devMachine) {
    }

    @Override
    public void onError() {
        if (STARTED.equals(state)) {
            state = STOPPED;
            eventBus.fireEvent(WsAgentStateEvent.createWsAgentStoppedEvent());
        }
    }

    @Override
    public void onOpen() {
        //need to make sure ready state equals 1 (OPEN) in same situations after opening it still equals 0 (CONNECTING)
        new Timer() {
            @Override
            public void run() {
                if (messageBus.getReadyState().equals(OPEN)) {
                    cancel();

                    for (AsyncCallback<MessageBus> callback : messageBusCallbacks) {
                        callback.onSuccess(messageBus);
                    }

                    messageBusCallbacks.clear();
                }
            }
        }.scheduleRepeating(300);
    }

    /** Returns state of the ws agent */
    @Deprecated
    public WsAgentState getState() {
        return state;
    }

    @Deprecated
    public Promise<MessageBus> getMessageBus() {
        return createFromAsyncRequest(callback -> {
            if (messageBus != null) {
                callback.onSuccess(messageBus);
            } else {
                messageBusCallbacks.add(callback);
            }
        });
    }

    /** Establishes EverREST WebSocket connection. */
    private void establishConnection() {
        final String wsAgentRestEndpointURL = appContext.getDevAgentEndpoint().replaceFirst("http", "ws") + "/ws";

        messageBus = messageBusProvider.createMachineMessageBus(wsAgentRestEndpointURL);

        messageBus.addOnOpenHandler(this);
        messageBus.addOnErrorHandler(this);
    }
}
