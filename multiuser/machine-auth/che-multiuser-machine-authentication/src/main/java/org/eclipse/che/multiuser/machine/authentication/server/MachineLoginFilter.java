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
package org.eclipse.che.multiuser.machine.authentication.server;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.eclipse.che.multiuser.machine.authentication.shared.Constants.USER_ID_CLAIM;
import static org.eclipse.che.multiuser.machine.authentication.shared.Constants.WORKSPACE_ID_CLAIM;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
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
import javax.servlet.http.HttpSession;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.commons.auth.token.RequestTokenExtractor;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.eclipse.che.multiuser.api.permission.server.PermissionChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles requests that comes from machines with specific machine token.
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 * @author Anton Korneta
 */
@Singleton
public class MachineLoginFilter implements Filter {

  private static final Logger LOG = LoggerFactory.getLogger(MachineLoginFilter.class);

  private final RequestTokenExtractor tokenExtractor;
  private final UserManager userManager;
  private final PermissionChecker permissionChecker;
  private final JwtParser jwtParser;

  @Inject
  public MachineLoginFilter(
      RequestTokenExtractor tokenExtractor,
      UserManager userManager,
      MachineSigningKeyResolver machineKeyResolver,
      PermissionChecker permissionChecker) {
    this.tokenExtractor = tokenExtractor;
    this.userManager = userManager;
    this.permissionChecker = permissionChecker;
    this.jwtParser = Jwts.parser().setSigningKeyResolver(machineKeyResolver);
  }

  @Override
  public void init(FilterConfig filterConfig) {}

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
      HttpSession session = ((HttpServletRequest) request).getSession(true);
      Subject sessionSubject = (Subject) session.getAttribute("che_subject");
      if (sessionSubject == null || !sessionSubject.getToken().equals(token)) {
        try {
          sessionSubject = extractSubject(token);
          session.setAttribute("che_subject", sessionSubject);
        } catch (NotFoundException e) {
          sendErr(
              response,
              SC_UNAUTHORIZED,
              "Authentication with machine token failed because user for this token no longer exist.");
          return;
        }
      }

      try {
        EnvironmentContext.getCurrent().setSubject(sessionSubject);
        chain.doFilter(addUserInRequest(httpRequest, sessionSubject), response);
      } finally {
        EnvironmentContext.reset();
      }
    } catch (NotMachineTokenJwtException ex) {
      // not a machine token, bypass
      chain.doFilter(request, response);
    } catch (ServerException | JwtException e) {
      sendErr(
          response,
          SC_UNAUTHORIZED,
          format("Authentication with machine token failed cause: %s", e.getMessage()));
    }
  }

  private Subject extractSubject(String token) throws NotFoundException, ServerException {
    final Claims claims = jwtParser.parseClaimsJws(token).getBody();
    final String userId = claims.get(USER_ID_CLAIM, String.class);
    // check if user with such id exists
    final String userName = userManager.getById(userId).getName();
    final String workspaceId = claims.get(WORKSPACE_ID_CLAIM, String.class);
    return new MachineTokenAuthorizedSubject(
        new SubjectImpl(userName, userId, token, false), permissionChecker, workspaceId);
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
