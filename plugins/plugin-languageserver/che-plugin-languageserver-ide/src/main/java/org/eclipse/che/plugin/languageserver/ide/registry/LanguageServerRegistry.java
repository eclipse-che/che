/*
 * *****************************************************************************
 *  Copyright (c) 2012-2016 Codenvy, S.A.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *    Codenvy, S.A. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.plugin.languageserver.ide.registry;

import io.typefox.lsapi.InitializeResult;
import io.typefox.lsapi.LanguageDescription;
import io.typefox.lsapi.ServerCapabilities;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
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
import org.eclipse.che.plugin.languageserver.shared.event.LanguageServerInitializeEventDto;
import org.eclipse.che.plugin.languageserver.shared.lsapi.InitializeResultDTO;
import org.eclipse.che.plugin.languageserver.shared.model.impl.InitializeResultImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Anatoliy Bazko
 */
@Singleton
public class LanguageServerRegistry {
    private final EventBus                            eventBus;
    private final LanguageServerRegistryServiceClient languageServerRegistryServiceClient;

    private final Map<String, List<InitializeResult>> extensionToInitResult;
    private final Map<String, List<InitializeResult>> languageIdToInitResult;

    @Inject
    public LanguageServerRegistry(EventBus eventBus,
                                  LanguageServerRegistryServiceClient languageServerRegistryServiceClient) {
        this.eventBus = eventBus;
        this.languageServerRegistryServiceClient = languageServerRegistryServiceClient;
        this.extensionToInitResult = new HashMap<>();
        this.languageIdToInitResult = new HashMap<>();
    }

    /**
     * Returns serve capabilities by language id.
     */
    public List<InitializeResult> getCapabilitiesByLanguageId(String languageId) {
        List<InitializeResult> initializeResults = languageIdToInitResult.get(languageId);
        return initializeResults == null ? Collections.<InitializeResult>emptyList() : initializeResults;
    }

    /**
     * Returns serve capabilities by file extension.
     */
    public List<InitializeResult> getCapabilitiesByExtension(String ext) {
        List<InitializeResult> initializeResults = extensionToInitResult.get(ext);
        return initializeResults == null ? Collections.<InitializeResult>emptyList() : initializeResults;
    }

    /**
     * Registers language server description and capabilities.
     */
    public void register(LanguageDescription languageDescription, ServerCapabilities capabilities) {
        InitializeResult initializeResult = new InitializeResultImpl(capabilities, languageDescription);

        String languageId = languageDescription.getLanguageId();
        if (!languageIdToInitResult.containsKey(languageId)) {
            languageIdToInitResult.put(languageId, new ArrayList<InitializeResult>());
        }
        languageIdToInitResult.get(languageId).add(initializeResult);

        for (String ext : languageDescription.getFileExtensions()) {
            if (!extensionToInitResult.containsKey(ext)) {
                extensionToInitResult.put(ext, new ArrayList<InitializeResult>());
            }
            extensionToInitResult.get(ext).add(initializeResult);
        }
    }

    @Inject
    protected void registerAllServers() {
        eventBus.addHandler(WsAgentStateEvent.TYPE, new WsAgentStateHandler() {
            @Override
            public void onWsAgentStarted(WsAgentStateEvent event) {
                Promise<List<InitializeResultDTO>> registeredLanguages = languageServerRegistryServiceClient.getRegisteredLanguages();
                registeredLanguages.then(new Operation<List<InitializeResultDTO>>() {
                    @Override
                    public void apply(List<InitializeResultDTO> initialResults) throws OperationException {
                        for (InitializeResultDTO initializeResultDTO : initialResults) {
                            for (LanguageDescription languageDescription : initializeResultDTO.getSupportedLanguages()) {
                                register(languageDescription, initializeResultDTO.getCapabilities());
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
                            register(initializeEvent.getSupportedLanguages(), initializeEvent.getServerCapabilities());
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
                extensionToInitResult.clear();
                languageIdToInitResult.clear();
            }
        });
    }
}
