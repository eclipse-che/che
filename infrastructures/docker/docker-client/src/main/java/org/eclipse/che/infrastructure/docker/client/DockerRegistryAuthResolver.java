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
package org.eclipse.che.infrastructure.docker.client;

import static com.google.common.collect.Sets.newHashSet;

import com.google.inject.Inject;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.json.JsonHelper;
import org.eclipse.che.infrastructure.docker.client.dto.AuthConfig;
import org.eclipse.che.infrastructure.docker.client.dto.AuthConfigs;

/**
 * Class for preparing auth header value for docker registry.
 *
 * @author Mykola Morhun
 */
public class DockerRegistryAuthResolver {

  public static final Set<String> DEFAULT_REGISTRY_SYNONYMS =
      Collections.unmodifiableSet(
          newHashSet(
              null,
              "",
              "docker.io",
              "index.docker.io",
              "https://index.docker.io",
              "https://index.docker.io/v1",
              "https://index.docker.io/v1/"));

  public static final String DEFAULT_REGISTRY = "https://index.docker.io/v1/";

  private final InitialAuthConfig initialAuthConfig;
  private final DockerRegistryDynamicAuthResolver dynamicAuthResolver;

  @Inject
  public DockerRegistryAuthResolver(
      InitialAuthConfig initialAuthConfig, DockerRegistryDynamicAuthResolver dynamicAuthResolver) {
    this.initialAuthConfig = initialAuthConfig;
    this.dynamicAuthResolver = dynamicAuthResolver;
  }

  /**
   * Looks for auth credentials for specified registry and encode it in base64. First searches in
   * the passed params and then in the configured credentials. If nothing found, empty encoded json
   * will be returned.
   *
   * @param registry registry to which API call will be applied
   * @param paramAuthConfigs credentials for provided registry
   * @return base64 encoded X-Registry-Auth header value
   */
  public String getXRegistryAuthHeaderValue(
      @Nullable String registry, @Nullable AuthConfigs paramAuthConfigs) {
    AuthConfig authConfig = getAuthConfigForRegistry(registry, paramAuthConfigs);

    String authConfigJson;
    if (authConfig == null) {
      // empty auth config
      authConfigJson = "{}";
    } else {
      authConfigJson = JsonHelper.toJson(authConfig);
    }

    return Base64.getEncoder().encodeToString(authConfigJson.getBytes());
  }

  /**
   * Builds list of auth configs. Adds auth configs from current API call and from initial auth
   * config.
   *
   * @param paramAuthConfigs auth header values for provided registry
   * @return base64 encoded X-Registry-Config header value
   */
  public String getXRegistryConfigHeaderValue(@Nullable AuthConfigs paramAuthConfigs) {
    Map<String, AuthConfig> authConfigs = new HashMap<>();

    authConfigs.putAll(initialAuthConfig.getAuthConfigs().getConfigs());
    if (paramAuthConfigs != null && paramAuthConfigs.getConfigs() != null) {
      authConfigs.putAll(paramAuthConfigs.getConfigs());
    }
    authConfigs.putAll(dynamicAuthResolver.getXRegistryConfig());

    authConfigs = normalizeDockerHubRegistryUrl(authConfigs);

    return Base64.getEncoder().encodeToString(JsonHelper.toJson(authConfigs).getBytes());
  }

  /**
   * Returns authorization header value for basic auth method for given registry. If registry is not
   * configured empty string will be returned.
   *
   * @param registry registry to which auth config should be found
   * @param paramAuthConfigs additional auth configs per this request
   * @return authorization header value for basic auth method or empty string if registry isn't
   *     configured
   */
  public String getBasicAuthHeaderValue(
      @Nullable String registry, @Nullable AuthConfigs paramAuthConfigs) {
    AuthConfig authConfig = getAuthConfigForRegistry(registry, paramAuthConfigs);

    if (authConfig != null) {
      return "Basic "
          + Base64.getEncoder()
              .encodeToString(
                  (authConfig.getUsername() + ':' + authConfig.getPassword()).getBytes());
    }
    return "";
  }

  /**
   * Looks for auth configuration for given registry. If given registry is not configured then null
   * will be returned.
   *
   * @param registry registry to which auth config should be found
   * @param paramAuthConfigs additional auth configs
   * @return auth config for given registry or null is it isn't configured
   */
  private AuthConfig getAuthConfigForRegistry(
      @Nullable String registry, @Nullable AuthConfigs paramAuthConfigs) {
    String normalizedRegistry =
        DEFAULT_REGISTRY_SYNONYMS.contains(registry) ? DEFAULT_REGISTRY : registry;

    AuthConfig authConfig = null;
    if (paramAuthConfigs != null && paramAuthConfigs.getConfigs() != null) {
      authConfig =
          normalizeDockerHubRegistryUrl(paramAuthConfigs.getConfigs()).get(normalizedRegistry);
    }
    if (authConfig == null) {
      authConfig =
          normalizeDockerHubRegistryUrl(initialAuthConfig.getAuthConfigs().getConfigs())
              .get(normalizedRegistry);
    }
    if (authConfig == null) {
      authConfig = dynamicAuthResolver.getXRegistryAuth(registry);
    }

    return authConfig;
  }

  private Map<String, AuthConfig> normalizeDockerHubRegistryUrl(
      Map<String, AuthConfig> authConfigs) {
    for (String defaultRegistryAlias : DEFAULT_REGISTRY_SYNONYMS) {
      if (authConfigs.containsKey(defaultRegistryAlias)) {
        Map<String, AuthConfig> normalizedAuthConfigMap = new HashMap<>(authConfigs);
        normalizedAuthConfigMap.put(
            DEFAULT_REGISTRY, normalizedAuthConfigMap.remove(defaultRegistryAlias));
        return normalizedAuthConfigMap;
      }
    }
    return authConfigs;
  }
}
