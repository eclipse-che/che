/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.keycloak.server.deploy;

import com.google.inject.servlet.ServletModule;
import javax.inject.Singleton;
import org.eclipse.che.commons.logback.filter.IdentityIdLoggerFilter;
import org.eclipse.che.multiuser.keycloak.server.KeycloakAuthenticationFilter;
import org.eclipse.che.multiuser.keycloak.server.KeycloakEnvironmentInitalizationFilter;
import org.eclipse.che.multiuser.keycloak.server.UnavailableResourceInMultiUserFilter;

public class KeycloakServletModule extends ServletModule {
  @Override
  protected void configureServlets() {
    bind(KeycloakAuthenticationFilter.class).in(Singleton.class);

    // Not contains /docs/ (for swagger) and not ends with '/oauth/callback/' or
    // '/keycloak/settings/' or '/system/state'
    filterRegex("^(?!.*(/docs/))(?!.*(/keycloak/settings/?|/oauth/callback/?|/system/state/?)$).*")
        .through(KeycloakAuthenticationFilter.class);
    filterRegex("^(?!.*(/docs/))(?!.*(/keycloak/settings/?|/oauth/callback/?|/system/state/?)$).*")
        .through(KeycloakEnvironmentInitalizationFilter.class);
    filterRegex("^(?!.*(/docs/))(?!.*(/keycloak/settings/?|/api/oauth/callback/?)$).*")
        .through(IdentityIdLoggerFilter.class);

    filterRegex("/user/?.*").through(UnavailableResourceInMultiUserFilter.class);
    filterRegex("/profile/.*/attributes").through(UnavailableResourceInMultiUserFilter.class);
  }
}
