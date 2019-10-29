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
import static org.eclipse.che.multiuser.machine.authentication.shared.Constants.USER_ID_CLAIM;

import io.jsonwebtoken.Claims;
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
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.commons.auth.token.RequestTokenExtractor;
import org.eclipse.che.multiuser.api.authentication.commons.SessionStore;
import org.eclipse.che.multiuser.api.authentication.commons.filter.SessionCachingFilter;
import org.eclipse.che.multiuser.api.permission.server.PermissionChecker;

/**
 * Handles requests that comes from machines with specific machine token.
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 * @author Anton Korneta
 */
@Singleton
public class MachineLoginFilter extends SessionCachingFilter {

  private final RequestTokenExtractor tokenExtractor;
  private final JwtParser jwtParser;

  @Inject
  public MachineLoginFilter(
      SessionStore sessionStore,
      RequestTokenExtractor tokenExtractor,
      UserManager userManager,
      MachineSigningKeyResolver machineKeyResolver,
      PermissionChecker permissionChecker) {
    super(sessionStore, tokenExtractor, new MachineTokenSubjectSupplier(Jwts.parser().setSigningKeyResolver(machineKeyResolver), userManager,
        permissionChecker));
    this.tokenExtractor = tokenExtractor;
    this.jwtParser = Jwts.parser().setSigningKeyResolver(machineKeyResolver);
  }

  @Override
  public void init(FilterConfig filterConfig) {
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    final String token = tokenExtractor.getToken((HttpServletRequest) request);
    if (isNullOrEmpty(token)) {
      // note that unmodified request returned
      chain.doFilter(request, response);
      return;
    }

    super.doFilter(request, response, chain);
  }

  @Override
  protected String getUserId(HttpServletRequest httpRequest) {
    final Claims claims = jwtParser.parseClaimsJws(tokenExtractor.getToken(httpRequest)).getBody();
    return claims.get(USER_ID_CLAIM, String.class);
  }

  @Override
  public void destroy() {
  }
}
