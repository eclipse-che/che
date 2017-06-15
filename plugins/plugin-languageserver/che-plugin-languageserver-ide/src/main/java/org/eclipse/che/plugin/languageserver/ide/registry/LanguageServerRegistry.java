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
package org.eclipse.che.plugin.languageserver.ide.registry;

import com.google.gwt.core.client.Callback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.languageserver.shared.ProjectExtensionKey;
import org.eclipse.che.api.languageserver.shared.event.LanguageServerInitializeEvent;
import org.eclipse.che.api.languageserver.shared.model.ExtendedInitializeResult;
import org.eclipse.che.api.languageserver.shared.model.LanguageDescription;
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
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;
import org.eclipse.che.ide.ui.loaders.request.MessageLoader;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.MessageBusProvider;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.rest.SubscriptionHandler;
import org.eclipse.che.ide.websocket.rest.Unmarshallable;
import org.eclipse.che.plugin.languageserver.ide.service.LanguageServerRegistryJsonRpcClient;
import org.eclipse.che.plugin.languageserver.ide.service.LanguageServerRegistryServiceClient;
import org.eclipse.lsp4j.ServerCapabilities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.api.languageserver.shared.ProjectExtensionKey.createProjectKey;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.WARNING;


/**
 * @author Anatoliy Bazko
 */
@Singleton
public class LanguageServerRegistry {
    private final EventBus                                                                eventBus;
    private final LanguageServerRegistryJsonRpcClient                                     jsonRpcClient;
    private final LanguageServerRegistryServiceClient                                     client;
    private final Map<ProjectExtensionKey, ExtendedInitializeResult>                      projectToInitResult;
    private final Map<ProjectExtensionKey, Callback<ExtendedInitializeResult, Throwable>> callbackMap;
    private       LoaderFactory                                                           loaderFactory;
    private       NotificationManager                                                     notificationManager;

    @Inject
    public LanguageServerRegistry(EventBus eventBus,
                                  LoaderFactory loaderFactory,
                                  NotificationManager notificationManager,
                                  LanguageServerRegistryJsonRpcClient jsonRpcClient,
                                  LanguageServerRegistryServiceClient client) {
        this.eventBus = eventBus;
        this.loaderFactory = loaderFactory;
        this.notificationManager = notificationManager;
        this.jsonRpcClient = jsonRpcClient;
        this.client = client;
        this.projectToInitResult = new HashMap<>();
        this.callbackMap = new HashMap<>();
    }

    /**
     * Registers language server description and capabilities.
     */
    protected void register(String projectPath, LanguageDescription languageDescription, ServerCapabilities capabilities) {
        ExtendedInitializeResult initializeResult = new ExtendedInitializeResult(projectPath, capabilities, languageDescription);
        for (String ext : languageDescription.getFileExtensions()) {
            ProjectExtensionKey key = createProjectKey(projectPath, ext);
            projectToInitResult.put(key, initializeResult);

            if (callbackMap.containsKey(key)) {
                Callback<ExtendedInitializeResult, Throwable> callback = callbackMap.remove(key);
                callback.onSuccess(initializeResult);
            }
        }
    }

    public Promise<ExtendedInitializeResult> getOrInitializeServer(String projectPath, String ext, String filePath) {
        final ProjectExtensionKey key = createProjectKey(projectPath, ext);
        if (projectToInitResult.containsKey(key)) {
            return Promises.resolve(projectToInitResult.get(key));
        } else {
            //call initialize service
            final MessageLoader loader = loaderFactory.newLoader("Initializing Language Server for " + ext);
            loader.show();

            jsonRpcClient.initializeServer(filePath)
                         .onSuccess(loader::hide)
                         .onFailure((error) -> {
                             notificationManager
                                     .notify("Failed to initialize language server for " + ext + " : ", error.getMessage(), FAIL,
                                             EMERGE_MODE);
                             loader.hide();
                         })
                         .onTimeout(() -> {
                             notificationManager
                                     .notify("Possible problem starting a language server", "The server either hangs or starts long",
                                             WARNING,
                                             EMERGE_MODE);
                             loader.hide();
                         });

            //wait for response
            return CallbackPromiseHelper.createFromCallback(callback -> callbackMap.put(key, callback));
        }
    }

    @Inject
    protected void registerAllServers() {
        eventBus.addHandler(WsAgentStateEvent.TYPE, new WsAgentStateHandler() {
            @Override
            public void onWsAgentStarted(WsAgentStateEvent event) {
                Promise<List<ExtendedInitializeResult>> registeredLanguages = client.getRegisteredLanguages();

                registeredLanguages.then(new Operation<List<ExtendedInitializeResult>>() {
                    @Override
                    public void apply(List<ExtendedInitializeResult> initialResults) throws OperationException {
                        for (ExtendedInitializeResult initializeResultDTO : initialResults) {
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
            public void onWsAgentStopped(WsAgentStateEvent event) {
            }
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
                Unmarshallable<LanguageServerInitializeEvent> unmarshaller =
                        unmarshallerFactory.newWSUnmarshaller(LanguageServerInitializeEvent.class);

                try {
                    messageBus.subscribe("languageserver", new SubscriptionHandler<LanguageServerInitializeEvent>(unmarshaller) {
                        @Override
                        protected void onMessageReceived(LanguageServerInitializeEvent initializeEvent) {
                            register(initializeEvent.getProjectPath(),
                                     initializeEvent.getSupportedLanguages(),
                                     initializeEvent.getServerCapabilities());
                        }

                        @Override
                        protected void onErrorReceived(Throwable exception) {
                            notificationManager.notify(exception.getMessage(),
                                                       FAIL,
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
