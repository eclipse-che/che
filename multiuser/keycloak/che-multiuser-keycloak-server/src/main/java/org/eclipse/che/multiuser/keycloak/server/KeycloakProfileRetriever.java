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
package org.eclipse.che.multiuser.keycloak.server;

import java.io.IOException;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fetches user profile from Keycloack server.
 *
 * @author David Festal <dfestal@redhat.com>
 */
@Singleton
public class KeycloakProfileRetriever {
  private static final Logger LOG = LoggerFactory.getLogger(KeycloakProfileRetriever.class);

  private final String keyclockCurrentUserInfoUrl;
  private final HttpJsonRequestFactory requestFactory;

  @Inject
  public KeycloakProfileRetriever(OIDCInfo oidcInfo, HttpJsonRequestFactory requestFactory) {
    this.requestFactory = requestFactory;
    this.keyclockCurrentUserInfoUrl = oidcInfo.getUserInfoEndpoint();
  }

  /**
   * Retrieves attributes from keycloak using default request factory assuming that there is an
   * {@code Subject} with authorization token present in {@code EnvironmentContext}
   *
   * @return map of user attributes from keycloak
   * @throws ServerException in an error happened
   */
  public Map<String, String> retrieveKeycloakAttributes() throws ServerException {
    try {
      return requestFactory.fromUrl(keyclockCurrentUserInfoUrl).request().asProperties();
    } catch (IOException | ApiException e) {
      LOG.warn("Exception during retrieval of the Keycloak user profile", e);
      throw new ServerException("Exception during retrieval of the Keycloak user profile", e);
    }
  }

  /**
   * Retrieves attributes from keycloak using default request factory and provided authorization
   * header for cases when no {@code Subject} set in {@code EnvironmentContext}
   *
   * @return map of user attributes from keycloak
   * @throws ServerException in an error happened
   */
  public Map<String, String> retrieveKeycloakAttributes(@NotNull String authorizationHeader)
      throws ServerException {
    try {
      return requestFactory
          .fromUrl(keyclockCurrentUserInfoUrl)
          .setAuthorizationHeader(authorizationHeader)
          .request()
          .asProperties();
    } catch (IOException | ApiException e) {
      LOG.warn("Exception during retrieval of the Keycloak user profile", e);
      throw new ServerException("Exception during retrieval of the Keycloak user profile", e);
    }
  }
}
