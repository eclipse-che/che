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

import static org.eclipse.che.multiuser.machine.authentication.shared.Constants.MACHINE_TOKEN_KIND;

import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.SigningKeyResolverAdapter;
import java.security.Key;
import java.security.PublicKey;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Resolves signing key based on id from JWT header */
@Singleton
public class KeycloakSigningKeyResolver extends SigningKeyResolverAdapter {

  private final JwkProvider jwkProvider;

  private static final Logger LOG = LoggerFactory.getLogger(KeycloakSigningKeyResolver.class);

  @Inject
  KeycloakSigningKeyResolver(JwkProvider jwkProvider) {
    this.jwkProvider = jwkProvider;
  }

  @Override
  public Key resolveSigningKey(JwsHeader header, String plaintext) {
    if (MACHINE_TOKEN_KIND.equals(header.get("kind"))) {
      throw new MachineTokenJwtException(); // machine token, doesn't need to verify
    }
    return getJwtPublicKey(header);
  }

  @Override
  public Key resolveSigningKey(JwsHeader header, Claims claims) {
    if (MACHINE_TOKEN_KIND.equals(header.get("kind"))) {
      throw new MachineTokenJwtException(); // machine token, doesn't need to verify
    }
    return getJwtPublicKey(header);
  }

  private synchronized PublicKey getJwtPublicKey(JwsHeader<?> header) {
    String kid = header.getKeyId();
    if (header.getKeyId() == null) {
      LOG.warn(
          "'kid' is missing in the JWT token header. This is not possible to validate the token with OIDC provider keys");
      throw new JwtException("'kid' is missing in the JWT token header.");
    }
    try {
      return jwkProvider.get(kid).getPublicKey();
    } catch (JwkException e) {
      throw new JwtException(
          "Error during the retrieval of the public key during JWT token validation", e);
    }
  }
}
