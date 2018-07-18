/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.languageserver;

import static org.eclipse.che.api.fs.server.WsPathUtils.ROOT;

import com.google.inject.Singleton;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Collections;
import javax.inject.Inject;
import org.eclipse.che.api.fs.server.PathTransformer;
import org.eclipse.che.api.languageserver.RegistryContainer.Registry;
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
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.OnTypeFormattingCapabilities;
import org.eclipse.lsp4j.RangeFormattingCapabilities;
import org.eclipse.lsp4j.ReferencesCapabilities;
import org.eclipse.lsp4j.RenameCapabilities;
import org.eclipse.lsp4j.SignatureHelpCapabilities;
import org.eclipse.lsp4j.SymbolCapabilities;
import org.eclipse.lsp4j.SynchronizationCapabilities;
import org.eclipse.lsp4j.TextDocumentClientCapabilities;
import org.eclipse.lsp4j.WorkspaceClientCapabilities;
import org.eclipse.lsp4j.WorkspaceEditCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class that simplifies creation of a language server initialize parameters.
 *
 * @author Dmytro Kulieshov
 */
@Singleton
class InitializeParamsProvider {
  private static final Logger LOG = LoggerFactory.getLogger(InitializeParamsProvider.class);

  private final Registry<Boolean> localityRegistry;
  private final URI rootUri;

  @Inject
  InitializeParamsProvider(PathTransformer pathTransformer, RegistryContainer registryContainer) {
    this.rootUri = pathTransformer.transform(ROOT).toUri();
    this.localityRegistry = registryContainer.localityRegistry;
  }

  InitializeParams get(String id) throws LanguageServerException {
    InitializeParams initializeParams = new InitializeParams();
    LOG.debug("Initialize params constructing: started");

    Boolean locality = localityRegistry.get(id);
    LOG.debug("Locality: {}", locality);

    Integer processId = locality ? ProcessIdProvider.get() : null;
    initializeParams.setProcessId(processId);
    LOG.debug("Process id: {}", processId);

    String rootPath = Paths.get(rootUri).toAbsolutePath().toString();
    initializeParams.setRootPath(rootPath);
    LOG.debug("Root path: {}", rootPath);

    String rootUri = this.rootUri.toString();
    initializeParams.setRootUri(rootUri);
    LOG.debug("Root URI: {}", rootUri);

    ClientCapabilities capabilities = ClientCapabilitiesProvider.get();
    initializeParams.setCapabilities(capabilities);
    LOG.debug("Client capabilities: {}", capabilities);

    String clientName = ClientCapabilitiesProvider.CLIENT_NAME;
    initializeParams.setClientName(clientName);
    LOG.debug("Client name: {}", clientName);

    LOG.debug("Initialize params constructing: finished");
    return initializeParams;
  }

  /** Provides the id of current process */
  private static class ProcessIdProvider {
    private static Integer processId;

    private static int get() {
      if (processId == null) {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        int prefixEnd = name.indexOf('@');
        if (prefixEnd != -1) {
          String prefix = name.substring(0, prefixEnd);
          try {
            processId = Integer.parseInt(prefix);
          } catch (NumberFormatException e) {
            LOG.error("Failed to recognize the pid of the process", e);
          }
        } else {
          LOG.error("Failed to recognize the pid of the process");
        }
      }

      return processId;
    }
  }

  /** Provides client capabilities */
  private static class ClientCapabilitiesProvider {
    private static final String CLIENT_NAME = "EclipseChe";

    private static ClientCapabilities clientCapabilities;

    private static ClientCapabilities get() {
      if (clientCapabilities == null) {
        clientCapabilities = new ClientCapabilities();

        WorkspaceClientCapabilities workspace = new WorkspaceClientCapabilities();
        workspace.setApplyEdit(false);
        workspace.setDidChangeConfiguration(new DidChangeConfigurationCapabilities());
        workspace.setDidChangeWatchedFiles(new DidChangeWatchedFilesCapabilities());
        workspace.setExecuteCommand(new ExecuteCommandCapabilities());
        workspace.setSymbol(new SymbolCapabilities());
        workspace.setWorkspaceEdit(new WorkspaceEditCapabilities());
        clientCapabilities.setWorkspace(workspace);

        TextDocumentClientCapabilities textDocument = new TextDocumentClientCapabilities();
        textDocument.setCodeAction(new CodeActionCapabilities());
        textDocument.setCodeLens(new CodeLensCapabilities());
        CompletionItemCapabilities ciCaps = new CompletionItemCapabilities(true);
        ciCaps.setDocumentationFormat(Collections.singletonList(MarkupKind.MARKDOWN));
        textDocument.setCompletion(new CompletionCapabilities(ciCaps));
        textDocument.setDefinition(new DefinitionCapabilities());
        textDocument.setDocumentHighlight(new DocumentHighlightCapabilities());
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
        clientCapabilities.setTextDocument(textDocument);
      }

      return clientCapabilities;
    }
  }
}
