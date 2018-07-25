/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.languageserver;

import javax.inject.Singleton;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

/**
 * Language server wrapper extended with id and server capabilities
 *
 * @author Yevhen Vydolob
 * @author Dmytro Kulieshov
 */
@Singleton
class ExtendedLanguageServer {
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

  TextDocumentService getTextDocumentService() {
    return languageServer.getTextDocumentService();
  }

  WorkspaceService getWorkspaceService() {
    return languageServer.getWorkspaceService();
  }
}
