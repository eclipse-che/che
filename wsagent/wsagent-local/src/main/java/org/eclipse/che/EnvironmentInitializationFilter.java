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
package org.eclipse.che;

import java.io.IOException;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;

/**
 * Sets dummy subject into {@link EnvironmentContext}
 *
 * @author Max Shaposhnik (mshaposh@redhat.com)
 */
@Singleton
public class EnvironmentInitializationFilter implements Filter {

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {}

  @Override
  public final void doFilter(
      ServletRequest request, ServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {
    Subject subject = new SubjectImpl("che", "che", "dummy_token", false);
    final EnvironmentContext environmentContext = EnvironmentContext.getCurrent();
    try {
      environmentContext.setSubject(subject);
      filterChain.doFilter(request, response);
    } finally {
      EnvironmentContext.reset();
    }
  }

  public void destroy() {}
}
