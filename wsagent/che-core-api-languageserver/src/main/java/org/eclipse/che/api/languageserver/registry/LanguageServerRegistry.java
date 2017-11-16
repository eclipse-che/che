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

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import org.eclipse.che.api.languageserver.exception.LanguageServerException;
import org.eclipse.che.api.languageserver.shared.model.LanguageDescription;
import org.eclipse.lsp4j.ServerCapabilities;

/** @author Anatoliy Bazko */
public interface LanguageServerRegistry extends Observable<ServerInitializerObserver> {
  /**
   * Finds appropriate language servers according to file uri.
   *
   * @throws LanguageServerException
   */
  List<Collection<InitializedLanguageServer>> getApplicableLanguageServers(String fileUri)
      throws LanguageServerException;

  /** Returns all available servers. */
  List<LanguageDescription> getSupportedLanguages();

  /**
   * Initialize the language servers that apply to this file
   *
   * @param fileUri
   * @return
   * @throws LanguageServerException
   */
  ServerCapabilities initialize(String fileUri) throws LanguageServerException;

  ServerCapabilities getCapabilities(String fileUri) throws LanguageServerException;

  InitializedLanguageServer getServer(String id);

  Optional<InitializedLanguageServer> findServer(Predicate<InitializedLanguageServer> condition);
}
