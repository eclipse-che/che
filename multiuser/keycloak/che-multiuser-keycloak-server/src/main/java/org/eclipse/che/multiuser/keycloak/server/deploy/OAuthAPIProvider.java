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
import org.eclipse.che.multiuser.keycloak.server.oauth2.KeycloakOAuthAPI;
import org.eclipse.che.security.oauth.CheOAuthAPI;
import org.eclipse.che.security.oauth.OAuthAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides appropriate OAuth Authentication API depending on configuration.
 *
 * @author Mykhailo Kuznietsov.
 */
public class OAuthAPIProvider implements Provider<OAuthAPI> {
  private static final Logger LOG = LoggerFactory.getLogger(OAuthAPIProvider.class);
  private String oauthType;
  private Injector injector;

  @Inject
  public OAuthAPIProvider(@Nullable @Named("che.oauth.type") String oauthType, Injector injector) {
    this.oauthType = oauthType == null ? "" : oauthType;
    this.injector = injector;
  }

  @Override
  public OAuthAPI get() {
    switch (oauthType) {
      case "che":
        return injector.getInstance(CheOAuthAPI.class);
      case "keycloak":
        return injector.getInstance(KeycloakOAuthAPI.class);
      default:
        LOG.warn(
            "Unknown value configured for OAuth authentication service type, using OAuthAuthenticationService by default");
        return injector.getInstance(CheOAuthAPI.class);
    }
  }
}
