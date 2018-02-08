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
package org.eclipse.che.commons.logback.filter;

import java.io.IOException;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.eclipse.che.commons.subject.Subject;
import org.slf4j.MDC;

/**
 * A servlet filter that retrieves the identity_id from the servlet context and put it in the MDC
 * context. Logback can be configured to display this value in each log message when available. MDC
 * property name is `identity_id`.
 */
@Singleton
public class IdentityIdLoggerFilter implements Filter {

  private static final String IDENTITY_ID_MDC_KEY = "identity_id";

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {}

  @Override
  public final void doFilter(
      ServletRequest request, ServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {
    final HttpServletRequest httpRequest = (HttpServletRequest) request;
    final HttpSession session = httpRequest.getSession();
    Subject subject = (Subject) session.getAttribute("che_subject");

    if (subject != null && subject.getUserId() != null) {
      MDC.put(IDENTITY_ID_MDC_KEY, subject.getUserId());
    }

    try {
      filterChain.doFilter(request, response);
    } finally {
      MDC.remove(IDENTITY_ID_MDC_KEY);
    }
  }

  @Override
  public void destroy() {}
}
