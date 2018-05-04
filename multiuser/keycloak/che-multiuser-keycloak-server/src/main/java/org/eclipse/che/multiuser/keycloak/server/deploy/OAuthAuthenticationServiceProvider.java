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
package org.eclipse.che.multiuser.keycloak.server.deploy;

import com.google.inject.Injector;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.multiuser.keycloak.server.oauth2.KeycloakOAuthAuthenticationService;
import org.eclipse.che.security.oauth.CheOAuthAuthenticationService;
import org.eclipse.che.security.oauth.OAuthAuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides appropriate OAuth Authentication REST service depending on configuration.
 *
 * @author Mykhailo Kuznietsov.
 */
public class OAuthAuthenticationServiceProvider implements Provider<OAuthAuthenticationService> {
  private static final Logger LOG =
      LoggerFactory.getLogger(OAuthAuthenticationServiceProvider.class);
  final String oauthType;
  final Injector injector;

  @Inject
  public OAuthAuthenticationServiceProvider(
      @Nullable @Named("che.oauth.type") String oauthType, Injector injector) {
    this.oauthType = oauthType == null ? "" : oauthType;
    this.injector = injector;
  }

  @Override
  public OAuthAuthenticationService get() {
    switch (oauthType) {
      case "che":
        return injector.getInstance(CheOAuthAuthenticationService.class);
      case "keycloak":
        return injector.getInstance(KeycloakOAuthAuthenticationService.class);
      default:
        LOG.warn(
            "Unknown value configured for OAuth authentication service type, using CheOAuthAuthenticationService by default");
        return injector.getInstance(CheOAuthAuthenticationService.class);
    }
  }
}
