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

import static java.util.stream.Collectors.toSet;

import java.util.Objects;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.languageserver.RegistryContainer.Registry;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.services.LanguageServer;

/**
 * Utility class that simplifies finding of language server instances
 *
 * @author Dmytro Kulieshov
 */
@Singleton
class FindServer {
  private final FindId findId;
  private final Registry<ServerCapabilities> serverCapabilities;
  private final Registry<LanguageServer> languageServers;

  @Inject
  FindServer(RegistryContainer registryContainer, FindId findId) {
    this.findId = findId;
    this.serverCapabilities = registryContainer.serverCapabilitiesRegistry;
    this.languageServers = registryContainer.languageServerRegistry;
  }

  /**
   * Finds initialized language server instances that correspond to a specified workspace path.
   *
   * @param wsPath absolute workspace path
   * @return set of language server instances
   */
  Set<ExtendedLanguageServer> byPath(String wsPath) {
    return findId.byPath(wsPath).stream().map(this::byId).filter(Objects::nonNull).collect(toSet());
  }

  /**
   * Finds initialized language server instance that corresponds to a specified id.
   *
   * @param id language server id
   * @return language server instance or <code>null</code> if no sever found
   */
  ExtendedLanguageServer byId(String id) {
    ServerCapabilities serverCapabilities = this.serverCapabilities.getOrNull(id);
    if (serverCapabilities == null) {
      return null;
    }

    LanguageServer languageServer = languageServers.getOrNull(id);
    if (languageServer == null) {
      return null;
    }
    return new ExtendedLanguageServer(id, serverCapabilities, languageServer);
  }
}
