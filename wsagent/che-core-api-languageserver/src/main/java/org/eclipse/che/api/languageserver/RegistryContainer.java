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

import static com.google.common.collect.Maps.newConcurrentMap;

import com.google.common.collect.ImmutableMap;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.PathMatcher;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.inject.Singleton;
import org.eclipse.che.api.languageserver.LanguageServerConfig.CommunicationProvider;
import org.eclipse.che.api.languageserver.LanguageServerConfig.InstanceProvider;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.services.LanguageServer;

/**
 * Instance contains all language server internal registries.
 *
 * @author Dmytro Kulieshov
 */
@Singleton
class RegistryContainer {
  final Registry<String> idRegistry = new Registry<>();
  final Registry<ServerCapabilities> serverCapabilitiesRegistry = new Registry<>();
  final Registry<CommunicationProvider> communicationProviderRegistry = new Registry<>();
  final Registry<InstanceProvider> instanceProviderRegistry = new Registry<>();
  final Registry<LanguageServer> languageServerRegistry = new Registry<>();
  final Registry<Pair<InputStream, OutputStream>> ioStreamRegistry = new Registry<>();
  final Registry<Boolean> localityRegistry = new Registry<>();
  final Registry<Set<PathMatcher>> pathMatcherRegistry = new Registry<>();
  final Registry<Set<Pattern>> patternRegistry = new Registry<>();
  final Registry<String> languageFilterRegistry = new Registry<>();
  final Registry<String> projectsRootRegistry = new Registry<>();

  /**
   * Simple parametrized registry class backed by map-based internal registry, where the key is the
   * id of a language server.
   *
   * @param <T> type of a registry element
   */
  class Registry<T> {
    private final Map<String, T> innerRegistry = newConcurrentMap();

    /**
     * Add an element to the registry
     *
     * @param id language server id
     * @param t registry element
     * @return language server id
     */
    String add(String id, T t) {
      innerRegistry.put(id, t);
      return id;
    }

    /**
     * Checks if the registry already contain the value for the specified language server.
     *
     * @param id language server id
     * @return true if contains, false if not
     */
    boolean contains(String id) {
      return innerRegistry.containsKey(id);
    }

    /**
     * Get registry element that corresponds to a specified language server.
     *
     * @param id language server id
     * @return registry element or <code>null</code> is no value is stored for specified language
     *     server
     */
    T getOrNull(String id) {
      if (contains(id)) {
        return innerRegistry.get(id);
      } else {
        return null;
      }
    }

    /**
     * Get registry element that corresponds to a specified language server or default value if it
     * is not present.
     *
     * @param id language server id
     * @return registry element or default value if no value is stored for specified language server
     */
    T getOrDefault(String id, T defaultValue) {
      T value = getOrNull(id);
      return value == null ? defaultValue : value;
    }

    /**
     * Get registry element that corresponds to a specified language server.
     *
     * @param id language server id
     * @return registry element
     * @throws LanguageServerException if no value is stored for specified language server
     */
    T get(String id) throws LanguageServerException {
      T t = getOrNull(id);

      if (t == null) {
        String error = String.format("No element with id '%s'", id);
        String explanation = "Inconsistent registry or wrong language server ID";
        String message = String.format("%s. %s", error, explanation);
        throw new LanguageServerException(message);
      }

      return t;
    }

    /**
     * Get an inner registry copy.
     *
     * @return copy of inner registry map.
     */
    Map<String, T> getAll() {
      return ImmutableMap.copyOf(innerRegistry);
    }
  }
}
