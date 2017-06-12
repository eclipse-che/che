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
import org.eclipse.che.commons.lang.Pair;
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

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author Anatoliy Bazko
 */
@Singleton
public class ServerInitializerImpl implements ServerInitializer {
    private final static Logger LOG = LoggerFactory.getLogger(ServerInitializerImpl.class);

    private static final int    PROCESS_ID  = getProcessId();
    private static final String CLIENT_NAME = "EclipseChe";

    private final List<ServerInitializerObserver> observers = new ArrayList<>();

    private LanguageClient languageClient;

    @Inject
    public ServerInitializerImpl(EventService eventService) {

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
    public CompletableFuture<Pair<LanguageServer, InitializeResult>> initialize(LanguageServerLauncher launcher, String projectPath)
                    throws LanguageServerException {
        InitializeParams initializeParams = prepareInitializeParams(projectPath);
        String launcherId = launcher.getDescription().getId();
        CompletableFuture<Pair<LanguageServer, InitializeResult>> result= new CompletableFuture<Pair<LanguageServer,InitializeResult>>();

        LanguageServer server;
        try {
            server = launcher.launch(projectPath, languageClient);
        } catch (LanguageServerException e) {
            result.completeExceptionally(new LanguageServerException(
                            "Can't initialize Language Server " + launcherId + " on " + projectPath + ". " + e.getMessage(), e));
            return result;
        }
        registerCallbacks(server, launcher);

        CompletableFuture<InitializeResult> completableFuture = server.initialize(initializeParams);
        completableFuture.thenAccept((InitializeResult res) -> {
            onServerInitialized(launcher, server, res.getCapabilities(), projectPath);
            result.complete(Pair.of(server, res));
            LOG.info("Initialized Language Server {} on project {}", launcherId, projectPath);
        }).exceptionally((Throwable e) -> {
            server.shutdown();
            server.exit();
            result.completeExceptionally(e);
            return null;
        });
        return result;
    }

    protected void registerCallbacks(LanguageServer server, LanguageServerLauncher launcher) {
        if (server instanceof ServerInitializerObserver) {
            addObserver((ServerInitializerObserver) server);
        }

        if (launcher instanceof ServerInitializerObserver) {
            addObserver((ServerInitializerObserver) launcher);
        }
    }

    private InitializeParams prepareInitializeParams(String projectPath) {
        InitializeParams initializeParams = new InitializeParams();
        initializeParams.setProcessId(PROCESS_ID);
        initializeParams.setRootPath(projectPath);
        initializeParams.setCapabilities(new ClientCapabilities());
        initializeParams.setClientName(CLIENT_NAME);
        return initializeParams;
    }

    private void onServerInitialized(LanguageServerLauncher launcher, LanguageServer server, ServerCapabilities capabilities, String projectPath) {
        observers.forEach(observer -> observer.onServerInitialized(launcher, server, capabilities   , projectPath));
    }
}
