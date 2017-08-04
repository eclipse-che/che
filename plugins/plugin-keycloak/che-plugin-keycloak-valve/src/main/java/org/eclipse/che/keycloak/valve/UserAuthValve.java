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
package org.eclipse.che.keycloak.valve;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.eclipse.che.keycloak.shared.KeycloakSettings;
import org.keycloak.adapters.tomcat.KeycloakAuthenticatorValve;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Map;

/**
 * Performs Keycloak and OpenShift.io user validation. Prompts user to login if necessary
 * and checks if the logged in user has access to the current Che instance.
 *
 * @see KeycloakAuthenticatorValve
 *
 * @author amisevsk
 */
public class UserAuthValve extends KeycloakAuthenticatorValve {

    synchronized void retrieveKeycloakSettingsIfNecessary(String apiEndpoint) {
        Map<String, String> keycloakSettings = KeycloakSettings.get();
        if (keycloakSettings == null) {
            KeycloakSettings.pullFromApiEndpointIfNecessary(apiEndpoint);
        }
    }

    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {
        retrieveKeycloakSettingsIfNecessary(request.getScheme() + "://" + request.getLocalAddr() + ":" + request.getLocalPort() + "/api");
        super.invoke(request, response);
    }
}
