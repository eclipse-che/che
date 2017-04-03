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
import com.google.gwt.regexp.shared.RegExp;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import io.typefox.lsapi.InitializeResult;
import io.typefox.lsapi.ServerCapabilities;
import org.eclipse.che.api.languageserver.shared.event.LanguageServerInitializeEventDto;
import org.eclipse.che.api.languageserver.shared.lsapi.InitializeResultDTO;
import org.eclipse.che.api.languageserver.shared.lsapi.LanguageDescriptionDTO;
import org.eclipse.che.api.languageserver.shared.model.LanguageDescription;
import org.eclipse.che.api.languageserver.shared.model.impl.InitializeResultImpl;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.api.promises.client.callback.CallbackPromiseHelper;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateHandler;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.util.Pair;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.MessageBusProvider;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.rest.SubscriptionHandler;
import org.eclipse.che.ide.websocket.rest.Unmarshallable;
import org.eclipse.che.plugin.languageserver.ide.service.LanguageServerRegistryServiceClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Anatoliy Bazko
 */
@Singleton
public class LanguageServerRegistry {
    private final EventBus eventBus;
    private final LanguageServerRegistryServiceClient client;

    private final Map<Pair<String, LanguageDescription>, InitializeResult> projectToInitResult;
    private final Map<Pair<String, LanguageDescription>, Callback<InitializeResult, Throwable>> callbackMap;
    private List<LanguageDescriptionDTO> supportedLanguages = new ArrayList<LanguageDescriptionDTO>();
    private PromiseProvider promiseProvider;

    @Inject
    public LanguageServerRegistry(EventBus eventBus, LanguageServerRegistryServiceClient client, PromiseProvider promiseProvider) {
        this.eventBus = eventBus;
        this.client = client;
        this.projectToInitResult = new HashMap<>();
        this.callbackMap = new HashMap<>();
        this.promiseProvider = promiseProvider;
    }

    /**
     * Registers language server description and capabilities.
     */
    protected void register(String projectPath, LanguageDescription languageDescription, ServerCapabilities capabilities) {
        InitializeResult initializeResult = new InitializeResultImpl(capabilities, languageDescription);
        Pair<String, LanguageDescription> key = Pair.of(projectPath, languageDescription);
        projectToInitResult.put(key, initializeResult);

        if (callbackMap.containsKey(key)) {
            Callback<InitializeResult, Throwable> callback = callbackMap.remove(key);
            callback.onSuccess(initializeResult);
        }
    }

    public Promise<InitializeResult> getOrInitializeServer(String projectPath, String ext, String filePath) {
        LanguageDescription languageDescription = findLanguageDescription(filePath);
        if (languageDescription != null) {
            Pair<String, LanguageDescription> key = Pair.of(projectPath, languageDescription);
            if (projectToInitResult.containsKey(key)) {
                return promiseProvider.resolve(projectToInitResult.get(key));
            } else {
                // call initialize service
                client.initializeServer(filePath);
                // wait for response
                return CallbackPromiseHelper.createFromCallback(new CallbackPromiseHelper.Call<InitializeResult, Throwable>() {
                    @Override
                    public void makeCall(Callback<InitializeResult, Throwable> callback) {
                        callbackMap.put(key, callback);
                    }
                });
            }
        }
        return promiseProvider.resolve(null);
    }

    private LanguageDescription findLanguageDescription(String filePath) {
        List<LanguageDescriptionDTO> langs = new ArrayList<>(supportedLanguages);
        for (LanguageDescriptionDTO lannguageDescription : langs) {
            if (matchByPattern(lannguageDescription, filePath)) {
                return lannguageDescription;
            }
        }
        for (LanguageDescriptionDTO lannguageDescription : langs) {
            if (matchByExtension(lannguageDescription, filePath)) {
                return lannguageDescription;
            }
        }

        return null;

    }

    private boolean matchByExtension(LanguageDescriptionDTO languageDescription, String path) {
        String fileExtension = getFileExtension(path);

        List<String> fileExtensions = languageDescription.getFileExtensions();
        for (String ext : fileExtensions) {
            if (ext.equals(fileExtension)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchByPattern(LanguageDescriptionDTO languageDescription, String path) {
        List<String> patterns = languageDescription.getFileNamePatterns();
        if (patterns != null) {
            String name = getFileName(path);
            for (String p : patterns) {
                RegExp pattern = RegExp.compile(p);
                if (pattern.test(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String getFileExtension(String path) {
        return suffixAfter(path, '.');
    }

    private String getFileName(String path) {
        return suffixAfter(path, '/');
    }

    private String suffixAfter(String path, char c) {
        int lastPos = path.lastIndexOf(c);
        if (lastPos < 0) {
            return path;
        } else if (lastPos == path.length() - 1) {
            return "";
        } else {
            return path.substring(lastPos + 1);
        }
    }

    @Inject
    protected void registerAllServers() {
        eventBus.addHandler(WsAgentStateEvent.TYPE, new WsAgentStateHandler() {
            @Override
            public void onWsAgentStarted(WsAgentStateEvent event) {
                client.getSupportedLanguages().then(new Operation<List<LanguageDescriptionDTO>>() {

                    @Override
                    public void apply(List<LanguageDescriptionDTO> arg) throws OperationException {
                        supportedLanguages.addAll(arg);
                    }
                }).catchError(err -> {

                });

                Promise<List<InitializeResultDTO>> registeredLanguages = client.getRegisteredLanguages();

                registeredLanguages.then(new Operation<List<InitializeResultDTO>>() {
                    @Override
                    public void apply(List<InitializeResultDTO> initialResults) throws OperationException {
                        for (InitializeResultDTO initializeResultDTO : initialResults) {
                            for (LanguageDescription languageDescription : initializeResultDTO.getSupportedLanguages()) {
                                register(initializeResultDTO.getProject(), languageDescription, initializeResultDTO.getCapabilities());
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
    protected void subscribeToInitializeEvent(final DtoUnmarshallerFactory unmarshallerFactory, final MessageBusProvider messageBusProvider,
                    final NotificationManager notificationManager, final EventBus eventBus) {
        eventBus.addHandler(WsAgentStateEvent.TYPE, new WsAgentStateHandler() {
            @Override
            public void onWsAgentStarted(WsAgentStateEvent event) {
                MessageBus messageBus = messageBusProvider.getMachineMessageBus();
                Unmarshallable<LanguageServerInitializeEventDto> unmarshaller = unmarshallerFactory
                                .newWSUnmarshaller(LanguageServerInitializeEventDto.class);

                try {
                    messageBus.subscribe("languageserver", new SubscriptionHandler<LanguageServerInitializeEventDto>(unmarshaller) {
                        @Override
                        protected void onMessageReceived(LanguageServerInitializeEventDto initializeEvent) {
                            register(initializeEvent.getProjectPath(), initializeEvent.getSupportedLanguages(),
                                            initializeEvent.getServerCapabilities());
                        }

                        @Override
                        protected void onErrorReceived(Throwable exception) {
                            notificationManager.notify(exception.getMessage(), StatusNotification.Status.FAIL,
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
