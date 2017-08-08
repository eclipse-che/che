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

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Max Shaposhnik (mshaposh@redhat.com)
 */
public class KeycloakSettings {
    private static final Logger LOG = Logger.getLogger(KeycloakSettings.class.getName());

    private static Map<String, String> settings = null;

    public static Map<String, String> get() {
        return settings;
    }

    public static void set(Map<String, String> theSettings) {
        settings = theSettings;
    }

    @SuppressWarnings("unchecked")
    public static void pullFromApiEndpointIfNecessary(String apiEndpoint) {
        if (settings == null) {
            URL url;
            HttpURLConnection conn;
            try {
                url = new URL(KeycloakConstants.getEndpoint(apiEndpoint));
                LOG.info("Pulling Keycloak settings from URL :" + url);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                ObjectMapper mapper = new ObjectMapper();
                settings = mapper.readValue(in, Map.class);
            } catch (IOException e) {
                LOG.log(Level.SEVERE, "Exception during Keycloak settings retrieval", e);
            }
        }
    }
}
