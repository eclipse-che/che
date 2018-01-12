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
package org.eclipse.che.commons.auth.token;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;

/** Extract sso token from request headers. */
public class HeaderRequestTokenExtractor implements RequestTokenExtractor {
  @Override
  public String getToken(HttpServletRequest req) {
    if (req.getHeader(HttpHeaders.AUTHORIZATION) == null) {
      return null;
    }
    return req.getHeader(HttpHeaders.AUTHORIZATION).toLowerCase().startsWith("bearer")
        ? req.getHeader(HttpHeaders.AUTHORIZATION).split(" ")[1]
        : req.getHeader(HttpHeaders.AUTHORIZATION);
  }
}
