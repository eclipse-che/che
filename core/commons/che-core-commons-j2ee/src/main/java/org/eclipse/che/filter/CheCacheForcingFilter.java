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
package org.eclipse.che.filter;

import com.xemantic.tadedon.servlet.CacheForcingFilter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * Forcing caching for the given URL resource patterns.
 *
 * @author Max Shaposhnik
 */
public class CheCacheForcingFilter extends CacheForcingFilter {

  private Set<Pattern> actionPatterns = new HashSet<>();

  @Override
  public void init(FilterConfig filterConfig) {
    Enumeration<String> names = filterConfig.getInitParameterNames();
    while (names.hasMoreElements()) {
      String name = names.nextElement();
      if (name.startsWith("pattern")) {
        actionPatterns.add(Pattern.compile(filterConfig.getInitParameter(name)));
      }
    }
  }

  /** {@inheritDoc} */
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    for (Pattern pattern : actionPatterns) {
      if (pattern.matcher(((HttpServletRequest) request).getRequestURI()).matches()) {
        super.doFilter(request, response, chain);
        return;
      }
    }
    chain.doFilter(request, response);
  }
}
