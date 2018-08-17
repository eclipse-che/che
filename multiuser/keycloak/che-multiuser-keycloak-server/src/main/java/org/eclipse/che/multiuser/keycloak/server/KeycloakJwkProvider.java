/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
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

import com.auth0.jwk.GuavaCachedJwkProvider;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import java.net.MalformedURLException;
import java.net.URL;
import javax.inject.Inject;
import javax.inject.Provider;
import org.eclipse.che.inject.ConfigurationException;
import org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants;

/** Constructs {@link UrlJwkProvider} based on Jwk endpoint from keycloak settings */
public class KeycloakJwkProvider implements Provider<JwkProvider> {

  private final JwkProvider jwkProvider;

  @Inject
  public KeycloakJwkProvider(KeycloakSettings keycloakSettings) throws MalformedURLException {
    final String jwksUrl = keycloakSettings.get().get(KeycloakConstants.JWKS_ENDPOINT_SETTING);
    if (jwksUrl == null) {
      throw new ConfigurationException("Jwks endpoint url not found in keycloak settings");
    }
    this.jwkProvider = new GuavaCachedJwkProvider(new UrlJwkProvider(new URL(jwksUrl)));
  }

  @Override
  public JwkProvider get() {
    return jwkProvider;
  }
}
