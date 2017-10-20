/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.java.languageserver;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import org.eclipse.che.api.languageserver.exception.LanguageServerException;
import org.eclipse.che.api.languageserver.launcher.LanguageServerLauncher;
import org.eclipse.che.api.languageserver.launcher.LanguageServerLauncherTemplate;
import org.eclipse.che.api.languageserver.registry.LanguageServerDescription;
import org.eclipse.che.api.languageserver.registry.ServerInitializerObserver;
import org.eclipse.che.api.languageserver.service.FileContentAccess;
import org.eclipse.che.api.languageserver.util.DynamicWrapper;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Evgen Vidolob
 * @author Anatolii Bazko
 * @author Thomas Mäder
 */
@Singleton
public class JavaLanguageServerLauncher extends LanguageServerLauncherTemplate
    implements ServerInitializerObserver {
  private static final Logger LOG = LoggerFactory.getLogger(JavaLanguageServerLauncher.class);

  private static final LanguageServerDescription DESCRIPTION = createServerDescription();

  private final Path launchScript;

  @Inject
  public JavaLanguageServerLauncher() {
    launchScript = Paths.get(System.getenv("HOME"), "che/ls-java/launch.sh");
  }

  @Override
  public boolean isAbleToLaunch() {
    return Files.exists(launchScript);
  }

  protected LanguageServer connectToLanguageServer(
      final Process languageServerProcess, LanguageClient client) {
    Object javaLangClient =
        Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class[] {LanguageClient.class, JavaLanguageClient.class},
            new DynamicWrapper(this, client));

    Launcher<JavaLanguageServer> launcher =
        Launcher.createLauncher(
            javaLangClient,
            JavaLanguageServer.class,
            languageServerProcess.getInputStream(),
            languageServerProcess.getOutputStream());
    launcher.startListening();
    JavaLanguageServer proxy = launcher.getRemoteProxy();
    LanguageServer wrapped =
        (LanguageServer)
            Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[] {LanguageServer.class, FileContentAccess.class},
                new DynamicWrapper(new JavaLSWrapper(proxy), proxy));
    return wrapped;
  }

  public void sendStatusReport(StatusReport report) {
    LOG.info("{}: {}", report.getType(), report.getMessage());
  }

  protected Process startLanguageServerProcess(String projectPath) throws LanguageServerException {
    ProcessBuilder processBuilder = new ProcessBuilder(launchScript.toString());
    processBuilder.directory(launchScript.getParent().toFile());
    processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE);
    processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);
    try {
      return processBuilder.start();
    } catch (IOException e) {
      throw new LanguageServerException("Can't start Java language server", e);
    }
  }

  public LanguageServerDescription getDescription() {
    return DESCRIPTION;
  }

  private static LanguageServerDescription createServerDescription() {
    LanguageServerDescription description =
        new LanguageServerDescription(
            "org.eclipse.che.plugin.java.languageserver",
            Arrays.asList("javaSource"),
            Collections.emptyList(),
            Arrays.asList(
                "glob:**/*.java",
                "glob:**/pom.xml",
                "glob:**/*.gradle",
                "glob:**/.project",
                "glob:**/.classpath",
                "glob:**/settings/*.prefs"));
    return description;
  }

  @Override
  public void onServerInitialized(
      LanguageServerLauncher launcher,
      LanguageServer server,
      ServerCapabilities capabilities,
      String projectPath) {
    Map<String, String> settings =
        Collections.singletonMap("java.configuration.updateBuildConfiguration", "automatic");
    server.getWorkspaceService().didChangeConfiguration(new DidChangeConfigurationParams(settings));
  }
}
