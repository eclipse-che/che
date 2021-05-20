/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
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
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

import com.google.common.base.Splitter;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.eclipse.che.multiuser.api.authentication.commons.SessionStore;
import org.eclipse.che.multiuser.api.authentication.commons.filter.MultiUserEnvironmentInitializationFilter;
import org.eclipse.che.multiuser.api.authentication.commons.token.RequestTokenExtractor;
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
public class KeycloakEnvironmentInitializationFilter
    extends MultiUserEnvironmentInitializationFilter {

  private static final Logger LOG =
      LoggerFactory.getLogger(KeycloakEnvironmentInitializationFilter.class);

  private final KeycloakUserManager userManager;
  private final KeycloakProfileRetriever keycloakProfileRetriever;
  private final PermissionChecker permissionChecker;
  private final KeycloakSettings keycloakSettings;
  private final JwtParser jwtParser;
  private final Map<String, String> userNameReplacementPatterns;

  @Inject
  public KeycloakEnvironmentInitializationFilter(
      SessionStore sessionStore,
      JwtParser jwtParser,
      KeycloakUserManager userManager,
      KeycloakProfileRetriever keycloakProfileRetriever,
      RequestTokenExtractor tokenExtractor,
      PermissionChecker permissionChecker,
      KeycloakSettings settings,
      @Nullable @Named("che.keycloak.username.replacement_patterns")
          String userNameReplacementPatterns) {
    super(sessionStore, tokenExtractor);
    this.jwtParser = jwtParser;
    this.userManager = userManager;
    this.keycloakProfileRetriever = keycloakProfileRetriever;
    this.permissionChecker = permissionChecker;
    this.keycloakSettings = settings;
    this.userNameReplacementPatterns =
        isNullOrEmpty(userNameReplacementPatterns)
            ? Collections.emptyMap()
            : Splitter.on(",").withKeyValueSeparator("=").split(userNameReplacementPatterns);
  }

  @Override
  public void init(FilterConfig filterConfig) {}

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {
    try {
      super.doFilter(request, response, filterChain);
    } catch (MachineTokenJwtException mte) {
      filterChain.doFilter(request, response);
    } catch (JwtException e) {
      sendError(response, SC_UNAUTHORIZED, e.getMessage());
      return;
    }
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
              keycloakSettings.get().get(KeycloakConstants.USERNAME_CLAIM_SETTING), String.class);
      if (username == null) { // fallback to unique id promised by spec
        // https://openid.net/specs/openid-connect-basic-1_0.html#ClaimStability
        username = claims.getIssuer() + ":" + claims.getSubject();
      }
      if (!userNameReplacementPatterns.isEmpty()) {
        for (Map.Entry<String, String> entry : userNameReplacementPatterns.entrySet()) {
          username = username.replaceAll(entry.getKey(), entry.getValue());
        }
      }
      String id = claims.getSubject();

      String email =
          retrieveEmail(token, claims, id)
              .orElseThrow(
                  () ->
                      new JwtException(
                          "Unable to authenticate user because email address is not set in keycloak profile"));
      User user = userManager.getOrCreateUser(id, email, username);
      return new AuthorizedSubject(
          new SubjectImpl(user.getName(), user.getId(), token, false), permissionChecker);
    } catch (ServerException | ConflictException e) {
      throw new ServletException(
          "Unable to identify user " + claims.getSubject() + " in Che database", e);
    }
  }

  @Override
  protected void handleMissingToken(
      ServletRequest request, ServletResponse response, FilterChain chain) throws IOException {
    sendError(response, 401, "Authorization token is missing");
  }

  private Optional<String> retrieveEmail(String token, Claims claims, String id)
      throws ServerException {
    String email = claims.get("email", String.class);

    if (isNullOrEmpty(email)) {
      try {
        userManager.getById(id);
      } catch (NotFoundException e) {
        Map<String, String> profileAttributes =
            keycloakProfileRetriever.retrieveKeycloakAttributes("Bearer " + token);
        email = profileAttributes.get("email");
      }
    }
    return Optional.ofNullable(email);
  }

  @Override
  public void destroy() {}
}
