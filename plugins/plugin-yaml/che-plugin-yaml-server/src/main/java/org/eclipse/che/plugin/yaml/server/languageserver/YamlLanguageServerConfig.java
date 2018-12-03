/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.yaml.server.languageserver;

import static java.util.Collections.emptySet;
import static java.util.Collections.singletonMap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import javax.annotation.PostConstruct;
import javax.inject.Named;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.languageserver.DefaultInstanceProvider;
import org.eclipse.che.api.languageserver.LanguageServerConfig;
import org.eclipse.che.api.languageserver.LanguageServerInitializedEvent;
import org.eclipse.che.api.languageserver.ProcessCommunicationProvider;
import org.eclipse.che.api.project.server.impl.RootDirPathProvider;
import org.eclipse.che.plugin.yaml.server.inject.YamlModule;
import org.eclipse.che.plugin.yaml.shared.PreferenceHelper;
import org.eclipse.che.plugin.yaml.shared.YamlPreference;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.eclipse.lsp4j.jsonrpc.services.ServiceEndpoints;
import org.eclipse.lsp4j.services.LanguageServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Launcher for Yaml Language Server
 *
 * @author Joshua Pinkney
 */
@Singleton
public class YamlLanguageServerConfig implements LanguageServerConfig {

  private static final Logger LOG = LoggerFactory.getLogger(YamlLanguageServerConfig.class);
  private static final String REGEX = ".*\\.(yaml|yml)";

  private static LanguageServer yamlLanguageServer;

  private final Path launchScript;
  private final EventService eventService;
  private final HttpJsonRequestFactory requestFactory;
  private final RootDirPathProvider rootDirPathProvider;
  private final String apiUrl;

  @Inject
  public YamlLanguageServerConfig(
      EventService eventService,
      @Named("che.api") String apiUrl,
      HttpJsonRequestFactory requestFactory,
      RootDirPathProvider rootDirPathProvider) {
    this.eventService = eventService;
    this.apiUrl = apiUrl;
    this.requestFactory = requestFactory;
    this.rootDirPathProvider = rootDirPathProvider;
    launchScript = Paths.get(System.getenv("HOME"), "che/ls-yaml/launch.sh");
  }

  @PostConstruct
  private void subscribe() {
    eventService.subscribe(this::onLanguageServerInitialized, LanguageServerInitializedEvent.class);
  }

  protected static LanguageServer getYamlLanguageServer() {
    return yamlLanguageServer;
  }

  protected static void setYamlLanguageServer(LanguageServer yamlLanguageServer) {
    YamlLanguageServerConfig.yamlLanguageServer = yamlLanguageServer;
  }

  public void onLanguageServerInitialized(LanguageServerInitializedEvent event) {

    try {
      Map<String, String> preferences =
          requestFactory.fromUrl(apiUrl + "/preferences").useGetMethod().request().asProperties();

      Endpoint endpoint = ServiceEndpoints.toEndpoint(event.getLanguageServer());
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

    if (yamlPreferenceMap == null || "".equals(jsonStr)) {
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

  @Override
  public RegexProvider getRegexpProvider() {
    return new RegexProvider() {
      @Override
      public Map<String, String> getLanguageRegexes() {
        return singletonMap(YamlModule.LANGUAGE_ID, REGEX);
      }

      @Override
      public Set<String> getFileWatchPatterns() {
        return emptySet();
      }
    };
  }

  @Override
  public CommunicationProvider getCommunicationProvider() {
    ProcessBuilder processBuilder = new ProcessBuilder(launchScript.toString());
    processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE);
    processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);

    return new ProcessCommunicationProvider(processBuilder, YamlModule.LANGUAGE_ID);
  }

  @Override
  public InstanceProvider getInstanceProvider() {
    return DefaultInstanceProvider.getInstance();
  }

  @Override
  public InstallerStatusProvider getInstallerStatusProvider() {
    return new InstallerStatusProvider() {
      @Override
      public boolean isSuccessfullyInstalled() {
        return launchScript.toFile().exists();
      }

      @Override
      public String getCause() {
        return isSuccessfullyInstalled() ? null : "Launch script file does not exist";
      }
    };
  }

  @Override
  public String getProjectsRoot() {
    return rootDirPathProvider.get();
  }
}
