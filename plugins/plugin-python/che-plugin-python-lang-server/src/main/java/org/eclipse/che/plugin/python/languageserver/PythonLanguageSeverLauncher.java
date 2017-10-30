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
package org.eclipse.che.plugin.python.languageserver;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import javax.inject.Singleton;
import org.eclipse.che.api.languageserver.exception.LanguageServerException;
import org.eclipse.che.api.languageserver.launcher.LanguageServerLauncherTemplate;
import org.eclipse.che.api.languageserver.registry.DocumentFilter;
import org.eclipse.che.api.languageserver.registry.LanguageServerDescription;
import org.eclipse.che.plugin.python.shared.ProjectAttributes;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;

/** Launches language server for Python */
@Singleton
public class PythonLanguageSeverLauncher extends LanguageServerLauncherTemplate {

  private static final LanguageServerDescription DESCRIPTION = createServerDescription();
  private static final String REGEX = ".*\\.py";

  private final Path launchScript;

  public PythonLanguageSeverLauncher() {
    launchScript = Paths.get(System.getenv("HOME"), "che/ls-python/launch.sh");
  }

  @Override
  public boolean isAbleToLaunch() {
    return launchScript.toFile().exists();
  }

  @Override
  protected Process startLanguageServerProcess(String fileUri) throws LanguageServerException {
    ProcessBuilder processBuilder = new ProcessBuilder(launchScript.toString());
    processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE);
    processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);

    try {
      return processBuilder.start();
    } catch (IOException e) {
      throw new LanguageServerException("Can't start Python language server", e);
    }
  }

  @Override
  protected LanguageServer connectToLanguageServer(
      final Process languageServerProcess, LanguageClient client) {
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
    return DESCRIPTION;
  }

  private static LanguageServerDescription createServerDescription() {
    LanguageServerDescription description =
        new LanguageServerDescription(
            "org.eclipse.che.plugin.python.languageserver",
            null,
            Arrays.asList(new DocumentFilter(ProjectAttributes.PYTHON_ID, REGEX, null)));
    return description;
  }
}
