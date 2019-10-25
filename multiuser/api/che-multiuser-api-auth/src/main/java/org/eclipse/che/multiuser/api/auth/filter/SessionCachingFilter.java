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
package org.eclipse.che.multiuser.api.auth.filter;

import javax.servlet.Filter;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

public abstract class SessionCachingFilter implements Filter {

  private SessionStore sessionStore;

  public SessionCachingFilter(SessionStore sessionStore) {
    this.sessionStore = sessionStore;
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
      boolean existAndValid = true;
      if (session == null) {
        existAndValid = false;
      } else {
        try {
          session.isNew();
        } catch (IllegalStateException e) {
          //invalidated, remove and request new one
          sessionStore.remove(userId);
          existAndValid = false;
        }
      }

      if (!existAndValid && createNew) {
        session = super.getSession(true);
        sessionStore.saveSession(userId, session);
      }
      return session;
    }
  }

  protected abstract String getUserId(HttpServletRequest request);

  @Override
  public void destroy() {}
}
