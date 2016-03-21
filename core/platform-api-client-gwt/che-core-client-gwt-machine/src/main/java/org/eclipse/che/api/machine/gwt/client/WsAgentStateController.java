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
package org.eclipse.che.api.machine.gwt.client;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.machine.gwt.client.events.WsAgentStateEvent;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.RestServiceInfo;
import org.eclipse.che.ide.rest.StringUnmarshaller;
import org.eclipse.che.ide.ui.loaders.initialization.InitialLoadingInfo;
import org.eclipse.che.ide.ui.loaders.initialization.LoaderPresenter;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.MessageBusProvider;
import org.eclipse.che.ide.websocket.events.ConnectionClosedHandler;
import org.eclipse.che.ide.websocket.events.ConnectionErrorHandler;
import org.eclipse.che.ide.websocket.events.ConnectionOpenedHandler;
import org.eclipse.che.ide.websocket.events.WebSocketClosedEvent;

import java.util.ArrayList;
import java.util.List;

import static org.eclipse.che.api.machine.gwt.client.WsAgentState.STARTED;
import static org.eclipse.che.api.machine.gwt.client.WsAgentState.STOPPED;
import static org.eclipse.che.ide.ui.loaders.initialization.InitialLoadingInfo.Operations.WS_AGENT_BOOTING;
import static org.eclipse.che.ide.ui.loaders.initialization.OperationInfo.Status.IN_PROGRESS;
import static org.eclipse.che.ide.ui.loaders.initialization.OperationInfo.Status.SUCCESS;

/**
 * @author Roman Nikitenko
 * @author Valeriy Svydenko
 */
@Singleton
public class WsAgentStateController implements ConnectionOpenedHandler, ConnectionClosedHandler, ConnectionErrorHandler {

    private final EventBus                 eventBus;
    private final MessageBusProvider       messageBusProvider;
    private final InitialLoadingInfo       initialLoadingInfo;
    private final LoaderPresenter          loader;
    private final AsyncRequestFactory      asyncRequestFactory;
    private final String                   extPath;

    //not used now added it for future if it we will have possibility check that service available for client call
    private final List<RestServiceInfo>    availableServices;

    private MessageBus                     messageBus;
    private WsAgentState                   state;
    private String                         wsUrl;
    private String                         wsId;
    private AsyncCallback<MessageBus>      messageBusCallback;

    @Inject
    public WsAgentStateController(EventBus eventBus,
                                  LoaderPresenter loader,
                                  MessageBusProvider messageBusProvider,
                                  AsyncRequestFactory asyncRequestFactory,
                                  @Named("cheExtensionPath") String extPath,
                                  InitialLoadingInfo initialLoadingInfo) {
        this.loader = loader;
        this.eventBus = eventBus;
        this.messageBusProvider = messageBusProvider;
        this.asyncRequestFactory = asyncRequestFactory;
        this.extPath = extPath;
        this.initialLoadingInfo = initialLoadingInfo;
        availableServices = new ArrayList<>();
    }

    public void initialize(String wsUrl, String wsId) {
        this.wsUrl = wsUrl + "/" + wsId;
        this.wsId = wsId;
        this.state = STOPPED;
        initialLoadingInfo.setOperationStatus(WS_AGENT_BOOTING.getValue(), IN_PROGRESS);
        checkHttpConnection();
    }

    @Override
    public void onClose(WebSocketClosedEvent event) {
        Log.info(getClass(), "Test WS connection closed with code " + event.getCode() + " reason: " + event.getReason());
        if (state.equals(STARTED)) {
            state = STOPPED;
            eventBus.fireEvent(WsAgentStateEvent.createWsAgentStoppedEvent());
        }
    }

    @Override
    public void onError() {
        Log.info(getClass(), "Test WS connection error");
        if (state.equals(STARTED)) {
            state = STOPPED;
            eventBus.fireEvent(WsAgentStateEvent.createWsAgentStoppedEvent());
        }
    }

    @Override
    public void onOpen() {
        messageBus.removeOnOpenHandler(this);
        MessageBus.ReadyState readyState = messageBus.getReadyState();
        Log.info(getClass(), readyState.toString());
        //need to make sure ready state equals 1 (OPEN) in same situations after opening it still equals 0 (CONNECTING)
        if (!readyState.equals(MessageBus.ReadyState.OPEN)) {
            new Timer() {
                @Override
                public void run() {
                    Log.info(getClass(), messageBus.getReadyState());
                    if (messageBus.getReadyState().equals(MessageBus.ReadyState.OPEN)) {
                        cancel();
                        started();
                    }
                }
            }.scheduleRepeating(100);
        } else {
            started();
        }
    }

    private void started() {
        state = STARTED;
        initialLoadingInfo.setOperationStatus(WS_AGENT_BOOTING.getValue(), SUCCESS);
        loader.hide();

        if (messageBusCallback != null) {
            messageBusCallback.onSuccess(messageBus);
        }
        eventBus.fireEvent(WsAgentStateEvent.createWsAgentStartedEvent());
    }



    public WsAgentState getState() {
        return state;
    }

    public Promise<MessageBus> getMessageBus() {
        return AsyncPromiseHelper.createFromAsyncRequest(new AsyncPromiseHelper.RequestCall<MessageBus>() {
            @Override
            public void makeCall(AsyncCallback<MessageBus> callback) {
                if (messageBus != null) {
                    callback.onSuccess(messageBus);
                } else {
                    WsAgentStateController.this.messageBusCallback = callback;
                }
            }
        });
    }

    /**
     * Goto checking HTTP connection via getting all registered REST Services
     */
    private void checkHttpConnection() {
        asyncRequestFactory.createGetRequest(extPath + "/" + wsId + "/").send(new AsyncRequestCallback<String>(new StringUnmarshaller()) {
            @Override
            protected void onSuccess(String result) {
                JSONObject object = JSONParser.parseStrict(result).isObject();
                if (object.containsKey("rootResources")) {
                    JSONArray rootResources = object.get("rootResources").isArray();
                    for (int i = 0; i < rootResources.size(); i++) {
                        JSONObject rootResource = rootResources.get(i).isObject();
                        String regex = rootResource.get("regex").isString().stringValue();
                        String fqn = rootResource.get("fqn").isString().stringValue();
                        String path = rootResource.get("path").isString().stringValue();
                        availableServices.add(new RestServiceInfo(fqn,regex,path));
                    }
                    checkWsConnection();
                }
            }

            @Override
            protected void onFailure(Throwable exception) {
                Log.error(getClass(), exception.getMessage());
                new Timer() {
                    @Override
                    public void run() {
                        checkHttpConnection();
                    }
                }.schedule(1000);
            }
        });
    }

    /**
     * Try to connect via WebSocket connection
     */
    private void checkWsConnection() {
        if (messageBus != null) {
            messageBus.cancelReconnection();
        }
        messageBus = messageBusProvider.createMachineMessageBus(wsUrl);

        messageBus.addOnCloseHandler(this);
        messageBus.addOnCloseHandler(this);
        messageBus.addOnOpenHandler(this);
    }
}
