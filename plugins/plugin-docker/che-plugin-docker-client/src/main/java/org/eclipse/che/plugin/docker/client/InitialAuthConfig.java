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

import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.inject.ConfigurationProperties;
import org.eclipse.che.plugin.docker.client.dto.AuthConfig;
import org.eclipse.che.plugin.docker.client.dto.AuthConfigs;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Collects auth configurations for private docker registries. Credential might be configured in .properties files, see details {@link
 * org.eclipse.che.inject.CheBootstrap}. Credentials configured as (key=value) pairs. Key is string that starts with prefix
 * {@code docker.registry.auth.} followed by url and credentials of docker registry server.
 * <pre>{@code
 * docker.registry.auth.url=localhost:5000
 * docker.registry.auth.username=user1
 * docker.registry.auth.password=pass
 * }</pre>
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class InitialAuthConfig {
    private static final String CONFIGURATION_PREFIX         = "docker.registry.auth.";
    private static final String CONFIGURATION_PREFIX_PATTERN = "docker\\.registry\\.auth\\..+";

    AuthConfig predefinedConfig;

    private String serverAddress = "https://index.docker.io/v1/";

    /** For testing purposes */
    public InitialAuthConfig() {
    }

    @Inject
    public InitialAuthConfig(ConfigurationProperties configurationProperties) {
        String username = null, password = null;
        for (Map.Entry<String, String> e : configurationProperties.getProperties(CONFIGURATION_PREFIX_PATTERN).entrySet()) {
            final String classifier = e.getKey().replaceFirst(CONFIGURATION_PREFIX, "");
            switch (classifier) {
                case "url": {
                    serverAddress = e.getValue();
                    break;
                }
                case "username": {
                    username = e.getValue();
                    break;
                }
                case "password": {
                    password = e.getValue();
                    break;
                }
            }
        }
        if (!isNullOrEmpty(serverAddress) && !isNullOrEmpty(username) && !isNullOrEmpty(password)) {
            predefinedConfig = DtoFactory.newDto(AuthConfig.class).withUsername(username)
                                                                  .withPassword(password);
        }
    }

    public AuthConfigs getAuthConfigs() {
        AuthConfigs authConfigs = DtoFactory.newDto(AuthConfigs.class);
        if (predefinedConfig != null) {
            authConfigs.getConfigs().put(serverAddress, predefinedConfig);
        }
        return authConfigs;
    }

}
