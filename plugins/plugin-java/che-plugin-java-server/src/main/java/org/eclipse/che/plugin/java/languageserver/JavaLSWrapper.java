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
package org.eclipse.che.plugin.java.languageserver;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.eclipse.che.api.languageserver.service.FileContentAccess;
import org.eclipse.che.api.languageserver.util.DynamicWrapper;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.FileEvent;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.WorkspaceClientCapabilities;
import org.eclipse.lsp4j.WorkspaceEditCapabilities;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

public class JavaLSWrapper {
  private JavaLanguageServer wrapped;
  private JavaLanguageServerLauncher launcher;

  public JavaLSWrapper(JavaLanguageServerLauncher launcher, JavaLanguageServer wrapped) {
    this.launcher = launcher;
    this.wrapped = wrapped;
  }

  public CompletableFuture<String> getFileContent(String uri) {
    return wrapped.classFileContents(new org.eclipse.lsp4j.TextDocumentIdentifier(uri));
  }

  public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
    Map<String, Object> initOptions = new HashMap<>();
    Map<String, String> extendedCapabilities = new HashMap<>();
    extendedCapabilities.put("progressReportProvider", "true");
    extendedCapabilities.put("classFileContentsSupport", "true");
    initOptions.put("extendedClientCapabilities", extendedCapabilities);

    WorkspaceClientCapabilities workspaceClientCapabilities =
        params.getCapabilities().getWorkspace();
    WorkspaceEditCapabilities workspaceEditCapabilities = new WorkspaceEditCapabilities();
    workspaceEditCapabilities.setResourceChanges(true);
    workspaceClientCapabilities.setWorkspaceEdit(workspaceEditCapabilities);

    Map<String, String> settings = new HashMap<>();
    settings.put("java.configuration.updateBuildConfiguration", "automatic");
    initOptions.put("settings", settings);
    params.setInitializationOptions(initOptions);
    return wrapped
        .initialize(params)
        .thenApply(
            result -> {
              result.getCapabilities().setDocumentSymbolProvider(false);
              result.getCapabilities().setReferencesProvider(false);
              result.getCapabilities().setRenameProvider(false);
              return result;
            });
  }

  public TextDocumentService getTextDocumentService() {
    return (TextDocumentService)
        Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class[] {TextDocumentService.class, FileContentAccess.class},
            new DynamicWrapper(
                new JavaTextDocumentServiceWraper(wrapped.getTextDocumentService()),
                wrapped.getTextDocumentService()));
  }

  public WorkspaceService getWorkspaceService() {
    return (WorkspaceService)
        Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class[] {WorkspaceService.class},
            new DynamicWrapper(this, wrapped.getWorkspaceService()));
  }

  public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
    for (FileEvent event : params.getChanges()) {
      if (event.getUri().endsWith("/pom.xml")) {
        launcher.pomChanged(event.getUri());
      }
    }
    wrapped.getWorkspaceService().didChangeWatchedFiles(params);
  }
}
