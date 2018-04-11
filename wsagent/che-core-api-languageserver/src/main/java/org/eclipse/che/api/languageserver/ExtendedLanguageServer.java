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
package org.eclipse.che.api.languageserver;

import java.util.concurrent.CompletableFuture;
import javax.inject.Singleton;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.InitializedParams;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

/** Language server wrapper extended with id and server capabilities */
@Singleton
class ExtendedLanguageServer implements LanguageServer {
  private final String id;
  private final ServerCapabilities serverCapabilities;
  private final LanguageServer languageServer;

  ExtendedLanguageServer(
      String id, ServerCapabilities serverCapabilities, LanguageServer languageServer) {
    this.id = id;
    this.serverCapabilities = serverCapabilities;
    this.languageServer = languageServer;
  }

  String getId() {
    return id;
  }

  ServerCapabilities getCapabilities() {
    return serverCapabilities;
  }

  @Override
  public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
    return languageServer.initialize(params);
  }

  @Override
  public void initialized(InitializedParams params) {
    languageServer.initialized(params);
  }

  @Override
  public void initialized() {
    languageServer.initialized();
  }

  @Override
  public CompletableFuture<Object> shutdown() {
    return languageServer.shutdown();
  }

  @Override
  public void exit() {
    languageServer.exit();
  }

  @Override
  public TextDocumentService getTextDocumentService() {
    return languageServer.getTextDocumentService();
  }

  @Override
  public WorkspaceService getWorkspaceService() {
    return languageServer.getWorkspaceService();
  }
}
