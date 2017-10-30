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

import java.io.IOException;
import java.util.List;
import org.eclipse.che.api.languageserver.exception.LanguageServerException;
import org.eclipse.che.api.languageserver.launcher.LanguageServerLauncherTemplate;
import org.eclipse.che.api.languageserver.registry.LanguageServerDescription;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;

/** */
public class LocalTestLSLauncher extends LanguageServerLauncherTemplate {

  private final LanguageServerDescription description;
  private final List<String> command;

  public LocalTestLSLauncher(List<String> command, LanguageServerDescription description) {
    this.command = command;
    this.description = description;
  }

  @Override
  protected Process startLanguageServerProcess(String fileUri) throws LanguageServerException {
    ProcessBuilder processBuilder = new ProcessBuilder(command);
    processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE);
    processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);
    try {
      return processBuilder.start();
    } catch (IOException e) {
      throw new LanguageServerException("Can't start TypeScript language server", e);
    }
  }

  @Override
  protected LanguageServer connectToLanguageServer(
      Process languageServerProcess, LanguageClient client) throws LanguageServerException {
    Launcher<LanguageServer> launcher =
        Launcher.createLauncher(
            client,
            LanguageServer.class,
            languageServerProcess.getInputStream(),
            languageServerProcess.getOutputStream());
    launcher.startListening();
    return launcher.getRemoteProxy();
  }

  @Override
  public LanguageServerDescription getDescription() {
    return description;
  }

  @Override
  public boolean isAbleToLaunch() {
    return true;
  }
}
