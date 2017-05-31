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
package org.eclipse.che.ide.api.machine;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper;
import org.eclipse.che.api.workspace.shared.dto.WsAgentHealthStateDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.dialogs.ConfirmCallback;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.ui.loaders.LoaderPresenter;
import org.eclipse.che.ide.util.browser.BrowserUtils;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.MessageBusProvider;
import org.eclipse.che.ide.websocket.events.ConnectionClosedHandler;
import org.eclipse.che.ide.websocket.events.ConnectionErrorHandler;
import org.eclipse.che.ide.websocket.events.ConnectionOpenedHandler;
import org.eclipse.che.ide.websocket.events.WebSocketClosedEvent;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static org.eclipse.che.ide.api.machine.WsAgentState.STARTED;
import static org.eclipse.che.ide.api.machine.WsAgentState.STOPPED;

/**
 * Controls workspace agent's state, defines actions to be perform on different events related to websocket
 * connection (close, open, error, etc.), checks http/websocket connection to control it's state. Currently
 * there are only two states that a workspace agent can be at: {@link WsAgentState#STARTED} or
 * {@link WsAgentState#STOPPED}.
 *
 * @author Roman Nikitenko
 * @author Valeriy Svydenko
 */
@Singleton
public class WsAgentStateController implements ConnectionOpenedHandler, ConnectionClosedHandler, ConnectionErrorHandler {
    private final EventBus               eventBus;
    private final MessageBusProvider     messageBusProvider;
    private final DialogFactory          dialogFactory;
    private final AppContext             appContext;
    private final AsyncRequestFactory    asyncRequestFactory;
    private final LoaderPresenter        loader;
    private       DevMachine             devMachine;
    private       MessageBus             messageBus;
    private       WsAgentState           state;
    private List<AsyncCallback<MessageBus>> messageBusCallbacks = newArrayList();
    private List<AsyncCallback<DevMachine>> devMachineCallbacks = newArrayList();

    @Inject
    public WsAgentStateController(EventBus eventBus,
                                  LoaderPresenter loader,
                                  MessageBusProvider messageBusProvider,
                                  AsyncRequestFactory asyncRequestFactory,
                                  DialogFactory dialogFactory,
                                  AppContext appContext) {
        this.loader = loader;
        this.eventBus = eventBus;
        this.messageBusProvider = messageBusProvider;
        this.asyncRequestFactory = asyncRequestFactory;
        this.dialogFactory = dialogFactory;
        this.appContext = appContext;
    }

    public void initialize(DevMachine devMachine) {
        checkNotNull(devMachine, "Developer machine should not be a null");

        this.devMachine = devMachine;
        this.state = STOPPED;
        loader.show(LoaderPresenter.Phase.STARTING_WORKSPACE_AGENT);
        checkHttpConnection();
    }

    @Override
    public void onClose(WebSocketClosedEvent event) {
        if (STARTED.equals(state)) {
//            checkWsAgentHealth();
        }
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
        messageBus.removeOnOpenHandler(this);
        //need to make sure ready state equals 1 (OPEN) in same situations after opening it still equals 0 (CONNECTING)
        new Timer() {
            @Override
            public void run() {
                if (messageBus.getReadyState().equals(MessageBus.ReadyState.OPEN)) {
                    cancel();
                    started();
                }
            }
        }.scheduleRepeating(300);
    }

    /** Returns state of the ws agent */
    public WsAgentState getState() {
        return state;
    }

    public Promise<MessageBus> getMessageBus() {
        return AsyncPromiseHelper.createFromAsyncRequest(callback -> {
            if (messageBus != null) {
                callback.onSuccess(messageBus);
            } else {
                WsAgentStateController.this.messageBusCallbacks.add(callback);
            }
        });
    }

