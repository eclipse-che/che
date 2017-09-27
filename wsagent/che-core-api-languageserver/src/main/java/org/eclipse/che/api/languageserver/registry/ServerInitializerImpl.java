/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.languageserver.registry;

import com.google.inject.Singleton;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.eclipse.che.api.languageserver.exception.LanguageServerException;
import org.eclipse.che.api.languageserver.launcher.LanguageServerLauncher;
import org.eclipse.che.api.languageserver.service.LanguageServiceUtils;
import org.eclipse.che.commons.lang.Pair;
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
import org.eclipse.lsp4j.OnTypeFormattingCapabilities;
import org.eclipse.lsp4j.RangeFormattingCapabilities;
import org.eclipse.lsp4j.ReferencesCapabilities;
import org.eclipse.lsp4j.RenameCapabilities;
import org.eclipse.lsp4j.ServerCapabilities;
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

/** @author Anatoliy Bazko */
@Singleton
public class ServerInitializerImpl implements ServerInitializer {
  private static final Logger LOG = LoggerFactory.getLogger(ServerInitializerImpl.class);

  private static final int PROCESS_ID = getProcessId();

  public static ClientCapabilities CLIENT_CAPABILITIES;

  private final List<ServerInitializerObserver> observers = new ArrayList<>();

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
  public CompletableFuture<Pair<LanguageServer, InitializeResult>> initialize(
      LanguageServerLauncher launcher, LanguageClient client, String projectPath)
      throws LanguageServerException {
    InitializeParams initializeParams = prepareInitializeParams(launcher, projectPath);
    String launcherId = launcher.getDescription().getId();
    CompletableFuture<Pair<LanguageServer, InitializeResult>> result =
        new CompletableFuture<Pair<LanguageServer, InitializeResult>>();

    long threadId = Thread.currentThread().getId();
    LanguageServer server;
    try {
      server = launcher.launch(projectPath, client);
      LOG.info(
          "Launched language server {} on thread {} and project {}",
          launcherId,
          threadId,
          projectPath);
    } catch (LanguageServerException e) {
      result.completeExceptionally(
          new LanguageServerException(
              "Can't initialize Language Server "
                  + launcherId
                  + " on "
                  + projectPath
                  + ". "
                  + e.getMessage(),
              e));
      return result;
    }
    registerCallbacks(server, launcher);

    LOG.info(
        "Initializing language server {} on thread {} and project {}",
        launcherId,
        threadId,
        projectPath);

    CompletableFuture<InitializeResult> completableFuture = server.initialize(initializeParams);
    completableFuture
        .thenAccept(
            (InitializeResult res) -> {
              LOG.info(
                  "Initialized language server {} on thread {} and project {}",
                  launcherId,
                  threadId,
                  projectPath);
              onServerInitialized(launcher, server, res.getCapabilities(), projectPath);
              result.complete(Pair.of(server, res));
            })
        .exceptionally(
            (Throwable e) -> {
              server.shutdown();
              server.exit(); // TODO: WAIT FOR SHUTDOWN TO COMPLETE
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

  private InitializeParams prepareInitializeParams(
      LanguageServerLauncher launcher, String projectPath) {
    InitializeParams initializeParams = new InitializeParams();

    if (launcher.isLocal()) {
      initializeParams.setProcessId(PROCESS_ID);
    } else {
      initializeParams.setProcessId(null);
    }

    initializeParams.setRootPath(LanguageServiceUtils.removeUriScheme(projectPath));
    initializeParams.setRootUri(projectPath);

    if (CLIENT_CAPABILITIES == null) {
      CLIENT_CAPABILITIES = new ClientCapabilities();
      WorkspaceClientCapabilities workspace = new WorkspaceClientCapabilities();
      workspace.setApplyEdit(false); // Change when support added
      workspace.setDidChangeConfiguration(new DidChangeConfigurationCapabilities());
      workspace.setDidChangeWatchedFiles(new DidChangeWatchedFilesCapabilities());
      workspace.setExecuteCommand(new ExecuteCommandCapabilities());
      workspace.setSymbol(new SymbolCapabilities());
      workspace.setWorkspaceEdit(new WorkspaceEditCapabilities());
      CLIENT_CAPABILITIES.setWorkspace(workspace);

      TextDocumentClientCapabilities textDocument = new TextDocumentClientCapabilities();
      textDocument.setCodeAction(new CodeActionCapabilities(false));
      textDocument.setCodeLens(new CodeLensCapabilities(false));
      textDocument.setCompletion(new CompletionCapabilities(new CompletionItemCapabilities(false)));
      textDocument.setDefinition(new DefinitionCapabilities(false));
      textDocument.setDocumentHighlight(new DocumentHighlightCapabilities(false));
      textDocument.setDocumentLink(new DocumentLinkCapabilities(false));
      textDocument.setDocumentSymbol(new DocumentSymbolCapabilities(false));
      textDocument.setFormatting(new FormattingCapabilities(false));
      textDocument.setHover(new HoverCapabilities(false));
      textDocument.setOnTypeFormatting(new OnTypeFormattingCapabilities(false));
      textDocument.setRangeFormatting(new RangeFormattingCapabilities(false));
      textDocument.setReferences(new ReferencesCapabilities(false));
      textDocument.setRename(new RenameCapabilities(false));
      textDocument.setSignatureHelp(new SignatureHelpCapabilities(false));
      textDocument.setSynchronization(new SynchronizationCapabilities(true, false, true));
      CLIENT_CAPABILITIES.setTextDocument(textDocument);
    }
    initializeParams.setCapabilities(CLIENT_CAPABILITIES);
    return initializeParams;
  }

  private void onServerInitialized(
      LanguageServerLauncher launcher,
      LanguageServer server,
      ServerCapabilities capabilities,
      String projectPath) {
    observers.forEach(
        observer -> observer.onServerInitialized(launcher, server, capabilities, projectPath));
  }
}
