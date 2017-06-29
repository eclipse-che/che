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
import org.eclipse.lsp4j.CodeActionCapabilities;
import org.eclipse.lsp4j.CodeLensCapabilities;
import org.eclipse.lsp4j.CompletionCapabilities;
import org.eclipse.lsp4j.CompletionItemCapabilities;
import org.eclipse.lsp4j.DefinitionCapabilities;
import org.eclipse.lsp4j.DidChangeConfigurationCapabilities;
import org.eclipse.lsp4j.DidChangeWatchedFilesCapabilities;
import org.eclipse.lsp4j.DocumentHighlightCapabilities;
import org.eclipse.lsp4j.DocumentLinkCapabilities;
import org.eclipse.lsp4j.DocumentSymbolCapabilities;
import org.eclipse.lsp4j.ExecuteCommandCapabilities;
import org.eclipse.lsp4j.FormattingCapabilities;
import org.eclipse.lsp4j.HoverCapabilities;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.OnTypeFormattingCapabilities;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.RangeFormattingCapabilities;
import org.eclipse.lsp4j.ReferencesCapabilities;
import org.eclipse.lsp4j.RenameCapabilities;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.SignatureHelpCapabilities;
import org.eclipse.lsp4j.SymbolCapabilities;
import org.eclipse.lsp4j.SynchronizationCapabilities;
import org.eclipse.lsp4j.TextDocumentClientCapabilities;
import org.eclipse.lsp4j.WorkspaceClientCapabilities;
import org.eclipse.lsp4j.WorkspaceEditCapabilities;
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

    public static ClientCapabilities CLIENT_CAPABILITIES;

    private final List<ServerInitializerObserver> observers = new ArrayList<>();

    private final ConcurrentHashMap<String, LanguageServer>                    languageIdToServers;
    private final ConcurrentHashMap<LanguageServer, LanguageServerDescription> serversToInitResult;

    private LanguageClient languageClient;

    @Inject
    public ServerInitializerImpl(EventService eventService) {
        this.languageIdToServers = new ConcurrentHashMap<>();
        this.serversToInitResult = new ConcurrentHashMap<>();

        languageClient = new LanguageClient() {

            @Override
            public void telemetryEvent(Object object) {
                // TODO Auto-generated method stub
            }

            @Override
            public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams requestParams) {
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
    public LanguageServer initialize(LanguageServerLauncher launcher, String projectPath) throws LanguageServerException {
        String languageId = launcher.getLanguageDescription().getLanguageId();

        synchronized (launcher) {
            LanguageServer server = languageIdToServers.get(languageId);
            if (server != null) {
                server = doInitialize(launcher, projectPath);
            } else {
                server = doInitialize(launcher, projectPath);
                languageIdToServers.put(languageId, server);
            }
            onServerInitialized(server, serversToInitResult.get(server).getInitializeResult().getCapabilities(),
                                launcher.getLanguageDescription(), projectPath);
            return server;
        }
    }

    @Override
    public Map<LanguageServer, LanguageServerDescription> getInitializedServers() {
        return Collections.unmodifiableMap(serversToInitResult);
    }

    protected LanguageServer doInitialize(LanguageServerLauncher launcher, String projectPath) throws LanguageServerException {
        String languageId = launcher.getLanguageDescription().getLanguageId();
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
            serversToInitResult.put(server, new LanguageServerDescription(initializeResult, launcher.getLanguageDescription()));
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
            addObserver((ServerInitializerObserver)launcher);
        }
    }

    protected InitializeParams prepareInitializeParams(String projectPath) {
        InitializeParams initializeParams = new InitializeParams();
        initializeParams.setProcessId(PROCESS_ID);
        initializeParams.setRootPath(projectPath);

        if (CLIENT_CAPABILITIES == null) {
            CLIENT_CAPABILITIES = new ClientCapabilities();
            WorkspaceClientCapabilities workspace = new WorkspaceClientCapabilities();
            workspace.setApplyEdit(false); //Change when support added
            workspace.setDidChangeConfiguration(new DidChangeConfigurationCapabilities(false));
            workspace.setDidChangeWatchedFiles(new DidChangeWatchedFilesCapabilities(false));
            workspace.setExecuteCommand(new ExecuteCommandCapabilities(false));
            workspace.setSymbol(new SymbolCapabilities(false));
            workspace.setWorkspaceEdit(new WorkspaceEditCapabilities(false));
            CLIENT_CAPABILITIES.setWorkspace(workspace);

            TextDocumentClientCapabilities textDocument = new TextDocumentClientCapabilities();
            textDocument.setCodeAction(new CodeActionCapabilities(false));
            textDocument.setCodeLens(new CodeLensCapabilities(false));
            textDocument.setCompletion(new CompletionCapabilities(new CompletionItemCapabilities(true)));
            textDocument.setDefinition(new DefinitionCapabilities(false));
            textDocument.setDocumentHighlight(new DocumentHighlightCapabilities(true));
            textDocument.setDocumentLink(new DocumentLinkCapabilities());
            textDocument.setDocumentSymbol(new DocumentSymbolCapabilities());
            textDocument.setFormatting(new FormattingCapabilities());
            textDocument.setHover(new HoverCapabilities());
            textDocument.setOnTypeFormatting(new OnTypeFormattingCapabilities());
            textDocument.setRangeFormatting(new RangeFormattingCapabilities());
            textDocument.setReferences(new ReferencesCapabilities());
            textDocument.setRename(new RenameCapabilities());
            textDocument.setSignatureHelp(new SignatureHelpCapabilities());
            textDocument.setSynchronization(new SynchronizationCapabilities(true, false, true));
            CLIENT_CAPABILITIES.setTextDocument(textDocument);
        }
        initializeParams.setCapabilities(CLIENT_CAPABILITIES);
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
