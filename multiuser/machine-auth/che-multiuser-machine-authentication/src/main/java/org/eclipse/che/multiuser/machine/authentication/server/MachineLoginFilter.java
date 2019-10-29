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
import static org.eclipse.che.multiuser.machine.authentication.shared.Constants.USER_ID_CLAIM;
import static org.eclipse.che.multiuser.machine.authentication.shared.Constants.WORKSPACE_ID_CLAIM;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.commons.auth.token.RequestTokenExtractor;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.eclipse.che.multiuser.api.authentication.commons.SessionStore;
import org.eclipse.che.multiuser.api.authentication.commons.filter.EnvironmentInitalizationFilter;
import org.eclipse.che.multiuser.api.permission.server.PermissionChecker;

/**
 * Handles requests that comes from machines with specific machine token.
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 * @author Anton Korneta
 */
@Singleton
public class MachineLoginFilter extends EnvironmentInitalizationFilter {

  private final RequestTokenExtractor tokenExtractor;
  private final UserManager userManager;
  private final JwtParser jwtParser;
  private final PermissionChecker permissionChecker;

  @Inject
  public MachineLoginFilter(
      SessionStore sessionStore,
      RequestTokenExtractor tokenExtractor,
      UserManager userManager,
      MachineSigningKeyResolver machineKeyResolver,
      PermissionChecker permissionChecker) {
    super(sessionStore, tokenExtractor);
    this.tokenExtractor = tokenExtractor;
    this.userManager = userManager;
    this.jwtParser = Jwts.parser().setSigningKeyResolver(machineKeyResolver);
    this.permissionChecker = permissionChecker;
  }

  @Override
  public void init(FilterConfig filterConfig) {
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    final String token = tokenExtractor.getToken((HttpServletRequest) request);
    if (isNullOrEmpty(token)) {
      chain.doFilter(request, response);
      return;
    }
    try {
      super.doFilter(request, response, chain);
    } catch (NotMachineTokenJwtException e) {
      chain.doFilter(request, response);
    }
  }

  @Override
  public Subject extractSubject(String token) {
    try {
      final Claims claims = jwtParser.parseClaimsJws(token).getBody();
      final String userId = claims.get(USER_ID_CLAIM, String.class);
      // check if user with such id exists
      final String userName = userManager.getById(userId).getName();
      final String workspaceId = claims.get(WORKSPACE_ID_CLAIM, String.class);
      return new MachineTokenAuthorizedSubject(
          new SubjectImpl(userName, userId, token, false), permissionChecker, workspaceId);
    } catch (NotFoundException e) {
      throw new JwtException("Authentication with machine token failed because user for this token no longer exist.");
    } catch (ServerException | JwtException e) {
      throw new JwtException(format("Authentication with machine token failed cause: %s", e.getMessage()), e);
    }
  }


  @Override
  protected String getUserId(String token) {
    final Claims claims = jwtParser.parseClaimsJws(token).getBody();
    return claims.get(USER_ID_CLAIM, String.class);
  }

  @Override
  public void destroy() {
  }
}
