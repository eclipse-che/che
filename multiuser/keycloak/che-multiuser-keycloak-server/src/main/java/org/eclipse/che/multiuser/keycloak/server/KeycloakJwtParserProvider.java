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

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants;

/** Provides instance of {@link JwtParser} */
@Singleton
public class KeycloakJwtParserProvider implements Provider<JwtParser> {

  private final JwtParser jwtParser;

  @Inject
  public KeycloakJwtParserProvider(
      @Named(KeycloakConstants.ALLOWED_CLOCK_SKEW_SEC) long allowedClockSkewSec,
      KeycloakSigningKeyResolver keycloakSigningKeyResolver) {
    this.jwtParser =
        Jwts.parser()
            .setAllowedClockSkewSeconds(allowedClockSkewSec)
            .setSigningKeyResolver(keycloakSigningKeyResolver);
  }

  @Override
  public JwtParser get() {
    return jwtParser;
  }
}
