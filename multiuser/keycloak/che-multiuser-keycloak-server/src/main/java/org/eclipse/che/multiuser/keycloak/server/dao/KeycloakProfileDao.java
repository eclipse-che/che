/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.keycloak.server.dao;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.model.impl.ProfileImpl;
import org.eclipse.che.api.user.server.spi.ProfileDao;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.multiuser.keycloak.server.KeycloakProfileRetriever;

/**
 * Fetches user profile from Keycloak server.
 *
 * @author Max Shaposhnik (mshaposh@redhat.com)
 * @author Sergii Leshchenko
 */
public class KeycloakProfileDao implements ProfileDao {

  private final KeycloakProfileRetriever keycloakProfileRetriever;

  @Inject
  public KeycloakProfileDao(KeycloakProfileRetriever keycloakProfileRetriever) {
    this.keycloakProfileRetriever = keycloakProfileRetriever;
  }

  @Override
  public void create(ProfileImpl profile) throws ServerException, ConflictException {
    throw new ServerException("Given operation doesn't supported on current configured storage.");
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

    // Retrieving own profile
    Map<String, String> keycloakUserAttributes =
        keycloakProfileRetriever.retrieveKeycloakAttributes();

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
