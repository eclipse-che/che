/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.docker.client;

import com.google.inject.Inject;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.json.JsonHelper;
import org.eclipse.che.plugin.docker.client.dto.AuthConfig;
import org.eclipse.che.plugin.docker.client.dto.AuthConfigs;

import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

/**
 * Class for preparing auth header value for docker registry.
 *
 * @author Mykola Morhun
 */
public class DockerRegistryAuthResolver {

    public static final Set<String> DEFAULT_REGISTRY_SYNONYMS = Collections.unmodifiableSet(newHashSet(null,
                                                                                                       "",
                                                                                                       "docker.io",
                                                                                                       "index.docker.io",
                                                                                                       "https://index.docker.io",
                                                                                                       "https://index.docker.io/v1",
                                                                                                       "https://index.docker.io/v1/"));

    public static final String DEFAULT_REGISTRY = "https://index.docker.io/v1/";

    private final InitialAuthConfig initialAuthConfig;

    @Inject
    public DockerRegistryAuthResolver(InitialAuthConfig initialAuthConfig) {
        this.initialAuthConfig = initialAuthConfig;
    }

    /**
     * Looks for auth credentials for specified registry and encode it in base64.
     * First searches in the passed params and then in the configured credentials.
     * If nothing found, empty encoded json will be returned.
     *
     * @param registry
     *         registry to which API call will be applied
     * @param paramAuthConfigs
     *         credentials for provided registry
     * @return base64 encoded X-Registry-Auth header value
     */
    public String getXRegistryAuthHeaderValue(@Nullable String registry, @Nullable AuthConfigs paramAuthConfigs) {
        if (DEFAULT_REGISTRY_SYNONYMS.contains(registry)) {
            registry = DEFAULT_REGISTRY;
        }

        AuthConfig authConfig = null;
        if (paramAuthConfigs != null && paramAuthConfigs.getConfigs() != null) {
            authConfig = paramAuthConfigs.getConfigs().get(registry);
        }
        if (authConfig == null) {
            authConfig = initialAuthConfig.getAuthConfigs().getConfigs().get(registry);
        }

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
     * Builds list of auth configs.
     * Adds auth configs from current API call and from initial auth config.
     *
     * @param paramAuthConfigs
     *         auth header values for provided registry
     * @return base64 encoded X-Registry-Config header value
     */
    public String getXRegistryConfigHeaderValue(@Nullable AuthConfigs paramAuthConfigs) {
        Map<String, AuthConfig> authConfigs = new HashMap<>();

        authConfigs.putAll(initialAuthConfig.getAuthConfigs().getConfigs());
        if (paramAuthConfigs != null && paramAuthConfigs.getConfigs() != null) {
            authConfigs.putAll(paramAuthConfigs.getConfigs());
        }

        // normalize docker hub registry url
        AuthConfig abnormalDefaultRegistryAuthConfig = authConfigs.remove("docker.io");
        if (abnormalDefaultRegistryAuthConfig != null) {
            authConfigs.put(DEFAULT_REGISTRY, abnormalDefaultRegistryAuthConfig);
        }

        return Base64.getEncoder().encodeToString(JsonHelper.toJson(authConfigs).getBytes());
    }

}
