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

import static com.google.common.base.Strings.isNullOrEmpty;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.opentracing.Tracer;
import java.io.IOException;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.commons.auth.token.RequestTokenExtractor;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.eclipse.che.multiuser.api.authentication.commons.SessionStore;
import org.eclipse.che.multiuser.api.authentication.commons.filter.EnvironmentInitalizationFilter;
import org.eclipse.che.multiuser.api.permission.server.AuthorizedSubject;
import org.eclipse.che.multiuser.api.permission.server.PermissionChecker;
import org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sets subject attribute into session based on keycloak authentication data.
 *
 * @author Max Shaposhnik (mshaposhnik@redhat.com)
 */
@Singleton
public class KeycloakEnvironmentInitalizationFilter extends EnvironmentInitalizationFilter {

  private static final Logger LOG =
      LoggerFactory.getLogger(KeycloakEnvironmentInitalizationFilter.class);

  private final KeycloakUserManager userManager;
  private final KeycloakProfileRetriever keycloakProfileRetriever;
  private final RequestTokenExtractor tokenExtractor;
  private final PermissionChecker permissionChecker;
  private final KeycloakSettings keycloakSettings;
  private final Tracer tracer;
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
    super(sessionStore, tokenExtractor);
    this.jwtParser = jwtParser;
    this.userManager = userManager;
    this.keycloakProfileRetriever = keycloakProfileRetriever;
    this.tokenExtractor = tokenExtractor;
    this.permissionChecker = permissionChecker;
    this.keycloakSettings = settings;
    this.tracer = tracer;
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
  protected String getUserId(String token) {
    Claims claims = jwtParser.parseClaimsJws(token).getBody();
    return claims.getSubject();
  }

  @Override
  public Subject extractSubject(String token) throws ServletException {

    Jws<Claims> jwt = jwtParser.parseClaimsJws(token);
    Claims claims = jwt.getBody();
    LOG.debug("JWT = {}", jwt);
    // OK, we can trust this JWT

    try {
      String username =
          claims.get(
              keycloakSettings.get().get(KeycloakConstants.USERNAME_CLAIM_SETTING),
              String.class);
      if (username == null) { // fallback to unique id promised by spec
        // https://openid.net/specs/openid-connect-basic-1_0.html#ClaimStability
        username = claims.getIssuer() + ":" + claims.getSubject();
      }
      String id = claims.getSubject();

      String email = retrieveEmail(token, claims, username, id);
      if (email == null) {
        throw new JwtException("Unable to authenticate user because email address is not set in keycloak profile");
      }
      User user = userManager.getOrCreateUser(id, email, username);
      return
          new AuthorizedSubject(
              new SubjectImpl(user.getName(), user.getId(), token, false), permissionChecker);
    } catch (ServerException | ConflictException e) {
      throw new ServletException(
          "Unable to identify user " + claims.getSubject() + " in Che database", e);
    }
  }

  private String retrieveEmail(String token, Claims claims,
      String username, String id) throws ServerException {
    String email = claims.get("email", String.class);

    if (isNullOrEmpty(email)) {
      boolean userNotFound = false;
      try {
        userManager.getById(id);
      } catch (NotFoundException e) {
        userNotFound = true;
      }
      if (userNotFound) {
        try {
          EnvironmentContext.getCurrent()
              .setSubject(new SubjectImpl(username, id, token, true));
          Map<String, String> profileAttributes =
              keycloakProfileRetriever.retrieveKeycloakAttributes();
          email = profileAttributes.get("email");
        } finally {
          EnvironmentContext.reset();
        }
      }
    }
    return email;
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

}
