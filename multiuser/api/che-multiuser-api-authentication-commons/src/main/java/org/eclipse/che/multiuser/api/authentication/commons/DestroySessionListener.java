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
package org.eclipse.che.multiuser.api.authentication.commons;

import static org.eclipse.che.multiuser.api.authentication.commons.Constants.CHE_SUBJECT_ATTRIBUTE;

import com.google.inject.Injector;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import org.eclipse.che.commons.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Purges deleted sessions from sessions cache store. */
public class DestroySessionListener implements HttpSessionListener {

  private static final Logger LOG = LoggerFactory.getLogger(DestroySessionListener.class);

  @Override
  public final void sessionCreated(HttpSessionEvent se) {}

  @Override
  public void sessionDestroyed(HttpSessionEvent se) {

    ServletContext servletContext = se.getSession().getServletContext();

    SessionStore sessionStore = getInstance(SessionStore.class, servletContext);
    if (sessionStore == null) {
      LOG.error(
          "Unable to remove session from store. Session store is not configured in servlet context.");
      return;
    }

    Subject subject = (Subject) se.getSession().getAttribute(CHE_SUBJECT_ATTRIBUTE);
    if (subject != null) {
      sessionStore.remove(subject.getUserId());
    }
  }

  /** Searches component in servlet context when with help of guice injector. */
  @SuppressWarnings("unchecked")
  private <T> T getInstance(Class<T> type, ServletContext servletContext) {
    T result = (T) servletContext.getAttribute(type.getName());
    if (result == null) {
      Injector injector = (Injector) servletContext.getAttribute(Injector.class.getName());
      if (injector != null) {
        result = injector.getInstance(type);
        if (result != null) {
          servletContext.setAttribute(type.getName(), result);
        }
      }
    }
    return result;
  }
}
