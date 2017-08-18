/*
 * Copyright 2010 Xemantic
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xemantic.tadedon.servlet;

import java.io.IOException;
import java.util.Date;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet applying "cache forever" HTTP headers.
 *
 * <p>See: <a href="http://code.google.com/p/doctype/wiki/ArticleHttpCaching">ArticleHttpCaching</a>
 *
 * <p>Created on Aug 6, 2010
 *
 * @author hshsce
 */
public class CacheForcingFilter extends SimpleFilter {

  private static final long ONE_MONTH_IN_SECONDS = 60L * 60L * 24L * 30L;

  private static final long ONE_MONTH_IN_MILISECONDS = 1000L * ONE_MONTH_IN_SECONDS;

  /** {@inheritDoc} */
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    if (response instanceof HttpServletResponse) {
      HttpServletResponse httpResponse = (HttpServletResponse) response;
      Date now = new Date();
      httpResponse.setDateHeader("Date", now.getTime());
      httpResponse.setDateHeader("Expires", now.getTime() + ONE_MONTH_IN_MILISECONDS);
      httpResponse.setHeader("Pragma", "no-cache");
      httpResponse.setHeader("Cache-control", "public, max-age=" + ONE_MONTH_IN_SECONDS);
    }
    chain.doFilter(request, response);
  }
}
