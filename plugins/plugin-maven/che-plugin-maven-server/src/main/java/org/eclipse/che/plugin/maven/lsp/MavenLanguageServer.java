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
package org.eclipse.che.plugin.maven.lsp;

import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

/**
 * Implementation of language server form pom.xml files. Only implements diagnostics, so far. This
 * language server runs in-process in the ws-agent.
 *
 * @author Thomas Mäder
 */
@Singleton
public class MavenLanguageServer implements LanguageServer {

  private final MavenTextDocumentService textDocumentService;

  @Inject
  public MavenLanguageServer(MavenTextDocumentService textDocumentService) {
    this.textDocumentService = textDocumentService;
  }

  @Override
  public CompletableFuture<Object> shutdown() {
    return null;
  }

  @Override
  public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
    ServerCapabilities capabilities = new ServerCapabilities();
    capabilities.setTextDocumentSync(TextDocumentSyncKind.Incremental);
    return CompletableFuture.completedFuture(new InitializeResult(capabilities));
  }

  @Override
  public WorkspaceService getWorkspaceService() {
    return null;
  }

  @Override
  public TextDocumentService getTextDocumentService() {
    return textDocumentService;
  }

  @Override
  public void exit() {}
}
