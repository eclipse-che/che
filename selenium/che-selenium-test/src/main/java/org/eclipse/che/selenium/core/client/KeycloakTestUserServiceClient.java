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

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.eclipse.che.selenium.core.provider.TestKeycloakProvider;
import org.eclipse.che.selenium.core.provider.TestRealmProvider;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client of REST API of user service of keycloak server.
 *
 * @author Dmytro Nochevnov
 */
@Singleton
public class KeycloakTestUserServiceClient implements TestUserServiceClient {
  private static final Logger LOG = LoggerFactory.getLogger(KeycloakTestUserServiceClient.class);

  private final Keycloak keycloak;
  private final String realm;

  @Inject
  public KeycloakTestUserServiceClient(
      TestRealmProvider realmProvider, TestKeycloakProvider keycloakProvider) {
    this.realm = realmProvider.get();
    this.keycloak = keycloakProvider.get();
  }

  /**
   * Delete user by id.
   *
   * @param id user's id
   * @throws IOException
   */
  public void delete(String id) throws IOException {
    keycloak.realm(realm).users().delete(id);
  }

  /**
   * Creates user.
   *
   * @return id of user
   */
  public String create(String name, String email, String password) throws IOException {
    CredentialRepresentation credential = new CredentialRepresentation();
    credential.setType(CredentialRepresentation.PASSWORD);
    credential.setValue(password);
    credential.setTemporary(false);

    UserRepresentation user = new UserRepresentation();
    user.setUsername(name);
    user.setEmail(email);
    user.setEnabled(true);
    user.setCredentials(ImmutableList.of(credential));

    try {
      Response res = keycloak.realm(realm).users().create(user);
      if (res.getStatus() != Status.CREATED.getStatusCode()) {
        throw new IOException(
            String.format(
                "Server status '%s'. Error message: '%s'", res.getStatus(), res.getEntity()));
      }

      return res.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
    } catch (RuntimeException e) {
      throw new IOException(
          String.format("Error of creation of user with name '%s' occurred.", name), e);
    }
  }
}
