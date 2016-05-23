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
package org.eclipse.che.ide.api.machine;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
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

import static org.eclipse.che.ide.ui.loaders.initialization.InitialLoadingInfo.Operations.WS_AGENT_BOOTING;
import static org.eclipse.che.ide.ui.loaders.initialization.OperationInfo.Status.IN_PROGRESS;
import static org.eclipse.che.ide.ui.loaders.initialization.OperationInfo.Status.SUCCESS;

/**
 * @author Roman Nikitenko
 * @author Valeriy Svydenko
 */
@Singleton
public class WsAgentStateController implements ConnectionOpenedHandler, ConnectionClosedHandler, ConnectionErrorHandler {

    private final EventBus            eventBus;
    private final MessageBusProvider  messageBusProvider;
    private final InitialLoadingInfo  initialLoadingInfo;
    private final LoaderPresenter     loader;
    private final AsyncRequestFactory asyncRequestFactory;
    private       DevMachine          devMachine;

    //not used now added it for future if it we will have possibility check that service available for client call
    private final List<RestServiceInfo> availableServices;

    private MessageBus                messageBus;
    private WsAgentState              state;
    private AsyncCallback<MessageBus> messageBusCallback;
    private AsyncCallback<DevMachine> devMachineCallback;

    @Inject
    public WsAgentStateController(EventBus eventBus,
                                  LoaderPresenter loader,
                                  MessageBusProvider messageBusProvider,
                                  AsyncRequestFactory asyncRequestFactory,
                                  InitialLoadingInfo initialLoadingInfo) {
        this.loader = loader;
        this.eventBus = eventBus;
        this.messageBusProvider = messageBusProvider;
        this.asyncRequestFactory = asyncRequestFactory;
        this.initialLoadingInfo = initialLoadingInfo;
        this.availableServices = new ArrayList<>();
    }

    public void initialize(DevMachine devMachine) {
        this.devMachine = devMachine;
        this.state = WsAgentState.STOPPED;
        initialLoadingInfo.setOperationStatus(WS_AGENT_BOOTING.getValue(), IN_PROGRESS);
        checkHttpConnection();
    }

    @Override
    public void onClose(WebSocketClosedEvent event) {
        Log.info(getClass(), "Test WS connection closed with code " + event.getCode() + " reason: " + event.getReason());
        if (state.equals(WsAgentState.STARTED)) {
            state = WsAgentState.STOPPED;
            eventBus.fireEvent(WsAgentStateEvent.createWsAgentStoppedEvent());
        }
    }

    @Override
    public void onError() {
        Log.info(getClass(), "Test WS connection error");
        if (state.equals(WsAgentState.STARTED)) {
            state = WsAgentState.STOPPED;
            eventBus.fireEvent(WsAgentStateEvent.createWsAgentStoppedEvent());
        }
    }

    @Override
    public void onOpen() {
        messageBus.removeOnOpenHandler(this);
        MessageBus.ReadyState readyState = messageBus.getReadyState();
        Log.info(getClass(), readyState.toString());
        //need to make sure ready state equals 1 (OPEN) in same situations after opening it still equals 0 (CONNECTING)
        new Timer() {
            @Override
            public void run() {
                Log.info(getClass(), messageBus.getReadyState());
                if (messageBus.getReadyState().equals(MessageBus.ReadyState.OPEN)) {
                    cancel();
                    started();
                }
            }
        }.scheduleRepeating(300);
    }

    private void started() {
        state = WsAgentState.STARTED;
        initialLoadingInfo.setOperationStatus(WS_AGENT_BOOTING.getValue(), SUCCESS);
        loader.hide();

        if (messageBusCallback != null) {
            messageBusCallback.onSuccess(messageBus);
        }
        if (devMachineCallback != null) {
            devMachineCallback.onSuccess(devMachine);
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


    public Promise<DevMachine> getDevMachine() {
        return AsyncPromiseHelper.createFromAsyncRequest(new AsyncPromiseHelper.RequestCall<DevMachine>() {
            @Override
            public void makeCall(AsyncCallback<DevMachine> callback) {
                if (messageBus != null) {
                    callback.onSuccess(devMachine);
                } else {
                    WsAgentStateController.this.devMachineCallback = callback;
                }
            }
        });
    }


    /**
     * Goto checking HTTP connection via getting all registered REST Services
     */
    private void checkHttpConnection() {
        String url = devMachine.getWsAgentBaseUrl() + '/'; //here we add trailing slash because
                                                          // {@link org.eclipse.che.api.core.rest.ApiInfoService} mapped in this way
        asyncRequestFactory.createGetRequest(url)
                           .send(new StringUnmarshaller())
                           .then(new Operation<String>() {
                               @Override
                               public void apply(String result) throws OperationException {
                                   JSONObject object = null;
                                   try {
                                       object = JSONParser.parseStrict(result).isObject();
                                   } catch (Exception exception) {
                                       Log.warn(getClass(), "Parse root resources failed.");
                                   }

                                   if (object != null && object.containsKey("rootResources")) {
                                       JSONArray rootResources = object.get("rootResources").isArray();
                                       for (int i = 0; i < rootResources.size(); i++) {
                                           JSONObject rootResource = rootResources.get(i).isObject();
                                           String regex = rootResource.get("regex").isString().stringValue();
                                           String fqn = rootResource.get("fqn").isString().stringValue();
                                           String path = rootResource.get("path").isString().stringValue();
                                           availableServices.add(new RestServiceInfo(fqn, regex, path));
                                       }
                                   }

                                   checkWsConnection();
                               }
                           })
                           .catchError(new Operation<PromiseError>() {
                               @Override
                               public void apply(PromiseError arg) throws OperationException {
                                   Log.error(getClass(), arg.getMessage());
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
        messageBus = messageBusProvider.createMachineMessageBus(devMachine.getWsAgentWebSocketUrl());
        messageBus.addOnCloseHandler(this);
        messageBus.addOnCloseHandler(this);
        messageBus.addOnOpenHandler(this);
    }
}
