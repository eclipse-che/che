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
package org.eclipse.che.multiuser.api.authentication.commons;

import static org.eclipse.che.multiuser.api.authentication.commons.Constants.CHE_SUBJECT_ATTRIBUTE;

import com.google.inject.Injector;
import java.util.Optional;
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
  public final void sessionCreated(HttpSessionEvent sessionEvent) {}

  @Override
  public void sessionDestroyed(HttpSessionEvent sessionEvent) {

    ServletContext servletContext = sessionEvent.getSession().getServletContext();

    Optional<SessionStore> sessionStoreOptional = getSessionStoreInstance(servletContext);
    if (!sessionStoreOptional.isPresent()) {
      LOG.error(
          "Unable to remove session from store. Session store is not configured in servlet context.");
      return;
    }
    SessionStore sessionStore = sessionStoreOptional.get();
    Subject subject = (Subject) sessionEvent.getSession().getAttribute(CHE_SUBJECT_ATTRIBUTE);
    if (subject != null) {
      sessionStore.remove(subject.getUserId());
    }
  }

  /** Searches session store component in servlet context when with help of guice injector. */
  private Optional<SessionStore> getSessionStoreInstance(ServletContext servletContext) {
    String attributeName = SessionStore.class.getName();
    SessionStore result = (SessionStore) servletContext.getAttribute(attributeName);
    if (result == null) {
      Injector injector = (Injector) servletContext.getAttribute(Injector.class.getName());
      if (injector != null) {
        result = injector.getInstance(SessionStore.class);
        if (result != null) {
          servletContext.setAttribute(attributeName, result);
        }
      }
    }
    return Optional.ofNullable(result);
  }
}
