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

import java.io.IOException;
import java.security.Principal;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;
import org.eclipse.che.commons.auth.token.RequestTokenExtractor;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.multiuser.api.authentication.commons.SessionStore;
import org.eclipse.che.multiuser.api.authentication.commons.SubjectSupplier;

public abstract class SessionCachingFilter implements Filter {

  public static final String CHE_SUBJECT_ATTRIBUTE = "che_subject";
  private SessionStore sessionStore;
  private SubjectSupplier subjectSupplier;
  private RequestTokenExtractor tokenExtractor;

  public SessionCachingFilter(SessionStore sessionStore,
      RequestTokenExtractor tokenExtractor,
      SubjectSupplier subjectSupplier) {
    this.sessionStore = sessionStore;
    this.subjectSupplier = subjectSupplier;
    this.tokenExtractor = tokenExtractor;
  }

  protected class SessionCachedHttpRequest extends HttpServletRequestWrapper {

    /**
     * Constructs a request object wrapping the given request.
     *
     * @throws IllegalArgumentException if the request is null
     */
    public SessionCachedHttpRequest(ServletRequest request) {
      super((HttpServletRequest) request);
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

      String userId = getUserId(this);
      if (userId == null) {
        // fallback to default
        return super.getSession(createNew);
      }
      session = sessionStore.getSession(userId);
      if (session == null && createNew) {
        session = super.getSession(true);
        sessionStore.saveSession(userId, session);
      }
      return session;
    }

  }

  protected abstract String getUserId(HttpServletRequest request);

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    final String token = tokenExtractor.getToken((HttpServletRequest) request);
    HttpServletRequest httpRequest = new SessionCachedHttpRequest(request);
    HttpSession session = httpRequest.getSession(true);
    Subject sessionSubject = (Subject) session.getAttribute(CHE_SUBJECT_ATTRIBUTE);
    if (sessionSubject == null) {
      sessionSubject = subjectSupplier.getSubject(httpRequest, response, chain, token);
      session.setAttribute(CHE_SUBJECT_ATTRIBUTE, sessionSubject);

    }

    if (sessionSubject == null) {
      // nothing to do else, we probably meet error and have response object fulfilled with details
      return;
    }

    try {
      EnvironmentContext.getCurrent().setSubject(sessionSubject);
      chain.doFilter(addUserInRequest(httpRequest, sessionSubject), response);
    } finally {
      EnvironmentContext.reset();
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

  @Override
  public void destroy() {}
}
