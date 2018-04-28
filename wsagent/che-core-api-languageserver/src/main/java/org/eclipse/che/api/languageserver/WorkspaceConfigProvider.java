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

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static javax.ws.rs.core.UriBuilder.fromUri;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.model.workspace.Runtime;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.core.model.workspace.runtime.Server;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.languageserver.LanguageServerConfig.CommunicationProvider;
import org.eclipse.che.api.workspace.server.WorkspaceService;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides all language server configuration that is defined within the workspace configuration.
 *
 * @author Dmytro Kulieshov
 */
@Singleton
class WorkspaceConfigProvider implements LanguageServerConfigProvider {
  private static final Logger LOG = LoggerFactory.getLogger(WorkspaceConfigProvider.class);

  private final String workspaceId;
  private final WorkspaceProvider workspaceProvider;
  private final ConfigExtractor configExtractor;

  @Inject
  WorkspaceConfigProvider(
      @Named("env.CHE_WORKSPACE_ID") String workspaceId,
      @Named("che.api") String apiEndpoint,
      HttpJsonRequestFactory httpJsonRequestFactory,
      JsonParser jsonParser) {
    this.workspaceId = workspaceId;
    this.workspaceProvider = new WorkspaceProvider(apiEndpoint, httpJsonRequestFactory);
    this.configExtractor = new ConfigExtractor(jsonParser);
  }

  @Override
  public Map<String, LanguageServerConfig> getAll() {
    Workspace workspace = workspaceProvider.get(workspaceId);
    if (workspace == null) {
      LOG.error("Can't get workspace");
      return ImmutableMap.of();
    }

    Runtime runtime = workspace.getRuntime();
    if (runtime == null) {
      LOG.error("Can't get runtime");
      return ImmutableMap.of();
    }

    Map<String, LanguageServerConfig> configs = newHashMap();

    for (Entry<String, ? extends Machine> machineEntry : runtime.getMachines().entrySet()) {
      Map<String, ? extends Server> servers = machineEntry.getValue().getServers();

      for (Entry<String, ? extends Server> serverEntry : servers.entrySet()) {
        Server server = serverEntry.getValue();
        String serverUrl = server.getUrl();
        Map<String, String> attributes = server.getAttributes();

        if (!"ls".equals(attributes.get("type"))) {
          continue;
        }

        try {
          String id = configExtractor.extractId(attributes);
          Map<String, String> languageRegexes = configExtractor.extractLanguageRegexes(attributes);
          Set<String> fileWatchPatterns = configExtractor.extractFileWatchPatterns(attributes);
          CommunicationProvider communicationProvider =
              new SocketCommunicationProvider(new URI(serverUrl));

          configs.put(
              id,
              new LanguageServerConfig() {
                @Override
                public RegexProvider getRegexpProvider() {
                  return new RegexProvider() {
                    @Override
                    public Map<String, String> getLanguageRegexes() {
                      return languageRegexes;
                    }

                    @Override
                    public Set<String> getFileWatchPatterns() {
                      return fileWatchPatterns;
                    }
                  };
                }

                @Override
                public CommunicationProvider getCommunicationProvider() {
                  return communicationProvider;
                }

                @Override
                public InstanceProvider getInstanceProvider() {
                  return DefaultInstanceProvider.getInstance();
                }
              });

        } catch (URISyntaxException e) {
          LOG.error("Can't parse server URI: {}, proceeding", serverUrl, e);
        }
      }
    }

    return configs;
  }

  private class ConfigExtractor {
    private final JsonParser jsonParser;

    private ConfigExtractor(JsonParser jsonParser) {
      this.jsonParser = jsonParser;
    }

    private String extractId(Map<String, String> attributes) {
      return attributes.get("id");
    }

    private Map<String, String> extractLanguageRegexes(Map<String, String> attributes) {
      String filtersAsString = attributes.get("languageRegexes");

      if (filtersAsString == null) {
        return emptyMap();
      }

      return newHashSet(jsonParser.parse(filtersAsString).getAsJsonArray())
          .stream()
          .map(JsonElement::getAsJsonObject)
          .collect(toMap(this::getLanguageId, this::getRegex));
    }

    private String getLanguageId(JsonObject it) {
      return it.get("languageId").getAsString();
    }

    private String getRegex(JsonObject it) {
      return it.get("regex").getAsString();
    }

    private Set<String> extractFileWatchPatterns(Map<String, String> attributes) {
      String patternsAsString = attributes.get("fileWatchPatterns");

      if (patternsAsString == null) {
        return emptySet();
      }

      return newHashSet(jsonParser.parse(patternsAsString).getAsJsonArray())
          .stream()
          .map(JsonElement::getAsString)
          .collect(toSet());
    }
  }

  private class WorkspaceProvider {
    private final String apiEndpoint;
    private final HttpJsonRequestFactory httpJsonRequestFactory;

    private WorkspaceProvider(String apiEndpoint, HttpJsonRequestFactory httpJsonRequestFactory) {
      this.apiEndpoint = apiEndpoint;
      this.httpJsonRequestFactory = httpJsonRequestFactory;
    }

    private Workspace get(String workspaceId) {
      try {
        String href =
            fromUri(apiEndpoint)
                .path(WorkspaceService.class)
                .path(WorkspaceService.class, "getByKey")
                .queryParam("includeInternalServers", true)
                .build(workspaceId)
                .toString();
        return httpJsonRequestFactory
            .fromUrl(href)
            .useGetMethod()
            .request()
            .asDto(WorkspaceDto.class);
      } catch (IOException | ApiException e) {
        LOG.error("Did not manage to get workspace configuration: {}", workspaceId, e);
      }
      return null;
    }
  }
}
