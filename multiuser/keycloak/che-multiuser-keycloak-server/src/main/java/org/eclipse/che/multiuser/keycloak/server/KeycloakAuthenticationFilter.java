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

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.che.commons.auth.token.RequestTokenExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class KeycloakAuthenticationFilter extends AbstractKeycloakFilter {

  private static final Logger LOG = LoggerFactory.getLogger(KeycloakAuthenticationFilter.class);

  private RequestTokenExtractor tokenExtractor;

  @Inject
  public KeycloakAuthenticationFilter(RequestTokenExtractor tokenExtractor) {
    this.tokenExtractor = tokenExtractor;
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) req;

    final String token = tokenExtractor.getToken(request);
    if (token == null) {
      send401(res, "Authorization token is missed");
      return;
    }

    Jws<Claims> jwt;
    try {
      if (shouldSkipAuthentication(request, token)) {
        chain.doFilter(req, res);
        return;
      }
      jwt = jwtParser.parseClaimsJws(token);
      LOG.debug("JWT = ", jwt);
      // OK, we can trust this JWT
    } catch (ExpiredJwtException e) {
      send401(res, "The specified token is expired");
      return;
    } catch (JwtException e) {
      send401(res, "Token validation failed: " + e.getMessage());
      return;
    }
    request.setAttribute("token", jwt);
    chain.doFilter(req, res);
  }

  private void send401(ServletResponse res, String message) throws IOException {
    HttpServletResponse response = (HttpServletResponse) res;
    response.getOutputStream().write(message.getBytes());
    response.setStatus(401);
  }
}