    /**
     * Goto checking HTTP connection via getting all registered REST Services
     */
    private void checkHttpConnection() {
        //here we add trailing slash because {@link org.eclipse.che.api.core.rest.ApiInfoService} mapped in this way
        String url = appContext.getDevAgentEndpoint() + '/';
        asyncRequestFactory.createGetRequest(url).send().then(ignored -> {
            checkWsConnection();
        }).catchError(ignored -> {
            // FIXME: spi
            new Timer() {
                @Override
                public void run() {
                    checkHttpConnection();
                }
            }.schedule(1000);
//            checkWsAgentHealth();
        });
    }

    private void started() {
        state = STARTED;
        loader.setSuccess(LoaderPresenter.Phase.STARTING_WORKSPACE_AGENT);

        for (AsyncCallback<MessageBus> callback : messageBusCallbacks) {
            callback.onSuccess(messageBus);
        }
        messageBusCallbacks.clear();

        for (AsyncCallback<DevMachine> callback : devMachineCallbacks) {
            callback.onSuccess(devMachine);
        }
        devMachineCallbacks.clear();

        eventBus.fireEvent(WsAgentStateEvent.createWsAgentStartedEvent());
    }

    private void checkStateOfWsAgent(WsAgentHealthStateDto agentHealthStateDto) {
        final int statusCode = agentHealthStateDto.getCode();
        final String infoWindowTitle = "Workspace Agent Not Responding";
        final boolean reloadPage = true;
        final boolean createSnapshot = true;
        final ConfirmCallback stopCallback = new StopCallback(!reloadPage, createSnapshot);
        final ConfirmCallback stopAndReloadCallback = new StopCallback(reloadPage, !createSnapshot);

        if (statusCode == 200) {
            dialogFactory.createChoiceDialog(infoWindowTitle,
                                             "Workspace agent is no longer responding. To fix the problem, verify you have a" +
                                             " good network connection and restart the workspace.",
                                             "Restart",
                                             "Close",
                                             stopAndReloadCallback,
                                             stopCallback).show();
        } else {
            dialogFactory.createChoiceDialog(infoWindowTitle,
                                             "Workspace agent is no longer responding. To fix the problem, restart the workspace.",
                                             "Restart",
                                             "Close",
                                             stopAndReloadCallback,
                                             stopCallback).show();
        }
    }

    /**
     * Try to connect via WebSocket connection
     */
    private void checkWsConnection() {
        if (messageBus != null) {
            messageBus.cancelReconnection();
        }
        messageBus = messageBusProvider.createMachineMessageBus(devMachine.getWsAgentWebSocketUrl());
        // TODO: need to remove all handlers when ws-agent stopped
        messageBus.addOnCloseHandler(this);
        messageBus.addOnErrorHandler(this);
        messageBus.addOnOpenHandler(this);
    }

//    private void checkWsAgentHealth() {
//        workspaceServiceClient.getWsAgentState(appContext.getWorkspaceId(), devMachine.getName()).then(agentHealthState -> {
//            if (RUNNING.equals(agentHealthState.getWorkspaceStatus())) {
//                checkStateOfWsAgent(agentHealthState);
//            }
//        }).catchError(new Operation<PromiseError>() {
//            @Override
//            public void apply(PromiseError arg) throws OperationException {
//                if (arg.getCause() instanceof ServerDisconnectedException) {
//                    dialogFactory.createMessageDialog("Server Unavailable",
//                                                      "Server is not responding. Your admin must restart it.",
//                                                      null).show();
//                }
//                Log.error(getClass(), arg.getMessage());
//            }
//        });
//    }

    private class StopCallback implements ConfirmCallback {

        private final boolean reloadPage;
        private final boolean createSnapshot;

        private StopCallback(boolean reloadPage, boolean createSnapshot) {
            this.reloadPage = reloadPage;
            this.createSnapshot = createSnapshot;
        }

        @Override
        public void accepted() {
//            workspaceServiceClient.stop(appContext.getWorkspaceId(), createSnapshot).then(ignored -> {
//                if (reloadPage) {
//                    BrowserUtils.reloadPage(false);
//                }
//            });
        }
    }

}
