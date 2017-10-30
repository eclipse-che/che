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
package org.eclipse.che.plugin.csharp.languageserver;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import org.eclipse.che.api.languageserver.exception.LanguageServerException;
import org.eclipse.che.api.languageserver.launcher.LanguageServerLauncherTemplate;
import org.eclipse.che.api.languageserver.launcher.LaunchingStrategy;
import org.eclipse.che.api.languageserver.launcher.PerProjectLaunchingStrategy;
import org.eclipse.che.api.languageserver.registry.DocumentFilter;
import org.eclipse.che.api.languageserver.registry.LanguageServerDescription;
import org.eclipse.che.api.languageserver.service.LanguageServiceUtils;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.plugin.csharp.inject.CSharpModule;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;

/** @author Evgen Vidolob */
@Singleton
public class CSharpLanguageServerLauncher extends LanguageServerLauncherTemplate {
  private static final String REGEX = ".*\\.(cs|csx)";

  private static final LanguageServerDescription DESCRIPTION = createServerDescription();

  private final Path launchScript;

  @Inject
  public CSharpLanguageServerLauncher() {
    launchScript = Paths.get(System.getenv("HOME"), "che/ls-csharp/launch.sh");
  }

  @Override
  protected Process startLanguageServerProcess(String fileUri) throws LanguageServerException {
    restoreDependencies(LanguageServiceUtils.extractProjectPath(fileUri));

    ProcessBuilder processBuilder = new ProcessBuilder(launchScript.toString());
    processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE);
    processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);

    try {
      return processBuilder.start();
    } catch (IOException e) {
      throw new LanguageServerException("Can't start CSharp language server", e);
    }
  }

  @Override
  protected LaunchingStrategy createLauncherStrategy() {
    return PerProjectLaunchingStrategy.INSTANCE;
  }

  private void restoreDependencies(String projectPath) throws LanguageServerException {
    ProcessBuilder processBuilder = new ProcessBuilder("dotnet", "restore");
    processBuilder.directory(new File(LanguageServiceUtils.removeUriScheme(projectPath)));
    try {
      Process process = processBuilder.start();
      int resultCode = process.waitFor();
      if (resultCode != 0) {
        String err = IoUtil.readStream(process.getErrorStream());
        String in = IoUtil.readStream(process.getInputStream());
        throw new LanguageServerException(
            "Can't restore dependencies. Error: " + err + ". Output: " + in);
      }
    } catch (IOException | InterruptedException e) {
      throw new LanguageServerException("Can't start CSharp language server", e);
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
            "org.eclipse.che.plugin.csharp.languageserver",
            null,
            Collections.singletonList(new DocumentFilter(CSharpModule.LANGUAGE_ID, REGEX, null)));
    return description;
  }

  @Override
  public boolean isAbleToLaunch() {
    return Files.exists(launchScript);
  }
}
