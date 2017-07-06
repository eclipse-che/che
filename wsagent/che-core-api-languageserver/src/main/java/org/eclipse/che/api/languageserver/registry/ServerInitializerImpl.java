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
package org.eclipse.che.api.languageserver.registry;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.languageserver.exception.LanguageServerException;
import org.eclipse.che.api.languageserver.launcher.LanguageServerLauncher;
import org.eclipse.che.api.languageserver.shared.model.LanguageDescription;
import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

/**
 * @author Anatoliy Bazko
 */
@Singleton
public class ServerInitializerImpl implements ServerInitializer {
    private final static Logger LOG = LoggerFactory.getLogger(ServerInitializerImpl.class);

    private static final int    PROCESS_ID  = getProcessId();
    private static final String CLIENT_NAME = "EclipseChe";

    private final List<ServerInitializerObserver> observers = new ArrayList<>();

    private final ConcurrentHashMap<LanguageServer, LanguageServerDescription> serversToInitResult;

    private LanguageClient languageClient;

    @Inject
    public ServerInitializerImpl(EventService eventService) {
        this.serversToInitResult = new ConcurrentHashMap<>();

        languageClient = new LanguageClient() {

            @Override
            public void telemetryEvent(Object object) {
                // TODO Auto-generated method stub
            }

            @Override
            public CompletableFuture<Void> showMessageRequest(ShowMessageRequestParams requestParams) {
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public void showMessage(MessageParams messageParams) {
                eventService.publish(messageParams);
            }

            @Override
            public void publishDiagnostics(PublishDiagnosticsParams diagnostics) {
                eventService.publish(diagnostics);
            }

            @Override
            public void logMessage(MessageParams message) {
                LOG.error(message.getType() + " " + message.getMessage());
            }
        };
    }

    private static int getProcessId() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        int prefixEnd = name.indexOf('@');
        if (prefixEnd != -1) {
            String prefix = name.substring(0, prefixEnd);
            try {
                return Integer.parseInt(prefix);
            } catch (NumberFormatException ignored) {
            }
        }

        LOG.error("Failed to recognize the pid of the process");
        return -1;
    }

    @Override
    public void addObserver(ServerInitializerObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(ServerInitializerObserver observer) {
        observers.remove(observer);
    }

    @Override
    public LanguageServer initialize(LanguageDescription language, LanguageServerLauncher launcher, String projectPath) throws LanguageServerException {
        synchronized (launcher) {
            LanguageServer server = doInitialize(language, launcher, projectPath);
            onServerInitialized(server, serversToInitResult.get(server).getInitializeResult().getCapabilities(),
                                language, projectPath);
            return server;
        }
    }

    @Override
    public Map<LanguageServer, LanguageServerDescription> getInitializedServers() {
        return Collections.unmodifiableMap(serversToInitResult);
    }

    protected LanguageServer doInitialize(LanguageDescription language, LanguageServerLauncher launcher, String projectPath) throws LanguageServerException {
        String languageId = launcher.getLanguageId();
        InitializeParams initializeParams = prepareInitializeParams(projectPath);

        LanguageServer server;
        try {
            server = launcher.launch(projectPath, languageClient);
        } catch (LanguageServerException e) {
            throw new LanguageServerException(
                    "Can't initialize Language Server " + languageId + " on " + projectPath + ". " + e.getMessage(), e);
        }
        registerCallbacks(server, launcher);

        CompletableFuture<InitializeResult> completableFuture = server.initialize(initializeParams);
        try {
            InitializeResult initializeResult = completableFuture.get();
            serversToInitResult.put(server, new LanguageServerDescription(initializeResult, language));
        } catch (InterruptedException | ExecutionException e) {
            server.shutdown();
            server.exit();

            throw new LanguageServerException("Error fetching server capabilities " + languageId + ". " + e.getMessage(), e);
        }

        LOG.info("Initialized Language Server {} on project {}", languageId, projectPath);
        return server;
    }

    protected void registerCallbacks(LanguageServer server, LanguageServerLauncher launcher) {

        if (server instanceof ServerInitializerObserver) {
            addObserver((ServerInitializerObserver)server);
        }

        if (launcher instanceof ServerInitializerObserver) {
            addObserver((ServerInitializerObserver) launcher);
        }
    }

    protected InitializeParams prepareInitializeParams(String projectPath) {
        InitializeParams initializeParams = new InitializeParams();
        initializeParams.setProcessId(PROCESS_ID);
        initializeParams.setRootPath(projectPath);
        initializeParams.setCapabilities(new ClientCapabilities());
        initializeParams.setClientName(CLIENT_NAME);
        return initializeParams;
    }

    protected void onServerInitialized(LanguageServer server,
                                       ServerCapabilities capabilities,
                                       LanguageDescription languageDescription,
                                       String projectPath) {
        observers.forEach(observer -> observer.onServerInitialized(server, capabilities, languageDescription, projectPath));
    }

    @PreDestroy
    protected void shutdown() {
        for (LanguageServer server : serversToInitResult.keySet()) {
            server.shutdown();
            server.exit();
        }
    }

}
