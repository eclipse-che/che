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
package org.eclipse.che.multiuser.api.authentication.commons.filter;

import static org.eclipse.che.multiuser.api.authentication.commons.Constants.CHE_SUBJECT_ATTRIBUTE;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.multiuser.api.authentication.commons.SessionStore;
import org.eclipse.che.multiuser.api.authentication.commons.SubjectHttpRequestWrapper;
import org.eclipse.che.multiuser.api.authentication.commons.token.RequestTokenExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs basic environment initialization actions as follows:
 *
 * <ul>
 *   <li>Extracts token from request
 *   <li>Checks token for validity and fetch user Id (implementation specific)
 *   <li>Fetch cached {@link HttpSession} or requests to create new one
 *   <li>Gets {@link Subject} stored in session or construct new (implementation specific))
 *   <li>Set subject for current request into {@link EnvironmentContext}
 * </ul>
 *
 * @author Max Shaposhnyk (mshaposh@redhat.com)
 */
public abstract class MultiUserEnvironmentInitializationFilter implements Filter {

  private static final Logger LOG =
      LoggerFactory.getLogger(MultiUserEnvironmentInitializationFilter.class);

  private final SessionStore sessionStore;
  private final RequestTokenExtractor tokenExtractor;

  public MultiUserEnvironmentInitializationFilter(
      SessionStore sessionStore, RequestTokenExtractor tokenExtractor) {
    this.sessionStore = sessionStore;
    this.tokenExtractor = tokenExtractor;
  }

  /**
   * Wraps {@link HttpServletRequest} to handle HTTP session creation requests and return cached one
   * if possible, so the same user will always have single session despite the fact he is using
   * different kind of tokens or several tokens of the same kind (for example logged in in different
   * browsers)
   */
  protected class SessionCachedHttpRequest extends HttpServletRequestWrapper {

    private final String userId;

    /**
     * Constructs a request object wrapping the given request.
     *
     * @throws IllegalArgumentException if the request is null
     */
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

    /* Finds cached session or creates new if allowed */
    private HttpSession getOrCreateSession(boolean createNew) {
      HttpSession session = super.getSession(false);
      if (session != null) {
        return session;
      }
      if (!createNew) {
        return sessionStore.getSession(userId);
      } else {
        return sessionStore.getSession(
            userId, s -> SessionCachedHttpRequest.super.getSession(true));
      }
    }
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    Subject sessionSubject;
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    final String token = tokenExtractor.getToken(httpRequest);
    if (token == null) {
      handleMissingToken(request, response, chain);
      return;
    }
    String userId = getUserId(token); // make sure token still valid before continue
    // retrieve cached session if any or create new
    httpRequest = new SessionCachedHttpRequest(request, userId);
    HttpSession session = httpRequest.getSession(true);
    // retrieve and check / create new subject
    sessionSubject = (Subject) session.getAttribute(CHE_SUBJECT_ATTRIBUTE);
    if (sessionSubject == null) {
      sessionSubject = extractSubject(token);
      session.setAttribute(CHE_SUBJECT_ATTRIBUTE, sessionSubject);
    } else if (!sessionSubject.getUserId().equals(userId)) {
      LOG.debug(
          "Invalidating session with mismatched user IDs: old was '{}', new is '{}'.",
          sessionSubject.getUserId(),
          userId);
      session.invalidate();
      HttpSession new_session = httpRequest.getSession(true);
      sessionSubject = extractSubject(token);
      new_session.setAttribute(CHE_SUBJECT_ATTRIBUTE, sessionSubject);
    } else if (!sessionSubject.getToken().equals(token)) {
      sessionSubject = extractSubject(token);
      session.setAttribute(CHE_SUBJECT_ATTRIBUTE, sessionSubject);
    }
    // set current subject
    try {
      EnvironmentContext.getCurrent().setSubject(sessionSubject);
      chain.doFilter(new SubjectHttpRequestWrapper(httpRequest, sessionSubject), response);
    } finally {
      EnvironmentContext.reset();
    }
  }

  /**
   * Calculates id of the user from given authentication token. This <b>may</b> also imply
   * verification of token signature or encryption for encrypted/signed tokens (like JWT etc)
   *
   * @param token authentication token
   * @return user id given token belongs to
   */
  protected abstract String getUserId(String token);

  /**
   * Calculates user {@link Subject} from given authentication token.
   *
   * @param token authentication token
   * @return constructed subject
   */
  protected abstract Subject extractSubject(String token) throws ServletException;

  /**
   * Describes behavior when the token is missed. In case if performing authentication by given
   * implementing filter isn't required, it may be invocation of {@link FilterChain#doFilter} or
   * throwing of appropriate exception otherwise.
   *
   * @param request http request
   * @param response http response
   * @param chain filter chain
   * @throws IOException inherited from {@link FilterChain#doFilter}
   * @throws ServletException inherited from {@link FilterChain#doFilter}
   */
  protected abstract void handleMissingToken(
      ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException;

  /**
   * Sends appropriate error status code and message into response.
   *
   * @param res response to send error message
   * @param errorCode status code to send
   * @param message sessage to send
   * @throws IOException inherited from {@link HttpServletResponse#sendError}
   */
  protected void sendError(ServletResponse res, int errorCode, String message) throws IOException {
    HttpServletResponse response = (HttpServletResponse) res;
    response.sendError(errorCode, message);
  }
}
