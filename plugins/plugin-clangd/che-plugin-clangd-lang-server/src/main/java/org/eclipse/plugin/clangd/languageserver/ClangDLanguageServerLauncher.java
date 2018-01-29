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
package org.eclipse.plugin.clangd.languageserver;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import org.eclipse.che.api.languageserver.exception.LanguageServerException;
import org.eclipse.che.api.languageserver.launcher.LanguageServerLauncher;
import org.eclipse.che.api.languageserver.launcher.LanguageServerLauncherTemplate;
import org.eclipse.che.api.languageserver.registry.DocumentFilter;
import org.eclipse.che.api.languageserver.registry.LanguageServerDescription;
import org.eclipse.che.api.languageserver.registry.ServerInitializerObserver;
import org.eclipse.che.api.languageserver.service.LanguageServiceUtils;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.plugin.clangd.inject.ClangModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Alexander Andrienko */
/** @author Hanno Kolvenbach */
@Singleton
public class ClangDLanguageServerLauncher extends LanguageServerLauncherTemplate
    implements ServerInitializerObserver {

  private static final LanguageServerDescription DESCRIPTION = createServerDescription();
  private static final String REGEX = ".*\\.(c|h|cc|hh|cpp|hpp|cxx|hxx|C|H|CC|HH|CPP|HPP|CXX|HXX)";
  private final Path launchScript;

  private static final Logger LOG = LoggerFactory.getLogger(ClangDLanguageServerLauncher.class);

  @Inject
  public ClangDLanguageServerLauncher() {
    launchScript = Paths.get(System.getenv("HOME"), "che/ls-clangd/launch.sh");
  }

  @Override
  public boolean isAbleToLaunch() {
    return Files.exists(launchScript);
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

  protected Process startLanguageServerProcess(String projectPath) throws LanguageServerException {

    ProcessBuilder processBuilder = new ProcessBuilder();

    processBuilder.directory(new File(LanguageServiceUtils.removeUriScheme(projectPath)));
    processBuilder.command(launchScript.toString()).inheritIO();
    processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE);

    processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);
    try {
      return processBuilder.start();
    } catch (IOException e) {
      throw new LanguageServerException("Can't start ClangD language server", e);
    }
  }

  @Override
  public void onServerInitialized(
      LanguageServerLauncher launcher,
      LanguageServer server,
      ServerCapabilities capabilities,
      String projectPath) {
    LOG.debug(projectPath);
    LOG.debug("clangd language server initialized");
  }

  @Override
  public LanguageServerDescription getDescription() {
    return DESCRIPTION;
  }

  private static LanguageServerDescription createServerDescription() {
    LanguageServerDescription description =
        new LanguageServerDescription(
            "org.eclipse.che.plugin.clangd.languageserver",
            null,
            Arrays.asList(new DocumentFilter(ClangModule.LANGUAGE_ID, REGEX, null)));
    return description;
  }
}
