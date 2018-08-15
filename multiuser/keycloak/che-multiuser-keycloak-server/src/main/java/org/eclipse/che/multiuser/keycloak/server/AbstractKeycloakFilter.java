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

import com.auth0.jwk.GuavaCachedJwkProvider;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.google.common.annotations.VisibleForTesting;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SigningKeyResolverAdapter;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Key;
import java.security.PublicKey;
import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base abstract class for the Keycloak-related servlet filters.
 *
 * <p>In particular it defines common use-cases when the authentication / multi-user logic should be
 * skipped
 */
public abstract class AbstractKeycloakFilter implements Filter {

  protected final JwtParser jwtParser;
  protected KeycloakSettings keycloakSettings;
  protected JwkProvider jwkProvider;

  private static final Logger LOG = LoggerFactory.getLogger(AbstractKeycloakFilter.class);

  public AbstractKeycloakFilter(KeycloakSettings keycloakSettings, long allowedClockSkewSec)
      throws MalformedURLException {
    this.keycloakSettings = keycloakSettings;
    String jwksUrl = keycloakSettings.get().get(KeycloakConstants.JWKS_ENDPOINT_SETTING);
    if (jwksUrl != null) {
      this.jwkProvider = new GuavaCachedJwkProvider(new UrlJwkProvider(new URL(jwksUrl)));
    }
    jwtParser =
        Jwts.parser()
            .setAllowedClockSkewSeconds(allowedClockSkewSec)
            .setSigningKeyResolver(new SigningKeyResolverByKind());
  }

  /** when a request came from a machine with valid token then auth is not required */
  protected boolean shouldSkipAuthentication(HttpServletRequest request, String token) {
    if (token == null) {
      if (request.getRequestURI() != null
          && request.getRequestURI().endsWith("api/keycloak/OIDCKeycloak.js")) {
        return true;
      }
      return false;
    }
    try {
      jwtParser.parse(token);
      return false;
    } catch (MachineTokenJwtException e) {
      return true;
    }
  }

  @Override
  public void init(FilterConfig filterConfig) {}

  @Override
  public void destroy() {}

  @VisibleForTesting
  class SigningKeyResolverByKind extends SigningKeyResolverAdapter {

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
  }

  protected synchronized PublicKey getJwtPublicKey(JwsHeader<?> header) {
    String kid = header.getKeyId();
    if (header.getKeyId() == null) {
      LOG.warn(
          "'kid' is missing in the JWT token header. This is not possible to validate the token with OIDC provider keys");
      throw new JwtException("'kid' is missing in the JWT token header.");
    }
    if (jwkProvider == null) {
      LOG.warn(
          "JWK provider is not available: This is not possible to validate the token with OIDC provider keys.\n"
              + "Please look into the startup logs to find out the root cause");
      throw new JwtException("JWK provider is not available");
    }
    try {
      return jwkProvider.get(kid).getPublicKey();
    } catch (JwkException e) {
      throw new JwtException(
          "Error during the retrieval of the public key during JWT token validation", e);
    }
  }

  private class MachineTokenJwtException extends JwtException {
    public MachineTokenJwtException() {
      super("This is an machine token");
    }
  }
}
