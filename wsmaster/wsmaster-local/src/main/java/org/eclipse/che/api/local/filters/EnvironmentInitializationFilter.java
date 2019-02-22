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
package org.eclipse.che.api.local.filters;

import io.opentracing.Tracer;
import java.io.IOException;
import java.security.Principal;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.eclipse.che.commons.tracing.OptionalTracer;
import org.eclipse.che.commons.tracing.TracingTags;

/**
 * Fills environment context with information about current subject.
 *
 * @author Dmitry Shnurenko
 */
@Singleton
public class EnvironmentInitializationFilter implements Filter {

  @Inject OptionalTracer optionalTracer;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {}

  @Override
  public final void doFilter(
      ServletRequest request, ServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {
    final HttpServletRequest httpRequest = (HttpServletRequest) request;
    Subject subject = new SubjectImpl("che", "che", "dummy_token", false);
    HttpSession session = httpRequest.getSession();
    session.setAttribute("codenvy_user", subject);

    final EnvironmentContext environmentContext = EnvironmentContext.getCurrent();

    try {
      environmentContext.setSubject(subject);
      Tracer tracer = OptionalTracer.fromNullable(optionalTracer);
      if (tracer != null) {
        TracingTags.USER_ID.set(tracer.activeSpan(), subject.getUserId());
      }
      TracingTags.USER_ID.set(subject.getUserId());
      filterChain.doFilter(addUserInRequest(httpRequest, subject), response);
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
        return () -> subject.getUserName();
      }
    };
  }

  @Override
  public void destroy() {}
}
