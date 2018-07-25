/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.machine.authentication.server;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.eclipse.che.multiuser.machine.authentication.shared.Constants.MACHINE_TOKEN_KIND;
import static org.eclipse.che.multiuser.machine.authentication.shared.Constants.USER_ID_CLAIM;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import java.io.IOException;
import java.security.Principal;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.commons.auth.token.RequestTokenExtractor;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.eclipse.che.multiuser.api.permission.server.AuthorizedSubject;
import org.eclipse.che.multiuser.api.permission.server.PermissionChecker;
import org.eclipse.che.multiuser.machine.authentication.server.signature.SignatureKeyManager;

/**
 * Handles requests that comes from machines with specific machine token.
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 * @author Anton Korneta
 */
@Singleton
public class MachineLoginFilter implements Filter {

  private final RequestTokenExtractor tokenExtractor;
  private final UserManager userManager;
  private final SignatureKeyManager keyManager;
  private final PermissionChecker permissionChecker;

  @Inject
  public MachineLoginFilter(
      RequestTokenExtractor tokenExtractor,
      UserManager userManager,
      SignatureKeyManager keyManager,
      PermissionChecker permissionChecker) {
    this.tokenExtractor = tokenExtractor;
    this.userManager = userManager;
    this.keyManager = keyManager;
    this.permissionChecker = permissionChecker;
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {}

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    final HttpServletRequest httpRequest = (HttpServletRequest) request;
    final String token = tokenExtractor.getToken(httpRequest);

    if (isNullOrEmpty(token)) {
      chain.doFilter(request, response);
      return;
    }

    // check token signature and verify is this token machine or not
    try {
      final Jws<Claims> jwt =
          Jwts.parser().setSigningKey(keyManager.getKeyPair().getPublic()).parseClaimsJws(token);
      final Claims claims = jwt.getBody();

      if (!isMachineToken(jwt)) {
        chain.doFilter(request, response);
        return;
      }

      try {
        final String userId = claims.get(USER_ID_CLAIM, String.class);
        // check if user with such id exists
        final String userName = userManager.getById(userId).getName();
        final Subject authorizedSubject =
            new AuthorizedSubject(
                new SubjectImpl(userName, userId, token, false), permissionChecker);
        EnvironmentContext.getCurrent().setSubject(authorizedSubject);
        chain.doFilter(addUserInRequest(httpRequest, authorizedSubject), response);
      } catch (NotFoundException ex) {
        sendErr(
            response,
            SC_UNAUTHORIZED,
            "Authentication with machine token failed because user for this token no longer exist.");
      } catch (ServerException ex) {
        sendErr(
            response,
            SC_UNAUTHORIZED,
            format("Authentication with machine token failed cause: %s", ex.getMessage()));
      } finally {
        EnvironmentContext.reset();
      }
    } catch (UnsupportedJwtException
        | MalformedJwtException
        | SignatureException
        | ExpiredJwtException ex) {
      // signature check failed
      chain.doFilter(request, response);
    }
  }

  /** Checks whether given token from a machine. */
  private boolean isMachineToken(Jws<Claims> jwt) {
    return MACHINE_TOKEN_KIND.equals(jwt.getHeader().get("kind"));
  }

  /** Sets given error code with err message into give response. */
  private static void sendErr(ServletResponse res, int errCode, String msg) throws IOException {
    final HttpServletResponse response = (HttpServletResponse) res;
    response.sendError(errCode, msg);
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

  @Override
  public void destroy() {}
}
