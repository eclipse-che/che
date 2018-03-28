/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.keycloak.server.dao;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.user.server.model.impl.ProfileImpl;
import org.eclipse.che.api.user.server.spi.ProfileDao;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.multiuser.keycloak.server.KeycloakSettings;
import org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fetches user profile from Keycloack server.
 *
 * @author Max Shaposhnik (mshaposh@redhat.com)
 * @author Sergii Leshchenko
 */
public class KeycloakProfileDao implements ProfileDao {
  private static final Logger LOG = LoggerFactory.getLogger(KeycloakProfileDao.class);

  private final String keyclockCurrentUserInfoUrl;
  private final HttpJsonRequestFactory requestFactory;

  @Inject
  public KeycloakProfileDao(
      KeycloakSettings keycloakSettings, HttpJsonRequestFactory requestFactory) {
    this.requestFactory = requestFactory;
    this.keyclockCurrentUserInfoUrl =
        keycloakSettings.get().get(KeycloakConstants.USERINFO_ENDPOINT_SETTING);
  }

  @Override
  public void create(ProfileImpl profile) throws ServerException, ConflictException {
    // this method intentionally left blank
  }

  @Override
  public void update(ProfileImpl profile) throws NotFoundException, ServerException {
    throw new ServerException("Given operation doesn't supported on current configured storage.");
  }

  @Override
  public void remove(String userId) throws ServerException {
    throw new ServerException("Given operation doesn't supported on current configured storage.");
  }

  @Override
  public ProfileImpl getById(String userId) throws NotFoundException, ServerException {
    requireNonNull(userId, "Required non-null id");
    String currentUserId = EnvironmentContext.getCurrent().getSubject().getUserId();
    if (!userId.equals(currentUserId)) {
      throw new ServerException(
          "It's not allowed to get foreign profile on current configured storage.");
    }

    Map<String, String> keycloakUserAttributes;
    // Retrieving own profile
    try {
      keycloakUserAttributes =
          requestFactory.fromUrl(keyclockCurrentUserInfoUrl).request().asProperties();
    } catch (IOException | ApiException e) {
      LOG.warn("Exception during retrieval of the Keycloak user profile", e);
      throw new ServerException("Exception during retrieval of the Keycloak user profile", e);
    }

    return new ProfileImpl(userId, mapAttributes(keycloakUserAttributes));
  }

  private Map<String, String> mapAttributes(Map<String, String> keycloakUserAttributes) {
    Map<String, String> profileAttributes = new HashMap<>();
    String givenName = keycloakUserAttributes.remove("given_name");
    if (givenName != null) {
      profileAttributes.put("firstName", givenName);
    }

    String familyName = keycloakUserAttributes.remove("family_name");
    if (familyName != null) {
      profileAttributes.put("lastName", familyName);
    }

    // profile should be accessible from user object
    keycloakUserAttributes.remove("email");

    profileAttributes.putAll(keycloakUserAttributes);
    return profileAttributes;
  }
}
