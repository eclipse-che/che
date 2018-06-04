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
package org.eclipse.che.api.languageserver;

import static java.util.stream.Collectors.toSet;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.languageserver.LanguageServerConfig.CommunicationProvider;
import org.eclipse.che.api.languageserver.LanguageServerConfig.InstallerStatusProvider;
import org.eclipse.che.api.languageserver.LanguageServerConfig.InstanceProvider;
import org.eclipse.che.api.languageserver.RegistryContainer.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Process all provided language server configuration and fill corresponding registries.
 *
 * @author Dmytro Kulieshov
 */
@Singleton
class LanguageServerConfigInitializer {
  private static final Logger LOG = LoggerFactory.getLogger(LanguageServerConfigInitializer.class);
  private final Registry<String> idRegistry;
  private final Registry<Set<PathMatcher>> pathMatcherRegistry;
  private final Registry<Set<Pattern>> patternRegistry;
  private final Registry<InstanceProvider> instanceProviderRegistry;
  private final Registry<CommunicationProvider> communicationProviderRegistry;
  private final Registry<Boolean> localityRegistry;
  private final Registry<String> languageFilterRegistry;
  private final Set<LanguageServerConfigProvider> providers;

  @Inject
  LanguageServerConfigInitializer(
      Set<LanguageServerConfigProvider> providers, RegistryContainer registryContainer) {
    this.providers = providers;
    this.idRegistry = registryContainer.idRegistry;
    this.pathMatcherRegistry = registryContainer.pathMatcherRegistry;
    this.patternRegistry = registryContainer.patternRegistry;
    this.instanceProviderRegistry = registryContainer.instanceProviderRegistry;
    this.communicationProviderRegistry = registryContainer.communicationProviderRegistry;
    this.localityRegistry = registryContainer.localityRegistry;
    this.languageFilterRegistry = registryContainer.languageFilterRegistry;
  }

  void initialize() {
    LOG.info("Language server config processing: started");
    for (LanguageServerConfigProvider provider : providers) {
      Map<String, LanguageServerConfig> configs = provider.getAll();
      for (Entry<String, LanguageServerConfig> entry : configs.entrySet()) {
        String id = entry.getKey();
        LOG.debug("Processing for language server {}: started", id);

        if (idRegistry.contains(id)) {
          LOG.debug("Language server with such id is already registered");
          continue;
        }

        LanguageServerConfig config = entry.getValue();

        InstallerStatusProvider installerStatusProvider = config.getInstallerStatusProvider();
        boolean isLocal = installerStatusProvider != null;
        LOG.debug("Locality: {}", isLocal);

        if (isLocal && !installerStatusProvider.isSuccessfullyInstalled()) {
          String cause = installerStatusProvider.getCause();
          LOG.debug("Installation for a language server with id '{}' is not performed", id);
          LOG.debug("The cause: {}", cause);
          LOG.debug("Skipping this language server configuration");

          continue;
        }

        CommunicationProvider communicationProvider = config.getCommunicationProvider();

        InstanceProvider instanceProvider = config.getInstanceProvider();

        Map<String, String> languageRegexes = config.getRegexpProvider().getLanguageRegexes();
        LOG.debug("Language regexes: {}", languageRegexes);

        Set<String> fileWatchPatterns = config.getRegexpProvider().getFileWatchPatterns();
        LOG.debug("File watch patterns: {}", fileWatchPatterns);

        Set<Pattern> patterns =
            languageRegexes.values().stream().map(Pattern::compile).collect(toSet());

        FileSystem fileSystem = FileSystems.getDefault();
        Set<PathMatcher> pathMatchers =
            fileWatchPatterns.stream().map(fileSystem::getPathMatcher).collect(toSet());

        idRegistry.add(id, id);
        pathMatcherRegistry.add(id, pathMatchers);
        patternRegistry.add(id, patterns);
        instanceProviderRegistry.add(id, instanceProvider);
        communicationProviderRegistry.add(id, communicationProvider);
        localityRegistry.add(id, isLocal);

        languageRegexes.forEach(languageFilterRegistry::add);

        LOG.debug("Processing for language server {}: finished", id);
      }
    }
    LOG.debug("Language server config processing: finished");
  }
}
