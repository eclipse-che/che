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
package org.eclipse.che.core.metrics;

import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

@Singleton
public class ApiResponceMetricFilter implements Filter {

  @Inject protected ApiResponseCounter apiResponseCounter;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {}

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {
    filterChain.doFilter(request, response);
    if (response instanceof HttpServletResponse) {
      HttpServletResponse httpResponse = (HttpServletResponse) response;
      int status = httpResponse.getStatus() / 100;
      switch (status) {
        case 2:
          apiResponseCounter.incrementSuccessResponseCounter();
          break;
        case 3:
          apiResponseCounter.incrementRedirectResponseCounter();
          break;
        case 4:
          apiResponseCounter.incrementClientErrorResponseCounter();
          break;
        case 5:
          apiResponseCounter.incrementServerErrorResponceCounter();
          break;
      }
    }
  }

  @Override
  public void destroy() {}
}
