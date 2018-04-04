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
package org.eclipse.che.plugin.json.languageserver;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.languageserver.exception.LanguageServerException;
import org.eclipse.che.api.languageserver.launcher.LanguageServerLauncher;
import org.eclipse.che.api.languageserver.launcher.LanguageServerLauncherTemplate;
import org.eclipse.che.api.languageserver.registry.DocumentFilter;
import org.eclipse.che.api.languageserver.registry.LanguageServerDescription;
import org.eclipse.che.api.languageserver.registry.ServerInitializerObserver;
import org.eclipse.che.plugin.json.inject.JsonModule;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.services.ServiceEndpoints;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;

/**
 * @author Evgen Vidolob
 * @author Anatolii Bazko
 */
@Singleton
public class JsonLanguageServerLauncher extends LanguageServerLauncherTemplate
    implements ServerInitializerObserver {
  private static final String REGEX = ".*\\.(json|bowerrc|jshintrc|jscsrc|eslintrc|babelrc)";
  private static final LanguageServerDescription DESCRIPTION = createServerDescription();

  private final Path launchScript;

  @Inject
  public JsonLanguageServerLauncher() {
    launchScript = Paths.get(System.getenv("HOME"), "che/ls-json/launch.sh");
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
      throw new LanguageServerException("Can't start JSON language server", e);
    }
  }

  @Override
  public void onServerInitialized(
      LanguageServerLauncher launcher,
      LanguageServer server,
      ServerCapabilities capabilities,
      String projectPath) {
    Endpoint endpoint = ServiceEndpoints.toEndpoint(server);
    JsonExtension serviceObject = ServiceEndpoints.toServiceObject(endpoint, JsonExtension.class);
    Map<String, String[]> associations = new HashMap<>();
    associations.put("/*.schema.json", new String[] {"http://json-schema.org/draft-04/schema#"});
    associations.put("/bower.json", new String[] {"http://json.schemastore.org/bower"});
    associations.put("/.bower.json", new String[] {"http://json.schemastore.org/bower"});
    associations.put("/.bowerrc", new String[] {"http://json.schemastore.org/bowerrc"});
    associations.put("/composer.json", new String[] {"https://getcomposer.org/schema.json"});
    associations.put("/package.json", new String[] {"http://json.schemastore.org/package"});
    associations.put("/jsconfig.json", new String[] {"http://json.schemastore.org/jsconfig"});
    associations.put("/tsconfig.json", new String[] {"http://json.schemastore.org/tsconfig"});
    serviceObject.jsonSchemaAssociation(associations);
  }

  public LanguageServerDescription getDescription() {
    return DESCRIPTION;
  }

  private static LanguageServerDescription createServerDescription() {
    LanguageServerDescription description =
        new LanguageServerDescription(
            "org.eclipse.che.plugin.json.languageserver",
            null,
            Arrays.asList(new DocumentFilter(JsonModule.LANGUAGE_ID, REGEX, null)));
    return description;
  }
}
