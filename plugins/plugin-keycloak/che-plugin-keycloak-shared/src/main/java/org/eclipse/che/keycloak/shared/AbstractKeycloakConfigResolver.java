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
package org.eclipse.che.keycloak.shared;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.spi.HttpFacade;
import org.keycloak.representations.adapters.config.AdapterConfig;

import java.util.Map;
import java.util.logging.Logger;

/**
 * Creates keycloak deployment config using values from {@link KeycloakSettings}.
 *
 * @author Max Shaposhnik (mshaposh@redhat.com)
 */
public abstract class AbstractKeycloakConfigResolver implements KeycloakConfigResolver {

    private static final Logger LOG = Logger.getLogger(AbstractKeycloakConfigResolver.class.getName());

    protected KeycloakDeployment keycloakDeployment = null;

    protected abstract AdapterConfig prepareConfig();

    @Override
    public KeycloakDeployment resolve(HttpFacade.Request facade) {
        if (keycloakDeployment != null) {
            return keycloakDeployment;
        }
        Map<String, String> settings = KeycloakSettings.get();
        if (settings == null) {
            LOG.severe("Keycloak settings are not set: Keycloak cannot be correctly initialized");
            throw new RuntimeException("Keycloak settings are not set: Keycloak cannot be correctly initialized");
        }
        AdapterConfig config = prepareConfig();
        config.setAuthServerUrl(settings.get(KeycloakConstants.AUTH_SERVER_URL_SETTING));
        config.setRealm(settings.get(KeycloakConstants.REALM_SETTING));
        config.setResource(settings.get(KeycloakConstants.CLIENT_ID_SETTING));
        keycloakDeployment = KeycloakDeploymentBuilder.build(config);
        return keycloakDeployment;
    }
}
