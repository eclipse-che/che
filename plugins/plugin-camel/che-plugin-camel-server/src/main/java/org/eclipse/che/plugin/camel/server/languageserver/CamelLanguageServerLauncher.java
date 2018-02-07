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
package org.eclipse.che.plugin.camel.server.languageserver;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import javax.inject.Named;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.languageserver.exception.LanguageServerException;
import org.eclipse.che.api.languageserver.launcher.LanguageServerLauncher;
import org.eclipse.che.api.languageserver.launcher.LanguageServerLauncherTemplate;
import org.eclipse.che.api.languageserver.registry.DocumentFilter;
import org.eclipse.che.api.languageserver.registry.LanguageServerDescription;
import org.eclipse.che.api.languageserver.registry.ServerInitializerObserver;
import org.eclipse.che.plugin.camel.server.inject.CamelModule;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;

/** Launcher for Apache Camel Language Server */
@Singleton
public class CamelLanguageServerLauncher extends LanguageServerLauncherTemplate
    implements ServerInitializerObserver {

  private static final String REGEX = ".*\\.(xml)";
  private static final LanguageServerDescription DESCRIPTION = createServerDescription();
  private static LanguageServer camelLanguageServer;
  private final Path launchScript;

  @Inject
  public CamelLanguageServerLauncher(
      @Named("che.api") String apiUrl, HttpJsonRequestFactory requestFactory) {
    launchScript = Paths.get(System.getenv("HOME"), "che/ls-camel/launch.sh");
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
    setCamelLanguageServer(launcher.getRemoteProxy());
    return launcher.getRemoteProxy();
  }

  protected Process startLanguageServerProcess(String projectPath) throws LanguageServerException {
    ProcessBuilder processBuilder = new ProcessBuilder(launchScript.toString());
    processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE);
    processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);
    try {
      return processBuilder.start();
    } catch (IOException e) {
      throw new LanguageServerException("Can't start Apache Camel language server", e);
    }
  }

  protected static LanguageServer getCamelLanguageServer() {
    return camelLanguageServer;
  }

  protected static void setCamelLanguageServer(LanguageServer camelLanguageServer) {
    CamelLanguageServerLauncher.camelLanguageServer = camelLanguageServer;
  }

  public LanguageServerDescription getDescription() {
    return DESCRIPTION;
  }

  private static LanguageServerDescription createServerDescription() {
    LanguageServerDescription description =
        new LanguageServerDescription(
            "org.eclipse.che.plugin.camel.server.languageserver",
            null,
            Arrays.asList(new DocumentFilter(CamelModule.LANGUAGE_ID, REGEX, null)));
    return description;
  }

  @Override
  public void onServerInitialized(
      LanguageServerLauncher arg0, LanguageServer arg1, ServerCapabilities arg2, String arg3) {
    // nothing to do
  }
}
