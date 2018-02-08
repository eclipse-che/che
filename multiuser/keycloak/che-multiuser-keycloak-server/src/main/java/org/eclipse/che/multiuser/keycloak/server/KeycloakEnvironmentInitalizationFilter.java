/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.keycloak.server;

import static java.util.Collections.emptyList;
import static org.eclipse.che.commons.lang.NameGenerator.generate;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import java.io.IOException;
import java.security.Principal;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.commons.auth.token.RequestTokenExtractor;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.eclipse.che.multiuser.api.permission.server.AuthorizedSubject;
import org.eclipse.che.multiuser.api.permission.server.PermissionChecker;

/**
 * Sets subject attribute into session based on keycloak authentication data.
 *
 * @author Max Shaposhnik (mshaposhnik@redhat.com)
 */
@Singleton
public class KeycloakEnvironmentInitalizationFilter extends AbstractKeycloakFilter {

  private final UserManager userManager;
  private final RequestTokenExtractor tokenExtractor;
  private final PermissionChecker permissionChecker;

  @Inject
  public KeycloakEnvironmentInitalizationFilter(
      UserManager userManager,
      RequestTokenExtractor tokenExtractor,
      PermissionChecker permissionChecker) {
    this.userManager = userManager;
    this.tokenExtractor = tokenExtractor;
    this.permissionChecker = permissionChecker;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {

    final HttpServletRequest httpRequest = (HttpServletRequest) request;
    final String token = tokenExtractor.getToken(httpRequest);
    if (shouldSkipAuthentication(httpRequest, token)) {
      filterChain.doFilter(request, response);
      return;
    }

    final HttpSession session = httpRequest.getSession();
    Subject subject = (Subject) session.getAttribute("che_subject");
    if (subject == null || !subject.getToken().equals(token)) {
      Jwt jwtToken = (Jwt) httpRequest.getAttribute("token");
      if (jwtToken == null) {
        throw new ServletException("Cannot detect or instantiate user.");
      }
      Claims claims = (Claims) jwtToken.getBody();

      try {
        User user =
            getOrCreateUser(
                claims.getSubject(),
                claims.get("email", String.class),
                claims.get("preferred_username", String.class));
        subject =
            new AuthorizedSubject(
                new SubjectImpl(user.getName(), user.getId(), token, false), permissionChecker);
        session.setAttribute("che_subject", subject);
      } catch (ServerException | ConflictException e) {
        throw new ServletException(
            "Unable to identify user " + claims.getSubject() + " in Che database", e);
      }
    }

    try {
      EnvironmentContext.getCurrent().setSubject(subject);
      filterChain.doFilter(addUserInRequest(httpRequest, subject), response);
    } finally {
      EnvironmentContext.reset();
    }
  }

  private User getOrCreateUser(String id, String email, String username)
      throws ServerException, ConflictException {
    Optional<User> user = getUser(id);
    if (!user.isPresent()) {
      synchronized (this) {
        user = getUser(id);
        if (!user.isPresent()) {
          final UserImpl cheUser = new UserImpl(id, email, username, generate("", 12), emptyList());
          try {
            return userManager.create(cheUser, false);
          } catch (ConflictException ex) {
            cheUser.setName(generate(cheUser.getName(), 4));
            return userManager.create(cheUser, false);
          }
        }
      }
    }
    return actualizeUser(user.get(), email);
  }
  /** Performs check that emails in JWT and local DB are match, and synchronize them otherwise */
  private User actualizeUser(User actualUser, String email) throws ServerException {
    if (actualUser.getEmail().equals(email)) {
      return actualUser;
    }
    UserImpl update = new UserImpl(actualUser);
    update.setEmail(email);
    try {
      userManager.update(update);
    } catch (NotFoundException e) {
      throw new ServerException("Unable to actualize user email. User not found.", e);
    } catch (ConflictException e) {
      throw new ServerException(
          "Unable to actualize user email. Another user with such email exists", e);
    }
    return update;
  }

  private Optional<User> getUser(String id) throws ServerException {
    try {
      return Optional.of(userManager.getById(id));
    } catch (NotFoundException e) {
      return Optional.empty();
    }
  }

  private HttpServletRequest addUserInRequest(
      final HttpServletRequest httpRequest, final Subject subject) {
    return new HttpServletRequestWrapper(httpRequest) {
      @Override
      public String getRemoteUser() {
        return subject.getUserName();
      }

      @Override
      public Principal getUserPrincipal() {
        return subject::getUserName;
      }
    };
  }
}
