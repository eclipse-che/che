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
import io.jsonwebtoken.JwtParser;
import io.opentracing.Tracer;
import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.che.commons.auth.token.RequestTokenExtractor;
import org.eclipse.che.multiuser.api.authentication.commons.SessionStore;
import org.eclipse.che.multiuser.api.authentication.commons.filter.SessionCachingFilter;
import org.eclipse.che.multiuser.api.permission.server.PermissionChecker;

/**
 * Sets subject attribute into session based on keycloak authentication data.
 *
 * @author Max Shaposhnik (mshaposhnik@redhat.com)
 */
@Singleton
public class KeycloakEnvironmentInitalizationFilter extends SessionCachingFilter {

  private final RequestTokenExtractor tokenExtractor;
  private final JwtParser jwtParser;

  @Inject
  public KeycloakEnvironmentInitalizationFilter(
      SessionStore sessionStore,
      JwtParser jwtParser,
      KeycloakUserManager userManager,
      KeycloakProfileRetriever keycloakProfileRetriever,
      RequestTokenExtractor tokenExtractor,
      PermissionChecker permissionChecker,
      KeycloakSettings settings,
      Tracer tracer) {
    super(sessionStore, tokenExtractor, new KeycloakTokenSubjectSupplpier(jwtParser, userManager, permissionChecker, keycloakProfileRetriever, settings));
    this.jwtParser = jwtParser;
    this.tokenExtractor = tokenExtractor;
  }

  @Override
  public void init(FilterConfig filterConfig) {
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {

    final String token = tokenExtractor.getToken((HttpServletRequest) request);
    if (token == null) {
      sendError(response, 401, "Authorization token is missed");
      return;
    }
    if (shouldSkipAuthentication(token)) {
      // note that unmodified request returned
      filterChain.doFilter(request, response);
      return;
    }

    super.doFilter(request, response, filterChain);
  }



  @Override
  protected String getUserId(HttpServletRequest httpRequest) {
    final String token = tokenExtractor.getToken(httpRequest);
    Claims claims = jwtParser.parseClaimsJws(token).getBody();
    return claims.getSubject();
  }

  /**
   * when a request came from a machine with valid token then auth is not required
   */
  private boolean shouldSkipAuthentication(String token) {
    try {
      jwtParser.parse(token);
      return false;
    } catch (MachineTokenJwtException e) {
      return true;
    }
  }

  private void sendError(ServletResponse res, int errorCode, String message) throws IOException {
    HttpServletResponse response = (HttpServletResponse) res;
    response.getOutputStream().write(message.getBytes());
    response.setStatus(errorCode);
  }
}
