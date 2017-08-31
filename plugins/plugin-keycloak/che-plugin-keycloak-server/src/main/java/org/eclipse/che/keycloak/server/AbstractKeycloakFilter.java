package org.eclipse.che.keycloak.server;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

/**
 * Base abstract class for the Keycloak-related servlet filters.
 * 
 * In particular it defines commnon use-cases when the
 * authentication / multi-user logic should be skipped
 *
 */
public abstract class AbstractKeycloakFilter implements Filter {

  protected boolean shouldSkipAuthentication(HttpServletRequest request, String token) {
    return request.getScheme().startsWith("ws") || (token != null && token.startsWith("machine"));
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {}

  @Override
  public void destroy() {}
}
