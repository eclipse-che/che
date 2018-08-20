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
package org.eclipse.che.multiuser.keycloak.server;

import io.jsonwebtoken.JwtParser;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;

/**
 * Base abstract class for the Keycloak-related servlet filters.
 *
 * <p>In particular it defines common use-cases when the authentication / multi-user logic should be
 * skipped
 */
public abstract class AbstractKeycloakFilter implements Filter {

  @Inject protected JwtParser jwtParser;

  /** when a request came from a machine with valid token then auth is not required */
  boolean shouldSkipAuthentication(HttpServletRequest request, String token) {
    if (token == null) {
      return request.getRequestURI() != null
          && request.getRequestURI().endsWith("api/keycloak/OIDCKeycloak.js");
    }
    try {
      jwtParser.parse(token);
      return false;
    } catch (MachineTokenJwtException e) {
      return true;
    }
  }

  @Override
  public void init(FilterConfig filterConfig) {}

  @Override
  public void destroy() {}
}
