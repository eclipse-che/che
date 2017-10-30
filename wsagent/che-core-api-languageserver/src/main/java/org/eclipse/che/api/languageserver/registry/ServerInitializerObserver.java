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

import org.eclipse.che.api.languageserver.launcher.LanguageServerLauncher;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.services.LanguageServer;

/** @author Anatoliy Bazko */
public interface ServerInitializerObserver {

  /**
   * Notifies observers when server is initialized and ready to use.
   *
   * @param server the {@link LanguageServer}
   * @param capabilities the supported capabilities by server
   * @param languageDescription
   * @param projectPath
   */
  void onServerInitialized(
      LanguageServerLauncher launcher,
      LanguageServer server,
      ServerCapabilities capabilities,
      String rootPath);
}
