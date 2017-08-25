package org.eclipse.che.keycloak.server;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

public abstract class AbstractKeycloakFilter implements Filter {

  protected boolean shouldSkipAuthentication(HttpServletRequest request, String token) {
    return request.getScheme().startsWith("ws")
        || (token != null && token.startsWith("machine"));
  }
  
  @Override
  public void init(FilterConfig filterConfig) throws ServletException {}
  
  @Override
  public void destroy() {}
}
