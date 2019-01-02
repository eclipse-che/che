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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ApiResponceMetricFilter implements Filter {

  private static Logger LOG = LoggerFactory.getLogger(ApiResponceMetricFilter.class);

  @Inject ApiResponseCounter apiResponseCoutner;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {}

  @Override
  public void doFilter(
      ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
      throws IOException, ServletException {
    filterChain.doFilter(servletRequest, servletResponse);
    if (servletResponse instanceof HttpServletResponse) {
      HttpServletResponse hts = (HttpServletResponse) servletResponse;
      if (hts.getStatus() / 100 == 2) {
        apiResponseCoutner.incrementSuccessResponseCounter();
        LOG.info("incremented200");
      }
      if (hts.getStatus() / 100 == 3) {
        apiResponseCoutner.incrementRedirectResonseCounter();
        LOG.info("incremented300");
      }
      if (hts.getStatus() / 100 == 4) {
        apiResponseCoutner.incrementClientErrorResponseCounter();
        LOG.info("incremented400");
      }
      if (hts.getStatus() / 100 == 5) {
        apiResponseCoutner.incrementServerErrorResponceCounter();
        LOG.info("incremented500");
      }
    }
  }

  @Override
  public void destroy() {}
}
