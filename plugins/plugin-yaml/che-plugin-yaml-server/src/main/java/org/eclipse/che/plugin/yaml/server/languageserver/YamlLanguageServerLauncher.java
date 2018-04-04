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
package org.eclipse.che.plugin.yaml.server.languageserver;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import javax.inject.Named;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.languageserver.exception.LanguageServerException;
import org.eclipse.che.api.languageserver.launcher.LanguageServerLauncher;
import org.eclipse.che.api.languageserver.launcher.LanguageServerLauncherTemplate;
import org.eclipse.che.api.languageserver.registry.DocumentFilter;
import org.eclipse.che.api.languageserver.registry.LanguageServerDescription;
import org.eclipse.che.api.languageserver.registry.ServerInitializerObserver;
import org.eclipse.che.plugin.yaml.server.inject.YamlModule;
import org.eclipse.che.plugin.yaml.shared.PreferenceHelper;
import org.eclipse.che.plugin.yaml.shared.YamlPreference;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.services.ServiceEndpoints;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Launcher for Yaml Language Server
 *
 * @author Joshua Pinkney
 */
@Singleton
public class YamlLanguageServerLauncher extends LanguageServerLauncherTemplate
    implements ServerInitializerObserver {

  private static final Logger LOG = LoggerFactory.getLogger(YamlLanguageServerLauncher.class);

  private static final String REGEX = ".*\\.(yaml|yml)";
  private static final LanguageServerDescription DESCRIPTION = createServerDescription();
  private static LanguageServer yamlLanguageServer;
  private final Path launchScript;
  private HttpJsonRequestFactory requestFactory;
  private String apiUrl;

  @Inject
  public YamlLanguageServerLauncher(
      @Named("che.api") String apiUrl, HttpJsonRequestFactory requestFactory) {
    this.apiUrl = apiUrl;
    this.requestFactory = requestFactory;
    launchScript = Paths.get(System.getenv("HOME"), "che/ls-yaml/launch.sh");
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
    setYamlLanguageServer(launcher.getRemoteProxy());
    return launcher.getRemoteProxy();
  }

  protected Process startLanguageServerProcess(String projectPath) throws LanguageServerException {
    ProcessBuilder processBuilder = new ProcessBuilder(launchScript.toString());
    processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE);
    processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);
    try {
      return processBuilder.start();
    } catch (IOException e) {
      throw new LanguageServerException("Can't start YAML language server", e);
    }
  }

  protected static LanguageServer getYamlLanguageServer() {
    return yamlLanguageServer;
  }

  protected static void setYamlLanguageServer(LanguageServer yamlLanguageServer) {
    YamlLanguageServerLauncher.yamlLanguageServer = yamlLanguageServer;
  }

  @Override
  public void onServerInitialized(
      LanguageServerLauncher launcher,
      LanguageServer server,
      ServerCapabilities capabilities,
      String projectPath) {

    try {
      Map<String, String> preferences =
          requestFactory.fromUrl(apiUrl + "/preferences").useGetMethod().request().asProperties();

      Endpoint endpoint = ServiceEndpoints.toEndpoint(server);
      YamlSchemaAssociations serviceObject =
          ServiceEndpoints.toServiceObject(endpoint, YamlSchemaAssociations.class);
      Map<String, String[]> associations =
          jsonToSchemaAssociations(preferences.get("yaml.preferences"));
      serviceObject.yamlSchemaAssociation(associations);

    } catch (ApiException | IOException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
  }

  /**
   * Converts json string to map of schema associations
   *
   * @param jsonStr The json string you would like to change to schema association
   * @return Map of schema associations
   */
  private Map<String, String[]> jsonToSchemaAssociations(String jsonStr) {
    List<YamlPreference> preferenceList = jsonToYamlPreference(jsonStr);

    if (preferenceList == null) {
      return null;
    }

    Map<String, List<String>> preferenceSchemaMap =
        PreferenceHelper.yamlPreferenceToMap(preferenceList);
    Map<String, String[]> jsonSchemaMap = new HashMap<String, String[]>();
    for (Map.Entry<String, List<String>> preferenceEntry : preferenceSchemaMap.entrySet()) {
      jsonSchemaMap.put(
          preferenceEntry.getKey(), preferenceEntry.getValue().toArray(new String[0]));
    }
    return jsonSchemaMap;
  }

  /**
   * Converts json string to yaml preference
   *
   * @param jsonStr The json string you would like to change into Yaml Preferences
   * @return List of Yaml Preferences
   */
  private List<YamlPreference> jsonToYamlPreference(String jsonStr) {

    List<YamlPreference> preferences = new ArrayList<YamlPreference>();

    Type type = new TypeToken<Map<String, String[]>>() {}.getType();
    Map<String, String[]> yamlPreferenceMap = new Gson().fromJson(jsonStr, type);

    if (yamlPreferenceMap == null || jsonStr == "") {
      return new ArrayList<YamlPreference>();
    }

    for (Map.Entry<String, String[]> yamlPreferenceMapItem : yamlPreferenceMap.entrySet()) {
      for (String url : yamlPreferenceMapItem.getValue()) {
        YamlPreference newYamlPref = new YamlPreference(url, yamlPreferenceMapItem.getKey());
        preferences.add(newYamlPref);
      }
    }

    return preferences;
  }

  public LanguageServerDescription getDescription() {
    return DESCRIPTION;
  }

  private static LanguageServerDescription createServerDescription() {
    LanguageServerDescription description =
        new LanguageServerDescription(
            "org.eclipse.che.plugin.yaml.server.languageserver",
            null,
            Arrays.asList(new DocumentFilter(YamlModule.LANGUAGE_ID, REGEX, null)));
    return description;
  }
}
