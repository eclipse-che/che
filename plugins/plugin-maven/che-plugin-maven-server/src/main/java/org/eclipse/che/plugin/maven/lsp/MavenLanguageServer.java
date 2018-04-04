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
import org.eclipse.che.plugin.maven.server.core.reconcile.PomReconciler;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

/**
 * Implementation of language server form pom.xml files. Only implements diagnostics, so far. This
 * language server runs in-process in the ws-agent.
 *
 * @author Thomas Mäder
 */
public class MavenLanguageServer implements LanguageServer {
  private MavenTextDocumentService textDocumentService;
  private PomReconciler reconciler;

  public MavenLanguageServer(LanguageClient client, PomReconciler reconciler) {
    this.textDocumentService = new MavenTextDocumentService(reconciler);
    this.reconciler = reconciler;
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

  public void reconcile(String pomPath, String projectPath) {
    reconciler.reconcilePath(pomPath, projectPath);
  }
}
