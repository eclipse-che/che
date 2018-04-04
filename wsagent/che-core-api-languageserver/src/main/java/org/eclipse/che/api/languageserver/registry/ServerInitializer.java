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

import java.util.concurrent.CompletableFuture;
import org.eclipse.che.api.languageserver.exception.LanguageServerException;
import org.eclipse.che.api.languageserver.launcher.LanguageServerLauncher;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;

/**
 * Is responsible to start new {@link LanguageServer}.
 *
 * @author Anatoliy Bazko
 */
public interface ServerInitializer extends ServerInitializerObservable {
  /**
   * Initialize new {@link LanguageServer} with given project path.
   *
   * @return
   */
  CompletableFuture<Pair<LanguageServer, InitializeResult>> initialize(
      LanguageServerLauncher launcher, LanguageClient client, String projectPath)
      throws LanguageServerException;
}
