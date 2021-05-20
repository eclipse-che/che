/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.keycloak.server.deploy;

import com.google.inject.servlet.ServletModule;
import org.eclipse.che.commons.logback.filter.IdentityIdLoggerFilter;
import org.eclipse.che.multiuser.keycloak.server.KeycloakEnvironmentInitializationFilter;
import org.eclipse.che.multiuser.keycloak.server.UnavailableResourceInMultiUserFilter;

public class KeycloakServletModule extends ServletModule {

  private static final String KEYCLOAK_FILTER_PATHS =
      "^"
          // not equals to /keycloak/OIDCKeycloak.js
          + "(?!/keycloak/(OIDC|oidc)[^\\/]+$)"
          // not contains /docs/ (for swagger)
          + "(?!.*(/docs/))"
          // not ends with '/oauth/callback/' or '/oauth/1.0/callback/' or '/keycloak/settings/' or
          // '/system/state'
          + "(?!.*(/keycloak/settings/?|/oauth/callback/?|/oauth/1.0/callback/?|/system/state/?)$)"
          // all other
          + ".*";

  @Override
  protected void configureServlets() {
    filterRegex(KEYCLOAK_FILTER_PATHS).through(KeycloakEnvironmentInitializationFilter.class);
    filterRegex(KEYCLOAK_FILTER_PATHS).through(IdentityIdLoggerFilter.class);

    // Ban change password (POST /user/password) and create a user (POST /user/) methods
    // but not remove user (DELETE /user/{USER_ID}
    filterRegex("^/user(/password/?|/)?$").through(UnavailableResourceInMultiUserFilter.class);

    filterRegex("^/profile/(.*/)?attributes$").through(UnavailableResourceInMultiUserFilter.class);
  }
}
