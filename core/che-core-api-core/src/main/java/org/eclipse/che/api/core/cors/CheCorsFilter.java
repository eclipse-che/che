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
package org.eclipse.che.api.core.cors;

import com.google.inject.Singleton;
import java.io.IOException;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.apache.catalina.filters.CorsFilter;

/**
 * The special filter which provides filtering requests in according to settings which are set to
 * {@link CorsFilter}. The class allows to configure parameters, such as support credentials in
 * request, or
 *
 * @author Dmitry Shnurenko
 */
@Singleton
public class CheCorsFilter implements Filter {

  private CorsFilter corsFilter;

  @Inject private CheCorsFilterConfig cheCorsFilterConfig;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    corsFilter = new CorsFilter();

    corsFilter.init(cheCorsFilterConfig);
  }

  @Override
  public void doFilter(
      ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
      throws IOException, ServletException {
    corsFilter.doFilter(servletRequest, servletResponse, filterChain);
  }

  @Override
  public void destroy() {
    corsFilter.destroy();
  }
}
