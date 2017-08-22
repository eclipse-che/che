/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.core.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import javax.inject.Named;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.eclipse.che.keycloak.shared.KeycloakConstants;
import org.eclipse.che.selenium.core.user.AdminTestUser;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Musienko Maxim
 * @author Dmytro Nochevnov
 */
@Singleton
public class TestUserServiceClient {
  private static final Logger LOG = LoggerFactory.getLogger(TestUserServiceClient.class);

  private final Keycloak keycloak;
  private final String realm;

  @Inject
  public TestUserServiceClient(
      AdminTestUser adminTestUser,
      @Named(KeycloakConstants.AUTH_SERVER_URL_SETTING) String authServerUrl,
      @Named(KeycloakConstants.REALM_SETTING) String realm,
      @Named(KeycloakConstants.PRIVATE_CLIENT_ID_SETTING) String clientId,
      @Named(KeycloakConstants.PRIVATE_CLIENT_SECRET_SETTING) String clientSecret) {
    this.realm = realm;
    this.keycloak =
        KeycloakBuilder.builder()
            .serverUrl(authServerUrl)
            .realm("master")
            .username(adminTestUser.getName())
            .password(adminTestUser.getPassword())
            .clientId("admin-cli")
            .build();
  }

  public void delete(String id) throws IOException {
    keycloak.realm(realm).users().delete(id);
  }

  /**
   * Creates user.
   *
   * @param name
   * @param email
   * @param password
   * @return id of user
   * @throws Exception
   */
  public String create(String name, String email, String password) throws IOException {
    String userId;

    UserRepresentation user = new UserRepresentation();
    user.setUsername(name);
    user.setEmail(email);
    user.setEnabled(true);

    try {
      Response res = keycloak.realm(realm).users().create(user);
      if (res.getStatus() != Status.CREATED.getStatusCode()) {
        throw new IOException(
            String.format(
                "Server status '%s'. Error message: '%s'", res.getStatus(), res.getEntity()));
      }

      userId = res.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");

      // set password
      CredentialRepresentation credential = new CredentialRepresentation();
      credential.setType(CredentialRepresentation.PASSWORD);
      credential.setValue(password);
      credential.setTemporary(false);

      keycloak.realm(realm).users().get(userId).resetPassword(credential);
    } catch (RuntimeException e) {
      throw new IOException(
          String.format("Error of creation of user with name '%s' occurred.", name), e);
    }

    return userId;
  }
}
