/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.keycloak.server;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.eclipse.che.keycloak.shared.KeycloakConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * @author Max Shaposhnik (mshaposh@redhat.com)
 */
public class KeycloakSettings {
    private static final Logger LOG = LoggerFactory.getLogger(KeycloakSettings.class);

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
            HttpURLConnection conn;
            try {
                conn = (HttpURLConnection)new URL(KeycloakConstants.getEndpoint(apiEndpoint)).openConnection();
                try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    settings = new ObjectMapper().readValue(in, Map.class);
                }
            } catch (IOException e) {
                LOG.error("Exception during Keycloak settings retrieval", e);
            }
        }
    }
}
