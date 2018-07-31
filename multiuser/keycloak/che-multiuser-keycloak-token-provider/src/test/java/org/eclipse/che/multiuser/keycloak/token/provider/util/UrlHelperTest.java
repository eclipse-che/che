/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.keycloak.token.provider.util;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import org.junit.Test;

public class UrlHelperTest {
  private static final String TOKEN = "kjhKJhLKJHSLKJDHDSKJAHLKAHSdshjs";
  private static final String SCOPE = "scope";
  private static final String RESPONSE_BODY = "access_token=" + TOKEN + "&scope=" + SCOPE;
  private static final String ACCESS_TOKEN = "access_token";

  @Test
  public void processQuery() {
    Map<String, String> parameters = UrlHelper.splitQuery(RESPONSE_BODY);
    String token = parameters.get(ACCESS_TOKEN);
    String scope = parameters.get(SCOPE);
    assertEquals(token, TOKEN);
    assertEquals(scope, SCOPE);
  }
}
