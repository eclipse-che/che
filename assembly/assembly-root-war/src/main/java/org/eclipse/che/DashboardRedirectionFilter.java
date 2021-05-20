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
package org.eclipse.che;

import java.io.IOException;
import java.util.regex.Pattern;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Redirect user to dashboard if request wasn't made to namespace/workspaceName or app resources.
 *
 * @author Max Shaposhnik
 */
@Singleton
public class DashboardRedirectionFilter implements Filter {

  // Describes IDE direct URL in format namespace/workspaceName, like user123/java-mysql
  private static final String IDE_DIRECT_URL = "/\\w+/\\w+";
  // Describes URL to app resources, like /_ide/loader.html
  private static final String APP_RESOURCES = "/_app/.*";

  private static final Pattern EXCLUDES =
      Pattern.compile("^(" + APP_RESOURCES + ")|(" + IDE_DIRECT_URL + ")$");

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse resp = (HttpServletResponse) response;

    if (("GET".equals(req.getMethod()) || "HEAD".equals(req.getMethod()))
        && !EXCLUDES.matcher(req.getRequestURI()).matches()) {
      resp.sendRedirect("/dashboard/");
      return;
    }
    chain.doFilter(request, response);
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {}

  @Override
  public void destroy() {}
}
