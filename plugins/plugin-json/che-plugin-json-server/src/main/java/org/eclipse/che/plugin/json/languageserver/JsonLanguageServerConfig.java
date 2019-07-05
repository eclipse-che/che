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
package org.eclipse.che.plugin.json.languageserver;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.languageserver.DefaultInstanceProvider;
import org.eclipse.che.api.languageserver.LanguageServerConfig;
import org.eclipse.che.api.languageserver.LanguageServerInitializedEvent;
import org.eclipse.che.api.languageserver.ProcessCommunicationProvider;
import org.eclipse.che.api.project.server.impl.RootDirPathProvider;
import org.eclipse.che.plugin.json.inject.JsonModule;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.eclipse.lsp4j.jsonrpc.services.ServiceEndpoints;

/**
 * @author Evgen Vidolob
 * @author Anatolii Bazko
 */
@Singleton
public class JsonLanguageServerConfig implements LanguageServerConfig {
  private static final String REGEX = ".*\\.(json|bowerrc|jshintrc|jscsrc|eslintrc|babelrc)";

  private final RootDirPathProvider rootDirPathProvider;
  private final EventService eventService;
  private final Path launchScript;

  @Inject
  public JsonLanguageServerConfig(
      RootDirPathProvider rootDirPathProvider, EventService eventService) {
    this.rootDirPathProvider = rootDirPathProvider;
    this.eventService = eventService;

    this.launchScript = Paths.get(System.getenv("HOME"), "che/ls-json/launch.sh");
  }

  @PostConstruct
  private void subscribe() {
    eventService.subscribe(this::onLanguageServerInitialized, LanguageServerInitializedEvent.class);
  }

  private void onLanguageServerInitialized(LanguageServerInitializedEvent event) {
    Endpoint endpoint = ServiceEndpoints.toEndpoint(event.getLanguageServer());
    JsonExtension serviceObject = ServiceEndpoints.toServiceObject(endpoint, JsonExtension.class);

    Map<String, String[]> associations =
        ImmutableMap.<String, String[]>builder()
            .put("/*.schema.json", new String[] {"http://json-schema.org/draft-04/schema#"})
            .put("/bower.json", new String[] {"http://json.schemastore.org/bower"})
            .put("/.bower.json", new String[] {"http://json.schemastore.org/bower"})
            .put("/.bowerrc", new String[] {"http://json.schemastore.org/bowerrc"})
            .put("/composer.json", new String[] {"https://getcomposer.org/schema.json"})
            .put("/package.json", new String[] {"http://json.schemastore.org/package"})
            .put("/jsconfig.json", new String[] {"http://json.schemastore.org/jsconfig"})
            .put("/tsconfig.json", new String[] {"http://json.schemastore.org/tsconfig"})
            .build();

    serviceObject.jsonSchemaAssociation(associations);
  }

  @Override
  public RegexProvider getRegexpProvider() {
    return new RegexProvider() {
      @Override
      public Map<String, String> getLanguageRegexes() {
        return ImmutableMap.of(JsonModule.LANGUAGE_ID, REGEX);
      }

      @Override
      public Set<String> getFileWatchPatterns() {
        return ImmutableSet.of();
      }
    };
  }

  @Override
  public CommunicationProvider getCommunicationProvider() {
    ProcessBuilder processBuilder = new ProcessBuilder(launchScript.toString());
    processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE);
    processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);

    return new ProcessCommunicationProvider(processBuilder, JsonModule.LANGUAGE_ID);
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
