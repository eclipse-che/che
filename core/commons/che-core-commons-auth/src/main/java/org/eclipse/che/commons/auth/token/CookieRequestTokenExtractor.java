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

import java.util.Arrays;
import java.util.Optional;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;


/**
 * Extract sso token from request headers.
 */
public class CookieRequestTokenExtractor implements RequestTokenExtractor {

  @Override
  public String getToken(HttpServletRequest req) {
    if (req.getCookies() == null) {
      return null;
    }
    Optional<Cookie> optional = Arrays.stream(req.getCookies())
        .filter(cookie -> cookie.getName().equals("access_token")).findFirst();
    return optional.map(Cookie::getValue).orElse(null);
  }
}
