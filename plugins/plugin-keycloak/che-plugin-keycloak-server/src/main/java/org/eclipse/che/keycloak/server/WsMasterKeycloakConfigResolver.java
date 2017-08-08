/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.keycloak.server;

import com.google.common.collect.ImmutableMap;

import org.eclipse.che.keycloak.shared.AbstractKeycloakConfigResolver;
import org.eclipse.che.keycloak.shared.KeycloakConstants;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.spi.HttpFacade;
import org.keycloak.common.enums.SslRequired;
import org.keycloak.representations.adapters.config.AdapterConfig;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

/**
 * Property-based config resolver for private clients, aka wsmaster.
 *
 * @author Max Shaposhnik (mshaposh@redhat.com)
 */
public class WsMasterKeycloakConfigResolver extends AbstractKeycloakConfigResolver {

    private final String              authURL;
    private final String              realm;
    private final String              clientId;
    private final String              clientSecret;
    private final Map<String, String> redirectRules;

    @Inject
    public WsMasterKeycloakConfigResolver(@Named(KeycloakConstants.AUTH_SERVER_URL_SETTING) String authURL,
                                          @Named(KeycloakConstants.PRIVATE_REALM_SETTING) String realm,
                                          @Named(KeycloakConstants.PRIVATE_CLIENT_ID_SETTING) String clientId,
                                          @Named(KeycloakConstants.PRIVATE_CLIENT_SECRET_SETTING) String clientSecret,
                                          @Named(KeycloakConstants.REWRITE_RULE_SETTING) Map<String, String> redirectRules) {

        this.authURL = authURL;
        this.realm = realm;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectRules = redirectRules;
    }

    @Override
    protected AdapterConfig prepareConfig() {
        AdapterConfig config = new AdapterConfig();
        config.setSslRequired(SslRequired.EXTERNAL.toString().toLowerCase());
        config.setCors(true);
        config.setBearerOnly(false);
        config.setPublicClient(false);
        config.setConnectionPoolSize(20);
        config.setDisableTrustManager(true);
        config.setCredentials(ImmutableMap.of("secret", clientSecret));
        if (redirectRules != null) {
            config.setRedirectRewriteRules(redirectRules);
        }
        return config;
    }


    @Override
    public KeycloakDeployment resolve(HttpFacade.Request facade) {
        if (keycloakDeployment != null) {
            return keycloakDeployment;
        }
        AdapterConfig config = prepareConfig();
        config.setAuthServerUrl(authURL);
        config.setRealm(realm);
        config.setResource(clientId);
        keycloakDeployment = KeycloakDeploymentBuilder.build(config);
        return keycloakDeployment;
    }
}
