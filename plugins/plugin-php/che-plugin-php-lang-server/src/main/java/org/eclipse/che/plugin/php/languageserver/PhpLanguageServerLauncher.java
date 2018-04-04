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
package org.eclipse.che.plugin.php.languageserver;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import org.eclipse.che.api.languageserver.exception.LanguageServerException;
import org.eclipse.che.api.languageserver.launcher.LanguageServerLauncherTemplate;
import org.eclipse.che.api.languageserver.registry.DocumentFilter;
import org.eclipse.che.api.languageserver.registry.LanguageServerDescription;
import org.eclipse.che.plugin.php.inject.PhpModule;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;

/**
 * @author Evgen Vidolob
 * @author Anatolii Bazko
 * @author Kaloyan Raev
 */
@Singleton
public class PhpLanguageServerLauncher extends LanguageServerLauncherTemplate {

  private static final String REGEX = ".*\\.php";

  private final Path launchScript;

  private static final LanguageServerDescription DESCRIPTION = createServerDescription();

  @Inject
  public PhpLanguageServerLauncher() {
    this.launchScript = Paths.get(System.getenv("HOME"), "che/ls-php/launch.sh");
  }

  @Override
  public boolean isAbleToLaunch() {
    return Files.exists(launchScript);
  }

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

  protected Process startLanguageServerProcess(String projectPath) throws LanguageServerException {
    ProcessBuilder processBuilder = new ProcessBuilder(launchScript.toString());
    processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE);
    processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);
    try {
      return processBuilder.start();
    } catch (IOException e) {
      throw new LanguageServerException("Can't start PHP language server", e);
    }
  }

  @Override
  public LanguageServerDescription getDescription() {
    return DESCRIPTION;
  }

  private static LanguageServerDescription createServerDescription() {
    LanguageServerDescription description =
        new LanguageServerDescription(
            "org.eclipse.che.plugin.php.languageserver",
            null,
            Arrays.asList(new DocumentFilter(PhpModule.LANGUAGE_ID, REGEX, null)));
    return description;
  }
}
