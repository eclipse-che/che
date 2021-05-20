/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
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

/**
 * Try to extract token from request in 3 steps. 1. From query parameter. 2. From header. 3. From
 * cookie.
 *
 * @author Sergii Kabashniuk
 */
public class ChainedTokenExtractor implements RequestTokenExtractor {

  private final HeaderRequestTokenExtractor headerRequestTokenExtractor;

  private final QueryRequestTokenExtractor queryRequestTokenExtractor;

  public ChainedTokenExtractor() {
    headerRequestTokenExtractor = new HeaderRequestTokenExtractor();
    queryRequestTokenExtractor = new QueryRequestTokenExtractor();
  }

  @Override
  public String getToken(HttpServletRequest req) {
    String token;
    if ((token = queryRequestTokenExtractor.getToken(req)) == null) {
      token = headerRequestTokenExtractor.getToken(req);
    }
    return token;
  }
}
