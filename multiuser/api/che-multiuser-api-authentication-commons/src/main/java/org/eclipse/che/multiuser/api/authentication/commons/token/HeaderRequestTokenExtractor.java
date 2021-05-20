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
package org.eclipse.che.multiuser.api.authentication.commons.token;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.HttpHeaders;

/** Extract sso token from request headers. */
public class HeaderRequestTokenExtractor implements RequestTokenExtractor {

  @Override
  public String getToken(HttpServletRequest req) {
    if (req.getHeader(HttpHeaders.AUTHORIZATION) == null) {
      return null;
    }
    if (req.getHeader(HttpHeaders.AUTHORIZATION).toLowerCase().startsWith("bearer")) {
      String[] parts = req.getHeader(HttpHeaders.AUTHORIZATION).split(" ");
      if (parts.length != 2) {
        throw new BadRequestException("Invalid authorization header format.");
      }
      return parts[1];
    } else {
      return req.getHeader(HttpHeaders.AUTHORIZATION);
    }
  }
}
