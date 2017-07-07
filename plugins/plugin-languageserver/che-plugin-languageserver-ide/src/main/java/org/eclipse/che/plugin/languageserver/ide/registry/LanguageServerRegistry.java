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

import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.languageserver.shared.ProjectLangugageKey;
import org.eclipse.che.api.languageserver.shared.event.LanguageServerInitializeEvent;
import org.eclipse.che.api.languageserver.shared.model.ExtendedInitializeResult;
import org.eclipse.che.api.languageserver.shared.model.LanguageDescription;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.api.promises.client.callback.CallbackPromiseHelper;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateHandler;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;
import org.eclipse.che.ide.ui.loaders.request.MessageLoader;
import org.eclipse.che.plugin.languageserver.ide.service.LanguageServerRegistryServiceClient;
import org.eclipse.lsp4j.ServerCapabilities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.eclipse.che.api.languageserver.shared.ProjectLangugageKey.createProjectKey;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.WARNING;


/**
 * @author Anatoliy Bazko
 */
@Singleton
public class LanguageServerRegistry {
    private final EventBus                            eventBus;
    private final LoaderFactory                             loaderFactory;
    private NotificationManager                       notificationManager;
    private final LanguageServerRegistryServiceClient client;

    private final Map<ProjectLangugageKey, ExtendedInitializeResult>                      projectToInitResult;
    private final Map<ProjectLangugageKey, Callback<ExtendedInitializeResult, Throwable>> callbackMap;
    private final Map<FileType, LanguageDescription>                                      registeredFileTypes = new ConcurrentHashMap<>();
    private final PromiseProvider    promiseProvider;
    private final RequestTransmitter requestTransmitter;
    private final FileTypeRegistry   fileTypeRegistry;

    private boolean subscribedToLanguageServerInitializationEvents = false;

    @Inject
    public LanguageServerRegistry(EventBus eventBus,
                                  LoaderFactory loaderFactory,
                                  NotificationManager notificationManager,
                                  LanguageServerRegistryServiceClient client,
                                  PromiseProvider promiseProvider,
                                  FileTypeRegistry fileTypeRegistry,
                                  RequestTransmitter requestTransmitter) {
        this.eventBus = eventBus;
        this.loaderFactory = loaderFactory;
        this.notificationManager = notificationManager;
        this.client = client;
        this.promiseProvider = promiseProvider;
        this.requestTransmitter = requestTransmitter;
        this.projectToInitResult = new HashMap<>();
        this.callbackMap = new HashMap<>();
        this.fileTypeRegistry = fileTypeRegistry;
    }

    @Inject
    protected void configureInitializeEventHandler(RequestHandlerConfigurator requestHandlerConfigurator) {
        requestHandlerConfigurator.newConfiguration()
                                  .methodName("languageServer/initialize/notify")
                                  .paramsAsDto(LanguageServerInitializeEvent.class)
                                  .noResult()
                                  .withConsumer(initializeEvent -> register(initializeEvent.getProjectPath(),
                                                                            initializeEvent.getSupportedLanguages(),
                                                                            initializeEvent.getServerCapabilities()));

    }

    /**
     * Registers language server description and capabilities.
     */
    protected void register(String projectPath, LanguageDescription languageDescription, ServerCapabilities capabilities) {
        ExtendedInitializeResult initializeResult = new ExtendedInitializeResult(projectPath, capabilities, languageDescription);
        ProjectLangugageKey key = createProjectKey(projectPath, languageDescription.getLanguageId());
            projectToInitResult.put(key, initializeResult);

            if (callbackMap.containsKey(key)) {
                Callback<ExtendedInitializeResult, Throwable> callback = callbackMap.remove(key);
                callback.onSuccess(initializeResult);
            }
        }

    public Promise<ExtendedInitializeResult> getOrInitializeServer(String projectPath, VirtualFile file) {
        subscribeToLanguageServerInitializationEventsIfNotYet();

        LanguageDescription languageDescription = getLanguageDescription(file);
        final ProjectLangugageKey key = createProjectKey(projectPath, languageDescription.getLanguageId());
        if (projectToInitResult.containsKey(key)) {
            return promiseProvider.resolve(projectToInitResult.get(key));
        } else {
            //call initialize service
            String path = file.getLocation().toString();
            final MessageLoader loader = loaderFactory.newLoader("Initializing Language Server for " + file.getName());
            loader.show();

            requestTransmitter.newRequest()
                              .endpointId("ws-agent")
                              .methodName("languageServer/initialize")
                              .paramsAsString(path)
                              .sendAndReceiveResultAsBoolean(30_000)
                              .onSuccess(loader::hide)
                              .onFailure(error -> {
                                  notificationManager
                                          .notify("Failed to initialize language server for " + path + " : ", error.getMessage(),
                                                  FAIL,
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

    private void subscribeToLanguageServerInitializationEventsIfNotYet() {
        if (subscribedToLanguageServerInitializationEvents) {
            return;
        }

        requestTransmitter.newRequest()
                          .endpointId("ws-agent")
                          .methodName("languageServer/initialize/subscribe")
                          .noParams()
                          .sendAndSkipResult();

        subscribedToLanguageServerInitializationEvents = true;
    }

    /**
     * Register a che file type and associate it with the given language
     * @param type the che file type
     * @param description the language description to associate with the file type
     */
    public void registerFileType(FileType type, LanguageDescription description) {
        fileTypeRegistry.registerFileType(type);
        registeredFileTypes.put(type, description);
    }

    /**
     * Get the language that is registered for this file. May return null if
     * none is found.
     * 
     * @param file the file in question
     * @return the langauge that is associated with the given file or <code>null</code> if not found.
     */
    public LanguageDescription getLanguageDescription(VirtualFile file) {
        FileType fileType = fileTypeRegistry.getFileTypeByFile(file);
        if (fileType == null) {
            return null;
        }
        return registeredFileTypes.get(fileType);
    }

}
