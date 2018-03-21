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
 * Returns internal server error when request is supposed to be handled by API war. Situation occurs
 * when API war failed to start and all requests to it handled by ROOT war.
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class ApiAccessRejectionFilter implements Filter {
  public static final String ERROR_MESSAGE = "Internal server occurs. API is not accessible";

  private static final byte[] ERROR_MESSAGE_IN_BYTES = ERROR_MESSAGE.getBytes();
  private static final Pattern API_PATTERN = Pattern.compile("^/api(/.*)?$");

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse resp = (HttpServletResponse) response;

    if (API_PATTERN.matcher(req.getRequestURI()).matches()) {
      resp.setStatus(500);
      resp.getOutputStream().write(ERROR_MESSAGE_IN_BYTES);
      return;
    }
    chain.doFilter(request, response);
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {}

  @Override
  public void destroy() {}
}
