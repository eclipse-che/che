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

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

import org.apache.commons.codec.binary.Base64;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.json.JsonHelper;
import org.eclipse.che.plugin.docker.client.dto.AuthConfig;
import org.eclipse.che.plugin.docker.client.dto.AuthConfigs;

import java.util.HashMap;
import java.util.Map;

/**
 * Class for preparing docker auth data.
 *
 * @author Mykola Morhun
 */
public class DockerRegistryAuthResolver {

    public static final ImmutableSet<String> DEFAULT_REGISTRY = ImmutableSet.of("",
                                                                                "docker.io",
                                                                                "index.docker.io",
                                                                                "https://index.docker.io",
                                                                                "https://index.docker.io/v1",
                                                                                "https://index.docker.io/v1/");
    public static final String DEFAULT_REGISTRY_VALUE = "docker.io";

    private final InitialAuthConfig initialAuthConfig;

    @Inject
    public DockerRegistryAuthResolver(InitialAuthConfig initialAuthConfig) {
        this.initialAuthConfig = initialAuthConfig;
    }

    /**
     * Looks for auth header for specified registry and encode it in base64.
     * First searches in the params and then in the initial auth config.
     * If nothing found empty encoded json will be returned.
     *
     * @param registry
     *         registry to which API call will be applied
     * @param paramsAuthConfigs
     *         auth data for current API call
     * @return base64 encoded X-Registry-Auth header value
     */
    public String getXRegistryAuthHeaderValue(String registry, @Nullable AuthConfigs paramsAuthConfigs) {
        Map<String, AuthConfig> authConfigsMap = null;
        if (paramsAuthConfigs != null) {
            authConfigsMap = paramsAuthConfigs.getConfigs();
        }

        if (DEFAULT_REGISTRY.contains(registry)) {
            registry = DEFAULT_REGISTRY_VALUE;
        }

        AuthConfig authConfig = null;
        if (authConfigsMap != null) {
            for(Map.Entry<String, AuthConfig> entry : authConfigsMap.entrySet()) {
                AuthConfig value = entry.getValue();
                if (value.getServeraddress().contains(registry)) {
                    authConfig = value;
                    break;
                }
            }
        }

        if (authConfig == null) {
            for(Map.Entry<String, AuthConfig> entry : initialAuthConfig.getAuthConfigs().getConfigs().entrySet()) {
                AuthConfig value = entry.getValue();
                if (value.getServeraddress().contains(registry)) {
                    authConfig = value;
                    break;
                }
            }
        }

        if (authConfig != null) {
            XRegistryAuthUnit auth = new XRegistryAuthUnit(authConfig.getUsername(), authConfig.getPassword());
            return Base64.encodeBase64String(JsonHelper.toJson(auth).getBytes());
        }

        return Base64.encodeBase64String("{}".getBytes());
    }

    /**
     * Builds list of auth configs.
     * Adds auth configs from current API call and from initial auth config.
     *
     * @param paramsAuthConfig
     *         auth config for current API call
     * @return base64 encoded X-Registry-Config header value
     */
    public String getXRegistryConfigHeaderValue(@Nullable Map<String,AuthConfig> paramsAuthConfig) {
        Map<String, XRegistryAuthUnit> authConfigs = new HashMap<>();

        for(Map.Entry<String, AuthConfig> entry : initialAuthConfig.getAuthConfigs().getConfigs().entrySet()) {
            AuthConfig value = entry.getValue();
            authConfigs.put(value.getServeraddress(),
                            new XRegistryAuthUnit(value.getUsername(), value.getPassword()));
        }

        if (paramsAuthConfig != null) {
            for(Map.Entry<String, AuthConfig> entry : paramsAuthConfig.entrySet()) {
                AuthConfig value = entry.getValue();
                authConfigs.put(entry.getKey(),
                                new XRegistryAuthUnit(value.getUsername(), value.getPassword()));
            }
        }

        return Base64.encodeBase64String(JsonHelper.toJson(authConfigs).getBytes());
    }

    /** This class is used for generate X-Registry-Auth and X-Registry-Config */
    // protected is needed for JsonHelper
    protected static class XRegistryAuthUnit {
        private String username;
        private String password;

        public XRegistryAuthUnit(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }

}
