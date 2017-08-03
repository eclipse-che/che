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

/**
 * @author Max Shaposhnik (mshaposh@redhat.com)
 */
public class KeycloakConstants {

    public static final String KEYCLOAK_SETTING_PREFIX = "che.keycloak.";
    public static final String KEYCLOAK_SETTINGS_ENDPOINT_PATH = "/keycloak/settings";

    public static final String AUTH_SERVER_URL_SETTING = KEYCLOAK_SETTING_PREFIX + "auth-server-url";
    public static final String REALM_SETTING           = KEYCLOAK_SETTING_PREFIX + "realm";
    public static final String CLIENT_ID_SETTING       = KEYCLOAK_SETTING_PREFIX + "client-id";

    public static final String OSO_ENDPOINT_SETTING    = KEYCLOAK_SETTING_PREFIX + "oso.endpoint";
    public static final String GITHUB_ENDPOINT_SETTING = KEYCLOAK_SETTING_PREFIX + "github.endpoint";

    public static final String getEndpoint(String apiEndpoint) {
        return apiEndpoint + KEYCLOAK_SETTINGS_ENDPOINT_PATH;
    }
}
