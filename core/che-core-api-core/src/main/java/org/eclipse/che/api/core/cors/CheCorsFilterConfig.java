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

import static org.apache.catalina.filters.CorsFilter.PARAM_CORS_ALLOWED_HEADERS;
import static org.apache.catalina.filters.CorsFilter.PARAM_CORS_ALLOWED_METHODS;
import static org.apache.catalina.filters.CorsFilter.PARAM_CORS_ALLOWED_ORIGINS;
import static org.apache.catalina.filters.CorsFilter.PARAM_CORS_EXPOSED_HEADERS;
import static org.apache.catalina.filters.CorsFilter.PARAM_CORS_PREFLIGHT_MAXAGE;
import static org.apache.catalina.filters.CorsFilter.PARAM_CORS_SUPPORT_CREDENTIALS;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

/**
 * Basic configuration for {@link CheCorsFilter}. Allowed origings and credentials support are
 * configurable through properties.
 *
 * @author Mykhailo Kuznietsov
 */
public class CheCorsFilterConfig implements FilterConfig {

  private final Map<String, String> filterParams;

  @Inject
  public CheCorsFilterConfig(
      @Named("che.cors.allow_credentials") boolean allowCredentials,
      @Named("che.cors.allowed_origins") String allowedOrigins) {
    filterParams = new HashMap<>();
    filterParams.put(PARAM_CORS_ALLOWED_ORIGINS, allowedOrigins);
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
    filterParams.put(PARAM_CORS_SUPPORT_CREDENTIALS, String.valueOf(allowCredentials));
    // preflight cache is available for 10 minutes
    filterParams.put(PARAM_CORS_PREFLIGHT_MAXAGE, "10");
  }

  @Override
  public String getFilterName() {
    return CheCorsFilter.class.getName();
  }

  @Override
  public ServletContext getServletContext() {
    throw new UnsupportedOperationException(
        "The method is not supported in " + CheCorsFilter.class);
  }

  @Override
  public String getInitParameter(String key) {
    return filterParams.get(key);
  }

  @Override
  public Enumeration<String> getInitParameterNames() {
    throw new UnsupportedOperationException(
        "The method is not supported in " + CheCorsFilter.class);
  }
}
