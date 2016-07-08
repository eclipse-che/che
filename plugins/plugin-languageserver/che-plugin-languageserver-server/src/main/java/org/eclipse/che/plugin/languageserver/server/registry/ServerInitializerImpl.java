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
package org.eclipse.che.plugin.languageserver.server.registry;

import io.typefox.lsapi.InitializeParamsImpl;
import io.typefox.lsapi.InitializeResult;
import io.typefox.lsapi.LanguageDescription;
import io.typefox.lsapi.ServerCapabilities;
import io.typefox.lsapi.services.LanguageServer;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.plugin.languageserver.server.exception.LanguageServerException;
import org.eclipse.che.plugin.languageserver.server.factory.LanguageServerFactory;
import org.eclipse.che.plugin.languageserver.server.messager.PublishDiagnosticsParamsMessenger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * @author Anatoliy Bazko
 */
@Singleton
public class ServerInitializerImpl implements ServerInitializer {
    private final static Logger LOG = LoggerFactory.getLogger(ServerInitializerImpl.class);

    private static final int    PROCESS_ID  = getProcessId();
    private static final String CLIENT_NAME = "EclipseChe";

    private final List<ServerInitializerObserver>   observers;
    private final PublishDiagnosticsParamsMessenger publishDiagnosticsParamsMessenger;

    @Inject
    public ServerInitializerImpl(PublishDiagnosticsParamsMessenger publishDiagnosticsParamsMessenger) {
        this.observers = new ArrayList<>();
        this.publishDiagnosticsParamsMessenger = publishDiagnosticsParamsMessenger;
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
    public LanguageServer initialize(LanguageServerFactory factory, String projectPath) {
        String languageId = factory.getLanguageDescription().getLanguageId();
        InitializeParamsImpl initializeParams = prepareInitializeParams(projectPath);

        LanguageServer server;
        try {
            server = factory.create(projectPath);
        } catch (LanguageServerException e) {
            LOG.error("Can't initialize Language Server {} on {}. " + e.getMessage(), languageId, projectPath);
            return null;
        }
        registerCallbacks(server);

        CompletableFuture<InitializeResult> completableFuture = server.initialize(initializeParams);
        try {
            InitializeResult initializeResult = completableFuture.get();
            onServerInitialized(server, initializeResult.getCapabilities(), factory.getLanguageDescription());
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Error fetching server capabilities. " + e.getMessage(), e);
            onServerInitialized(server, newDto(ServerCapabilities.class), factory.getLanguageDescription());
        }

        LOG.info("Initialized Language Server {} on project {}", languageId, projectPath);
        return server;
    }

    protected void registerCallbacks(LanguageServer server) {
        server.getTextDocumentService().onPublishDiagnostics(publishDiagnosticsParamsMessenger::onEvent);
        server.getWindowService().onLogMessage(messageParams -> LOG.error(messageParams.getType() + " " + messageParams.getMessage()));
    }

    protected InitializeParamsImpl prepareInitializeParams(String projectPath) {
        InitializeParamsImpl initializeParams = new InitializeParamsImpl();
        initializeParams.setProcessId(PROCESS_ID);
        initializeParams.setRootPath(projectPath);
        initializeParams.setClientName(CLIENT_NAME);
        return initializeParams;
    }

    protected void onServerInitialized(LanguageServer server, ServerCapabilities capabilities, LanguageDescription languageDescription) {
        observers.stream().forEach(observer -> observer.onServerInitialized(server, capabilities, languageDescription));
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
}
