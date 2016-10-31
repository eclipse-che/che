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
package org.eclipse.che.plugin.languageserver.ide.registry;

import io.typefox.lsapi.InitializeResult;
import io.typefox.lsapi.ServerCapabilities;

import com.google.gwt.core.client.Callback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.languageserver.shared.ProjectExtensionKey;
import org.eclipse.che.api.languageserver.shared.event.LanguageServerInitializeEventDto;
import org.eclipse.che.api.languageserver.shared.lsapi.InitializeResultDTO;
import org.eclipse.che.api.languageserver.shared.model.LanguageDescription;
import org.eclipse.che.api.languageserver.shared.model.impl.InitializeResultImpl;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.callback.CallbackPromiseHelper;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateHandler;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.MessageBusProvider;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.rest.SubscriptionHandler;
import org.eclipse.che.ide.websocket.rest.Unmarshallable;
import org.eclipse.che.plugin.languageserver.ide.service.LanguageServerRegistryServiceClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.api.languageserver.shared.ProjectExtensionKey.createProjectKey;


/**
 * @author Anatoliy Bazko
 */
@Singleton
public class LanguageServerRegistry {
    private final EventBus                            eventBus;
    private final LanguageServerRegistryServiceClient client;

    private final Map<ProjectExtensionKey, InitializeResult>                      projectToInitResult;
    private final Map<ProjectExtensionKey, Callback<InitializeResult, Throwable>> callbackMap;

    @Inject
    public LanguageServerRegistry(EventBus eventBus,
                                  LanguageServerRegistryServiceClient client) {
        this.eventBus = eventBus;
        this.client = client;
        this.projectToInitResult = new HashMap<>();
        this.callbackMap = new HashMap<>();
    }

    /**
     * Registers language server description and capabilities.
     */
    protected void register(String projectPath, LanguageDescription languageDescription, ServerCapabilities capabilities) {
        InitializeResult initializeResult = new InitializeResultImpl(capabilities, languageDescription);
        for (String ext : languageDescription.getFileExtensions()) {
            ProjectExtensionKey key = createProjectKey(projectPath, ext);
            projectToInitResult.put(key, initializeResult);

            if (callbackMap.containsKey(key)) {
                Callback<InitializeResult, Throwable> callback = callbackMap.remove(key);
                callback.onSuccess(initializeResult);
            }
        }
    }

    public Promise<InitializeResult> getOrInitializeServer(String projectPath, String ext, String filePath) {
        final ProjectExtensionKey key = createProjectKey(projectPath, ext);
        if (projectToInitResult.containsKey(key)) {
            return Promises.resolve(projectToInitResult.get(key));
        } else {
            //call initialize service
            client.initializeServer(filePath);
            //wait for response
            return CallbackPromiseHelper.createFromCallback(new CallbackPromiseHelper.Call<InitializeResult, Throwable>() {
                @Override
                public void makeCall(Callback<InitializeResult, Throwable> callback) {
                    callbackMap.put(key, callback);
                }
            });
        }
    }

    @Inject
    protected void registerAllServers() {
        eventBus.addHandler(WsAgentStateEvent.TYPE, new WsAgentStateHandler() {
            @Override
            public void onWsAgentStarted(WsAgentStateEvent event) {
                Promise<List<InitializeResultDTO>> registeredLanguages = client.getRegisteredLanguages();

                registeredLanguages.then(new Operation<List<InitializeResultDTO>>() {
                    @Override
                    public void apply(List<InitializeResultDTO> initialResults) throws OperationException {
                        for (InitializeResultDTO initializeResultDTO : initialResults) {
                            for (LanguageDescription languageDescription : initializeResultDTO.getSupportedLanguages()) {
                                register(initializeResultDTO.getProject(),
                                         languageDescription,
                                         initializeResultDTO.getCapabilities());
                            }
                        }
                    }
                });
            }

            @Override
            public void onWsAgentStopped(WsAgentStateEvent event) { }
        });
    }

    @Inject
    protected void subscribeToInitializeEvent(final DtoUnmarshallerFactory unmarshallerFactory,
                                              final MessageBusProvider messageBusProvider,
                                              final NotificationManager notificationManager,
                                              final EventBus eventBus) {
        eventBus.addHandler(WsAgentStateEvent.TYPE, new WsAgentStateHandler() {
            @Override
            public void onWsAgentStarted(WsAgentStateEvent event) {
                MessageBus messageBus = messageBusProvider.getMachineMessageBus();
                Unmarshallable<LanguageServerInitializeEventDto> unmarshaller =
                        unmarshallerFactory.newWSUnmarshaller(LanguageServerInitializeEventDto.class);

                try {
                    messageBus.subscribe("languageserver", new SubscriptionHandler<LanguageServerInitializeEventDto>(unmarshaller) {
                        @Override
                        protected void onMessageReceived(LanguageServerInitializeEventDto initializeEvent) {
                            register(initializeEvent.getProjectPath(),
                                     initializeEvent.getSupportedLanguages(),
                                     initializeEvent.getServerCapabilities());
                        }

                        @Override
                        protected void onErrorReceived(Throwable exception) {
                            notificationManager.notify(exception.getMessage(),
                                                       StatusNotification.Status.FAIL,
                                                       StatusNotification.DisplayMode.NOT_EMERGE_MODE);
                        }
                    });
                } catch (WebSocketException exception) {
                    Log.error(getClass(), exception);
                }
            }

            @Override
            public void onWsAgentStopped(WsAgentStateEvent event) {
                projectToInitResult.clear();
            }
        });
    }
}
