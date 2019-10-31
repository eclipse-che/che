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
package org.eclipse.che.multiuser.api.authentication.commons.filter;

import static org.eclipse.che.multiuser.api.authentication.commons.Constants.CHE_SUBJECT_ATTRIBUTE;

import java.io.IOException;
import java.security.Principal;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.eclipse.che.commons.auth.token.RequestTokenExtractor;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.multiuser.api.authentication.commons.SessionStore;

public abstract class MultiuserEnvironmentInitializationFilter implements Filter {

  private final SessionStore sessionStore;
  private final RequestTokenExtractor tokenExtractor;

  public MultiuserEnvironmentInitializationFilter(
      SessionStore sessionStore, RequestTokenExtractor tokenExtractor) {
    this.sessionStore = sessionStore;
    this.tokenExtractor = tokenExtractor;
  }

  protected class SessionCachedHttpRequest extends HttpServletRequestWrapper {

    /**
     * Constructs a request object wrapping the given request.
     *
     * @throws IllegalArgumentException if the request is null
     */
    private final String userId;

    public SessionCachedHttpRequest(ServletRequest request, String userId) {
      super((HttpServletRequest) request);
      this.userId = userId;
    }

    @Override
    public HttpSession getSession() {
      return getOrCreateSession(true);
    }

    @Override
    public HttpSession getSession(boolean create) {
      return getOrCreateSession(create);
    }

    private HttpSession getOrCreateSession(boolean createNew) {
      HttpSession session = super.getSession(false);
      if (session != null) {
        return session;
      }
      synchronized (userId) {
        session = sessionStore.getSession(userId);
        if (session == null && createNew) {
          session = super.getSession(true);
          sessionStore.saveSession(userId, session);
        }
      }
      return session;
    }
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    Subject sessionSubject;
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    final String token = tokenExtractor.getToken(httpRequest);
    String userId = getUserId(token); // make sure token still valid before continue
    // retrieve cached session if any or create new
    httpRequest = new SessionCachedHttpRequest(request, userId);
    HttpSession session = httpRequest.getSession(true);
    // retrieve and check / create new subject
    sessionSubject = (Subject) session.getAttribute(CHE_SUBJECT_ATTRIBUTE);
    if (sessionSubject == null || !sessionSubject.getToken().equals(token)) {
      sessionSubject = extractSubject(token);
      session.setAttribute(CHE_SUBJECT_ATTRIBUTE, sessionSubject);
    }
    // set current subject
    try {
      EnvironmentContext.getCurrent().setSubject(sessionSubject);
      chain.doFilter(addUserInRequest(httpRequest, sessionSubject), response);
    } finally {
      EnvironmentContext.reset();
    }
  }

  protected abstract String getUserId(String token);

  protected abstract Subject extractSubject(String token) throws ServletException;

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

  protected void sendError(ServletResponse res, int errorCode, String message) throws IOException {
    HttpServletResponse response = (HttpServletResponse) res;
    response.sendError(errorCode, message);
  }

  @Override
  public void destroy() {}
}
