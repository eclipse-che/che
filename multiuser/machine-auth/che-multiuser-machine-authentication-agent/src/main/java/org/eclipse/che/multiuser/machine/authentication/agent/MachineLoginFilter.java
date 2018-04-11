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
package org.eclipse.che.multiuser.machine.authentication.agent;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.eclipse.che.multiuser.machine.authentication.shared.Constants.MACHINE_TOKEN_KIND;
import static org.eclipse.che.multiuser.machine.authentication.shared.Constants.USER_ID_CLAIM;
import static org.eclipse.che.multiuser.machine.authentication.shared.Constants.USER_NAME_CLAIM;
import static org.eclipse.che.multiuser.machine.authentication.shared.Constants.WORKSPACE_ID_CLAIM;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import java.io.IOException;
import java.security.PublicKey;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.eclipse.che.commons.auth.token.RequestTokenExtractor;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;

/**
 * Protects user's machine from unauthorized access.
 *
 * @author Anton Korneta
 */
@Singleton
public class MachineLoginFilter implements Filter {

  private final PublicKey publicKey;
  private final RequestTokenExtractor tokenExtractor;

  private final String workspaceId;

  @Inject
  public MachineLoginFilter(
      @Named("env.CHE_WORKSPACE_ID") String workspaceId,
      @Named("signature.public.key") PublicKey publicKey,
      RequestTokenExtractor tokenExtractor) {
    this.tokenExtractor = tokenExtractor;
    this.publicKey = publicKey;
    this.workspaceId = workspaceId;
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {}

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    final HttpServletRequest httpRequest = (HttpServletRequest) request;
    final HttpSession session = httpRequest.getSession(false);

    // sets subject from session
    final Subject sessionSubject;
    if (session != null && (sessionSubject = (Subject) session.getAttribute("principal")) != null) {
      try {
        EnvironmentContext.getCurrent().setSubject(sessionSubject);
        chain.doFilter(request, response);
        return;
      } finally {
        EnvironmentContext.reset();
      }
    }

    // retrieves a token from a request and verify it
    final String token = tokenExtractor.getToken(httpRequest);
    if (isNullOrEmpty(token)) {
      sendErr(response, SC_UNAUTHORIZED, "Authentication on machine failed, token is missed.");
      return;
    }

    // checks token signature and workspace identifier if ok then sets subject into the context
    try {
      final Jws<Claims> jwt = Jwts.parser().setSigningKey(publicKey).parseClaimsJws(token);
      final Claims claims = jwt.getBody();
      if (!isValidToken(jwt)) {
        sendErr(
            response, SC_UNAUTHORIZED, "Authentication on machine failed, invalid token provided.");
        return;
      }

      try {
        final SubjectImpl subject =
            new SubjectImpl(
                claims.get(USER_NAME_CLAIM, String.class),
                claims.get(USER_ID_CLAIM, String.class),
                token,
                false);
        EnvironmentContext.getCurrent().setSubject(subject);
        final HttpSession httpSession = httpRequest.getSession(true);
        httpSession.setAttribute("principal", subject);
        chain.doFilter(request, response);
      } finally {
        EnvironmentContext.reset();
      }
    } catch (ExpiredJwtException
        | UnsupportedJwtException
        | MalformedJwtException
        | SignatureException
        | IllegalArgumentException ex) {
      sendErr(
          response,
          SC_UNAUTHORIZED,
          format("Authentication on machine failed cause: '%s'", ex.getMessage()));
    }
  }

  /** Checks whether given JWT token is valid */
  private boolean isValidToken(Jws<Claims> jwt) {
    return MACHINE_TOKEN_KIND.equals(jwt.getHeader().get("kind"))
        && workspaceId.equals(jwt.getBody().get(WORKSPACE_ID_CLAIM, String.class));
  }

  /** Sets given error code with err message into give response. */
  private static void sendErr(ServletResponse res, int errCode, String msg) throws IOException {
    final HttpServletResponse response = (HttpServletResponse) res;
    response.sendError(errCode, msg);
  }

  @Override
  public void destroy() {}
}
