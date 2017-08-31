/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.keycloak.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.che.commons.auth.token.RequestTokenExtractor;
import org.eclipse.che.keycloak.shared.KeycloakConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class KeycloakAuthenticationFilter extends AbstractKeycloakFilter {

  private static final Logger LOG = LoggerFactory.getLogger(KeycloakAuthenticationFilter.class);

  private String authServerUrl;
  private String realm;
  private PublicKey publicKey = null;
  private RequestTokenExtractor tokenExtractor;

  @Inject
  public KeycloakAuthenticationFilter(
      @Named(KeycloakConstants.AUTH_SERVER_URL_SETTING) String authServerUrl,
      @Named(KeycloakConstants.REALM_SETTING) String realm,
      RequestTokenExtractor tokenExtractor) {
    this.authServerUrl = authServerUrl;
    this.realm = realm;
    this.tokenExtractor = tokenExtractor;
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) req;

    final String token = tokenExtractor.getToken(request);
    if (shouldSkipAuthentication(request, token)) {
      chain.doFilter(req, res);
      return;
    }

    final String requestURI = request.getRequestURI();
    if (token == null) {
      LOG.debug("No 'Authorization' header for {}", requestURI);
      send403(res);
      return;
    }

    Jws<Claims> jwt;
    try {
      jwt = Jwts.parser().setSigningKey(getJwtPublicKey(false)).parseClaimsJws(token);
      LOG.debug("JWT = " + jwt.toString());
      //OK, we can trust this JWT
    } catch (SignatureException
        | NoSuchAlgorithmException
        | InvalidKeySpecException
        | IllegalArgumentException e) {
      //don't trust the JWT!
      LOG.error("Failed verifying the JWT token", e);
      try {
        LOG.info("Retrying after updating the public key", e);
        jwt = Jwts.parser().setSigningKey(getJwtPublicKey(true)).parseClaimsJws(token);
        LOG.debug("JWT = " + jwt.toString());
        //OK, we can trust this JWT
      } catch (SignatureException
          | NoSuchAlgorithmException
          | InvalidKeySpecException
          | IllegalArgumentException ee) {
        //don't trust the JWT!
        LOG.error("Failed verifying the JWT token after public key update", e);
        send403(res);
        return;
      }
    }
    request.setAttribute("token", jwt);
    chain.doFilter(req, res);
  }

  private synchronized PublicKey getJwtPublicKey(boolean reset)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    if (reset) {
      publicKey = null;
    }
    if (publicKey == null) {
      URL url;
      HttpURLConnection conn;
      try {
        url = new URL(authServerUrl + "/realms/" + realm);
        LOG.info("Pulling realm public key from URL : {}", url);
        conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        Map<String, String> realmSettings;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
          ObjectMapper mapper = new ObjectMapper();
          //noinspection unchecked
          realmSettings = mapper.readValue(in, Map.class);
        }
        String encodedPublicKey = realmSettings.get("public_key");
        byte[] decoded = Base64.getDecoder().decode(encodedPublicKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        publicKey = kf.generatePublic(keySpec);
      } catch (IOException e) {
        LOG.error("Exception during retrieval of the Keycloak realm public key", e);
      }
    }
    return publicKey;
  }

  private void send403(ServletResponse res) throws IOException {
    HttpServletResponse response = (HttpServletResponse) res;
    response.sendError(403);
  }
}
