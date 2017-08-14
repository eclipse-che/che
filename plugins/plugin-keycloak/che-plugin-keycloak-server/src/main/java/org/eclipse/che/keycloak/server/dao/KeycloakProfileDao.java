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
package org.eclipse.che.keycloak.server.dao;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.model.impl.ProfileImpl;
import org.eclipse.che.api.user.server.spi.ProfileDao;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.keycloak.shared.KeycloakConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * @author Max Shaposhnik (mshaposh@redhat.com)
 */
public class KeycloakProfileDao implements ProfileDao {

    private static final Logger LOG = LoggerFactory.getLogger(KeycloakProfileDao.class);

    private final String authServerUrl;
    private final String realm;

    @Inject
    public KeycloakProfileDao(@Named(KeycloakConstants.AUTH_SERVER_URL_SETTING) String authServerUrl,
                              @Named(KeycloakConstants.REALM_SETTING) String realm) {
        this.authServerUrl = authServerUrl;
        this.realm = realm;
    }

    @Override
    public void create(ProfileImpl profile) throws ServerException, ConflictException {
        throw new  ServerException("Given operation doesn't supported on current configured storage.");
    }

    @Override
    public void update(ProfileImpl profile) throws NotFoundException, ServerException {
        throw new  ServerException("Given operation doesn't supported on current configured storage.");
    }

    @Override
    public void remove(String userId) throws ServerException {
        throw new  ServerException("Given operation doesn't supported on current configured storage.");
    }

    @Override
    public ProfileImpl getById(String userId) throws NotFoundException, ServerException {
        requireNonNull(userId, "Required non-null id");
        try {
            URL url = new URL(authServerUrl + "/realms/" + realm + "/protocol/openid-connect/userinfo");
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "bearer " + EnvironmentContext.getCurrent().getSubject().getToken());
            LOG.info("Pulling profile from URL : {}", url);
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                ObjectMapper mapper = new ObjectMapper();
                //noinspection unchecked
                Map<String, String> profileAttributes =  mapper.readValue(in, Map.class);
                return new ProfileImpl(userId, profileAttributes);
            }
        } catch (IOException e) {
            LOG.error("Exception during retrieval of the Keycloak user profile", e);
            throw new ServerException("Exception during retrieval of the Keycloak user profile", e);
        }
    }
}
