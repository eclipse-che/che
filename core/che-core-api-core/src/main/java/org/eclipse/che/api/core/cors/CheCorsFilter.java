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

import static org.apache.catalina.filters.CorsFilter.DEFAULT_ALLOWED_ORIGINS;
import static org.apache.catalina.filters.CorsFilter.PARAM_CORS_ALLOWED_HEADERS;
import static org.apache.catalina.filters.CorsFilter.PARAM_CORS_ALLOWED_METHODS;
import static org.apache.catalina.filters.CorsFilter.PARAM_CORS_ALLOWED_ORIGINS;
import static org.apache.catalina.filters.CorsFilter.PARAM_CORS_EXPOSED_HEADERS;
import static org.apache.catalina.filters.CorsFilter.PARAM_CORS_PREFLIGHT_MAXAGE;
import static org.apache.catalina.filters.CorsFilter.PARAM_CORS_SUPPORT_CREDENTIALS;

import com.google.inject.Singleton;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.apache.catalina.filters.CorsFilter;

/**
 * The special filter which provides filtering requests in according to settings which are set to
 * {@link CorsFilter}. More information about filter and parameters you can find in documentation.
 * The class contains business logic which allows to get allowed origin from any endpoint as it is
 * used by export workspace.
 *
 * @author Dmitry Shnurenko
 */
@Singleton
public class CheCorsFilter implements Filter {

  private CorsFilter corsFilter;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    corsFilter = new CorsFilter();

    corsFilter.init(new CheCorsFilterConfig());
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

  private class CheCorsFilterConfig implements FilterConfig {

    private final Map<String, String> filterParams;

    public CheCorsFilterConfig() {
      filterParams = new HashMap<>();
      filterParams.put(PARAM_CORS_ALLOWED_ORIGINS, DEFAULT_ALLOWED_ORIGINS);
      filterParams.put(
          PARAM_CORS_ALLOWED_METHODS, "GET," + "POST," + "HEAD," + "OPTIONS," + "PUT," + "DELETE");
      filterParams.put(
          PARAM_CORS_ALLOWED_HEADERS,
          "Content-Type,"
              + "X-Requested-With,"
              + "X-Oauth-Token,"
              + "accept,"
              + "Origin,"
              + "Authorization,"
              + "Access-Control-Request-Method,"
              + "Access-Control-Request-Headers");
      filterParams.put(PARAM_CORS_EXPOSED_HEADERS, "JAXRS-Body-Provided");
      filterParams.put(PARAM_CORS_SUPPORT_CREDENTIALS, "true");
      // preflight cache is available for 10 minutes
      filterParams.put(PARAM_CORS_PREFLIGHT_MAXAGE, "10");
    }

    @Override
    public String getFilterName() {
      return getClass().getName();
    }

    @Override
    public ServletContext getServletContext() {
      throw new UnsupportedOperationException("The method does not supported in " + getClass());
    }

    @Override
    public String getInitParameter(String key) {
      return filterParams.get(key);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
      throw new UnsupportedOperationException("The method does not supported in " + getClass());
    }
  }
}
