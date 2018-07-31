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

import com.google.inject.Injector;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.multiuser.keycloak.server.oauth2.DelegatedOAuthAPI;
import org.eclipse.che.security.oauth.EmbeddedOAuthAPI;
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
  public OAuthAPIProvider(
      @Nullable @Named("che.oauth.service_mode") String oauthType, Injector injector) {
    this.oauthType = oauthType;
    this.injector = injector;
  }

  @Override
  public OAuthAPI get() {
    switch (oauthType) {
      case "embedded":
        return injector.getInstance(EmbeddedOAuthAPI.class);
      case "delegated":
        return injector.getInstance(DelegatedOAuthAPI.class);
      default:
        throw new RuntimeException(
            "Unknown value configured for \"che.oauth.service_mode\", must be either \"embedded\", or \"delegated\"");
    }
  }
}
